CREATE TABLE reservierung (
                              reservierungs_nr INT AUTO_INCREMENT PRIMARY KEY,
                              ausleihdatum DATE NOT NULL,
                              rueckgabedatum DATE NOT NULL,
                              geraetetyp_id BIGINT NOT NULL,
                              mitarbeiter_id INT NOT NULL,

                              CONSTRAINT fk_reservierung_geraetetyp FOREIGN KEY (geraetetyp_id)
                                  REFERENCES geraetetyp(id),
                              CONSTRAINT fk_reservierung_mitarbeiter FOREIGN KEY (mitarbeiter_id)
                                  REFERENCES mitarbeiter(personal_nr)
);

CREATE TABLE ausleihe (
                          ausleihe_nr INT AUTO_INCREMENT PRIMARY KEY,
                          ausleihdatum DATE NOT NULL,
                          vereinbartes_rueckgabedatum DATE NOT NULL,
                          tatsaechliches_rueckgabedatum DATE,
                          geraet_id INT NOT NULL,
                          mitarbeiter_id INT NOT NULL,
                          reservierung_id INT,

                          CONSTRAINT fk_ausleihe_geraet FOREIGN KEY (geraet_id)
                              REFERENCES geraet(inventar_nr),
                          CONSTRAINT fk_ausleihe_mitarbeiter FOREIGN KEY (mitarbeiter_id)
                              REFERENCES mitarbeiter(personal_nr),
                          CONSTRAINT fk_ausleihe_reservierung FOREIGN KEY (reservierung_id)
                              REFERENCES reservierung(reservierungs_nr),
                          CONSTRAINT uk_ausleihe_reservierung UNIQUE (reservierung_id)
);