package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Ausleihe;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Reservierung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;


import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DBaccess_Ausleiheverwaltung {

    @PersistenceContext
    private EntityManager em;

    public Ausleihe leiheGeraetAus(Long geraetetypId,
                                   Integer personalNr,
                                   LocalDate ausleihdatum,
                                   LocalDate vereinbartesRueckgabedatum) {
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
        return ausleihe;
    }

    public Ausleihe leiheReserviertesGeraetAus(Integer reservierungsNr, LocalDate tatsaechlichesAusleihdatum) {
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
        return ausleihe;
    }

    public Ausleihe gibGeraetZurueck(Integer ausleiheNr, LocalDate rueckgabeDatum) {
        Ausleihe ausleihe = em.find(Ausleihe.class, ausleiheNr);
        if (ausleihe == null) {
            throw new IllegalArgumentException("Ausleihe nicht gefunden");
        }

        ausleihe.gibZurueck(rueckgabeDatum);
        return ausleihe;
    }

    public Ausleihe sucheAusleihe(Integer ausleiheNr) {
        return em.find(Ausleihe.class, ausleiheNr);
    }

    private Geraet ermittleFreiesGeraet(Long geraetetypId, LocalDate von, LocalDate bis) {
        List<Geraet> kandidaten = em.createQuery(
                        "SELECT g FROM Geraet g WHERE g.geraetetyp.id = :typId AND g.istAusleihbar = true", Geraet.class)
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
}
