-- the trays and parts from station 2's data.sql
INSERT INTO tray (id, part_type)
VALUES (1, 'HAM'),
       (2, 'RIBS'),
       (3, 'LOIN'),
       (4, 'HAM');

INSERT INTO product (id, product_type)
VALUES (1, 'PACKAGE'),
       (2, 'PACKAGE'),
       (3, 'HALF_ANIMAL');

-- product 1: ham package, product 2: ribs package,
-- product 3: half animal (ham + ribs + loin), parts 6 and 8 (loin) aren't packed yet
INSERT INTO part (id, part_type, weight_kg, tray_id, product_id)
VALUES (1, 'HAM', 12.1, 1, 1),
       (2, 'RIBS', 8.4, 2, 2),
       (3, 'LOIN', 6.2, 3, 3),
       (4, 'HAM', 11.8, 1, 1),
       (5, 'RIBS', 8.0, 2, 3),
       (6, 'LOIN', 6.0, 3, NULL),
       (7, 'HAM', 13.0, 4, 3),
       (8, 'LOIN', 6.5, 3, NULL);

INSERT INTO product_tray (product_id, tray_id)
VALUES (1, 1),
       (2, 2),
       (3, 4),
       (3, 2),
       (3, 3);

-- the ids above were given by hand, so new products continue after them
ALTER TABLE product ALTER COLUMN id RESTART WITH 4;
