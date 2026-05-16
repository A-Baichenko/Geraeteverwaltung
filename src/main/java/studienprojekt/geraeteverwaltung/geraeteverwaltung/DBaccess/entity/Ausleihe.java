package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity;

import jakarta.persistence.*;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;

import java.time.LocalDate;

@Entity
public class Ausleihe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ausleiheNr;

    private LocalDate ausleihdatum;
    private LocalDate vereinbartesRueckgabedatum;
    private LocalDate tatsaechlichesRueckgabedatum;

    @ManyToOne(optional = false)
    @JoinColumn(name = "geraet_id", nullable = false)
    private Geraet geraet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mitarbeiter_id", nullable = false)
    private Mitarbeiter mitarbeiter;

    @OneToOne
    @JoinColumn(name = "reservierung_id")
    private Reservierung reservierung;

    protected Ausleihe() {
    }

    public Ausleihe(LocalDate ausleihdatum,
                    LocalDate vereinbartesRueckgabedatum,
                    Geraet geraet,
                    Mitarbeiter mitarbeiter,
                    Reservierung reservierung) {
        if (ausleihdatum == null || vereinbartesRueckgabedatum == null || vereinbartesRueckgabedatum.isBefore(ausleihdatum)) {
            throw new IllegalArgumentException("Ungültiger Ausleihzeitraum");
        }
        if (geraet == null || mitarbeiter == null) {
            throw new IllegalArgumentException("Geraet und Mitarbeiter sind Pflichtfelder");
        }
        this.ausleihdatum = ausleihdatum;
        this.vereinbartesRueckgabedatum = vereinbartesRueckgabedatum;
        this.geraet = geraet;
        this.mitarbeiter = mitarbeiter;
        this.reservierung = reservierung;
    }

    public void gibZurueck(LocalDate rueckgabeDatum) {
        if (rueckgabeDatum == null || rueckgabeDatum.isBefore(ausleihdatum)) {
            throw new IllegalArgumentException("Ungültiges Rückgabedatum");
        }
        this.tatsaechlichesRueckgabedatum = rueckgabeDatum;
    }

    public Integer getAusleiheNr() {
        return ausleiheNr;
    }

    public LocalDate getAusleihdatum() {
        return ausleihdatum;
    }

    public LocalDate getVereinbartesRueckgabedatum() {
        return vereinbartesRueckgabedatum;
    }

    public LocalDate getTatsaechlichesRueckgabedatum() {
        return tatsaechlichesRueckgabedatum;
    }

    public Geraet getGeraet() {
        return geraet;
    }

    public Mitarbeiter getMitarbeiter() {
        return mitarbeiter;
    }

    public Reservierung getReservierung() {
        return reservierung;
    }

    public void loeseReservierungsbezug() {
        this.reservierung = null;
    }
}

