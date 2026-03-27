package studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Kategorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bezeichnung;

    @OneToMany(mappedBy = "kategorie", cascade = CascadeType.ALL)
    private List<Geraetetyp> geraetetypen = new ArrayList<>();

    protected Kategorie() {}

    public Kategorie(String bezeichnung) {
        if (bezeichnung == null || bezeichnung.isBlank()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein");
        }
        this.bezeichnung = bezeichnung;
    }

    public void aendere(String bezeichnung) {
        if (bezeichnung == null || bezeichnung.isBlank()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein");
        }
        this.bezeichnung = bezeichnung;
    }

    public Long getId() {
        return id;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public List<Geraetetyp> getGeraetetypen() {
        return geraetetypen;
    }
}
