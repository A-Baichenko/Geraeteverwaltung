package studienprojekt.geraeteverwaltung.REST.Controller.Tab_Geraeteverwaltung;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.GeraeteverwaltungReservierungsantraegeHtml;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Ausleiheverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Ausleihe;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;

@RestController
@RequestMapping("/api/geraeteverwaltung/lend-overview")
@Transactional
public class AusleiheUebersichtController {

    private final DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung;
    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;

    @PersistenceContext
    private EntityManager em;

    public AusleiheUebersichtController(
            DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung,
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung) {
        this.dbaccessAusleiheverwaltung = dbaccessAusleiheverwaltung;
        this.jwtService = jwtService;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
    }

    @GetMapping("/view-config")
    public ResponseEntity<?> getViewConfig(HttpServletRequest request) {
        AppUser user = authenticatedManagerOrAdmin(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        return ResponseEntity.ok(Map.of(
                "roleContext", Map.of(
                        "activeRole", user.getRole().name(),
                        "module", "geraeteverwalten",
                        "subArea", "ausleiheUebersicht"
                ),
                "layoutHtml", GeraeteverwaltungReservierungsantraegeHtml.content()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getOverview(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean returned,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        String filter = query == null ? "" : query.trim().toLowerCase();

        List<Ausleihe> ausleihen = em.createQuery(
                        "SELECT a FROM Ausleihe a " +
                                "JOIN FETCH a.mitarbeiter " +
                                "JOIN FETCH a.geraet g " +
                                "JOIN FETCH g.geraetetyp " +
                                "ORDER BY a.ausleihdatum DESC",
                        Ausleihe.class)
                .getResultList();

        List<Map<String, Object>> response = ausleihen.stream()
                .filter(a -> {
                    if (filter.isBlank()) {
                        return returned == null || (a.getTatsaechlichesRueckgabedatum() != null) == returned;
                    }

                    String mitarbeiterName = (
                            a.getMitarbeiter().getVorname() + " " + a.getMitarbeiter().getNachname()
                    ).toLowerCase();

                    boolean queryMatch = mitarbeiterName.contains(filter);
                    if (!queryMatch) {
                        return false;
                    }
                    return returned == null || (a.getTatsaechlichesRueckgabedatum() != null) == returned;
                })
                .map(a -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("ausleiheNr", a.getAusleiheNr());
                    map.put("mitarbeiterName", a.getMitarbeiter().getVorname() + " " + a.getMitarbeiter().getNachname());
                    map.put("geraetLabel", "Inventar #" + a.getGeraet().getInventarNr());
                    map.put("ausleihdatum", a.getAusleihdatum());
                    map.put("vereinbartesRueckgabedatum", a.getVereinbartesRueckgabedatum());
                    map.put(
                            "tatsaechlichesRueckgabedatum",
                            a.getTatsaechlichesRueckgabedatum() != null
                                    ? a.getTatsaechlichesRueckgabedatum()
                                    : "Heute bei Bestätigung"
                    );
                    map.put("zurueckgegeben", a.getTatsaechlichesRueckgabedatum() != null);
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{ausleiheNr}/return")
    public ResponseEntity<?> confirmReturn(
            @PathVariable Integer ausleiheNr,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        try {
            dbaccessAusleiheverwaltung.gibGeraetZurueck(ausleiheNr, LocalDate.now());
            return ResponseEntity.ok(Map.of("message", "Rückgabe erfolgreich bestätigt"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private AppUser authenticatedManagerOrAdmin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = jwtService.parseToken(token);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        AppUser user = dbaccessAppUserverwaltung.sucheNachUsername(claims.getSubject());
        if (user == null) {
            return null;
        }

        return user.getRole() == Role.GERAETE_VERWALTER || user.getRole() == Role.ADMIN ? user : null;
    }
}