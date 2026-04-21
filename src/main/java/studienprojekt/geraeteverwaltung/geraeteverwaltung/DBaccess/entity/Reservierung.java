package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity;

import jakarta.persistence.*;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;
import java.time.LocalDate;

@Entity
public class Reservierung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reservierungsNr;

    private LocalDate ausleihdatum;
    private LocalDate rueckgabedatum;

    @ManyToOne(optional = false)
    @JoinColumn(name = "geraetetyp_id", nullable = false)
    private Geraetetyp geraetetyp;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mitarbeiter_id", nullable = false)
    private Mitarbeiter mitarbeiter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserviertes_geraet_id", nullable = false)
    private Geraet reserviertesGeraet;

    protected Reservierung() {
    }

    public Reservierung(LocalDate ausleihdatum,
                        LocalDate rueckgabedatum,
                        Geraetetyp geraetetyp,
                        Mitarbeiter mitarbeiter,
                        Geraet reserviertesGeraet) {
        if (ausleihdatum == null || rueckgabedatum == null || rueckgabedatum.isBefore(ausleihdatum)) {
            throw new IllegalArgumentException("Ungültiger Reservierungszeitraum");
        }
        if (geraetetyp == null || mitarbeiter == null || reserviertesGeraet == null) {
            throw new IllegalArgumentException("Geraetetyp, Mitarbeiter und reserviertes Gerät sind Pflichtfelder");
        }

        this.ausleihdatum = ausleihdatum;
        this.rueckgabedatum = rueckgabedatum;
        this.geraetetyp = geraetetyp;
        this.mitarbeiter = mitarbeiter;
        this.reserviertesGeraet = reserviertesGeraet;
    }

    public void aendere(LocalDate ausleihdatum, LocalDate rueckgabedatum) {
        if (ausleihdatum == null || rueckgabedatum == null || rueckgabedatum.isBefore(ausleihdatum)) {
            throw new IllegalArgumentException("Ungültiger Reservierungszeitraum");
        }
        this.ausleihdatum = ausleihdatum;
        this.rueckgabedatum = rueckgabedatum;
    }

    public Ausleihe erstelleAusleihe(Geraet geraet, LocalDate tatsaechlichesAusleihdatum, LocalDate vereinbartesRueckgabedatum) {
        return new Ausleihe(tatsaechlichesAusleihdatum, vereinbartesRueckgabedatum, geraet, mitarbeiter, this);
    }

    public Integer getReservierungsNr() {
        return reservierungsNr;
    }

    public LocalDate getAusleihdatum() {
        return ausleihdatum;
    }

    public LocalDate getRueckgabedatum() {
        return rueckgabedatum;
    }

    public Geraetetyp getGeraetetyp() {
        return geraetetyp;
    }

    public Mitarbeiter getMitarbeiter() {
        return mitarbeiter;
    }

    public Geraet getReserviertesGeraet() {
        return reserviertesGeraet;
    }

    public void setReserviertesGeraet(Geraet reserviertesGeraet) {
        if (reserviertesGeraet == null) {
            throw new IllegalArgumentException("Reserviertes Gerät darf nicht null sein");
        }
        this.reserviertesGeraet = reserviertesGeraet;
    }
}
