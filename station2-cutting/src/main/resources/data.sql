-- the same animals as in station 1's data.sql
INSERT INTO animal (reg_no, species)
VALUES ('A-1001', 'Pig'),
       ('A-1002', 'Pig'),
       ('A-1003', 'Pig');

-- these trays are already closed and received by station 3 (see station 3's data.sql)
INSERT INTO tray (id, part_type, max_weight_kg, closed, forwarded)
VALUES (1, 'HAM', 50, TRUE, TRUE),
       (2, 'RIBS', 40, TRUE, TRUE),
       (3, 'LOIN', 40, TRUE, TRUE),
       (4, 'HAM', 50, TRUE, TRUE);

INSERT INTO part (id, part_type, weight_kg, animal_reg_no, tray_id)
VALUES (1, 'HAM', 12.1, 'A-1001', 1),
       (2, 'RIBS', 8.4, 'A-1001', 2),
       (3, 'LOIN', 6.2, 'A-1001', 3),
       (4, 'HAM', 11.8, 'A-1002', 1),
       (5, 'RIBS', 8.0, 'A-1002', 2),
       (6, 'LOIN', 6.0, 'A-1002', 3),
       (7, 'HAM', 13.0, 'A-1003', 4),
       (8, 'LOIN', 6.5, 'A-1003', 3);

-- the ids above were given by hand, so new trays/parts continue after them
ALTER TABLE tray ALTER COLUMN id RESTART WITH 5;
ALTER TABLE part ALTER COLUMN id RESTART WITH 9;
