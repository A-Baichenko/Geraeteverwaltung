package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Geraet {

    @Id
    private Integer inventarNr;

    private Integer serienNr;
    private LocalDate kaufdatum;
    private boolean istAusleihbar;

    @ManyToOne
    private Geraetetyp geraetetyp;

    protected Geraet() {}

    public Geraet(Integer inventarNr, Integer serienNr, LocalDate kaufdatum, boolean istAusleihbar, Geraetetyp geraetetyp) {
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
    }

    public void aendere(Integer serienNr, LocalDate kaufdatum, boolean istAusleihbar) {
        this.serienNr = serienNr;
        this.kaufdatum = kaufdatum;
        this.istAusleihbar = istAusleihbar;
    }

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
}
