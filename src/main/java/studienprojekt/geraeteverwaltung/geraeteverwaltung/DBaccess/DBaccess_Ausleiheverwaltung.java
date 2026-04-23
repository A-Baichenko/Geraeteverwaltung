package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Ausleihe;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.GeraetStatus;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Reservierung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;


import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DBaccess_Ausleiheverwaltung {

    @PersistenceContext
    private EntityManager em;

    public Ausleihe leiheGeraetAus(Long geraetetypId,
                                   Integer personalNr,
                                   LocalDate ausleihdatum,
                                   LocalDate vereinbartesRueckgabedatum) {
        validiereAusleihzeitraum(ausleihdatum, vereinbartesRueckgabedatum);
        Mitarbeiter mitarbeiter = em.find(Mitarbeiter.class, personalNr);
        if (mitarbeiter == null) {
            throw new IllegalArgumentException("Mitarbeiter nicht gefunden");
        }

        Geraet geraet = ermittleFreiesGeraet(geraetetypId, ausleihdatum, vereinbartesRueckgabedatum);
        if (geraet == null) {
            throw new IllegalStateException("Kein freies Gerät verfügbar");
        }

        Ausleihe ausleihe = new Ausleihe(ausleihdatum, vereinbartesRueckgabedatum, geraet, mitarbeiter, null);
        em.persist(ausleihe);
        markiereAlsAusgeliehen(geraet);
        return ausleihe;
    }

    public Ausleihe leiheReserviertesGeraetAus(Integer reservierungsNr, LocalDate tatsaechlichesAusleihdatum) {
        if (tatsaechlichesAusleihdatum != null && tatsaechlichesAusleihdatum.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ausleihedatum darf nicht in der Zukunft liegen");
        }
        Reservierung reservierung = em.find(Reservierung.class, reservierungsNr);
        if (reservierung == null) {
            throw new IllegalArgumentException("Reservierung nicht gefunden");
        }

        Geraet geraet = ermittleFreiesGeraet(
                reservierung.getGeraetetyp().getId(),
                tatsaechlichesAusleihdatum,
                reservierung.getRueckgabedatum()
        );

        if (geraet == null) {
            throw new IllegalStateException("Kein freies Gerät verfügbar");
        }

        Ausleihe ausleihe = reservierung.erstelleAusleihe(geraet, tatsaechlichesAusleihdatum, reservierung.getRueckgabedatum());
        em.persist(ausleihe);
        markiereAlsAusgeliehen(geraet);
        return ausleihe;
    }

    public Ausleihe nehmeReservierungAn(Integer reservierungsNr, Integer inventarNr, LocalDate tatsaechlichesAusleihdatum) {
        Reservierung reservierung = em.find(Reservierung.class, reservierungsNr);
        if (reservierung == null) {
            throw new IllegalArgumentException("Reservierung nicht gefunden");
        }

        Integer wirksameInventarNr = inventarNr != null
                ? inventarNr
                : (reservierung.getReserviertesGeraet() != null ? reservierung.getReserviertesGeraet().getInventarNr() : null);
        if (wirksameInventarNr == null) {
            throw new IllegalArgumentException("Für diese Reservierung wurde kein Gerät hinterlegt");
        }

        Geraet geraet = em.find(Geraet.class, wirksameInventarNr);
        if (geraet == null) {
            throw new IllegalArgumentException("Gerät nicht gefunden");
        }

        if (!geraet.isIstAusleihbar()) {
            throw new IllegalStateException("Gerät ist nicht verleihbar");
        }

        if (!geraet.getGeraetetyp().getId().equals(reservierung.getGeraetetyp().getId())) {
            throw new IllegalArgumentException("Gerätetyp des Geräts passt nicht zur Reservierung");
        }

        Long vorhandeneAusleihe = em.createQuery(
                        "SELECT COUNT(a) FROM Ausleihe a WHERE a.reservierung.reservierungsNr = :reservierungsNr",
                        Long.class)
                .setParameter("reservierungsNr", reservierungsNr)
                .getSingleResult();

        if (vorhandeneAusleihe > 0) {
            throw new IllegalStateException("Reservierung wurde bereits angenommen");
        }

        LocalDate ausleihdatum = reservierung.getAusleihdatum();
        LocalDate rueckgabedatum = reservierung.getRueckgabedatum();
        if (LocalDate.now().isBefore(ausleihdatum)) {
            throw new IllegalStateException("Reservierung kann erst am Ausleihtag ausgeliehen werden");
        }

        Long blockierendeAusleihen = em.createQuery(
                        "SELECT COUNT(a) FROM Ausleihe a WHERE a.geraet.inventarNr = :inventarNr " +
                                "AND a.ausleihdatum <= :bis " +
                                "AND COALESCE(a.tatsaechlichesRueckgabedatum, a.vereinbartesRueckgabedatum) >= :von", Long.class)
                .setParameter("inventarNr", wirksameInventarNr)
                .setParameter("von", ausleihdatum)
                .setParameter("bis", rueckgabedatum)
                .getSingleResult();

        if (blockierendeAusleihen > 0) {
            throw new IllegalStateException("Gerät ist im Reservierungszeitraum nicht verfügbar");
        }

        LocalDate wirksamesAusleihdatum = tatsaechlichesAusleihdatum != null ? tatsaechlichesAusleihdatum : ausleihdatum;
        Ausleihe ausleihe = reservierung.erstelleAusleihe(geraet, wirksamesAusleihdatum, rueckgabedatum);
        em.persist(ausleihe);
        markiereAlsAusgeliehen(geraet);
        return ausleihe;
    }

    public Ausleihe gibGeraetZurueck(Integer ausleiheNr, LocalDate rueckgabeDatum) {
        Ausleihe ausleihe = em.find(Ausleihe.class, ausleiheNr);
        if (ausleihe == null) {
            throw new IllegalArgumentException("Ausleihe nicht gefunden");
        }

        ausleihe.gibZurueck(rueckgabeDatum);
        aktualisiereStatusNachRueckgabe(ausleihe.getGeraet());
        return ausleihe;
    }

    public Ausleihe sucheAusleihe(Integer ausleiheNr) {
        return em.find(Ausleihe.class, ausleiheNr);
    }

    @Transactional(readOnly = true)
    public List<Integer> findeAktivAusgelieheneInventarnummern(LocalDate tag) {
        LocalDate referenzTag = tag != null ? tag : LocalDate.now();
        return em.createQuery(
                        "SELECT DISTINCT a.geraet.inventarNr FROM Ausleihe a " +
                                "WHERE a.ausleihdatum <= :tag " +
                                "AND COALESCE(a.tatsaechlichesRueckgabedatum, a.vereinbartesRueckgabedatum) >= :tag",
                        Integer.class)
                .setParameter("tag", referenzTag)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public Map<Integer, String> findeAktiveAusleiherJeInventar(LocalDate tag) {
        LocalDate referenzTag = tag != null ? tag : LocalDate.now();
        List<Ausleihe> aktiveAusleihen = em.createQuery(
                        "SELECT a FROM Ausleihe a " +
                                "JOIN FETCH a.geraet g " +
                                "JOIN FETCH a.mitarbeiter m " +
                                "WHERE a.ausleihdatum <= :tag " +
                                "AND COALESCE(a.tatsaechlichesRueckgabedatum, a.vereinbartesRueckgabedatum) >= :tag " +
                                "ORDER BY a.ausleihdatum DESC, a.ausleiheNr DESC",
                        Ausleihe.class)
                .setParameter("tag", referenzTag)
                .getResultList();

        Map<Integer, String> ausleiherNachInventar = new LinkedHashMap<>();
        for (Ausleihe ausleihe : aktiveAusleihen) {
            Integer inventarNr = ausleihe.getGeraet().getInventarNr();
            ausleiherNachInventar.putIfAbsent(
                    inventarNr,
                    ausleihe.getMitarbeiter().getVorname() + " " + ausleihe.getMitarbeiter().getNachname()
            );
        }
        return ausleiherNachInventar;
    }

    private Geraet ermittleFreiesGeraet(Long geraetetypId, LocalDate von, LocalDate bis) {
        List<Geraet> kandidaten = em.createQuery(
                        "SELECT g FROM Geraet g WHERE g.geraetetyp.id = :typId " +
                                "AND g.istAusleihbar = true " +
                                "AND g.staendigerNutzer IS NULL " +
                                "AND g.standort IS NULL",
                        Geraet.class)
                .setParameter("typId", geraetetypId)
                .getResultList();

        for (Geraet geraet : kandidaten) {
            Long blockierendeAusleihen = em.createQuery(
                            "SELECT COUNT(a) FROM Ausleihe a WHERE a.geraet.inventarNr = :inventarNr " +
                                    "AND a.ausleihdatum <= :bis " +
                                    "AND COALESCE(a.tatsaechlichesRueckgabedatum, a.vereinbartesRueckgabedatum) >= :von", Long.class)
                    .setParameter("inventarNr", geraet.getInventarNr())
                    .setParameter("von", von)
                    .setParameter("bis", bis)
                    .getSingleResult();

            if (blockierendeAusleihen == 0) {
                return geraet;
            }
        }

        return null;
    }

    private void markiereAlsAusgeliehen(Geraet geraet) {
        if (geraet != null) {
            geraet.setStatus(GeraetStatus.AUSGELIEHEN);
        }
    }

    private void aktualisiereStatusNachRueckgabe(Geraet geraet) {
        if (geraet == null) {
            return;
        }

        if (!geraet.isIstAusleihbar()) {
            geraet.setStatus(GeraetStatus.WARTUNG_DEFEKT);
            return;
        }

        if (geraet.getStaendigerNutzer() != null) {
            geraet.setStatus(GeraetStatus.FEST_ZUGEORDNET);
            return;
        }

        geraet.setStatus(GeraetStatus.VERFUEGBAR);
    }
    private void validiereAusleihzeitraum(LocalDate ausleihdatum, LocalDate rueckgabedatum) {
        if (ausleihdatum == null || rueckgabedatum == null) {
            throw new IllegalArgumentException("Ausleihdatum und Rückgabedatum sind erforderlich");
        }
        if (ausleihdatum.isAfter(rueckgabedatum)) {
            throw new IllegalArgumentException("Ausleihdatum darf nicht nach dem Rückgabedatum liegen");
        }
        if (ausleihdatum.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ausleihe in der Zukunft ist nicht erlaubt. Bitte zuerst reservieren.");
        }
    }


}
