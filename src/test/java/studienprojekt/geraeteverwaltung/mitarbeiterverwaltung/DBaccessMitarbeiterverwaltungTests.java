package studienprojekt.geraeteverwaltung.mitarbeiterverwaltung;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_Mitarbeiterverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Anrede;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DBaccess_Mitarbeiterverwaltung.class)
class DBaccessMitarbeiterverwaltungTests {

    @Autowired
    private DBaccess_Mitarbeiterverwaltung dbaccess;

    @Test
    void legeMitarbeiterAnUndSucheFunktioniert() {
        Mitarbeiter gespeichert = dbaccess.legeMitarbeiterAn(new Mitarbeiter(1, "Max", "Mustermann", Anrede.HERR));

        assertEquals(1, gespeichert.getPersonalNr());
        assertNotNull(dbaccess.sucheMitarbeiter(1));
    }

    @Test
    void bearbeiteMitarbeiterAktualisiertDaten() {
        dbaccess.legeMitarbeiterAn(new Mitarbeiter(2, "Erika", "Muster", Anrede.FRAU));

        Mitarbeiter aktualisiert = dbaccess.bearbeiteMitarbeiter(2, "Erika", "Mustermann", Anrede.DIVERS);

        assertEquals("Mustermann", aktualisiert.getNachname());
        assertEquals(Anrede.DIVERS, aktualisiert.getAnrede());
    }

    @Test
    void findeNachNameLiefertTreffer() {
        dbaccess.legeMitarbeiterAn(new Mitarbeiter(6, "Lena", "Kurz", Anrede.FRAU));

        Mitarbeiter treffer = dbaccess.findeNachName("Kurz");

        assertNotNull(treffer);
        assertEquals(6, treffer.getPersonalNr());
    }

    @Test
    void loescheMitarbeiterLiefertTrueBeiTreffer() {
        dbaccess.legeMitarbeiterAn(new Mitarbeiter(7, "Tom", "Berg", Anrede.HERR));

        boolean geloescht = dbaccess.loescheMitarbeiter(7);

        assertTrue(geloescht);
        assertNull(dbaccess.sucheMitarbeiter(7));
    }

    @Test
    void legeMitarbeiterAnMitDoppelterPersonalNrWirftFehler() {
        dbaccess.legeMitarbeiterAn(new Mitarbeiter(3, "Tom", "Tester", Anrede.HERR));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> dbaccess.legeMitarbeiterAn(new Mitarbeiter(3, "Tim", "Tester", Anrede.HERR)));

        assertTrue(ex.getMessage().contains("existiert bereits"));
    }

    @Test
    void bearbeiteUnbekanntenMitarbeiterWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> dbaccess.bearbeiteMitarbeiter(999, "A", "B", Anrede.FRAU));
    }

    @Test
    void sucheMitUngueltigerPersonalNrWirftFehler() {
        assertThrows(IllegalArgumentException.class, () -> dbaccess.sucheMitarbeiter(0));
    }

    @Test
    void loescheUnbekanntenMitarbeiterLiefertFalse() {
        assertFalse(dbaccess.loescheMitarbeiter(999));
    }

    @Test
    void findeNachNameMitLeeremSuchbegriffWirftFehler() {
        assertThrows(IllegalArgumentException.class, () -> dbaccess.findeNachName(" "));
    }

    @Test
    void konstruktorMitLeeremVornamenWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> new Mitarbeiter(4, "   ", "Nachname", Anrede.HERR));
    }

    @Test
    void aendereMitNullAnredeWirftFehler() {
        Mitarbeiter m = new Mitarbeiter(5, "Lisa", "Lang", Anrede.FRAU);

        assertThrows(IllegalArgumentException.class, () -> m.aendere("Lisa", "Lang", null));
    }
}
