package studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Anrede;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;

@Service
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
        Mitarbeiter gefunden = entityManager.find(Mitarbeiter.class, personalNr);
        if (gefunden == null) {
            throw new IllegalArgumentException("Mitarbeiter nicht gefunden");
        }

        gefunden.aendere(vorname, nachname, anrede);
        return gefunden;
    }

    public boolean loescheMitarbeiter(Integer personalNr) {
        Mitarbeiter gefunden = entityManager.find(Mitarbeiter.class, personalNr);
        if (gefunden == null) {
            return false;
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
}
