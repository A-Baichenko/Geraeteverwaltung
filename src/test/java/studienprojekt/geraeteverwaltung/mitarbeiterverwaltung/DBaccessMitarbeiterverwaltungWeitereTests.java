package studienprojekt.geraeteverwaltung.mitarbeiterverwaltung;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Kategorie;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_Mitarbeiterverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Anrede;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DBaccess_Mitarbeiterverwaltung.class)
class DBaccessMitarbeiterverwaltungWeitereTests {

    @Autowired
    private DBaccess_Mitarbeiterverwaltung dbaccess;

    @Autowired
    private EntityManager em;

    @Test
    void legeMitarbeiterAnUndSucheFunktioniert() {
        Mitarbeiter m = new Mitarbeiter();
        m.setPersonalNr(1);
        m.setVorname("Max");
        m.setNachname("Mustermann");
        m.setAnrede(Anrede.HERR);

        dbaccess.legeMitarbeiterAn(m);

        Mitarbeiter gefunden = dbaccess.sucheMitarbeiter(1);

        assertNotNull(gefunden);
        assertEquals("Max", gefunden.getVorname());
    }

    @Test
    void bearbeiteMitarbeiterAktualisiertDaten() {
        Mitarbeiter m = new Mitarbeiter();
        m.setPersonalNr(2);
        m.setVorname("Lisa");
        m.setNachname("Alt");
        m.setAnrede(Anrede.FRAU);

        dbaccess.legeMitarbeiterAn(m);

        Mitarbeiter bearbeitet =
                dbaccess.bearbeiteMitarbeiter(2, "Anna", "Neu", Anrede.FRAU);

        assertEquals("Anna", bearbeitet.getVorname());
        assertEquals("Neu", bearbeitet.getNachname());
    }

    @Test
    void loescheMitarbeiterEntferntMitarbeiter() {
        Mitarbeiter m = new Mitarbeiter();
        m.setPersonalNr(3);
        m.setVorname("Tom");
        m.setNachname("Test");
        m.setAnrede(Anrede.HERR);

        dbaccess.legeMitarbeiterAn(m);

        boolean geloescht = dbaccess.loescheMitarbeiter(3);

        assertTrue(geloescht);
        assertNull(dbaccess.sucheMitarbeiter(3));
    }

    @Test
    void findeNachNameFindetMitarbeiter() {
        Mitarbeiter m = new Mitarbeiter();
        m.setPersonalNr(4);
        m.setVorname("Julia");
        m.setNachname("Meier");
        m.setAnrede(Anrede.FRAU);

        dbaccess.legeMitarbeiterAn(m);

        Mitarbeiter gefunden = dbaccess.findeNachName("Julia");

        assertNotNull(gefunden);
        assertEquals(4, gefunden.getPersonalNr());
    }

    @Test
    void findeAlleMitarbeiterLiefertListe() {
        Mitarbeiter m1 = new Mitarbeiter();
        m1.setPersonalNr(5);
        m1.setVorname("A");
        m1.setNachname("A");
        m1.setAnrede(Anrede.HERR);

        Mitarbeiter m2 = new Mitarbeiter();
        m2.setPersonalNr(6);
        m2.setVorname("B");
        m2.setNachname("B");
        m2.setAnrede(Anrede.FRAU);

        dbaccess.legeMitarbeiterAn(m1);
        dbaccess.legeMitarbeiterAn(m2);

        List<Mitarbeiter> liste = dbaccess.findeAlleMitarbeiter();

        assertEquals(2, liste.size());
    }

    @Test
    void findeMitarbeiterNachFilterFunktioniert() {
        Mitarbeiter m = new Mitarbeiter();
        m.setPersonalNr(7);
        m.setVorname("Peter");
        m.setNachname("Parker");
        m.setAnrede(Anrede.HERR);

        dbaccess.legeMitarbeiterAn(m);

        List<Mitarbeiter> result =
                dbaccess.findeMitarbeiterNachFilter("peter");

        assertEquals(1, result.size());
    }

    @Test
    void loescheMitarbeiterLehntVerwendetenMitarbeiterAb() {
        Mitarbeiter m = new Mitarbeiter();
        m.setPersonalNr(8);
        m.setVorname("Ben");
        m.setNachname("Belegt");
        m.setAnrede(Anrede.HERR);
        dbaccess.legeMitarbeiterAn(m);

        Kategorie k = new Kategorie("Notebook");
        em.persist(k);
        Geraetetyp t = new Geraetetyp("Lenovo", "ThinkPad", k);
        em.persist(t);
        Geraet g = new Geraet(8, 808, LocalDate.now(), true, t);
        g.setStaendigerNutzer(m);
        em.persist(g);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dbaccess.loescheMitarbeiter(8)
        );

        assertEquals("Mitarbeiter ist noch in Verwendung und kann nicht gelÃ¶scht werden", ex.getMessage());
    }
}
