INSERT INTO mitarbeiter (personal_nr, vorname, nachname, anrede)
VALUES
    (1001, 'Test', 'Admin', 'DIVERS'),
    (1002, 'Test', 'Geraete', 'DIVERS'),
    (1003, 'Test', 'Raum', 'DIVERS'),
    (1004, 'Test', 'Personen', 'DIVERS'),
    (1005, 'Test', 'Mitarbeiter', 'DIVERS');

ALTER TABLE app_user ADD COLUMN personal_nr INT NULL;

UPDATE app_user SET personal_nr = 1001 WHERE username = 'test1admin';
UPDATE app_user SET personal_nr = 1002 WHERE username = 'test2geraete';
UPDATE app_user SET personal_nr = 1003 WHERE username = 'test3raum';
UPDATE app_user SET personal_nr = 1004 WHERE username = 'test4personen';
UPDATE app_user SET personal_nr = 1005 WHERE username = 'test5mitarbeiter';

ALTER TABLE app_user MODIFY personal_nr INT NOT NULL;
ALTER TABLE app_user ADD CONSTRAINT uq_app_user_personal_nr UNIQUE (personal_nr);
ALTER TABLE app_user ADD CONSTRAINT fk_app_user_mitarbeiter
    FOREIGN KEY (personal_nr) REFERENCES mitarbeiter (personal_nr);