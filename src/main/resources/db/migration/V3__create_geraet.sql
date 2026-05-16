-- KATEGORIE
CREATE TABLE kategorie (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           bezeichnung VARCHAR(255) NOT NULL
);

-- GERAETETYP
CREATE TABLE geraetetyp (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            hersteller VARCHAR(255) NOT NULL,
                            bezeichnung VARCHAR(255) NOT NULL,
                            kategorie_id BIGINT NOT NULL,

                            CONSTRAINT fk_typ_kategorie FOREIGN KEY (kategorie_id)
                                REFERENCES kategorie(id)
);

-- GERAET
CREATE TABLE geraet (
                        inventar_nr INT NOT NULL,
                        serien_nr INT,
                        kaufdatum DATE,
                        ist_ausleihbar BOOLEAN NOT NULL,

                        geraetetyp_id BIGINT NOT NULL,
                        mitarbeiter_id INT,
                        raum_id INT,

                        CONSTRAINT pk_geraet PRIMARY KEY (inventar_nr),

                        CONSTRAINT fk_geraet_typ FOREIGN KEY (geraetetyp_id)
                            REFERENCES geraetetyp(id),

                        CONSTRAINT fk_geraet_mitarbeiter FOREIGN KEY (mitarbeiter_id)
                            REFERENCES mitarbeiter(personal_nr),

                        CONSTRAINT fk_geraet_raum FOREIGN KEY (raum_id)
                            REFERENCES raum(raum_nr)
);