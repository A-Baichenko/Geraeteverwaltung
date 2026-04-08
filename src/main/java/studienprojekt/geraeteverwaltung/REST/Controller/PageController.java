package studienprojekt.geraeteverwaltung.REST.Controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;

@RestController
@RequestMapping("/api/page")
public class PageController {

    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;
    private final JwtService jwtService;

    public PageController(DBaccess_AppUserverwaltung dbaccessAppUserverwaltung, JwtService jwtService) {
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
        this.jwtService = jwtService;
    }

    @GetMapping("/home-content")
    public ResponseEntity<?> getHomeContent(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token fehlt"));
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = jwtService.parseToken(token);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token ungültig"));
        }

        String username = claims.getSubject();
        AppUser user = dbaccessAppUserverwaltung.sucheNachUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Benutzer nicht gefunden"));
        }

        if (user.getRole() != Role.ADMIN) {
            return ResponseEntity.ok(Map.of("html", "<div class=\"placeholder\">Kein Home-Board für diese Rolle.</div>"));
        }

        return ResponseEntity.ok(Map.of("html", adminHomeHtml()));
    }

    private String adminHomeHtml() {
        return """
                <div class="board">\s
                    <article class="column">\s
                        <h2>Verfügbar</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Verfügbar">\s
                        <div class="card-item">Kategorie <span>Anz.</span></div>
                    </article>
                    <article class="column">\s
                        <h2>Reserviert</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Reserviert">\s
                        <div class="card-item">Gerätetyp <span>Anz.</span></div>
                    </article>
                    <article class="column">\s
                        <h2>Ausgeliehen</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Ausgeliehen">\s
                        <div class="card-item">Daten <span>id</span></div>
                    </article>
                    <article class="column">\s
                        <h2>Überfällig</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Überfällig">\s
                        <div class="card-item">Daten <span>id</span></div>
                    </article>
                    <article class="column">\s
                        <h2>Nicht-Ausleihbar</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Nicht-Ausleihbar">\s
                        <div class="card-item">Daten <span>id</span></div>
                    </article>
                </div>
               \s""";
    }
}
