package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Reservierung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;

@Service
@Transactional
public class DBaccess_Reservierungsverwaltung {

    @PersistenceContext
    private EntityManager em;

    public Reservierung reserviereGeraet(Long geraetetypId, Integer personalNr, LocalDate ausleihdatum, LocalDate rueckgabedatum) {
        validiereDatumsbereich(ausleihdatum, rueckgabedatum);

        Geraetetyp geraetetyp = em.find(Geraetetyp.class, geraetetypId);
        Mitarbeiter mitarbeiter = em.find(Mitarbeiter.class, personalNr);

        if (geraetetyp == null || mitarbeiter == null) {
            throw new IllegalArgumentException("Geraetetyp oder Mitarbeiter nicht gefunden");
        }

        if (!istVerfuegbar(geraetetypId, ausleihdatum, rueckgabedatum)) {
            throw new IllegalStateException("Kein Gerät dieses Typs verfügbar");
        }

        Reservierung reservierung = new Reservierung(ausleihdatum, rueckgabedatum, geraetetyp, mitarbeiter);
        em.persist(reservierung);
        return reservierung;
    }

    public Reservierung bearbeiteEigeneReservierung(Integer reservierungsNr,
                                                    Integer personalNr,
                                                    LocalDate ausleihdatum,
                                                    LocalDate rueckgabedatum) {
        validiereDatumsbereich(ausleihdatum, rueckgabedatum);

        Reservierung reservierung = em.find(Reservierung.class, reservierungsNr);
        if (reservierung == null) {
            throw new IllegalArgumentException("Reservierung nicht gefunden");
        }
        if (!reservierung.getMitarbeiter().getPersonalNr().equals(personalNr)) {
            throw new IllegalArgumentException("Reservierung gehört nicht zu diesem Mitarbeiter");
        }
        if (!istVerfuegbarExklusive(reservierung.getGeraetetyp().getId(), ausleihdatum, rueckgabedatum, reservierungsNr)) {
            throw new IllegalStateException("Kein Gerät dieses Typs verfügbar");
        }

        reservierung.aendere(ausleihdatum, rueckgabedatum);
        return reservierung;
    }

    public Reservierung bearbeiteFremdeReservierung(Integer reservierungsNr, LocalDate ausleihdatum, LocalDate rueckgabedatum) {
        validiereDatumsbereich(ausleihdatum, rueckgabedatum);

        Reservierung reservierung = em.find(Reservierung.class, reservierungsNr);
        if (reservierung == null) {
            throw new IllegalArgumentException("Reservierung nicht gefunden");
        }
        if (!istVerfuegbarExklusive(reservierung.getGeraetetyp().getId(), ausleihdatum, rueckgabedatum, reservierungsNr)) {
            throw new IllegalStateException("Kein Gerät dieses Typs verfügbar");
        }

        reservierung.aendere(ausleihdatum, rueckgabedatum);
        return reservierung;
    }

    public boolean loescheEigeneReservierung(Integer reservierungsNr, Integer personalNr) {
        Reservierung reservierung = em.find(Reservierung.class, reservierungsNr);
        if (reservierung == null) {
            return false;
        }
        if (!reservierung.getMitarbeiter().getPersonalNr().equals(personalNr)) {
            return false;
        }
        em.remove(reservierung);
        return true;
    }

    public boolean loescheFremdeReservierung(Integer reservierungsNr) {
        Reservierung reservierung = em.find(Reservierung.class, reservierungsNr);
        if (reservierung == null) {
            return false;
        }
        em.remove(reservierung);
        return true;
    }

    public Reservierung sucheReservierung(Integer reservierungsNr) {
        return em.find(Reservierung.class, reservierungsNr);
    }

    public List<Reservierung> findeReservierungenFuerMitarbeiter(Integer personalNr) {
        return em.createQuery(
                        "SELECT r FROM Reservierung r JOIN FETCH r.geraetetyp WHERE r.mitarbeiter.personalNr = :personalNr ORDER BY r.ausleihdatum DESC",
                        Reservierung.class)
                .setParameter("personalNr", personalNr)
                .getResultList();
    }

    private boolean istVerfuegbar(Long geraetetypId, LocalDate von, LocalDate bis) {
        return istVerfuegbarExklusive(geraetetypId, von, bis, null);
    }

    private boolean istVerfuegbarExklusive(Long geraetetypId, LocalDate von, LocalDate bis, Integer exklusiveReservierungId) {
        Long verfuegbareGeraete = em.createQuery(
                        "SELECT COUNT(g) FROM Geraet g WHERE g.geraetetyp.id = :typId AND g.istAusleihbar = true", Long.class)
                .setParameter("typId", geraetetypId)
                .getSingleResult();

        String reservierungenJpql = "SELECT COUNT(r) FROM Reservierung r WHERE r.geraetetyp.id = :typId " +
                "AND r.ausleihdatum <= :bis AND r.rueckgabedatum >= :von" +
                (exklusiveReservierungId != null ? " AND r.reservierungsNr <> :id" : "");
        var reservierungenQuery = em.createQuery(reservierungenJpql, Long.class)
                .setParameter("typId", geraetetypId)
                .setParameter("von", von)
                .setParameter("bis", bis);
        if (exklusiveReservierungId != null) {
            reservierungenQuery.setParameter("id", exklusiveReservierungId);
        }
        Long aktiveReservierungen = reservierungenQuery.getSingleResult();

        Long aktiveAusleihen = em.createQuery(
                        "SELECT COUNT(a) FROM Ausleihe a WHERE a.geraet.geraetetyp.id = :typId " +
                                "AND a.ausleihdatum <= :bis " +
                                "AND COALESCE(a.tatsaechlichesRueckgabedatum, a.vereinbartesRueckgabedatum) >= :von", Long.class)
                .setParameter("typId", geraetetypId)
                .setParameter("von", von)
                .setParameter("bis", bis)
                .getSingleResult();

        return verfuegbareGeraete > (aktiveReservierungen + aktiveAusleihen);
    }

    private void validiereDatumsbereich(LocalDate ausleihdatum, LocalDate rueckgabedatum) {
        if (ausleihdatum == null || rueckgabedatum == null) {
            throw new IllegalArgumentException("Ausleihdatum und Rückgabedatum sind Pflichtfelder");
        }

        if (ausleihdatum.isAfter(rueckgabedatum)) {
            throw new IllegalArgumentException("Startdatum darf nicht nach dem Rückgabedatum liegen");
        }
    }
}