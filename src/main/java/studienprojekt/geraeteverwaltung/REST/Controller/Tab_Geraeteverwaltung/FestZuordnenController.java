package studienprojekt.geraeteverwaltung.REST.Controller.Tab_Geraeteverwaltung;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/api/geraeteverwaltung/fixed-assignments")
@Transactional
public class FestZuordnenController {

    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;
    private final DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung;
    private final DBaccess_Raumverwaltung dbaccessRaumverwaltung;
    private final DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung;


    @PersistenceContext
    private EntityManager em;

    public FestZuordnenController(
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung,
            DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung,
            DBaccess_Raumverwaltung dbaccessRaumverwaltung,
            DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung) {
        this.jwtService = jwtService;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
        this.dbaccessMitarbeiterverwaltung = dbaccessMitarbeiterverwaltung;
        this.dbaccessRaumverwaltung = dbaccessRaumverwaltung;
        this.dbaccessGeraeteverwaltung = dbaccessGeraeteverwaltung;
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
                        "subArea", "festZuordnen"
                ),
                "layoutHtml", GeraeteverwaltungReservierungsantraegeHtml.content()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getAssignments(
            @RequestParam(required = false) String query,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        String filter = query == null ? "" : query.trim().toLowerCase();

        List<Geraet> geraete = em.createQuery(
                        "SELECT g FROM Geraet g " +
                                "LEFT JOIN FETCH g.staendigerNutzer " +
                                "LEFT JOIN FETCH g.standort " +
                                "WHERE g.staendigerNutzer IS NOT NULL OR g.standort IS NOT NULL " +
                                "ORDER BY g.inventarNr",
                        Geraet.class)
                .getResultList();

        List<Map<String, Object>> response = geraete.stream()
                .filter(g -> {
                    if (filter.isBlank()) {
                        return true;
                    }

                    String mitarbeiter = g.getStaendigerNutzer() != null
                            ? (g.getStaendigerNutzer().getVorname() + " " + g.getStaendigerNutzer().getNachname()).toLowerCase()
                            : "";

                    String raum = g.getStandort() != null
                            ? String.valueOf(g.getStandort().getRaumNr()).toLowerCase()
                            : "";

                    String inventar = String.valueOf(g.getInventarNr()).toLowerCase();

                    return inventar.contains(filter) || mitarbeiter.contains(filter) || raum.contains(filter);
                })
                .map(g -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("inventarNr", g.getInventarNr());
                    map.put(
                            "mitarbeiterLabel",
                            g.getStaendigerNutzer() != null
                                    ? g.getStaendigerNutzer().getVorname() + " " + g.getStaendigerNutzer().getNachname()
                                    : null
                    );
                    map.put(
                            "raumLabel",
                            g.getStandort() != null
                                    ? "Raum " + g.getStandort().getRaumNr()
                                    : null
                    );
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
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
                                "raumNr", raum.getRaumNr()
                        ))
                        .toList()
        );
    }

    @GetMapping("/{inventarNr}")
    public ResponseEntity<?> getAssignment(
            @PathVariable Integer inventarNr,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        Geraet geraet = em.find(Geraet.class, inventarNr);
        if (geraet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Gerät nicht gefunden"));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("inventarNr", geraet.getInventarNr());
        response.put(
                "mitarbeiterPersonalNr",
                geraet.getStaendigerNutzer() != null ? geraet.getStaendigerNutzer().getPersonalNr() : null
        );
        response.put(
                "mitarbeiterLabel",
                geraet.getStaendigerNutzer() != null
                        ? geraet.getStaendigerNutzer().getVorname() + " " + geraet.getStaendigerNutzer().getNachname()
                        : null
        );
        response.put(
                "raumNr",
                geraet.getStandort() != null ? geraet.getStandort().getRaumNr() : null
        );
        response.put(
                "raumLabel",
                geraet.getStandort() != null ? "Raum " + geraet.getStandort().getRaumNr() : null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> saveAssignmentBySelection(
            @RequestBody SaveFixedAssignmentRequest requestBody,
            HttpServletRequest request) {

        return saveAssignmentInternal(null, requestBody, request);
    }


    @PutMapping("/{inventarNr}")
    public ResponseEntity<?> saveAssignment(
            @PathVariable Integer inventarNr,
            @RequestBody SaveFixedAssignmentRequest requestBody,
            HttpServletRequest request) {

        return saveAssignmentInternal(inventarNr, requestBody, request);
    }

    private ResponseEntity<?> saveAssignmentInternal(
            Integer inventarNr,
            SaveFixedAssignmentRequest requestBody,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }
        if (requestBody == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Anfrage ist leer"));
        }

        Geraet geraet;
        if (inventarNr != null || requestBody.inventarNr() != null) {
            Integer zielInventarNr = inventarNr != null ? inventarNr : requestBody.inventarNr();
            geraet = em.find(Geraet.class, zielInventarNr);
            if (geraet == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Gerät nicht gefunden"));
            }
        } else if (requestBody.geraetetypId() != null) {
            geraet = findeFreiesGeraetZumTyp(requestBody.geraetetypId());
            if (geraet == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "error",
                        "Für diesen Gerätetyp ist aktuell kein freies Gerät verfügbar"
                ));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Gerät oder Gerätetyp ist erforderlich"));
        }

        Mitarbeiter mitarbeiter = null;
        if (requestBody.mitarbeiterPersonalNr() != null) {
            mitarbeiter = dbaccessMitarbeiterverwaltung.sucheMitarbeiter(requestBody.mitarbeiterPersonalNr());
            if (mitarbeiter == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mitarbeiter nicht gefunden"));
            }
        }

        Raum raum = null;
        if (requestBody.raumNr() != null) {
            raum = dbaccessRaumverwaltung.sucheRaum(requestBody.raumNr());
            if (raum == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Raum nicht gefunden"));
            }
        }

        boolean sollFestZuordnen = mitarbeiter != null || raum != null;
        if (sollFestZuordnen && !istFuerFesteZuordnungVerfuegbar(geraet.getInventarNr())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error",
                    "Gerät ist aktuell ausgeliehen oder reserviert und kann deshalb nicht fest zugeordnet werden"
            ));
        }

        geraet.setStaendigerNutzer(mitarbeiter);
        geraet.setStandort(raum);
        geraet.aktualisiereStatusNachZuweisung();

        return ResponseEntity.ok(Map.of(
                "message", "Feste Zuordnung gespeichert",
                "inventarNr", geraet.getInventarNr()
        ));
    }

    @DeleteMapping("/{inventarNr}")
    public ResponseEntity<?> clearAssignment(
            @PathVariable Integer inventarNr,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        Geraet geraet = em.find(Geraet.class, inventarNr);
        if (geraet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Gerät nicht gefunden"));
        }

        geraet.setStaendigerNutzer(null);
        geraet.setStandort(null);
        geraet.macheNachAufhebenFesterZuordnungVerfuegbar();

        return ResponseEntity.ok(Map.of("message", "Feste Zuordnung aufgehoben"));
    }

    private Geraet findeFreiesGeraetZumTyp(Long geraetetypId) {
        List<Geraet> geraete = em.createQuery(
                        "SELECT g FROM Geraet g " +
                                "WHERE g.geraetetyp.id = :geraetetypId " +
                                "AND g.staendigerNutzer IS NULL " +
                                "AND g.standort IS NULL " +
                                "AND g.istAusleihbar = true " +
                                "ORDER BY g.inventarNr",
                        Geraet.class)
                .setParameter("geraetetypId", geraetetypId)
                .getResultList();

        return geraete.stream()
                .filter(g -> istFuerFesteZuordnungVerfuegbar(g.getInventarNr()))
                .findFirst()
                .orElse(null);
    }

    private boolean istFuerFesteZuordnungVerfuegbar(Integer inventarNr) {
        Long aktiveAusleihen = em.createQuery(
                        "SELECT COUNT(a) FROM Ausleihe a WHERE a.geraet.inventarNr = :inventarNr " +
                                "AND a.tatsaechlichesRueckgabedatum IS NULL " +
                                "AND a.ausleihdatum <= CURRENT_DATE " +
                                "AND a.vereinbartesRueckgabedatum >= CURRENT_DATE",
                        Long.class)
                .setParameter("inventarNr", inventarNr)
                .getSingleResult();
        if (aktiveAusleihen > 0) {
            return false;
        }

        Long aktiveReservierungen = em.createQuery(
                        "SELECT COUNT(r) FROM Reservierung r " +
                                "WHERE r.reserviertesGeraet.inventarNr = :inventarNr " +
                                "AND r.rueckgabedatum >= CURRENT_DATE " +
                                "AND NOT EXISTS (SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r)",
                        Long.class)
                .setParameter("inventarNr", inventarNr)
                .getSingleResult();

        return aktiveReservierungen == 0;
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

    public record SaveFixedAssignmentRequest(
            Integer inventarNr,
            Long geraetetypId,
            Integer mitarbeiterPersonalNr,
            Integer raumNr
    ) {
    }
}
