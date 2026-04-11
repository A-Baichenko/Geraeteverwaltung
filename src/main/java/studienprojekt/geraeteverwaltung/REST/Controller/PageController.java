package studienprojekt.geraeteverwaltung.REST.Controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.*;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;

@RestController
@RequestMapping("/api/page")
public class PageController {

    private static final List<String> TAB_ORDER = List.of(
            "home",
            "reservierung",
            "geraeteverwaltung",
            "mitarbeiterverwaltung",
            "raumverwaltung",
            "placeholder"
    );

    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;
    private final JwtService jwtService;

    public PageController(DBaccess_AppUserverwaltung dbaccessAppUserverwaltung, JwtService jwtService) {
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
        this.jwtService = jwtService;
    }

    @GetMapping("/tab-config")
    public ResponseEntity<?> getTabConfiguration(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nicht autorisiert"));
        }

        List<String> allowedTabs = TAB_ORDER.stream()
                .filter(tab -> isTabAllowedForRole(tab, user.getRole()))
                .toList();

        return ResponseEntity.ok(Map.of(
                "role", user.getRole().name(),
                "tabOrder", TAB_ORDER,
                "allowedTabs", allowedTabs
        ));
    }

    @GetMapping("/content/{tabKey}")
    public ResponseEntity<?> getTabContent(
            @PathVariable String tabKey,
            @RequestParam(defaultValue = "menu") String view,
            @RequestParam(defaultValue = "form") String sub,
            HttpServletRequest request) {

        AppUser user = authenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nicht autorisiert"));
        }

        if (!TAB_ORDER.contains(tabKey) || "placeholder".equals(tabKey)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Tab nicht gefunden"));
        }

        if (!isTabAllowedForRole(tabKey, user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Keine Berechtigung für diesen Tab"));
        }

        return ResponseEntity.ok(Map.of("html", htmlForTab(tabKey, view, sub)));
    }

    private AppUser authenticatedUser(HttpServletRequest request) {
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
        return dbaccessAppUserverwaltung.sucheNachUsername(claims.getSubject());
    }

    private boolean isTabAllowedForRole(String tabKey, Role role) {
        return switch (role) {
            case ADMIN -> true;
            case GERAETE_VERWALTER -> EnumSet.of(
                    Tab.HOME,
                    Tab.RESERVIERUNG,
                    Tab.GERAETEVERWALTUNG
            ).contains(Tab.fromKey(tabKey));
            case RAUM_VERWALTER -> Tab.RAUMVERWALTUNG.matches(tabKey);
            case PERSONEN_VERWALTER -> Tab.MITARBEITERVERWALTUNG.matches(tabKey);
            case MITARBEITER -> EnumSet.of(
                    Tab.HOME,
                    Tab.RESERVIERUNG
            ).contains(Tab.fromKey(tabKey));
        };
    }

    private String htmlForTab(String tabKey, String view, String sub) {
        return switch (Tab.fromKey(tabKey)) {
            case HOME -> HomeHtml.content();
            case RESERVIERUNG -> ReservierungHtml.content();
            case GERAETEVERWALTUNG -> GeraeteverwaltungHtml.content();
            case MITARBEITERVERWALTUNG -> MitarbeiterverwaltungHtml.content();
            case RAUMVERWALTUNG -> RaumverwaltungHtml.content();
            case PLACEHOLDER -> "";
        };
    }

    private enum Tab {
        HOME("home"),
        RESERVIERUNG("reservierung"),
        GERAETEVERWALTUNG("geraeteverwaltung"),
        MITARBEITERVERWALTUNG("mitarbeiterverwaltung"),
        RAUMVERWALTUNG("raumverwaltung"),
        PLACEHOLDER("placeholder");

        private final String key;

        Tab(String key) {
            this.key = key;
        }

        boolean matches(String candidate) {
            return key.equals(candidate);
        }

        static Tab fromKey(String tabKey) {
            for (Tab value : values()) {
                if (value.matches(tabKey)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unbekannter Tab");
        }
    }
}