package studienprojekt.geraeteverwaltung.raumverwaltung;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.DBaccess_Raumverwaltung;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DBaccess_Raumverwaltung.class)
class DBaccessRaumverwaltungTests {

    @Autowired
    private DBaccess_Raumverwaltung dbaccess;

    @Test
    void legeRaumAnUndSucheFunktioniert() {
        Raum gespeichert = dbaccess.legeRaumAn(new Raum(1, "Hauptgebaeude"));

        assertEquals(1, gespeichert.getRaumNr());
        assertNotNull(dbaccess.sucheRaum(1));
    }

    @Test
    void bearbeiteRaumAktualisiertDaten() {
        dbaccess.legeRaumAn(new Raum(2, "Altbau"));

        Raum aktualisiert = dbaccess.bearbeiteRaum(2, "Neubau");

        assertEquals("Neubau", aktualisiert.getGebaeude());
    }

    @Test
    void findeNachGebaeudeLiefertTreffer() {
        dbaccess.legeRaumAn(new Raum(6, "Turm"));

        Raum treffer = dbaccess.findeNachGebaeude("Turm");

        assertNotNull(treffer);
        assertEquals(6, treffer.getRaumNr());
    }

    @Test
    void loescheRaumLiefertTrueBeiTreffer() {
        dbaccess.legeRaumAn(new Raum(7, "Westfluegel"));

        boolean geloescht = dbaccess.loescheRaum(7);

        assertTrue(geloescht);
        assertNull(dbaccess.sucheRaum(7));
    }

    @Test
    void legeRaumAnMitDoppelterRaumNrWirftFehler() {
        dbaccess.legeRaumAn(new Raum(3, "GebaeudeA"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> dbaccess.legeRaumAn(new Raum(3, "GebaeudeB")));

        assertTrue(ex.getMessage().contains("existiert bereits"));
    }

    @Test
    void bearbeiteUnbekanntenRaumWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> dbaccess.bearbeiteRaum(999, "X"));
    }

    @Test
    void sucheMitUngueltigerRaumNrWirftFehler() {
        assertThrows(IllegalArgumentException.class, () -> dbaccess.sucheRaum(0));
    }

    @Test
    void loescheUnbekanntenRaumLiefertFalse() {
        assertFalse(dbaccess.loescheRaum(999));
    }

    @Test
    void findeNachGebaeudeMitLeeremSuchbegriffWirftFehler() {
        assertThrows(IllegalArgumentException.class, () -> dbaccess.findeNachGebaeude(" "));
    }

    @Test
    void konstruktorMitLeeremGebaeudeWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> new Raum(4, "   "));
    }

    @Test
    void aendereMitLeeremGebaeudeWirftFehler() {
        Raum r = new Raum(5, "GebaeudeX");

        assertThrows(IllegalArgumentException.class, () -> r.aendere(" "));
    }
}
