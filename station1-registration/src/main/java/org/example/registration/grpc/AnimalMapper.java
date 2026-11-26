package org.example.registration.grpc;

import org.example.registration.model.AnimalRecord;
import org.example.slaughterhouse.grpc.Animal;

public final class AnimalMapper {

    private AnimalMapper() {
    }

    public static Animal toProto(AnimalRecord animal) {
        return Animal.newBuilder()
                .setRegNo(animal.regNo())
                .setSpecies(animal.species())
                .setWeightKg(animal.weightKg())
                .setArrivalDate(animal.arrivalDate().toString())
                .setOrigin(animal.origin())
                .build();
    }
}
