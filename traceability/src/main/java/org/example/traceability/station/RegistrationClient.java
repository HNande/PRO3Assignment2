package org.example.traceability.station;

import org.example.traceability.model.AnimalRecord;
import java.util.List;

// Station 1 (registration). Throws StationUnavailableException if it can't be reached.
public interface RegistrationClient {

    // details of the animals, unknown registration numbers are skipped
    List<AnimalRecord> getAnimals(List<String> regNos);

}
