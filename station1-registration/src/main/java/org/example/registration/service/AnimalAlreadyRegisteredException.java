package org.example.registration.service;

public class AnimalAlreadyRegisteredException extends RuntimeException {

    public AnimalAlreadyRegisteredException(String regNo) {
        super("Animal " + regNo + " is already registered");
    }
}
