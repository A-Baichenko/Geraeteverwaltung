CREATE TABLE app_user (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          username VARCHAR(100) NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          role VARCHAR(50) NOT NULL,
                          CONSTRAINT pk_app_user PRIMARY KEY (id),
                          CONSTRAINT uq_app_user_username UNIQUE (username),
                          CONSTRAINT chk_app_user_role CHECK (role IN ('ADMIN', 'MITARBEITER', 'GERAETE_VERWALTER', 'RAUM_VERWALTER', 'PERSONEN_VERWALTER'))
);

INSERT INTO app_user (username, password, role) VALUES
                                                    ('test1admin', 'test', 'ADMIN'),
                                                    ('test2geraete', 'test', 'GERAETE_VERWALTER'),
                                                    ('test3raum', 'test', 'RAUM_VERWALTER'),
                                                    ('test4personen', 'test', 'PERSONEN_VERWALTER'),
                                                    ('test5mitarbeiter', 'test', 'MITARBEITER');