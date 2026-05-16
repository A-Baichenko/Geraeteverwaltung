-- =========================================================
-- TESTDATEN FÜR GERÄTE-/RAUM-/MITARBEITERVERWALTUNG (SCHULE)
-- Alle bisher vorhandenen Beispieldaten werden bereinigt.
-- =========================================================

DELETE FROM ausleihe;
DELETE FROM reservierung;
DELETE FROM geraet;
DELETE FROM geraetetyp;
DELETE FROM kategorie;
DELETE FROM raum;

-- Mitarbeiter bleiben für Benutzerkonten erhalten, werden aber auf schulische Testdaten aktualisiert.
UPDATE mitarbeiter SET vorname = 'Alex', nachname = 'Bergmann', anrede = 'DIVERS' WHERE personal_nr = 1001;
UPDATE mitarbeiter SET vorname = 'Sven', nachname = 'Neumann', anrede = 'HERR' WHERE personal_nr = 1002;
UPDATE mitarbeiter SET vorname = 'Lea', nachname = 'Kowalski', anrede = 'FRAU' WHERE personal_nr = 1003;
UPDATE mitarbeiter SET vorname = 'Mina', nachname = 'Schubert', anrede = 'FRAU' WHERE personal_nr = 1004;
UPDATE mitarbeiter SET vorname = 'David', nachname = 'Lorenz', anrede = 'HERR' WHERE personal_nr = 1005;

INSERT INTO mitarbeiter (personal_nr, vorname, nachname, anrede)
VALUES
    (1101, 'Sarah', 'Meyer', 'FRAU'),
    (1102, 'Jonas', 'Weber', 'HERR'),
    (1103, 'Nora', 'Nguyen', 'FRAU'),
    (1104, 'Emir', 'Kaya', 'HERR'),
    (1105, 'Timo', 'Schulz', 'HERR');

INSERT INTO raum (raum_nr, gebaeude) VALUES
                                         (101, 'A'),
                                         (102, 'A'),
                                         (103, 'A'),
                                         (104, 'A'),
                                         (115, 'A'),

                                         (201, 'B'),
                                         (202, 'B'),
                                         (203, 'B'),
                                         (204, 'B'),

                                         (301, 'C'),
                                         (302, 'C'),
                                         (303, 'C'),

                                         (401, 'D'),
                                         (402, 'D');

INSERT INTO kategorie (id, bezeichnung) VALUES
                                            (1, 'Laptop'),
                                            (2, 'Tablet'),
                                            (3, 'Präsentationstechnik'),
                                            (4, 'Audio');

INSERT INTO geraetetyp (id, hersteller, bezeichnung, kategorie_id) VALUES
                                                                       (1, 'Dell', 'Latitude 5440', 1),
                                                                       (2, 'Lenovo', 'ThinkPad L14', 1),
                                                                       (3, 'Apple', 'iPad 10. Gen', 2),
                                                                       (4, 'Epson', 'EB-FH52', 3),
                                                                       (5, 'JBL', 'PartyBox 110', 4);

INSERT INTO geraet (
    inventar_nr,
    serien_nr,
    kaufdatum,
    ist_ausleihbar,
    geraetetyp_id,
    mitarbeiter_id,
    raum_id
) VALUES
      (5001, 910001, '2024-02-12', true, 1, NULL, NULL),
      (5002, 910002, '2024-02-12', false, 1, 1101, 101),
      (5003, 910003, '2024-09-03', true, 1, NULL, NULL),
      (5004, 910004, '2024-09-03', false, 1, 1102, 102),
      (5005, 910005, '2024-10-11', true, 1, NULL, NULL),
      (5006, 910006, '2025-01-15', true, 1, NULL, NULL),
      (5007, 910007, '2025-02-20', false, 1, NULL, 104),

      (5101, 920101, '2023-11-20', true, 2, NULL, NULL),
      (5102, 920102, '2023-11-20', false, 2, NULL, 201),
      (5103, 920103, '2023-12-05', true, 2, NULL, NULL),
      (5104, 920104, '2024-01-18', true, 2, NULL, NULL),
      (5105, 920105, '2024-03-09', false, 2, 1105, 203),
      (5106, 920106, '2024-04-12', true, 2, NULL, NULL),

      (5201, 930201, '2025-01-08', true, 3, NULL, NULL),
      (5202, 930202, '2025-01-08', false, 3, NULL, 115),
      (5203, 930203, '2025-02-01', true, 3, NULL, NULL),
      (5204, 930204, '2025-02-03', true, 3, NULL, NULL),
      (5205, 930205, '2025-02-10', false, 3, 1102, 103),

      (5301, 940301, '2022-06-14', true, 4, NULL, NULL),
      (5302, 940302, '2022-06-14', false, 4, NULL, 202),
      (5303, 940303, '2023-03-11', true, 4, NULL, NULL),
      (5304, 940304, '2023-07-19', false, 4, NULL, 204),
      (5305, 940305, '2024-01-01', true, 4, NULL, NULL),

      (5401, 950401, '2021-03-18', true, 5, NULL, NULL),
      (5402, 950402, '2021-03-18', false, 5, NULL, 301),
      (5403, 950403, '2022-08-22', true, 5, NULL, NULL),
      (5404, 950404, '2023-05-30', true, 5, NULL, NULL),
      (5405, 950405, '2024-06-15', false, 5, NULL, 303),
      (5406, 950406, '2024-07-20', true, 5, NULL, NULL),
      (5407, 950407, '2025-01-12', false, 5, NULL, 303);