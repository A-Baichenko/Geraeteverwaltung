package studienprojekt.geraeteverwaltung.raumverwaltung;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Kategorie;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.DBaccess_Raumverwaltung;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DBaccess_Raumverwaltung.class)
class DBaccessRaumverwaltungWeitereTests {

    @Autowired
    private DBaccess_Raumverwaltung dbaccess;

    @Autowired
    private EntityManager em;

    @Test
    void legeRaumAnUndSucheFunktioniert() {
        Raum raum = new Raum();
        raum.setRaumNr(101);
        raum.setGebaeude("A");

        dbaccess.legeRaumAn(raum);

        Raum gefunden = dbaccess.sucheRaum(101);

        assertNotNull(gefunden);
        assertEquals("A", gefunden.getGebaeude());
    }

    @Test
    void bearbeiteRaumAktualisiertGebaeude() {
        Raum raum = new Raum();
        raum.setRaumNr(102);
        raum.setGebaeude("Alt");

        dbaccess.legeRaumAn(raum);

        Raum bearbeitet =
                dbaccess.bearbeiteRaum(102, "Neu");

        assertEquals("Neu", bearbeitet.getGebaeude());
    }

    @Test
    void loescheRaumEntferntRaum() {
        Raum raum = new Raum();
        raum.setRaumNr(103);
        raum.setGebaeude("B");

        dbaccess.legeRaumAn(raum);

        boolean geloescht = dbaccess.loescheRaum(103);

        assertTrue(geloescht);
        assertNull(dbaccess.sucheRaum(103));
    }

    @Test
    void findeNachGebaeudeFindetRaum() {
        Raum raum = new Raum();
        raum.setRaumNr(104);
        raum.setGebaeude("C");

        dbaccess.legeRaumAn(raum);

        Raum gefunden = dbaccess.findeNachGebaeude("C");

        assertNotNull(gefunden);
        assertEquals(104, gefunden.getRaumNr());
    }

    @Test
    void findeAlleRaeumeLiefertListe() {
        Raum r1 = new Raum();
        r1.setRaumNr(105);
        r1.setGebaeude("X");

        Raum r2 = new Raum();
        r2.setRaumNr(106);
        r2.setGebaeude("Y");

        dbaccess.legeRaumAn(r1);
        dbaccess.legeRaumAn(r2);

        List<Raum> liste = dbaccess.findeAlleRaeume();

        assertEquals(2, liste.size());
    }

    @Test
    void findeRaeumeNachFilterFunktioniert() {
        Raum raum = new Raum();
        raum.setRaumNr(107);
        raum.setGebaeude("Informatik");

        dbaccess.legeRaumAn(raum);

        List<Raum> result =
                dbaccess.findeRaeumeNachFilter("info");

        assertEquals(1, result.size());
    }

    @Test
    void loescheRaumLehntVerwendetenRaumAb() {
        Raum raum = new Raum();
        raum.setRaumNr(108);
        raum.setGebaeude("Labor");
        dbaccess.legeRaumAn(raum);

        Kategorie k = new Kategorie("Monitor");
        em.persist(k);
        Geraetetyp t = new Geraetetyp("Dell", "P2425", k);
        em.persist(t);
        Geraet g = new Geraet(108, 180, LocalDate.now(), true, t);
        g.setStandort(raum);
        em.persist(g);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dbaccess.loescheRaum(108)
        );

        assertEquals("Raum ist noch in Verwendung und kann nicht geloescht werden", ex.getMessage());
    }

    @Test
    void bearbeiteRaumKannRaumnummerAendernUndReferenzenBehalten() {
        Raum raum = new Raum();
        raum.setRaumNr(109);
        raum.setGebaeude("Altbau");
        dbaccess.legeRaumAn(raum);

        Kategorie k = new Kategorie("Beamer");
        em.persist(k);
        Geraetetyp t = new Geraetetyp("Epson", "EB-L", k);
        em.persist(t);
        Geraet g = new Geraet(109, 190, LocalDate.now(), true, t);
        g.setStandort(raum);
        em.persist(g);

        Raum bearbeitet = dbaccess.bearbeiteRaum(109, 209, "Neubau");
        em.flush();
        em.clear();

        assertEquals(209, bearbeitet.getRaumNr());
        assertNull(dbaccess.sucheRaum(109));
        assertEquals("Neubau", dbaccess.sucheRaum(209).getGebaeude());
        assertEquals(209, em.find(Geraet.class, 109).getStandort().getRaumNr());
    }
}
