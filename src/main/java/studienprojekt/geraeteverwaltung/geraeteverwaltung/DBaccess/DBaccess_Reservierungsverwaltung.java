package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
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

        Geraet reserviertesGeraet = ermittleReservierbaresGeraet(geraetetypId, ausleihdatum, rueckgabedatum, null);
        if (reserviertesGeraet == null) {
            throw new IllegalStateException("Kein konkret reservierbares Gerät verfügbar");
        }

        Reservierung reservierung = new Reservierung(ausleihdatum, rueckgabedatum, geraetetyp, mitarbeiter, reserviertesGeraet);
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

        if (!istGeraetImZeitraumFrei(reservierung.getReserviertesGeraet().getInventarNr(), ausleihdatum, rueckgabedatum, reservierungsNr)) {
            Geraet neuReserviertesGeraet = ermittleReservierbaresGeraet(
                    reservierung.getGeraetetyp().getId(),
                    ausleihdatum,
                    rueckgabedatum,
                    reservierungsNr
            );
            if (neuReserviertesGeraet == null) {
                throw new IllegalStateException("Kein konkret reservierbares Gerät verfügbar");
            }
            reservierung.setReserviertesGeraet(neuReserviertesGeraet);
        }
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
        if (!istGeraetImZeitraumFrei(reservierung.getReserviertesGeraet().getInventarNr(), ausleihdatum, rueckgabedatum, reservierungsNr)) {
            Geraet neuReserviertesGeraet = ermittleReservierbaresGeraet(
                    reservierung.getGeraetetyp().getId(),
                    ausleihdatum,
                    rueckgabedatum,
                    reservierungsNr
            );
            if (neuReserviertesGeraet == null) {
                throw new IllegalStateException("Kein konkret reservierbares Gerät verfügbar");
            }
            reservierung.setReserviertesGeraet(neuReserviertesGeraet);
        }
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
        loeseAusleiheBezugFuerAbgeschlosseneReservierung(reservierung);
        em.remove(reservierung);
        return true;
    }

    public boolean loescheFremdeReservierung(Integer reservierungsNr) {
        Reservierung reservierung = em.find(Reservierung.class, reservierungsNr);
        if (reservierung == null) {
            return false;
        }
        loeseAusleiheBezugFuerAbgeschlosseneReservierung(reservierung);
        em.remove(reservierung);
        return true;
    }

    public Reservierung sucheReservierung(Integer reservierungsNr) {
        return em.find(Reservierung.class, reservierungsNr);
    }

    public List<Reservierungsstatus> findeReservierungsstatusFuerMitarbeiter(Integer personalNr) {
        return em.createQuery(
                        "SELECT r, a.ausleiheNr, a.tatsaechlichesRueckgabedatum " +
                                "FROM Reservierung r " +
                                "JOIN FETCH r.geraetetyp " +
                                "LEFT JOIN Ausleihe a ON a.reservierung = r " +
                                "WHERE r.mitarbeiter.personalNr = :personalNr " +
                                "ORDER BY r.ausleihdatum DESC",
                        Object[].class)
                .setParameter("personalNr", personalNr)
                .getResultStream()
                .map(ergebnis -> new Reservierungsstatus(
                        (Reservierung) ergebnis[0],
                        (Integer) ergebnis[1],
                        (LocalDate) ergebnis[2]
                ))
                .toList();
    }

    public List<Reservierung> findeReservierungenFuerMitarbeiter(Integer personalNr) {
        return findeReservierungsstatusFuerMitarbeiter(personalNr).stream()
                .map(Reservierungsstatus::reservierung)
                .toList();
    }

    public List<Reservierung> findeOffeneReservierungen() {
        return em.createQuery(
                        "SELECT r FROM Reservierung r " +
                                "JOIN FETCH r.geraetetyp " +
                                "JOIN FETCH r.mitarbeiter " +
                                "WHERE NOT EXISTS (" +
                                "SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r" +
                                ") ORDER BY r.ausleihdatum ASC",
                        Reservierung.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Long> findeReservierteGeraetetypIds(LocalDate tag) {
        LocalDate referenzTag = tag != null ? tag : LocalDate.now();
        return em.createQuery(
                        "SELECT DISTINCT r.geraetetyp.id FROM Reservierung r " +
                                "WHERE r.ausleihdatum <= :tag AND r.rueckgabedatum >= :tag " +
                                "AND NOT EXISTS (" +
                                "SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r" +
                                ")",
                        Long.class)
                .setParameter("tag", referenzTag)
                .getResultList();
    }

    public List<Geraet> findeVerfuegbareGeraeteFuerReservierung(Reservierung reservierung) {
        List<Geraet> geraete = em.createQuery(
                        "SELECT g FROM Geraet g " +
                                "WHERE g.istAusleihbar = true " +
                                "AND g.staendigerNutzer IS NULL " +
                                "AND g.standort IS NULL " +
                                "AND g.geraetetyp.id = :geraetetypId " +
                                "AND NOT EXISTS (" +
                                "SELECT a.ausleiheNr FROM Ausleihe a " +
                                "WHERE a.geraet = g " +
                                "AND a.tatsaechlichesRueckgabedatum IS NULL " +
                                "AND a.ausleihdatum <= :rueckgabe " +
                                "AND a.vereinbartesRueckgabedatum >= :ausleihe" +
                                ") " +
                                "ORDER BY g.inventarNr ASC",
                        Geraet.class)
                .setParameter("geraetetypId", reservierung.getGeraetetyp().getId())
                .setParameter("ausleihe", reservierung.getAusleihdatum())
                .setParameter("rueckgabe", reservierung.getRueckgabedatum())
                .getResultList();
        Geraet reserviertesGeraet = reservierung.getReserviertesGeraet();
        if (reserviertesGeraet == null) {
            return geraete;
        }

        Integer reservierteInventarNr = reserviertesGeraet.getInventarNr();
        geraete.sort((a, b) -> {
            boolean aIstReserviert = a.getInventarNr().equals(reservierteInventarNr);
            boolean bIstReserviert = b.getInventarNr().equals(reservierteInventarNr);
            if (aIstReserviert == bIstReserviert) {
                return Integer.compare(a.getInventarNr(), b.getInventarNr());
            }
            return aIstReserviert ? -1 : 1;
        });
        return geraete;
    }

    public List<Zeitraum> findeNichtVerfuegbareZeitraeume(Long geraetetypId,
                                                          LocalDate start,
                                                          LocalDate ende,
                                                          Integer exklusiveReservierungId) {
        validiereDatumsbereich(start, ende);

        Long nutzbareKapazitaet = em.createQuery(
                        "SELECT COUNT(g) FROM Geraet g " +
                                "WHERE g.geraetetyp.id = :typId " +
                                "AND g.istAusleihbar = true " +
                                "AND g.staendigerNutzer IS NULL " +
                                "AND g.standort IS NULL",
                        Long.class)
                .setParameter("typId", geraetetypId)
                .getSingleResult();

        if (nutzbareKapazitaet == 0L) {
            return List.of(new Zeitraum(start, ende));
        }

        List<LocalDate> blockierteTage = new ArrayList<>();
        LocalDate tag = start;
        while (!tag.isAfter(ende)) {
            Long aktiveReservierungen = zaehleAktiveReservierungen(geraetetypId, tag, exklusiveReservierungId);
            Long aktiveAusleihen = zaehleAktiveAusleihen(geraetetypId, tag);
            if (nutzbareKapazitaet <= (aktiveReservierungen + aktiveAusleihen)) {
                blockierteTage.add(tag);
            }
            tag = tag.plusDays(1);
        }

        if (blockierteTage.isEmpty()) {
            return List.of();
        }

        List<Zeitraum> zeitraeume = new ArrayList<>();
        LocalDate von = blockierteTage.getFirst();
        LocalDate bis = von;

        for (int i = 1; i < blockierteTage.size(); i++) {
            LocalDate aktuellerTag = blockierteTage.get(i);
            if (aktuellerTag.equals(bis.plusDays(1))) {
                bis = aktuellerTag;
                continue;
            }
            zeitraeume.add(new Zeitraum(von, bis));
            von = aktuellerTag;
            bis = aktuellerTag;
        }
        zeitraeume.add(new Zeitraum(von, bis));
        return zeitraeume;
    }

    private boolean istVerfuegbar(Long geraetetypId, LocalDate von, LocalDate bis) {
        return istVerfuegbarExklusive(geraetetypId, von, bis, null);
    }

    private Geraet ermittleReservierbaresGeraet(Long geraetetypId, LocalDate von, LocalDate bis, Integer exklusiveReservierungId) {
        List<Geraet> kandidaten = em.createQuery(
                        "SELECT g FROM Geraet g WHERE g.geraetetyp.id = :typId " +
                                "AND g.istAusleihbar = true " +
                                "AND g.staendigerNutzer IS NULL " +
                                "AND g.standort IS NULL " +
                                "ORDER BY g.inventarNr ASC",
                        Geraet.class)
                .setParameter("typId", geraetetypId)
                .getResultList();

        for (Geraet kandidat : kandidaten) {
            if (istGeraetImZeitraumFrei(kandidat.getInventarNr(), von, bis, exklusiveReservierungId)) {
                return kandidat;
            }
        }
        return null;
    }

    private boolean istGeraetImZeitraumFrei(Integer inventarNr, LocalDate von, LocalDate bis, Integer exklusiveReservierungId) {
        Long blockierendeAusleihen = em.createQuery(
                        "SELECT COUNT(a) FROM Ausleihe a WHERE a.geraet.inventarNr = :inventarNr " +
                                "AND a.tatsaechlichesRueckgabedatum IS NULL " +
                                "AND a.ausleihdatum <= :bis " +
                                "AND a.vereinbartesRueckgabedatum >= :von",
                        Long.class)
                .setParameter("inventarNr", inventarNr)
                .setParameter("von", von)
                .setParameter("bis", bis)
                .getSingleResult();

        String reservierungsJpql = "SELECT COUNT(r) FROM Reservierung r WHERE r.reserviertesGeraet.inventarNr = :inventarNr " +
                "AND r.ausleihdatum <= :bis " +
                "AND r.rueckgabedatum >= :von " +
                "AND NOT EXISTS (SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r)" +
                (exklusiveReservierungId != null ? " AND r.reservierungsNr <> :id" : "");
        var query = em.createQuery(reservierungsJpql, Long.class)
                .setParameter("inventarNr", inventarNr)
                .setParameter("von", von)
                .setParameter("bis", bis);
        if (exklusiveReservierungId != null) {
            query.setParameter("id", exklusiveReservierungId);
        }
        Long blockierendeReservierungen = query.getSingleResult();

        return blockierendeAusleihen == 0 && blockierendeReservierungen == 0;
    }

    private boolean istVerfuegbarExklusive(Long geraetetypId, LocalDate von, LocalDate bis, Integer exklusiveReservierungId) {
        Long verfuegbareGeraete = em.createQuery(
                        "SELECT COUNT(g) FROM Geraet g WHERE g.geraetetyp.id = :typId " +
                                "AND g.istAusleihbar = true " +
                                "AND g.staendigerNutzer IS NULL " +
                                "AND g.standort IS NULL", Long.class)
                .setParameter("typId", geraetetypId)
                .getSingleResult();

        String reservierungenJpql = "SELECT COUNT(r) FROM Reservierung r WHERE r.geraetetyp.id = :typId " +
                "AND r.ausleihdatum <= :bis AND r.rueckgabedatum >= :von " +
                "AND NOT EXISTS (SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r)" +
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
                                "AND a.tatsaechlichesRueckgabedatum IS NULL " +
                                "AND a.ausleihdatum <= :bis " +
                                "AND a.vereinbartesRueckgabedatum >= :von", Long.class)
                .setParameter("typId", geraetetypId)
                .setParameter("von", von)
                .setParameter("bis", bis)
                .getSingleResult();

        return verfuegbareGeraete > (aktiveReservierungen + aktiveAusleihen);
    }

    private Long zaehleAktiveReservierungen(Long geraetetypId, LocalDate tag, Integer exklusiveReservierungId) {
        String reservierungenJpql = "SELECT COUNT(r) FROM Reservierung r WHERE r.geraetetyp.id = :typId " +
                "AND r.ausleihdatum <= :tag AND r.rueckgabedatum >= :tag " +
                "AND NOT EXISTS (SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r)" +
                (exklusiveReservierungId != null ? " AND r.reservierungsNr <> :id" : "");

        var query = em.createQuery(reservierungenJpql, Long.class)
                .setParameter("typId", geraetetypId)
                .setParameter("tag", tag);
        if (exklusiveReservierungId != null) {
            query.setParameter("id", exklusiveReservierungId);
        }
        return query.getSingleResult();
    }

    private Long zaehleAktiveAusleihen(Long geraetetypId, LocalDate tag) {
        return em.createQuery(
                        "SELECT COUNT(a) FROM Ausleihe a WHERE a.geraet.geraetetyp.id = :typId " +
                                "AND a.geraet.istAusleihbar = true " +
                                "AND a.geraet.staendigerNutzer IS NULL " +
                                "AND a.geraet.standort IS NULL " +
                                "AND a.tatsaechlichesRueckgabedatum IS NULL " +
                                "AND a.ausleihdatum <= :tag " +
                                "AND a.vereinbartesRueckgabedatum >= :tag", Long.class)
                .setParameter("typId", geraetetypId)
                .setParameter("tag", tag)
                .getSingleResult();
    }

    public int loescheAbgeschlosseneAlteReservierungen(LocalDate stichtag) {
        LocalDate wirksamerStichtag = stichtag != null ? stichtag : LocalDate.now();
        return em.createQuery(
                        "DELETE FROM Reservierung r " +
                                "WHERE r.rueckgabedatum < :stichtag " +
                                "AND NOT EXISTS (SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r)")
                .executeUpdate();
    }

    private void loeseAusleiheBezugFuerAbgeschlosseneReservierung(Reservierung reservierung) {
        if (reservierung == null || !reservierung.getRueckgabedatum().isBefore(LocalDate.now())) {
            return;
        }
        em.createQuery(
                        "UPDATE Ausleihe a SET a.reservierung = NULL WHERE a.reservierung = :reservierung")
                .setParameter("reservierung", reservierung)
                .executeUpdate();
    }

    private void validiereDatumsbereich(LocalDate ausleihdatum, LocalDate rueckgabedatum) {
        if (ausleihdatum == null || rueckgabedatum == null) {
            throw new IllegalArgumentException("Ausleihdatum und Rückgabedatum sind Pflichtfelder");
        }

        if (ausleihdatum.isAfter(rueckgabedatum)) {
            throw new IllegalArgumentException("Startdatum darf nicht nach dem Rückgabedatum liegen");
        }
    }

    public record Zeitraum(LocalDate start, LocalDate ende) {
    }
    public record Reservierungsstatus(Reservierung reservierung, Integer ausleiheNr, LocalDate tatsaechlichesRueckgabedatum) {
        public boolean istAktiv() {
            return ausleiheNr == null || tatsaechlichesRueckgabedatum == null;
        }
    }
}