package org.example.traceability.model;

import java.util.List;

// type is either PACKAGE or HALF_ANIMAL, trayIds are the trays the parts came from
public record ProductRecord(
    int id,
    String type,
    List<Integer> trayIds
) {
}
