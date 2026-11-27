package org.example.cutting.service;

import org.example.cutting.forwarding.TrayForwarder;
import org.example.cutting.model.AnimalRecord;
import org.example.cutting.model.PartRecord;
import org.example.cutting.model.TrayRecord;
import org.example.cutting.repository.AnimalRepository;
import org.example.cutting.repository.PartRepository;
import org.example.cutting.repository.TrayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CuttingServiceImpl implements CuttingService {

    private AnimalRepository animalRepository;
    private TrayRepository trayRepository;
    private PartRepository partRepository;
    private TrayForwarder trayForwarder;
    private double trayMaxWeightKg;

    @Autowired
    public CuttingServiceImpl(AnimalRepository animalRepository,
                              TrayRepository trayRepository,
                              PartRepository partRepository,
                              TrayForwarder trayForwarder,
                              @Value("${cutting.tray-max-weight-kg:50.0}") double trayMaxWeightKg) {
        this.animalRepository = animalRepository;
        this.trayRepository = trayRepository;
        this.partRepository = partRepository;
        this.trayForwarder = trayForwarder;
        this.trayMaxWeightKg = trayMaxWeightKg;
    }

    @Override
    public synchronized void receiveAnimal(AnimalRecord animal) {
        String regNo = normalize(animal.regNo());
        if (regNo.isEmpty()) {
            throw new IllegalArgumentException("Registration number is required");
        }
        if (!animalRepository.exists(regNo)) {
            animalRepository.save(new AnimalRecord(regNo, animal.species()));
        }
    }

    // synchronized so two parts can't be put into the same "last space" of a tray at once
    @Override
    public synchronized PartRecord cutPart(String animalRegNo, String partType, double weightKg) {
        String regNo = normalize(animalRegNo);
        String type = normalize(partType);
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Part type is required");
        }
        if (weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
        if (weightKg > trayMaxWeightKg) {
            throw new IllegalArgumentException("A part can't weigh more than a tray holds (" + trayMaxWeightKg + " kg)");
        }
        if (!animalRepository.exists(regNo)) {
            throw new NotFoundException("Animal " + regNo + " hasn't arrived at station 2");
        }

        TrayRecord tray = trayRepository.findOpenByPartType(type).orElse(null);
        if (tray != null && tray.currentWeightKg() + weightKg > tray.maxWeightKg()) {
            // the tray is full, it goes on to station 3
            closeAndForward(tray.id());
            tray = null;
        }
        if (tray == null) {
            tray = trayRepository.create(type, trayMaxWeightKg);
        }

        return partRepository.save(type, weightKg, regNo, tray.id());
    }

    @Override
    public synchronized TrayRecord closeTray(int trayId) {
        TrayRecord tray = getTray(trayId);
        if (!tray.closed()) {
            closeAndForward(trayId);
        }
        return getTray(trayId);
    }

    @Override
    public TrayRecord getTray(int trayId) {
        return trayRepository.findById(trayId)
                .orElseThrow(() -> new NotFoundException("Unknown tray " + trayId));
    }

    @Override
    public List<PartRecord> getPartsInTray(int trayId) {
        return partRepository.findByTrayId(trayId);
    }

    @Override
    public List<Integer> getTrayIdsForAnimal(String regNo) {
        return trayRepository.findIdsByAnimalRegNo(normalize(regNo));
    }

    @Override
    public List<String> getAnimalRegNosInTrays(List<Integer> trayIds) {
        return partRepository.findAnimalRegNosByTrayIds(trayIds);
    }

    private void closeAndForward(int trayId) {
        trayRepository.close(trayId);
        trayForwarder.forward(trayId);
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toUpperCase();
    }
}
