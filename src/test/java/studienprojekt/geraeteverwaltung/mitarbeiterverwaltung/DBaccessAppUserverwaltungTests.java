package studienprojekt.geraeteverwaltung.mitarbeiterverwaltung;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DBaccess_AppUserverwaltung.class)
class DBaccessAppUserverwaltungTests {

    @Autowired
    private DBaccess_AppUserverwaltung dbaccess;

    @Autowired
    private EntityManager em;


    @Test
    void sucheNachUsernameLiefertNullWennNichtGefunden() {
        AppUser gefunden = dbaccess.sucheNachUsername("unbekannt");

        assertNull(gefunden);
    }

    @Test
    void sucheNachUsernameMitLeeremStringWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> dbaccess.sucheNachUsername(""));
    }

    @Test
    void sucheNachUsernameMitNullWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> dbaccess.sucheNachUsername(null));
    }
}