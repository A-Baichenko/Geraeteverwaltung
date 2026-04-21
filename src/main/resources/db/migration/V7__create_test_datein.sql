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
                                         (115, 'A'),
                                         (201, 'B'),
                                         (202, 'B'),
                                         (301, 'C');

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
      (5001, 910001, '2024-02-12', true, 1, NULL, 101),
      (5002, 910002, '2024-02-12', true, 1, 1101, 101),
      (5003, 910003, '2024-09-03', true, 1, NULL, 102),

      (5101, 920101, '2023-11-20', true, 2, NULL, 201),
      (5102, 920102, '2023-11-20', true, 2, NULL, 201),

      (5201, 930201, '2025-01-08', true, 3, NULL, 115),
      (5202, 930202, '2025-01-08', true, 3, NULL, 115),

      (5301, 940301, '2022-06-14', true, 4, NULL, 202),
      (5401, 950401, '2021-03-18', false, 5, NULL, 301);