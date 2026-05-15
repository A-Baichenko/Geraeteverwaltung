package studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "raum")
public class Raum {

    @Id
    @Column(name = "raum_nr", nullable = false)
    private Integer raumNr;

    @Column(nullable = false, length = 100)
    private String gebaeude;

    public Raum() {
    }

    public Raum(Integer raumNr, String gebaeude) {

        if (raumNr == null || raumNr <= 0) {
            throw new IllegalArgumentException("raumNr muss > 0 sein");
        }

        if (gebaeude == null || gebaeude.isBlank()) {
            throw new IllegalArgumentException("gebaeude darf nicht leer sein");
        }

        this.raumNr = raumNr;
        this.gebaeude = gebaeude;
    }

    public Integer getRaumNr() {
        return raumNr;
    }

    public String getGebaeude() {
        return gebaeude;
    }

    public void setRaumNr(Integer raumNr) {

        if (raumNr == null || raumNr <= 0) {
            throw new IllegalArgumentException("raumNr muss > 0 sein");
        }

        this.raumNr = raumNr;
    }

    public void setGebaeude(String gebaeude) {

        if (gebaeude == null || gebaeude.isBlank()) {
            throw new IllegalArgumentException("gebaeude darf nicht leer sein");
        }

        this.gebaeude = gebaeude;
    }

    public void aendere(String gebaeude) {

        if (gebaeude == null || gebaeude.isBlank()) {
            throw new IllegalArgumentException("gebaeude darf nicht leer sein");
        }

        this.gebaeude = gebaeude;
    }
}