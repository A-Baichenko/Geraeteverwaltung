package studienprojekt.geraeteverwaltung.REST.Controller.Mitarbeiter_Raumverwaltung;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_Mitarbeiterverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Anrede;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;

@RestController
@RequestMapping("/api/mitarbeiterverwaltung")
public class MitarbeiterverwaltungController {

    private final DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;
    private final JwtService jwtService;

    public MitarbeiterverwaltungController(
            DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung,
            JwtService jwtService) {
        this.dbaccessMitarbeiterverwaltung = dbaccessMitarbeiterverwaltung;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String suchbegriff, HttpServletRequest request) {
        if (authenticatedPersonenverwalterOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Mitarbeiterverwalter oder Admin verfügbar"));
        }

        List<Mitarbeiter> mitarbeiter = dbaccessMitarbeiterverwaltung.findeMitarbeiterNachFilter(suchbegriff);
        return ResponseEntity.ok(mitarbeiter.stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MitarbeiterRequest payload, HttpServletRequest request) {
        if (authenticatedPersonenverwalterOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Mitarbeiterverwalter oder Admin verfügbar"));
        }

        try {
            Mitarbeiter gespeichert = dbaccessMitarbeiterverwaltung.legeMitarbeiterAn(
                    new Mitarbeiter(
                            payload.personalNr(),
                            payload.vorname(),
                            payload.nachname(),
                            parseAnrede(payload.anrede())
                    )
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(gespeichert));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{personalNr}")
    public ResponseEntity<?> update(
            @PathVariable Integer personalNr,
            @RequestBody MitarbeiterUpdateRequest payload,
            HttpServletRequest request) {

        if (authenticatedPersonenverwalterOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Mitarbeiterverwalter oder Admin verfügbar"));
        }

        try {
            Mitarbeiter bearbeitet = dbaccessMitarbeiterverwaltung.bearbeiteMitarbeiter(
                    personalNr,
                    payload.vorname(),
                    payload.nachname(),
                    parseAnrede(payload.anrede())
            );
            return ResponseEntity.ok(toResponse(bearbeitet));
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage() == null ? "Ungültige Eingabe" : ex.getMessage();
            HttpStatus status = message.contains("nicht gefunden") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", message));
        }
    }

    @DeleteMapping("/{personalNr}")
    public ResponseEntity<?> delete(@PathVariable Integer personalNr, HttpServletRequest request) {
        if (authenticatedPersonenverwalterOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Mitarbeiterverwalter oder Admin verfügbar"));
        }

        try {
            boolean geloescht = dbaccessMitarbeiterverwaltung.loescheMitarbeiter(personalNr);
            if (!geloescht) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Mitarbeiter nicht gefunden"));
            }
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", "Mitarbeiter gelöscht"));
    }

    private Map<String, Object> toResponse(Mitarbeiter mitarbeiter) {
        return Map.of(
                "personalNr", mitarbeiter.getPersonalNr(),
                "vorname", mitarbeiter.getVorname(),
                "nachname", mitarbeiter.getNachname(),
                "anrede", mitarbeiter.getAnrede().name(),
                "anzeigeName", mitarbeiter.getAnrede().name() + " " + mitarbeiter.getVorname() + " " + mitarbeiter.getNachname()
        );
    }


    private Anrede parseAnrede(String anrede) {
        if (anrede == null || anrede.isBlank()) {
            throw new IllegalArgumentException("Anrede darf nicht leer sein");
        }

        try {
            return Anrede.valueOf(anrede.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Ungültige Anrede. Erlaubt: HERR, FRAU, DIVERS");
        }
    }

    private AppUser authenticatedPersonenverwalterOrAdmin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        Claims claims;
        try {
            claims = jwtService.parseToken(authHeader.substring(7));
        } catch (IllegalArgumentException ex) {
            return null;
        }

        AppUser user = dbaccessAppUserverwaltung.sucheNachUsername(claims.getSubject());
        if (user == null) {
            return null;
        }

        return user.getRole() == Role.PERSONEN_VERWALTER || user.getRole() == Role.ADMIN ? user : null;
    }

    public record MitarbeiterRequest(Integer personalNr, String vorname, String nachname, String anrede) {
    }

    public record MitarbeiterUpdateRequest(String vorname, String nachname, String anrede) {
    }
}
