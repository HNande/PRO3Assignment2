package org.example.traceability.station;

import java.util.List;

// Station 2 (cutting). Throws StationUnavailableException if it can't be reached.
public interface CuttingClient {

    // all trays that contain a part of the animal
    List<Integer> getTrayIdsForAnimal(String regNo);

    // all animals that have a part in one of the trays
    List<String> getAnimalRegNosInTrays(List<Integer> trayIds);

}
