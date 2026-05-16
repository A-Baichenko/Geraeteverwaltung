package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Geraetetyp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hersteller;
    private String bezeichnung;

    @ManyToOne
    private Kategorie kategorie;

    @OneToMany(mappedBy = "geraetetyp", cascade = CascadeType.ALL)
    private List<Geraet> geraete = new ArrayList<>();

    protected Geraetetyp() {
    }

    public Geraetetyp(String hersteller, String bezeichnung, Kategorie kategorie) {
        if (hersteller == null || hersteller.isBlank()) {
            throw new IllegalArgumentException("Hersteller darf nicht leer sein");
        }
        if (bezeichnung == null || bezeichnung.isBlank()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein");
        }
        if (kategorie == null) {
            throw new IllegalArgumentException("Kategorie darf nicht null sein");
        }

        this.hersteller = hersteller;
        this.bezeichnung = bezeichnung;
        this.kategorie = kategorie;
    }

    public void aendere(String hersteller, String bezeichnung) {
        if (hersteller == null || hersteller.isBlank()) {
            throw new IllegalArgumentException("Hersteller darf nicht leer sein");
        }
        if (bezeichnung == null || bezeichnung.isBlank()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein");
        }

        this.hersteller = hersteller;
        this.bezeichnung = bezeichnung;
    }

    public Long getId() {
        return id;
    }

    public String getHersteller() {
        return hersteller;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public Kategorie getKategorie() {
        return kategorie;
    }

}
