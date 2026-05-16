ALTER TABLE geraet
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'VERFUEGBAR';

UPDATE geraet
SET status = 'WARTUNG_DEFEKT'
WHERE ist_ausleihbar = false;

UPDATE geraet
SET status = 'FEST_ZUGEORDNET'
WHERE ist_ausleihbar = true
  AND mitarbeiter_id IS NOT NULL;

UPDATE geraet
SET status = 'VERFUEGBAR'
WHERE ist_ausleihbar = true
  AND mitarbeiter_id IS NULL;

UPDATE geraet
SET status = 'AUSGELIEHEN'
WHERE inventar_nr IN (
    SELECT a.geraet_id
    FROM ausleihe a
    WHERE a.ausleihdatum <= CURRENT_DATE
      AND COALESCE(a.tatsaechliches_rueckgabedatum, a.vereinbartes_rueckgabedatum) >= CURRENT_DATE
);

UPDATE geraet
SET status = 'RESERVIERT'
WHERE status = 'VERFUEGBAR'
  AND geraetetyp_id IN (
    SELECT DISTINCT r.geraetetyp_id
    FROM reservierung r
    WHERE r.ausleihdatum <= CURRENT_DATE
      AND r.rueckgabedatum >= CURRENT_DATE
      AND NOT EXISTS (
        SELECT 1
        FROM ausleihe a
        WHERE a.reservierung_id = r.reservierungs_nr
    )
);

CREATE INDEX idx_geraet_status ON geraet(status);