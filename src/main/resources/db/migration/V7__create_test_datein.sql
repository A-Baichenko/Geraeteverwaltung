-- =========================================================
-- TESTDATEN FÜR GERÄTEVERWALTUNG / RESERVIERUNG / AUSLEIHE
-- =========================================================
-- =========================================================
-- RAUM
-- =========================================================
INSERT INTO raum (raum_nr, gebaeude) VALUES
                            (101, 'C'),
                            (102, 'C'),
                            (201, 'B'),
                            (202, 'B'),
                            (301, 'A');

-- =========================================================
-- KATEGORIE
-- =========================================================
INSERT INTO kategorie (id, bezeichnung) VALUES
                         (1, 'Laptop'),
                         (2, 'Beamer'),
                         (3, 'Tablet');

-- =========================================================
-- GERÄTETYP
-- =========================================================
INSERT INTO geraetetyp (id, hersteller, bezeichnung, kategorie_id) VALUES
                                    (1, 'Dell', 'Latitude 5520', 1),
                                    (2, 'HP', 'EliteBook 840', 1),
                                    (3, 'Epson', 'EB-X41', 2),
                                    (4, 'Samsung', 'Galaxy Tab S7', 3);

-- =========================================================
-- GERÄT
-- =========================================================
INSERT INTO geraet (
    inventar_nr,
    serien_nr,
    kaufdatum,
    ist_ausleihbar,
    geraetetyp_id,
    mitarbeiter_id,
    raum_id
) VALUES
      (1001, 1111, '2023-01-10', true, 1, NULL, 101),
      (1002, 1112, '2023-01-10', true, 1, NULL, 101),

      (2001, 2221, '2023-02-15', true, 2, NULL, 102),
      (2002, 2222, '2023-02-15', true, 2, NULL, 102),

      (3001, 3331, '2023-03-20', true, 3, NULL, 201),
      (3002, 3332, '2023-03-20', true, 3, NULL, 201),

      (4001, 4441, '2023-04-25', true, 4, NULL, 202),
      (4002, 4442, '2023-04-25', true, 4, NULL, 202);

-- =========================================================
-- RESERVIERUNG
-- =========================================================
INSERT INTO reservierung (
    reservierungs_nr,
    ausleihdatum,
    rueckgabedatum,
    geraetetyp_id,
    mitarbeiter_id
) VALUES
      (1, '2026-04-20', '2026-04-25', 1, 1001),
      (2, '2026-04-22', '2026-04-24', 3, 1002);

-- =========================================================
-- AUSLEIHE
-- =========================================================
INSERT INTO ausleihe (
    ausleihe_nr,
    ausleihdatum,
    vereinbartes_rueckgabedatum,
    tatsaechliches_rueckgabedatum,
    geraet_id,
    mitarbeiter_id,
    reservierung_id
) VALUES
      (1, '2026-04-20', '2026-04-25', NULL, 1001, 1001, NULL),
      (2, '2026-04-21', '2026-04-23', NULL, 3001, 1002, NULL);