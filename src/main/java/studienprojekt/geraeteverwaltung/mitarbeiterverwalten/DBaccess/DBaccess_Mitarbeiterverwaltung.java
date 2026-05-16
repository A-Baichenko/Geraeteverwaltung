package studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Ausleihe;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Reservierung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Anrede;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;

@Service
@Transactional
public class DBaccess_Mitarbeiterverwaltung {

    @PersistenceContext
    private EntityManager entityManager;

    public Mitarbeiter legeMitarbeiterAn(Mitarbeiter mitarbeiter) {
        if (mitarbeiter == null) {
            throw new IllegalArgumentException("mitarbeiter darf nicht null sein");
        }
        if (entityManager.find(Mitarbeiter.class, mitarbeiter.getPersonalNr()) != null) {
            throw new IllegalArgumentException("Mitarbeiter mit personalNr existiert bereits");
        }

        entityManager.persist(mitarbeiter);
        return mitarbeiter;
    }

    public Mitarbeiter sucheMitarbeiter(Integer personalNr) {
        if (personalNr == null || personalNr <= 0) {
            throw new IllegalArgumentException("personalNr muss > 0 sein");
        }
        return entityManager.find(Mitarbeiter.class, personalNr);
    }

    public Mitarbeiter bearbeiteMitarbeiter(Integer personalNr, String vorname, String nachname, Anrede anrede) {
        return bearbeiteMitarbeiter(personalNr, personalNr, vorname, nachname, anrede);
    }

    public Mitarbeiter bearbeiteMitarbeiter(
            Integer bisherigePersonalNr,
            Integer neuePersonalNr,
            String vorname,
            String nachname,
            Anrede anrede) {

        if (neuePersonalNr == null || neuePersonalNr <= 0) {
            throw new IllegalArgumentException("personalNr muss > 0 sein");
        }

        Mitarbeiter gefunden = entityManager.find(Mitarbeiter.class, bisherigePersonalNr);
        if (gefunden == null) {
            throw new IllegalArgumentException("Mitarbeiter nicht gefunden");
        }

        if (gefunden.getPersonalNr().equals(neuePersonalNr)) {
            gefunden.aendere(vorname, nachname, anrede);
            return gefunden;
        }

        if (entityManager.find(Mitarbeiter.class, neuePersonalNr) != null) {
            throw new IllegalArgumentException("Mitarbeiter mit personalNr existiert bereits");
        }

        Mitarbeiter verschoben = new Mitarbeiter(neuePersonalNr, vorname, nachname, anrede);
        entityManager.persist(verschoben);

        entityManager.createQuery(
                        "SELECT g FROM Geraet g WHERE g.staendigerNutzer = :mitarbeiter",
                        Geraet.class)
                .setParameter("mitarbeiter", gefunden)
                .getResultList()
                .forEach(geraet -> geraet.setStaendigerNutzer(verschoben));

        entityManager.createQuery(
                        "SELECT r FROM Reservierung r WHERE r.mitarbeiter = :mitarbeiter",
                        Reservierung.class)
                .setParameter("mitarbeiter", gefunden)
                .getResultList()
                .forEach(reservierung -> reservierung.setMitarbeiter(verschoben));

        entityManager.createQuery(
                        "SELECT a FROM Ausleihe a WHERE a.mitarbeiter = :mitarbeiter",
                        Ausleihe.class)
                .setParameter("mitarbeiter", gefunden)
                .getResultList()
                .forEach(ausleihe -> ausleihe.setMitarbeiter(verschoben));

        entityManager.createQuery(
                        "SELECT u FROM AppUser u WHERE u.mitarbeiter = :mitarbeiter",
                        AppUser.class)
                .setParameter("mitarbeiter", gefunden)
                .getResultList()
                .forEach(appUser -> appUser.setMitarbeiter(verschoben));

        entityManager.remove(gefunden);
        return verschoben;
    }

    public boolean loescheMitarbeiter(Integer personalNr) {
        Mitarbeiter gefunden = entityManager.find(Mitarbeiter.class, personalNr);
        if (gefunden == null) {
            return false;
        }

        long verwendungen = entityManager.createQuery(
                        "SELECT COUNT(g) FROM Geraet g WHERE g.staendigerNutzer = :mitarbeiter",
                        Long.class)
                .setParameter("mitarbeiter", gefunden)
                .getSingleResult()
                + entityManager.createQuery(
                        "SELECT COUNT(r) FROM Reservierung r WHERE r.mitarbeiter = :mitarbeiter",
                        Long.class)
                .setParameter("mitarbeiter", gefunden)
                .getSingleResult()
                + entityManager.createQuery(
                        "SELECT COUNT(a) FROM Ausleihe a WHERE a.mitarbeiter = :mitarbeiter",
                        Long.class)
                .setParameter("mitarbeiter", gefunden)
                .getSingleResult()
                + entityManager.createQuery(
                        "SELECT COUNT(u) FROM AppUser u WHERE u.mitarbeiter = :mitarbeiter",
                        Long.class)
                .setParameter("mitarbeiter", gefunden)
                .getSingleResult();

        if (verwendungen > 0) {
            throw new IllegalStateException("Mitarbeiter ist noch in Verwendung und kann nicht geloescht werden");
        }

        entityManager.remove(gefunden);
        return true;
    }

    @Transactional(readOnly = true)
    public Mitarbeiter findeNachName(String vornameOderNachname) {
        if (vornameOderNachname == null || vornameOderNachname.isBlank()) {
            throw new IllegalArgumentException("Suchbegriff darf nicht leer sein");
        }

        List<Mitarbeiter> treffer = entityManager.createQuery(
                        "SELECT m FROM Mitarbeiter m WHERE m.vorname = :suchbegriff OR m.nachname = :suchbegriff",
                        Mitarbeiter.class)
                .setParameter("suchbegriff", vornameOderNachname)
                .setMaxResults(1)
                .getResultList();

        return treffer.isEmpty() ? null : treffer.getFirst();
    }

    @Transactional(readOnly = true)
    public List<Mitarbeiter> findeAlleMitarbeiter() {
        return entityManager.createQuery(
                        "SELECT m FROM Mitarbeiter m ORDER BY m.nachname, m.vorname, m.personalNr",
                        Mitarbeiter.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Mitarbeiter> findeMitarbeiterNachFilter(String suchbegriff) {
        if (suchbegriff == null || suchbegriff.isBlank()) {
            return findeAlleMitarbeiter();
        }

        String filter = "%" + suchbegriff.toLowerCase() + "%";

        return entityManager.createQuery(
                        "SELECT m FROM Mitarbeiter m " +
                                "WHERE LOWER(m.vorname) LIKE :filter " +
                                "OR LOWER(m.nachname) LIKE :filter " +
                                "OR STR(m.personalNr) LIKE :filter " +
                                "OR LOWER(STR(m.anrede)) LIKE :filter " +
                                "ORDER BY m.nachname, m.vorname, m.personalNr",
                        Mitarbeiter.class)
                .setParameter("filter", filter)
                .getResultList();
    }
}
