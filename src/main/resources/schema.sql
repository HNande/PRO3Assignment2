DROP TABLE IF EXISTS product_tray CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS part CASCADE;
DROP TABLE IF EXISTS tray CASCADE;
DROP TABLE IF EXISTS animal CASCADE;

-- station 1: animals arrive, get weighed and registered
CREATE TABLE animal
(
    reg_no       VARCHAR(20) PRIMARY KEY,
    species      VARCHAR(50)      NOT NULL,
    weight_kg    DOUBLE PRECISION NOT NULL,
    arrival_date DATE             NOT NULL
);

-- station 2: a tray only holds one type of part
CREATE TABLE tray
(
    id            INT PRIMARY KEY,
    part_type     VARCHAR(50)      NOT NULL,
    max_weight_kg DOUBLE PRECISION NOT NULL
);

-- station 2: animals are cut into parts, each part goes into a tray
CREATE TABLE part
(
    id            INT PRIMARY KEY,
    part_type     VARCHAR(50)      NOT NULL,
    weight_kg     DOUBLE PRECISION NOT NULL,
    animal_reg_no VARCHAR(20)      NOT NULL REFERENCES animal (reg_no),
    tray_id       INT              NOT NULL REFERENCES tray (id)
);

-- station 3: products are packed from trays
CREATE TABLE product
(
    id           INT PRIMARY KEY,
    product_type VARCHAR(20) NOT NULL -- PACKAGE or HALF_ANIMAL
);

CREATE TABLE product_tray
(
    product_id INT NOT NULL REFERENCES product (id),
    tray_id    INT NOT NULL REFERENCES tray (id),
    PRIMARY KEY (product_id, tray_id)
);
