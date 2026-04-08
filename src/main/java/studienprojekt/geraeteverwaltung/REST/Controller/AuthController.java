package studienprojekt.geraeteverwaltung.REST.Controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;
    private final JwtService jwtService;

    public AuthController(DBaccess_AppUserverwaltung dbaccessAppUserverwaltung, JwtService jwtService) {
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        AppUser user = dbaccessAppUserverwaltung.sucheNachUsername(request.username());

        if (user == null || !user.getPassword().equals(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Benutzername oder Passwort falsch"));
        }

        String token = jwtService.createToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(buildUserResponse(user, token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
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

        return ResponseEntity.ok(buildUserResponse(user, null));
    }

    private Map<String, Object> buildUserResponse(AppUser user, String token) {
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        if (token != null) {
            response.put("token", token);
        }
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("personalNr", user.getMitarbeiter().getPersonalNr());
        response.put("mitarbeiterName", user.getMitarbeiter().getVorname() + " " + user.getMitarbeiter().getNachname());
        response.put("welcomeMessage", "Willkommen " + user.getRole().name().replace('_', ' '));
        return response;
    }

    public record LoginRequest(String username, String password) {
    }
}