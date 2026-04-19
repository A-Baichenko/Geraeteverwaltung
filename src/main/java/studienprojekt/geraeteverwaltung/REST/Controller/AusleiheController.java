package studienprojekt.geraeteverwaltung.REST.Controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.GeraeteverwaltungReservierungsantraegeHtml;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Ausleiheverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Geraeteverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Ausleihe;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_Mitarbeiterverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;

@RestController
@RequestMapping("/api/geraeteverwaltung/lend")
public class AusleiheController {

    private final DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung;
    private final DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung;
    private final DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung;
    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;

    public AusleiheController(
            DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung,
            DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung,
            DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung,
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung) {
        this.dbaccessAusleiheverwaltung = dbaccessAusleiheverwaltung;
        this.dbaccessGeraeteverwaltung = dbaccessGeraeteverwaltung;
        this.dbaccessMitarbeiterverwaltung = dbaccessMitarbeiterverwaltung;
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
                        "subArea", "ausleihe"
                ),
                "layoutHtml", GeraeteverwaltungReservierungsantraegeHtml.content()
        ));
    }

    @GetMapping("/device-types")
    public ResponseEntity<?> searchDeviceTypes(
            @RequestParam(required = false) String query,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        List<Geraetetyp> results = dbaccessGeraeteverwaltung.findeGeraetetypenNachFilter(query);

        return ResponseEntity.ok(
                results.stream()
                        .map(typ -> Map.of(
                                "id", typ.getId(),
                                "label", typ.getHersteller() + " " + typ.getBezeichnung(),
                                "hersteller", typ.getHersteller(),
                                "bezeichnung", typ.getBezeichnung()
                        ))
                        .toList()
        );
    }

    @GetMapping("/employees")
    public ResponseEntity<?> searchEmployees(
            @RequestParam(required = false) String query,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        List<Mitarbeiter> results = dbaccessMitarbeiterverwaltung.findeMitarbeiterNachFilter(query);

        return ResponseEntity.ok(
                results.stream()
                        .map(mitarbeiter -> Map.of(
                                "id", mitarbeiter.getPersonalNr(),
                                "label", mitarbeiter.getAnrede().name() + " " + mitarbeiter.getVorname() + " " + mitarbeiter.getNachname(),
                                "personalNr", mitarbeiter.getPersonalNr()
                        ))
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<?> createLend(
            @RequestBody CreateLendRequest requestBody,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        try {
            if (requestBody.geraetetypId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Gerät ist erforderlich"));
            }
            if (requestBody.personalNr() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mitarbeiter ist erforderlich"));
            }
            if (requestBody.ausleihdatum() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ausleihdatum ist erforderlich"));
            }
            if (requestBody.rueckgabedatum() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rückgabedatum ist erforderlich"));
            }

            Ausleihe ausleihe = dbaccessAusleiheverwaltung.leiheGeraetAus(
                    requestBody.geraetetypId(),
                    requestBody.personalNr(),
                    requestBody.ausleihdatum(),
                    requestBody.rueckgabedatum()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Ausleihe wurde erstellt",
                    "ausleiheNr", ausleihe.getAusleiheNr(),
                    "inventarNr", ausleihe.getGeraet().getInventarNr()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
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

    public record CreateLendRequest(
            Long geraetetypId,
            Integer personalNr,
            LocalDate ausleihdatum,
            LocalDate rueckgabedatum
    ) {
    }
}