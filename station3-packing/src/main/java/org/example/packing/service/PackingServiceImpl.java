package org.example.packing.service;

import org.example.packing.model.PartRecord;
import org.example.packing.model.ProductRecord;
import org.example.packing.repository.PartRepository;
import org.example.packing.repository.ProductRepository;
import org.example.packing.repository.TrayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PackingServiceImpl implements PackingService {

    private TrayRepository trayRepository;
    private PartRepository partRepository;
    private ProductRepository productRepository;
    private List<String> halfAnimalPartTypes;

    @Autowired
    public PackingServiceImpl(TrayRepository trayRepository,
                              PartRepository partRepository,
                              ProductRepository productRepository,
                              @Value("${packing.half-animal-parts}") String[] halfAnimalPartTypes) {
        this.trayRepository = trayRepository;
        this.partRepository = partRepository;
        this.productRepository = productRepository;
        this.halfAnimalPartTypes = Arrays.stream(halfAnimalPartTypes).map(this::normalize).toList();
    }

    @Override
    @Transactional
    public synchronized void receiveTray(int trayId, String partType, List<PartRecord> parts) {
        if (trayRepository.exists(trayId)) {
            return; // already received, e.g. station 2 retried after a network problem
        }
        String type = normalize(partType);
        for (PartRecord part : parts) {
            if (!normalize(part.partType()).equals(type)) {
                throw new IllegalArgumentException("Tray " + trayId + " holds " + type + ", not " + part.partType());
            }
        }
        trayRepository.save(trayId, type, parts);
    }

    @Override
    @Transactional
    public synchronized ProductRecord packPackage(String partType, int partCount) {
        String type = normalize(partType);
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Part type is required");
        }
        if (partCount <= 0) {
            throw new IllegalArgumentException("A package needs at least one part");
        }

        List<PartRecord> parts = partRepository.findAvailable(type, partCount);
        if (parts.size() < partCount) {
            throw new IllegalStateException("Only " + parts.size() + " " + type + " part(s) available, " + partCount + " needed");
        }
        return pack(ProductRecord.PACKAGE, parts);
    }

    @Override
    @Transactional
    public synchronized ProductRecord packHalfAnimal() {
        List<PartRecord> parts = new ArrayList<>();
        for (String type : halfAnimalPartTypes) {
            List<PartRecord> available = partRepository.findAvailable(type, 1);
            if (available.isEmpty()) {
                throw new IllegalStateException("No " + type + " available for half an animal");
            }
            parts.add(available.get(0));
        }
        return pack(ProductRecord.HALF_ANIMAL, parts);
    }

    @Override
    public ProductRecord getProduct(int productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Unknown product " + productId));
    }

    @Override
    public List<Integer> getTrayIdsInProduct(int productId) {
        return productRepository.findTrayIds(productId);
    }

    @Override
    public List<ProductRecord> getProductsForTrays(List<Integer> trayIds) {
        return productRepository.findByTrayIds(trayIds);
    }

    // registers the product with references back to the trays the parts came from
    private ProductRecord pack(String productType, List<PartRecord> parts) {
        int productId = productRepository.create(productType);
        for (PartRecord part : parts) {
            partRepository.assignToProduct(part.id(), productId);
        }
        parts.stream().map(PartRecord::trayId).distinct()
                .forEach(trayId -> productRepository.addTray(productId, trayId));
        return getProduct(productId);
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toUpperCase();
    }
}
