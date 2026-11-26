-- the same animals are in station 2's data.sql, so they count as already forwarded
INSERT INTO animal (reg_no, species, weight_kg, arrival_date, origin, forwarded)
VALUES ('A-1001', 'Pig', 112.5, '2026-10-01', 'Sundsvik Farm', TRUE),
       ('A-1002', 'Pig', 108.0, '2026-10-01', 'Sundsvik Farm', TRUE),
       ('A-1003', 'Pig', 120.3, '2026-10-02', 'Gävle Farm', TRUE);
