package studienprojekt.geraeteverwaltung.geraeteverwaltung;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Geraeteverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DBaccess_Geraeteverwaltung.class)
class DBaccessGeraeteverwaltungWeitereTests {

    @Autowired
    private DBaccess_Geraeteverwaltung dbaccess;

    @Autowired
    private EntityManager em;

    @Test
    void aktualisiereGeraetFunktioniert() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Monitor"));
        Geraetetyp t = dbaccess.legeGeraetetypAn(
                new Geraetetyp("LG", "UltraWide", k)
        );

        dbaccess.legeGeraetAn(
                new Geraet(1, 123, LocalDate.now(), true, t)
        );

        Geraet aktualisiert = dbaccess.aktualisiereGeraet(
                1,
                999,
                LocalDate.now(),
                false
        );

        assertEquals(999, aktualisiert.getSerienNr());
        assertFalse(aktualisiert.isIstAusleihbar());
    }

    @Test
    void loescheGeraetEntferntGeraet() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Laptop"));
        Geraetetyp t = dbaccess.legeGeraetetypAn(
                new Geraetetyp("Dell", "XPS", k)
        );

        dbaccess.legeGeraetAn(
                new Geraet(2, 111, LocalDate.now(), true, t)
        );

        dbaccess.loescheGeraet(2);

        assertNull(dbaccess.sucheGeraet(2));
    }

    @Test
    void findeGeraetetypenNachFilterFunktioniert() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Tablet"));

        dbaccess.legeGeraetetypAn(
                new Geraetetyp("Apple", "iPad", k)
        );

        List<Geraetetyp> result =
                dbaccess.findeGeraetetypenNachFilter("apple");

        assertEquals(1, result.size());
        assertEquals("Apple", result.get(0).getHersteller());
    }

    @Test
    void sucheGeraetetypByIdFunktioniert() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Laptop"));

        Geraetetyp typ = dbaccess.legeGeraetetypAn(
                new Geraetetyp("HP", "EliteBook", k)
        );

        Geraetetyp gefunden =
                dbaccess.sucheGeraetetypById(typ.getId());

        assertNotNull(gefunden);
        assertEquals("HP", gefunden.getHersteller());
    }

    @Test
    void findeAlleGeraeteMitDetailsLiefertListe() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Notebook"));
        Geraetetyp t = dbaccess.legeGeraetetypAn(
                new Geraetetyp("Lenovo", "ThinkPad", k)
        );

        dbaccess.legeGeraetAn(
                new Geraet(5, 555, LocalDate.now(), true, t)
        );

        List<Geraet> geraete =
                dbaccess.findeAlleGeraeteMitDetails();

        assertEquals(1, geraete.size());
    }

    @Test
    void aufgehobeneFesteZuordnungMachtGeraetWiederVerfuegbar() {
        Kategorie k = dbaccess.legeKategorieAn(new Kategorie("Dockingstation"));
        Geraetetyp t = dbaccess.legeGeraetetypAn(new Geraetetyp("Dell", "WD19", k));
        Geraet geraet = dbaccess.legeGeraetAn(new Geraet(6, 666, LocalDate.now(), false, t));

        geraet.macheNachAufhebenFesterZuordnungVerfuegbar();

        assertTrue(geraet.isIstAusleihbar());
        assertEquals(GeraetStatus.VERFUEGBAR, geraet.getStatus());
    }
}
