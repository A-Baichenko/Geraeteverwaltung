package studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mitarbeiter")
public class Mitarbeiter {

    @Id
    @Column(name = "personal_nr", nullable = false)
    private Integer personalNr;

    @Column(nullable = false, length = 100)
    private String vorname;

    @Column(nullable = false, length = 100)
    private String nachname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Anrede anrede;

    public Mitarbeiter() {
    }

    public Mitarbeiter(Integer personalNr, String vorname, String nachname, Anrede anrede) {

        if (personalNr == null || personalNr <= 0) {
            throw new IllegalArgumentException("personalNr muss > 0 sein");
        }

        if (vorname == null || vorname.isBlank()) {
            throw new IllegalArgumentException("vorname darf nicht leer sein");
        }

        if (nachname == null || nachname.isBlank()) {
            throw new IllegalArgumentException("nachname darf nicht leer sein");
        }

        if (anrede == null) {
            throw new IllegalArgumentException("anrede darf nicht null sein");
        }

        this.personalNr = personalNr;
        this.vorname = vorname;
        this.nachname = nachname;
        this.anrede = anrede;
    }

    public Integer getPersonalNr() {
        return personalNr;
    }

    public String getVorname() {
        return vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public Anrede getAnrede() {
        return anrede;
    }

    public void setPersonalNr(Integer personalNr) {

        if (personalNr == null || personalNr <= 0) {
            throw new IllegalArgumentException("personalNr muss > 0 sein");
        }

        this.personalNr = personalNr;
    }

    public void setVorname(String vorname) {

        if (vorname == null || vorname.isBlank()) {
            throw new IllegalArgumentException("vorname darf nicht leer sein");
        }

        this.vorname = vorname;
    }

    public void setNachname(String nachname) {

        if (nachname == null || nachname.isBlank()) {
            throw new IllegalArgumentException("nachname darf nicht leer sein");
        }

        this.nachname = nachname;
    }

    public void setAnrede(Anrede anrede) {

        if (anrede == null) {
            throw new IllegalArgumentException("anrede darf nicht null sein");
        }

        this.anrede = anrede;
    }

    public void aendere(String vorname, String nachname, Anrede anrede) {

        if (vorname == null || vorname.isBlank()) {
            throw new IllegalArgumentException("vorname darf nicht leer sein");
        }

        if (nachname == null || nachname.isBlank()) {
            throw new IllegalArgumentException("nachname darf nicht leer sein");
        }

        if (anrede == null) {
            throw new IllegalArgumentException("anrede darf nicht null sein");
        }

        this.vorname = vorname;
        this.nachname = nachname;
        this.anrede = anrede;
    }
}