package org.example.registration.forwarding;

import org.example.registration.model.AnimalRecord;

// Hands registered animals over to station 2 (cutting).
public interface AnimalForwarder {

    // Tries to send the animal now. If station 2 can't be reached the animal
    // stays "not forwarded" and is sent again later. Never throws.
    void forward(AnimalRecord animal);

    // Sends all animals station 2 hasn't received yet.
    void forwardPending();
}
