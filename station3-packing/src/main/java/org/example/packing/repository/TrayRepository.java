package org.example.packing.repository;

import org.example.packing.model.PartRecord;
import java.util.List;

public interface TrayRepository {

    boolean exists(int trayId);

    // Stores a tray received from station 2 together with its parts
    void save(int trayId, String partType, List<PartRecord> parts);

}
