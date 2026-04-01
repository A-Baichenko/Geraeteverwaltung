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

    protected Reservierung() {
    }

    public Reservierung(LocalDate ausleihdatum,
                        LocalDate rueckgabedatum,
                        Geraetetyp geraetetyp,
                        Mitarbeiter mitarbeiter) {
        if (ausleihdatum == null || rueckgabedatum == null || rueckgabedatum.isBefore(ausleihdatum)) {
            throw new IllegalArgumentException("Ungültiger Reservierungszeitraum");
        }
        if (geraetetyp == null || mitarbeiter == null) {
            throw new IllegalArgumentException("Geraetetyp und Mitarbeiter sind Pflichtfelder");
        }

        this.ausleihdatum = ausleihdatum;
        this.rueckgabedatum = rueckgabedatum;
        this.geraetetyp = geraetetyp;
        this.mitarbeiter = mitarbeiter;
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
}
