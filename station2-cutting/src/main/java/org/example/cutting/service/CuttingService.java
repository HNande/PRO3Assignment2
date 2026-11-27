package org.example.cutting.service;

import org.example.cutting.model.AnimalRecord;
import org.example.cutting.model.PartRecord;
import org.example.cutting.model.TrayRecord;
import java.util.List;

public interface CuttingService {

    // An animal handed over from station 1. Receiving the same animal twice is fine.
    void receiveAnimal(AnimalRecord animal);

    // Registers a part cut from the animal and puts it into an open tray of the same type.
    // If the part doesn't fit, the tray is closed and sent to station 3 and a new tray is started.
    // Throws NotFoundException if the animal hasn't arrived, IllegalArgumentException for bad input.
    PartRecord cutPart(String animalRegNo, String partType, double weightKg);

    // No more parts go into the tray, it is sent to station 3. Throws NotFoundException.
    TrayRecord closeTray(int trayId);

    // Throws NotFoundException.
    TrayRecord getTray(int trayId);

    List<PartRecord> getPartsInTray(int trayId);

    List<Integer> getTrayIdsForAnimal(String regNo);

    List<String> getAnimalRegNosInTrays(List<Integer> trayIds);

}
