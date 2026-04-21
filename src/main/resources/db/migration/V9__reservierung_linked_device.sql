ALTER TABLE reservierung
    ADD COLUMN reserviertes_geraet_id INT NULL;

UPDATE reservierung r
SET reserviertes_geraet_id = (
    SELECT MIN(g.inventar_nr)
    FROM geraet g
    WHERE g.geraetetyp_id = r.geraetetyp_id
);

ALTER TABLE reservierung
    MODIFY reserviertes_geraet_id INT NOT NULL;

ALTER TABLE reservierung
    ADD CONSTRAINT fk_reservierung_geraet
        FOREIGN KEY (reserviertes_geraet_id) REFERENCES geraet(inventar_nr);