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

        Ausleihe zurueckgegeben = ausleiheDb.gibGeraetZurueck(
                ausleihe.getAusleiheNr(),
                LocalDate.of(2026, 4, 18)
        );

        assertEquals(LocalDate.of(2026, 4, 18), zurueckgegeben.getTatsaechlichesRueckgabedatum());
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
}
