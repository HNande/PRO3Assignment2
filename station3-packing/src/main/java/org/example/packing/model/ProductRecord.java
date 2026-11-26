package org.example.packing.model;

import java.util.List;

// type is either PACKAGE or HALF_ANIMAL, trayIds are the trays the parts came from
public record ProductRecord(
    int id,
    String type,
    List<Integer> trayIds,
    List<Integer> partIds
) {
    public static final String PACKAGE = "PACKAGE";
    public static final String HALF_ANIMAL = "HALF_ANIMAL";
}
