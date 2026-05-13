package studienprojekt.geraeteverwaltung.geraeteverwaltung;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Ausleiheverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Geraeteverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Reservierungsverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.*;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_Mitarbeiterverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Anrede;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        DBaccess_Geraeteverwaltung.class,
        DBaccess_Mitarbeiterverwaltung.class,
        DBaccess_Reservierungsverwaltung.class,
        DBaccess_Ausleiheverwaltung.class
})
class DBaccessReservierungsAusleiheTests {

    @Autowired
    private DBaccess_Geraeteverwaltung geraeteDb;

    @Autowired
    private DBaccess_Mitarbeiterverwaltung mitarbeiterDb;

    @Autowired
    private DBaccess_Reservierungsverwaltung reservierungsDb;

    @Autowired
    private DBaccess_Ausleiheverwaltung ausleiheDb;

    @Test
    void reservierungUndAusleiheFunktionierenDurchgaengig() {
        Kategorie k = geraeteDb.legeKategorieAn(new Kategorie("Laptop"));
        Geraetetyp t = geraeteDb.legeGeraetetypAn(new Geraetetyp("Dell", "XPS", k));
        Geraet g = geraeteDb.legeGeraetAn(new Geraet(100, 1234, LocalDate.now(), true, t));

        Mitarbeiter m = mitarbeiterDb.legeMitarbeiterAn(new Mitarbeiter(42, "Mia", "Muster", Anrede.FRAU));

        Reservierung reservierung = reservierungsDb.reserviereGeraet(
                t.getId(),
                m.getPersonalNr(),
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 20)
        );

        assertNotNull(reservierung.getReservierungsNr());

        Ausleihe ausleihe = ausleiheDb.leiheReserviertesGeraetAus(
                reservierung.getReservierungsNr(),
                LocalDate.of(2026, 4, 10)
        );

        assertNotNull(ausleihe.getAusleiheNr());
        assertEquals(g.getInventarNr(), ausleihe.getGeraet().getInventarNr());
        assertEquals(GeraetStatus.AUSGELIEHEN, ausleihe.getGeraet().getStatus());

        Ausleihe zurueckgegeben = ausleiheDb.gibGeraetZurueck(
                ausleihe.getAusleiheNr(),
                LocalDate.of(2026, 4, 18)
        );

        assertEquals(LocalDate.of(2026, 4, 18), zurueckgegeben.getTatsaechlichesRueckgabedatum());
        assertEquals(GeraetStatus.VERFUEGBAR, zurueckgegeben.getGeraet().getStatus());
        assertTrue(ausleiheDb.findeAktivAusgelieheneInventarnummern(LocalDate.of(2026, 4, 18)).isEmpty());

        Map<Integer, String> aktiveAusleiher = ausleiheDb.findeAktiveAusleiherJeInventar(LocalDate.of(2026, 4, 18));
        assertFalse(aktiveAusleiher.containsKey(g.getInventarNr()));

        Ausleihe neueAusleihe = ausleiheDb.leiheGeraetAus(
                t.getId(),
                m.getPersonalNr(),
                LocalDate.of(2026, 4, 18),
                LocalDate.of(2026, 4, 19)
        );
        assertNotNull(neueAusleihe.getAusleiheNr());
        assertEquals(g.getInventarNr(), neueAusleihe.getGeraet().getInventarNr());
    }

    @Test
    void zweiteUeberlappendeReservierungWirdAbgelehntWennNurEinGeraetVorhanden() {
        Kategorie k = geraeteDb.legeKategorieAn(new Kategorie("Tablet"));
        Geraetetyp t = geraeteDb.legeGeraetetypAn(new Geraetetyp("Apple", "iPad", k));
        geraeteDb.legeGeraetAn(new Geraet(200, 5678, LocalDate.now(), true, t));

        Mitarbeiter m1 = mitarbeiterDb.legeMitarbeiterAn(new Mitarbeiter(1, "Anna", "A", Anrede.FRAU));
        Mitarbeiter m2 = mitarbeiterDb.legeMitarbeiterAn(new Mitarbeiter(2, "Ben", "B", Anrede.HERR));

        reservierungsDb.reserviereGeraet(
                t.getId(),
                m1.getPersonalNr(),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 5)
        );

        assertThrows(IllegalStateException.class, () -> reservierungsDb.reserviereGeraet(
                t.getId(),
                m2.getPersonalNr(),
                LocalDate.of(2026, 5, 3),
                LocalDate.of(2026, 5, 7)
        ));
    }

    @Test
    void ausleiheInDerZukunftWirdAbgelehnt() {
        Kategorie k = geraeteDb.legeKategorieAn(new Kategorie("Notebook"));
        Geraetetyp t = geraeteDb.legeGeraetetypAn(new Geraetetyp("Lenovo", "T14", k));
        geraeteDb.legeGeraetAn(new Geraet(300, 7890, LocalDate.now(), true, t));
        Mitarbeiter m = mitarbeiterDb.legeMitarbeiterAn(new Mitarbeiter(17, "Lena", "Test", Anrede.FRAU));

        assertThrows(IllegalArgumentException.class, () -> ausleiheDb.leiheGeraetAus(
                t.getId(),
                m.getPersonalNr(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        ));
    }

    @Test
    void festZugeordnetesGeraetIstNichtReservierbar() {
        Kategorie k = geraeteDb.legeKategorieAn(new Kategorie("Beamer"));
        Geraetetyp t = geraeteDb.legeGeraetetypAn(new Geraetetyp("Epson", "EB-X", k));
        Geraet g = geraeteDb.legeGeraetAn(new Geraet(400, 9999, LocalDate.now(), true, t));
        Mitarbeiter m = mitarbeiterDb.legeMitarbeiterAn(new Mitarbeiter(51, "Tom", "Tester", Anrede.HERR));
        g.setStaendigerNutzer(m);

        assertThrows(IllegalStateException.class, () -> reservierungsDb.reserviereGeraet(
                t.getId(),
                m.getPersonalNr(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        ));
    }
}
