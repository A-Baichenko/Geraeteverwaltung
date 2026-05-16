package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity;

import jakarta.persistence.*;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

import java.time.LocalDate;

@Entity
public class Geraet {

    @Id
    private Integer inventarNr;

    private Integer serienNr;
    private LocalDate kaufdatum;
    private boolean istAusleihbar;

    @Enumerated(EnumType.STRING)
    private GeraetStatus status;

    @ManyToOne
    @JoinColumn(name = "geraetetyp_id", nullable = false)
    private Geraetetyp geraetetyp;

    @ManyToOne
    @JoinColumn(name = "mitarbeiter_id")
    private Mitarbeiter staendigerNutzer;

    @ManyToOne
    @JoinColumn(name = "raum_id")
    private Raum standort;
    protected Geraet() {}

    public Geraet(Integer inventarNr,
                  Integer serienNr,
                  LocalDate kaufdatum,
                  boolean istAusleihbar,
                  Geraetetyp geraetetyp) {

        if (inventarNr == null || inventarNr <= 0) {
            throw new IllegalArgumentException("Inventarnummer ungültig");
        }
        if (geraetetyp == null) {
            throw new IllegalArgumentException("Geraetetyp darf nicht null sein");
        }

        this.inventarNr = inventarNr;
        this.serienNr = serienNr;
        this.kaufdatum = kaufdatum;
        this.istAusleihbar = istAusleihbar;
        this.geraetetyp = geraetetyp;
        this.status = istAusleihbar ? GeraetStatus.VERFUEGBAR : GeraetStatus.WARTUNG_DEFEKT;
    }

    // Änderungsmethode
    public void aendere(Integer serienNr, LocalDate kaufdatum, boolean istAusleihbar) {
        this.serienNr = serienNr;
        this.kaufdatum = kaufdatum;
        this.istAusleihbar = istAusleihbar;
        aktualisiereStatusNachZuweisung();
    }

    // Zuweisungen (wichtig für Use-Cases)

    public void setStaendigerNutzer(Mitarbeiter nutzer) {
        this.staendigerNutzer = nutzer;
        aktualisiereStatusNachZuweisung();
    }

    public void setStandort(Raum standort) {
        this.standort = standort;
        aktualisiereStatusNachZuweisung();
    }

    public void setGeraetetyp(Geraetetyp geraetetyp) {
        if (geraetetyp == null) {
            throw new IllegalArgumentException("Geraetetyp darf nicht null sein");
        }
        this.geraetetyp = geraetetyp;
    }
    // Getter

    public Integer getInventarNr() {
        return inventarNr;
    }

    public Integer getSerienNr() {
        return serienNr;
    }

    public LocalDate getKaufdatum() {
        return kaufdatum;
    }

    public boolean isIstAusleihbar() {
        return istAusleihbar;
    }

    public Geraetetyp getGeraetetyp() {
        return geraetetyp;
    }

    public Mitarbeiter getStaendigerNutzer() {
        return staendigerNutzer;
    }

    public Raum getStandort() {
        return standort;
    }

    public GeraetStatus getStatus() {
        return status;
    }

    public void setStatus(GeraetStatus status) {
        this.status = status;
    }

    public void aktualisiereStatusNachZuweisung() {
        boolean hatFesteZuordnung = this.staendigerNutzer != null || this.standort != null;
        if (hatFesteZuordnung) {
            this.status = GeraetStatus.FEST_ZUGEORDNET;
            return;
        }

        this.status = this.istAusleihbar ? GeraetStatus.VERFUEGBAR : GeraetStatus.WARTUNG_DEFEKT;
    }
}