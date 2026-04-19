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
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Geraeteverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_Mitarbeiterverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.DBaccess_Raumverwaltung;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

@RestController
@RequestMapping("/api/geraeteverwaltung/create-devices")
public class GeraeteanlegenController {

    private final DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung;
    private final DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung;
    private final DBaccess_Raumverwaltung dbaccessRaumverwaltung;
    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;

    public GeraeteanlegenController(
            DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung,
            DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung,
            DBaccess_Raumverwaltung dbaccessRaumverwaltung,
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung) {
        this.dbaccessGeraeteverwaltung = dbaccessGeraeteverwaltung;
        this.dbaccessMitarbeiterverwaltung = dbaccessMitarbeiterverwaltung;
        this.dbaccessRaumverwaltung = dbaccessRaumverwaltung;
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
                        "subArea", "geraeteanlegen"
                ),
                "sections", List.of(
                        Map.of("id", "createDevices", "title", "Geräte anlegen"),
                        Map.of("id", "searchPanel", "title", "Suche")
                ),
                "fieldConfig", List.of(
                        Map.of("key", "inventarNr", "label", "Inventar-Nr.", "readonly", false),
                        Map.of("key", "serienNr", "label", "Serien-Nr.", "readonly", false),
                        Map.of("key", "kaufdatum", "label", "Kaufdatum", "readonly", false),
                        Map.of("key", "geraetetyp", "label", "Gerätetyp", "readonly", true),
                        Map.of("key", "ausleihbar", "label", "Ausleihbar", "readonly", false),
                        Map.of("key", "mitarbeiter", "label", "Mitarbeiter", "readonly", true),
                        Map.of("key", "raum", "label", "Raum", "readonly", true)
                ),
                "actions", List.of(
                        Map.of("key", "save", "label", "Speichern"),
                        Map.of("key", "reset", "label", "Zurücksetzen"),
                        Map.of("key", "searchDeviceType", "label", "Gerätetyp suchen"),
                        Map.of("key", "searchEmployee", "label", "Mitarbeiter suchen"),
                        Map.of("key", "searchRoom", "label", "Raum suchen")
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
                                "bezeichnung", typ.getBezeichnung(),
                                "kategorie", typ.getKategorie() != null ? typ.getKategorie().getBezeichnung() : ""
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

        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        Mitarbeiter mitarbeiter = null;

        try {
            Integer personalNr = Integer.valueOf(query);
            mitarbeiter = dbaccessMitarbeiterverwaltung.sucheMitarbeiter(personalNr);
        } catch (NumberFormatException ex) {
            try {
                mitarbeiter = dbaccessMitarbeiterverwaltung.findeNachName(query);
            } catch (IllegalArgumentException ex2) {
                mitarbeiter = null;
            }
        }

        if (mitarbeiter == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(List.of(
                Map.of(
                        "id", mitarbeiter.getPersonalNr(),
                        "label", mitarbeiter.getVorname() + " " + mitarbeiter.getNachname(),
                        "personalNr", mitarbeiter.getPersonalNr(),
                        "vorname", mitarbeiter.getVorname(),
                        "nachname", mitarbeiter.getNachname()
                )
        ));
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> searchRooms(
            @RequestParam(required = false) String query,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        List<Raum> results = dbaccessRaumverwaltung.findeRaeumeNachFilter(query);

        return ResponseEntity.ok(
                results.stream()
                        .map(raum -> Map.of(
                                "id", raum.getRaumNr(),
                                "label", "Raum " + raum.getRaumNr() + " - " + raum.getGebaeude(),
                                "raumNr", raum.getRaumNr(),
                                "gebaeude", raum.getGebaeude()
                        ))
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<?> createDevice(
            @RequestBody CreateDeviceRequest createRequest,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        try {
            if (createRequest.inventarNr() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Inventar-Nr. ist erforderlich"));
            }
            if (createRequest.serienNr() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Serien-Nr. ist erforderlich"));
            }
            if (createRequest.kaufdatum() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Kaufdatum ist erforderlich"));
            }
            if (createRequest.geraetetypId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Gerätetyp ist erforderlich"));
            }

            Geraetetyp geraetetyp = dbaccessGeraeteverwaltung.sucheGeraetetypById(createRequest.geraetetypId());
            if (geraetetyp == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Gerätetyp nicht gefunden"));
            }

            Mitarbeiter mitarbeiter = null;
            if (createRequest.mitarbeiterPersonalNr() != null) {
                mitarbeiter = dbaccessMitarbeiterverwaltung.sucheMitarbeiter(createRequest.mitarbeiterPersonalNr());
                if (mitarbeiter == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Mitarbeiter nicht gefunden"));
                }
            }

            Raum raum = null;
            if (createRequest.raumNr() != null) {
                raum = dbaccessRaumverwaltung.sucheRaum(createRequest.raumNr());
                if (raum == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Raum nicht gefunden"));
                }
            }

            boolean ausleihbar = Boolean.TRUE.equals(createRequest.ausleihbar());

            Geraet neuesGeraet = new Geraet(
                    createRequest.inventarNr(),
                    createRequest.serienNr(),
                    createRequest.kaufdatum(),
                    ausleihbar,
                    geraetetyp
            );

            if (mitarbeiter != null) {
                neuesGeraet.setStaendigerNutzer(mitarbeiter);
            }

            if (raum != null) {
                neuesGeraet.setStandort(raum);
            }

            Geraet gespeichert = dbaccessGeraeteverwaltung.legeGeraetAn(neuesGeraet);

            return ResponseEntity.ok(Map.of(
                    "message", "Gerät wurde angelegt",
                    "inventarNr", gespeichert.getInventarNr(),
                    "serienNr", gespeichert.getSerienNr(),
                    "geraetetyp", gespeichert.getGeraetetyp().getHersteller() + " " + gespeichert.getGeraetetyp().getBezeichnung()
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

    public record CreateDeviceRequest(
            Integer inventarNr,
            Integer serienNr,
            LocalDate kaufdatum,
            Long geraetetypId,
            Boolean ausleihbar,
            Integer mitarbeiterPersonalNr,
            Integer raumNr
    ) {
    }
}