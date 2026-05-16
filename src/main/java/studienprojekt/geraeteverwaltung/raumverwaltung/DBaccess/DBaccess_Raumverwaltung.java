package studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

@Service
@Transactional
public class DBaccess_Raumverwaltung {

    @PersistenceContext
    private EntityManager entityManager;

    public Raum legeRaumAn(Raum raum) {
        if (raum == null) {
            throw new IllegalArgumentException("raum darf nicht null sein");
        }
        if (entityManager.find(Raum.class, raum.getRaumNr()) != null) {
            throw new IllegalArgumentException("Raum mit raumNr existiert bereits");
        }

        entityManager.persist(raum);
        return raum;
    }

    public Raum sucheRaum(Integer raumNr) {
        if (raumNr == null || raumNr <= 0) {
            throw new IllegalArgumentException("raumNr muss > 0 sein");
        }
        return entityManager.find(Raum.class, raumNr);
    }

    public Raum bearbeiteRaum(Integer raumNr, String gebaeude) {
        Raum gefunden = entityManager.find(Raum.class, raumNr);
        if (gefunden == null) {
            throw new IllegalArgumentException("Raum nicht gefunden");
        }

        gefunden.aendere(gebaeude);
        return gefunden;
    }

    public boolean loescheRaum(Integer raumNr) {
        Raum gefunden = entityManager.find(Raum.class, raumNr);
        if (gefunden == null) {
            return false;
        }

        entityManager.remove(gefunden);
        return true;
    }

    @Transactional(readOnly = true)
    public Raum findeNachGebaeude(String gebaeude) {
        if (gebaeude == null || gebaeude.isBlank()) {
            throw new IllegalArgumentException("Suchbegriff darf nicht leer sein");
        }

        List<Raum> treffer = entityManager.createQuery(
                        "SELECT r FROM Raum r WHERE r.gebaeude = :suchbegriff",
                        Raum.class)
                .setParameter("suchbegriff", gebaeude)
                .setMaxResults(1)
                .getResultList();

        return treffer.isEmpty() ? null : treffer.getFirst();
    }

    @Transactional(readOnly = true)
    public List<Raum> findeAlleRaeume() {
        return entityManager.createQuery(
                        "SELECT r FROM Raum r ORDER BY r.gebaeude, r.raumNr",
                        Raum.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Raum> findeRaeumeNachFilter(String suchbegriff) {
        if (suchbegriff == null || suchbegriff.isBlank()) {
            return findeAlleRaeume();
        }

        String filter = "%" + suchbegriff.toLowerCase() + "%";

        return entityManager.createQuery(
                        "SELECT r FROM Raum r " +
                                "WHERE LOWER(r.gebaeude) LIKE :filter " +
                                "OR STR(r.raumNr) LIKE :filter " +
                                "ORDER BY r.gebaeude, r.raumNr",
                        Raum.class)
                .setParameter("filter", filter)
                .getResultList();
    }
}