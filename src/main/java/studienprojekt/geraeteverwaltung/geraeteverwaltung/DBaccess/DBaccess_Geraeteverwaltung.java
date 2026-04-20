package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.GeraetStatus;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Kategorie;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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

    public List<Geraet> findeAlleGeraeteMitDetails() {
        return findeGeraeteMitDetailsNachFilter(null, null);
    }

    public List<Geraet> findeGeraeteMitDetailsNachFilter(String query, GeraetStatus status) {
        StringBuilder jpql = new StringBuilder(
                "SELECT g FROM Geraet g " +
                        "JOIN FETCH g.geraetetyp t " +
                        "JOIN FETCH t.kategorie k " +
                        "LEFT JOIN FETCH g.staendigerNutzer " +
                        "LEFT JOIN FETCH g.standort " +
                        "WHERE 1 = 1"
        );

        boolean hasQuery = query != null && !query.isBlank();
        if (status != null) {
            jpql.append(" AND g.status = :status");
        }
        if (hasQuery) {
            jpql.append(" AND (")
                    .append("LOWER(t.hersteller) LIKE :query ")
                    .append("OR LOWER(t.bezeichnung) LIKE :query ")
                    .append("OR LOWER(k.bezeichnung) LIKE :query ")
                    .append("OR LOWER(STR(g.inventarNr)) LIKE :query ")
                    .append("OR LOWER(STR(g.serienNr)) LIKE :query")
                    .append(")");
        }
        jpql.append(" ORDER BY k.bezeichnung, t.bezeichnung, g.inventarNr");

        var typedQuery = em.createQuery(jpql.toString(), Geraet.class);
        if (status != null) {
            typedQuery.setParameter("status", status);
        }
        if (hasQuery) {
            typedQuery.setParameter("query", "%" + query.toLowerCase(Locale.ROOT) + "%");
        }

        return typedQuery.getResultList();
    }

    public void synchronisiereStatus(LocalDate datum) {
        LocalDate referenz = datum != null ? datum : LocalDate.now();

        em.createQuery("UPDATE Geraet g SET g.status = :status WHERE g.istAusleihbar = false")
                .setParameter("status", GeraetStatus.WARTUNG_DEFEKT)
                .executeUpdate();

        em.createQuery("UPDATE Geraet g SET g.status = :status WHERE g.istAusleihbar = true AND g.staendigerNutzer IS NOT NULL")
                .setParameter("status", GeraetStatus.FEST_ZUGEORDNET)
                .executeUpdate();

        em.createQuery("UPDATE Geraet g SET g.status = :status WHERE g.istAusleihbar = true AND g.staendigerNutzer IS NULL")
                .setParameter("status", GeraetStatus.VERFUEGBAR)
                .executeUpdate();

        em.createQuery(
                        "UPDATE Geraet g SET g.status = :status " +
                                "WHERE EXISTS (" +
                                "SELECT a.ausleiheNr FROM Ausleihe a WHERE a.geraet = g " +
                                "AND a.ausleihdatum <= :tag " +
                                "AND COALESCE(a.tatsaechlichesRueckgabedatum, a.vereinbartesRueckgabedatum) >= :tag" +
                                ")")
                .setParameter("status", GeraetStatus.AUSGELIEHEN)
                .setParameter("tag", referenz)
                .executeUpdate();

        em.createQuery(
                        "UPDATE Geraet g SET g.status = :status " +
                                "WHERE g.status = :freiStatus " +
                                "AND EXISTS (" +
                                "SELECT r.reservierungsNr FROM Reservierung r " +
                                "WHERE r.geraetetyp = g.geraetetyp " +
                                "AND r.ausleihdatum <= :tag " +
                                "AND r.rueckgabedatum >= :tag " +
                                "AND NOT EXISTS (" +
                                "SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r" +
                                ")" +
                                ")")
                .setParameter("status", GeraetStatus.RESERVIERT)
                .setParameter("freiStatus", GeraetStatus.VERFUEGBAR)
                .setParameter("tag", referenz)
                .executeUpdate();
    }

    public long zaehleAusleihenZuGeraet(Integer inventarNr) {
        return em.createQuery("SELECT COUNT(a) FROM Ausleihe a WHERE a.geraet.inventarNr = :inventarNr", Long.class)
                .setParameter("inventarNr", inventarNr)
                .getSingleResult();
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

    public Geraetetyp sucheGeraetetypById(Long id) {
        return em.find(Geraetetyp.class, id);
    }

    public Geraet aktualisiereGeraet(Integer inventarNr, Integer serienNr, LocalDate kaufdatum, boolean istAusleihbar) {
        Geraet geraet = em.find(Geraet.class, inventarNr);
        if (geraet == null) {
            throw new IllegalArgumentException("Gerät nicht gefunden");
        }
        geraet.aendere(serienNr, kaufdatum, istAusleihbar);
        return geraet;
    }

    public Geraet verschiebeGeraet(Integer inventarNr, Geraetetyp geraetetyp, Mitarbeiter mitarbeiter, Raum raum) {
        Geraet geraet = em.find(Geraet.class, inventarNr);
        if (geraet == null) {
            throw new IllegalArgumentException("Gerät nicht gefunden");
        }
        if (geraetetyp == null) {
            throw new IllegalArgumentException("Gerätetyp nicht gefunden");
        }
        geraet.setGeraetetyp(geraetetyp);
        geraet.setStandort(raum);
        geraet.setStaendigerNutzer(mitarbeiter);
        return geraet;
    }

    public void loescheGeraet(Integer inventarNr) {
        Geraet geraet = em.find(Geraet.class, inventarNr);
        if (geraet == null) {
            throw new IllegalArgumentException("Gerät nicht gefunden");
        }
        em.remove(geraet);
    }

    @Transactional(readOnly = true)
    public List<Geraetetyp> findeGeraetetypenNachFilter(String suchbegriff) {
        if (suchbegriff == null || suchbegriff.isBlank()) {
            return em.createQuery(
                            "SELECT g FROM Geraetetyp g JOIN FETCH g.kategorie ORDER BY g.hersteller, g.bezeichnung",
                            Geraetetyp.class)
                    .getResultList();
        }

        String filter = "%" + suchbegriff.toLowerCase() + "%";

        return em.createQuery(
                        "SELECT g FROM Geraetetyp g JOIN FETCH g.kategorie " +
                                "WHERE LOWER(g.hersteller) LIKE :filter " +
                                "OR LOWER(g.bezeichnung) LIKE :filter " +
                                "OR LOWER(g.kategorie.bezeichnung) LIKE :filter " +
                                "ORDER BY g.hersteller, g.bezeichnung",
                        Geraetetyp.class)
                .setParameter("filter", filter)
                .getResultList();
    }
}