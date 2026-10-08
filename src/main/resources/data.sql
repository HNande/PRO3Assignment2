INSERT INTO animal (reg_no, species, weight_kg, arrival_date)
VALUES ('A-1001', 'Pig', 112.5, '2026-10-01'),
       ('A-1002', 'Pig', 108.0, '2026-10-01'),
       ('A-1003', 'Pig', 120.3, '2026-10-02');

INSERT INTO tray (id, part_type, max_weight_kg)
VALUES (1, 'HAM', 50),
       (2, 'RIBS', 40),
       (3, 'LOIN', 40),
       (4, 'HAM', 50);

INSERT INTO part (id, part_type, weight_kg, animal_reg_no, tray_id)
VALUES (1, 'HAM', 12.1, 'A-1001', 1),
       (2, 'RIBS', 8.4, 'A-1001', 2),
       (3, 'LOIN', 6.2, 'A-1001', 3),
       (4, 'HAM', 11.8, 'A-1002', 1),
       (5, 'RIBS', 8.0, 'A-1002', 2),
       (6, 'LOIN', 6.0, 'A-1002', 3),
       (7, 'HAM', 13.0, 'A-1003', 4),
       (8, 'LOIN', 6.5, 'A-1003', 3);

INSERT INTO product (id, product_type)
VALUES (1, 'PACKAGE'),
       (2, 'PACKAGE'),
       (3, 'HALF_ANIMAL'),
       (4, 'PACKAGE');

-- product 1: ham package, product 2: ribs package,
-- product 3: half animal (ham + ribs + loin), product 4: ham package
INSERT INTO product_tray (product_id, tray_id)
VALUES (1, 1),
       (2, 2),
       (3, 4),
       (3, 2),
       (3, 3),
       (4, 4);
