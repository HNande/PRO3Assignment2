package org.example.traceability.station;

public class StationUnavailableException extends RuntimeException {

    public StationUnavailableException(String station, Throwable cause) {
        super(station + " can't be reached right now", cause);
    }
}
