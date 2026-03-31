package studienprojekt.geraeteverwaltung.geraeteverwaltung;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Geraeteverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Kategorie;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DBaccess_Geraeteverwaltung.class)
class DBaccessGeraeteverwaltungTests {

    @Autowired
    private DBaccess_Geraeteverwaltung dbaccess;

    // ANLEGEN + SUCHE

    @Test
    void legeGeraetAnUndSucheFunktioniert() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Laptop"));
        Geraetetyp t = dbaccess.legeGeraetetypAn(new Geraetetyp("Dell", "XPS", k));

        Geraet gespeichert = dbaccess.legeGeraetAn(
                new Geraet(1, 123, LocalDate.now(), true, t)
        );

        assertEquals(1, gespeichert.getInventarNr());
        assertNotNull(dbaccess.sucheGeraet(1));
    }

    // BEARBEITEN

    @Test
    void bearbeiteGeraetAktualisiertDaten() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Monitor"));
        Geraetetyp t = dbaccess.legeGeraetetypAn(new Geraetetyp("LG", "UltraWide", k));

        dbaccess.legeGeraetAn(new Geraet(2, 111, LocalDate.now(), true, t));

        Geraet aktualisiert = dbaccess.bearbeiteGeraet(2, 999, false);

        assertEquals(999, aktualisiert.getSerienNr());
        assertFalse(aktualisiert.isIstAusleihbar());
    }

    @Test
    void bearbeiteUnbekanntesGeraetWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> dbaccess.bearbeiteGeraet(999, 1, true));
    }

    // KATEGORIE

    @Test
    void bearbeiteKategorieAktualisiertDaten() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Alt"));

        Kategorie aktualisiert = dbaccess.bearbeiteKategorie(k.getId(), "Neu");

        assertEquals("Neu", aktualisiert.getBezeichnung());
    }

    // GERAETETYP

    @Test
    void bearbeiteGeraetetypAktualisiertDaten() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Laptop"));
        Geraetetyp t = dbaccess.legeGeraetetypAn(new Geraetetyp("Dell", "Alt", k));

        Geraetetyp aktualisiert = dbaccess.bearbeiteGeraetetyp(t.getId(), "HP", "Neu");

        assertEquals("HP", aktualisiert.getHersteller());
        assertEquals("Neu", aktualisiert.getBezeichnung());
    }

    @Test
    void bearbeiteUnbekanntenGeraetetypWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> dbaccess.bearbeiteGeraetetyp(999L, "A", "B"));
    }

    @Test
    void bearbeiteUnbekannteKategorieWirftFehler() {
        assertThrows(IllegalArgumentException.class,
                () -> dbaccess.bearbeiteKategorie(999L, "Test"));
    }

    // GET KATEGORIEN

    @Test
    void getKategorienLiefertListe() {
        dbaccess.legeKategorieAn(new Kategorie("Laptop"));
        dbaccess.legeKategorieAn(new Kategorie("Monitor"));

        List<Kategorie> kategorien = dbaccess.getKategorien();

        assertNotNull(kategorien);
        assertEquals(2, kategorien.size());
    }

    // SUCHE GERAETETYP

    @Test
    void sucheGeraetetypNachBezeichnung() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Laptop"));
        dbaccess.legeGeraetetypAn(new Geraetetyp("Dell", "XPS", k));

        Geraetetyp treffer = dbaccess.sucheGeraetetyp("XPS");

        assertNotNull(treffer);
        assertEquals("XPS", treffer.getBezeichnung());
    }

    @Test
    void sucheGeraetetypNachHersteller() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Laptop"));
        dbaccess.legeGeraetetypAn(new Geraetetyp("Apple", "MacBook", k));

        Geraetetyp treffer = dbaccess.sucheGeraetetyp("Apple");

        assertNotNull(treffer);
        assertEquals("Apple", treffer.getHersteller());
    }

    @Test
    void sucheGeraetetypLiefertNullWennNichtGefunden() {
        assertNull(dbaccess.sucheGeraetetyp("Unbekannt"));
    }

    // EDGE CASES

    @Test
    void sucheGeraetLiefertNullWennNichtVorhanden() {
        assertNull(dbaccess.sucheGeraet(999));
    }

    @Test
    void legeGeraetMitDoppelterIdWirftFehler() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Tablet"));
        Geraetetyp t = dbaccess.legeGeraetetypAn(new Geraetetyp("Apple", "iPad", k));

        dbaccess.legeGeraetAn(new Geraet(10, 111, LocalDate.now(), true, t));

        assertThrows(Exception.class,
                () -> dbaccess.legeGeraetAn(new Geraet(10, 222, LocalDate.now(), true, t)));
    }
}