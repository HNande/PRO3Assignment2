package org.example.cutting.forwarding;

// Hands closed trays (with their parts) over to station 3 (packing).
public interface TrayForwarder {

    // Tries to send the tray now. If station 3 can't be reached the tray
    // stays "not forwarded" and is sent again later. Never throws.
    void forward(int trayId);

    // Sends all closed trays station 3 hasn't received yet.
    void forwardPending();
}
