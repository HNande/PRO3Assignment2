-- Station 1's own database
CREATE SCHEMA IF NOT EXISTS station1;

DROP TABLE IF EXISTS animal CASCADE;

-- animals arrive, get weighed and registered
CREATE TABLE animal
(
    reg_no       VARCHAR(20) PRIMARY KEY,
    species      VARCHAR(50)      NOT NULL,
    weight_kg    DOUBLE PRECISION NOT NULL,
    arrival_date DATE             NOT NULL,
    origin       VARCHAR(100)     NOT NULL,
    -- has station 2 received the animal yet? (store-and-forward)
    forwarded    BOOLEAN          NOT NULL DEFAULT FALSE
);
