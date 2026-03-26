CREATE TABLE mitarbeiter (
                             personal_nr INT NOT NULL,
                             vorname VARCHAR(100) NOT NULL,
                             nachname VARCHAR(100) NOT NULL,
                             anrede VARCHAR(20) NOT NULL,
                             CONSTRAINT pk_mitarbeiter PRIMARY KEY (personal_nr),
                             CONSTRAINT chk_mitarbeiter_anrede CHECK (anrede IN ('HERR', 'FRAU', 'DIVERS'))
);