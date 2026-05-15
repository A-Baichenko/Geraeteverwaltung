package studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @OneToOne(optional = false)
    @JoinColumn(name = "personal_nr", nullable = false, unique = true)
    private Mitarbeiter mitarbeiter;

    public AppUser() {
    }

    public AppUser(
            String username,
            String password,
            Role role,
            Mitarbeiter mitarbeiter
    ) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username darf nicht leer sein");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password darf nicht leer sein");
        }

        if (role == null) {
            throw new IllegalArgumentException("role darf nicht null sein");
        }

        if (mitarbeiter == null) {
            throw new IllegalArgumentException("mitarbeiter darf nicht null sein");
        }

        this.username = username;
        this.password = password;
        this.role = role;
        this.mitarbeiter = mitarbeiter;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Mitarbeiter getMitarbeiter() {
        return mitarbeiter;
    }

    public void setUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username darf nicht leer sein");
        }

        this.username = username;
    }

    public void setPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password darf nicht leer sein");
        }

        this.password = password;
    }

    public void setRole(Role role) {

        if (role == null) {
            throw new IllegalArgumentException("role darf nicht null sein");
        }

        this.role = role;
    }

    public void setMitarbeiter(Mitarbeiter mitarbeiter) {

        if (mitarbeiter == null) {
            throw new IllegalArgumentException("mitarbeiter darf nicht null sein");
        }

        this.mitarbeiter = mitarbeiter;
    }
}