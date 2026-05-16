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
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.DBaccess_Raumverwaltung;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

@RestController
@RequestMapping("/api/raumverwaltung")
public class RaumverwaltungController {

    private final DBaccess_Raumverwaltung dbaccessRaumverwaltung;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;
    private final JwtService jwtService;

    public RaumverwaltungController(
            DBaccess_Raumverwaltung dbaccessRaumverwaltung,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung,
            JwtService jwtService) {
        this.dbaccessRaumverwaltung = dbaccessRaumverwaltung;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String suchbegriff, HttpServletRequest request) {
        if (authenticatedRaumverwalterOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Raumverwalter oder Admin verfügbar"));
        }

        List<Raum> raeume = dbaccessRaumverwaltung.findeRaeumeNachFilter(suchbegriff);
        return ResponseEntity.ok(raeume.stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody RaumRequest payload, HttpServletRequest request) {
        if (authenticatedRaumverwalterOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Raumverwalter oder Admin verfügbar"));
        }

        try {
            Raum gespeichert = dbaccessRaumverwaltung.legeRaumAn(
                    new Raum(payload.raumNr(), payload.gebaeude())
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(gespeichert));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{raumNr}")
    public ResponseEntity<?> update(
            @PathVariable Integer raumNr,
            @RequestBody RaumUpdateRequest payload,
            HttpServletRequest request) {

        if (authenticatedRaumverwalterOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Raumverwalter oder Admin verfügbar"));
        }

        try {
            Raum bearbeitet = dbaccessRaumverwaltung.bearbeiteRaum(raumNr, payload.raumNr(), payload.gebaeude());
            return ResponseEntity.ok(toResponse(bearbeitet));
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage() == null ? "Ungültige Eingabe" : ex.getMessage();
            HttpStatus status = message.contains("nicht gefunden")
                    ? HttpStatus.NOT_FOUND
                    : message.contains("existiert bereits")
                    ? HttpStatus.CONFLICT
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", message));
        }
    }

    @DeleteMapping("/{raumNr}")
    public ResponseEntity<?> delete(@PathVariable Integer raumNr, HttpServletRequest request) {
        if (authenticatedRaumverwalterOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Raumverwalter oder Admin verfügbar"));
        }

        try {
            boolean geloescht = dbaccessRaumverwaltung.loescheRaum(raumNr);
            if (!geloescht) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Raum nicht gefunden"));
            }
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", "Raum gelöscht"));
    }

    private Map<String, Object> toResponse(Raum raum) {
        return Map.of(
                "raumNr", raum.getRaumNr(),
                "gebaeude", raum.getGebaeude(),
                "anzeigeName", raum.getGebaeude() + " / Raum " + raum.getRaumNr()
        );
    }

    private AppUser authenticatedRaumverwalterOrAdmin(HttpServletRequest request) {
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

        return user.getRole() == Role.RAUM_VERWALTER || user.getRole() == Role.ADMIN ? user : null;
    }

    public record RaumRequest(Integer raumNr, String gebaeude) {
    }

    public record RaumUpdateRequest(Integer raumNr, String gebaeude) {
    }
}
