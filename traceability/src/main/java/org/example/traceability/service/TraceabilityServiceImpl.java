package org.example.traceability.service;

import org.example.traceability.model.AnimalRecord;
import org.example.traceability.model.ProductRecord;
import org.example.traceability.station.CuttingClient;
import org.example.traceability.station.PackingClient;
import org.example.traceability.station.RegistrationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

// Each station only knows its own part of the chain, so we follow it across them:
// product -(station 3)-> trays -(station 2)-> animals -(station 1)-> animal details
@Service
public class TraceabilityServiceImpl implements TraceabilityService {

    private RegistrationClient registrationClient;
    private CuttingClient cuttingClient;
    private PackingClient packingClient;

    // Spring gives us whatever implements the interfaces (dependency inversion)
    @Autowired
    public TraceabilityServiceImpl(RegistrationClient registrationClient,
                                   CuttingClient cuttingClient,
                                   PackingClient packingClient) {
        this.registrationClient = registrationClient;
        this.cuttingClient = cuttingClient;
        this.packingClient = packingClient;
    }

    @Override
    public List<AnimalRecord> getAnimalsInProduct(int productId) {
        List<Integer> trayIds = packingClient.getTrayIdsInProduct(productId);
        if (trayIds.isEmpty()) {
            return List.of();
        }
        List<String> regNos = cuttingClient.getAnimalRegNosInTrays(trayIds);
        if (regNos.isEmpty()) {
            return List.of();
        }
        return registrationClient.getAnimals(regNos);
    }

    @Override
    public List<ProductRecord> getProductsForAnimal(String regNo) {
        List<Integer> trayIds = cuttingClient.getTrayIdsForAnimal(regNo.trim().toUpperCase());
        if (trayIds.isEmpty()) {
            return List.of();
        }
        return packingClient.getProductsForTrays(trayIds);
    }
}
