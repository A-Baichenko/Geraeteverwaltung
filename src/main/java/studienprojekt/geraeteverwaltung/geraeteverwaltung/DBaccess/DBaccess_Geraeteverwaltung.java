package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Kategorie;

import java.util.List;

@Service
@Transactional
public class DBaccess_Geraeteverwaltung {

    @PersistenceContext
    private EntityManager em;

    // ANLEGEN

    public Kategorie legeKategorieAn(Kategorie k) {
        em.persist(k);
        return k;
    }

    public Geraetetyp legeGeraetetypAn(Geraetetyp t) {
        em.persist(t);
        return t;
    }

    public Geraet legeGeraetAn(Geraet g) {
        em.persist(g);
        return g;
    }

    // BEARBEITEN

    public Geraet bearbeiteGeraet(Integer inventarNr, Integer serienNr, boolean istAusleihbar) {
        Geraet g = em.find(Geraet.class, inventarNr);
        if (g == null) throw new IllegalArgumentException("Nicht gefunden");

        g.aendere(serienNr, g.getKaufdatum(), istAusleihbar);
        return g;
    }

    public Geraetetyp bearbeiteGeraetetyp(Long id, String hersteller, String bezeichnung) {
        Geraetetyp t = em.find(Geraetetyp.class, id);
        if (t == null) throw new IllegalArgumentException("Nicht gefunden");

        t.aendere(hersteller, bezeichnung);
        return t;
    }

    public Kategorie bearbeiteKategorie(Long id, String bezeichnung) {
        Kategorie k = em.find(Kategorie.class, id);
        if (k == null) throw new IllegalArgumentException("Nicht gefunden");

        k.aendere(bezeichnung);
        return k;
    }

    // SUCHE

    public Geraet sucheGeraet(Integer inventarNr) {
        return em.find(Geraet.class, inventarNr);
    }

    public List<Kategorie> getKategorien() {
        return em.createQuery("SELECT k FROM Kategorie k", Kategorie.class).getResultList();
    }

    public Geraetetyp sucheGeraetetyp(String suchbegriff) {
        List<Geraetetyp> list = em.createQuery(
                        "SELECT g FROM Geraetetyp g WHERE g.bezeichnung = :s OR g.hersteller = :s",
                        Geraetetyp.class)
                .setParameter("s", suchbegriff)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }
}