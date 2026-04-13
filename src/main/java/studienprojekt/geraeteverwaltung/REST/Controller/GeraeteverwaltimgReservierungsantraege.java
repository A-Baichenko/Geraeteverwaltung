package studienprojekt.geraeteverwaltung.REST.Controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Ausleiheverwaltung;
import studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.GeraeteverwaltungReservierungsantraegeHtml;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Reservierungsverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Ausleihe;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Reservierung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;

@RestController
@RequestMapping("/api/geraeteverwaltung/reservation-requests")
public class GeraeteverwaltimgReservierungsantraege {

    private final DBaccess_Reservierungsverwaltung dbaccessReservierungsverwaltung;
    private final DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung;
    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;

    public GeraeteverwaltimgReservierungsantraege(
            DBaccess_Reservierungsverwaltung dbaccessReservierungsverwaltung,
            DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung,
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung) {
        this.dbaccessReservierungsverwaltung = dbaccessReservierungsverwaltung;
        this.dbaccessAusleiheverwaltung = dbaccessAusleiheverwaltung;
        this.jwtService = jwtService;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
    }

    @GetMapping("/view-config")
    public ResponseEntity<?> getViewConfig(HttpServletRequest request) {
        AppUser user = authenticatedManagerOrAdmin(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        return ResponseEntity.ok(Map.of(
                "roleContext", Map.of(
                        "activeRole", user.getRole().name(),
                        "module", "geraeteverwalten",
                        "subArea", "reservierungsantraege"
                ),
                "sections", List.of(
                        Map.of("id", "requestList", "title", "Reservierungsanträge"),
                        Map.of("id", "requestForm", "title", "Antrag öffnen"),
                        Map.of("id", "deviceSelection", "title", "Verfügbare Geräte wählen")
                ),
                "fieldConfig", List.of(
                        Map.of("key", "mitarbeiterName", "label", "Mitarbeiter", "readonly", true),
                        Map.of("key", "geraetetypName", "label", "Gerätetyp", "readonly", true),
                        Map.of("key", "ausleihdatum", "label", "Ausleihdatum", "readonly", false),
                        Map.of("key", "rueckgabedatum", "label", "Rückgabedatum", "readonly", false),
                        Map.of("key", "selectedDeviceLabel", "label", "Ausgewähltes Gerät", "readonly", true)
                ),
                "actions", List.of(
                        Map.of("key", "edit", "label", "Bearbeiten"),
                        Map.of("key", "delete", "label", "Löschen"),
                        Map.of("key", "accept", "label", "Annehmen")
                ),
                "layoutHtml", GeraeteverwaltungReservierungsantraegeHtml.content()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getPendingReservationRequests(HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        List<Reservierung> reservierungen = dbaccessReservierungsverwaltung.findeOffeneReservierungen();
        return ResponseEntity.ok(reservierungen.stream().map(this::toReservationResponse).toList());
    }

    @GetMapping("/{reservierungsNr}")
    public ResponseEntity<?> getReservationRequest(@PathVariable Integer reservierungsNr, HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        Reservierung reservierung = dbaccessReservierungsverwaltung.sucheReservierung(reservierungsNr);
        if (reservierung == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservierung nicht gefunden"));
        }

        return ResponseEntity.ok(toReservationResponse(reservierung));
    }

    @GetMapping("/{reservierungsNr}/available-devices")
    public ResponseEntity<?> getAvailableDevices(@PathVariable Integer reservierungsNr, HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        Reservierung reservierung = dbaccessReservierungsverwaltung.sucheReservierung(reservierungsNr);
        if (reservierung == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservierung nicht gefunden"));
        }

        List<Geraet> verfuegbareGeraete = dbaccessReservierungsverwaltung.findeVerfuegbareGeraeteFuerReservierung(reservierung);

        return ResponseEntity.ok(Map.of(
                "reservationId", reservierung.getReservierungsNr(),
                "deviceTypeId", reservierung.getGeraetetyp().getId(),
                "deviceTypeName", reservierung.getGeraetetyp().getHersteller() + " " + reservierung.getGeraetetyp().getBezeichnung(),
                "availableCount", verfuegbareGeraete.size(),
                "devices", verfuegbareGeraete.stream()
                        .map(geraet -> Map.of(
                                "inventarNr", geraet.getInventarNr(),
                                "serienNr", geraet.getSerienNr(),
                                "label", "Inventar #" + geraet.getInventarNr() + " • Serie " + geraet.getSerienNr()
                        ))
                        .toList()
        ));
    }

    @PostMapping("/{reservierungsNr}/accept")
    public ResponseEntity<?> acceptReservation(
            @PathVariable Integer reservierungsNr,
            @RequestBody AcceptReservationRequest acceptRequest,
            HttpServletRequest request) {

        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        try {
            Ausleihe ausleihe = dbaccessAusleiheverwaltung.nehmeReservierungAn(
                    reservierungsNr,
                    acceptRequest.inventarNr(),
                    null
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Reservierungsantrag wurde angenommen",
                    "ausleiheNr", ausleihe.getAusleiheNr(),
                    "geraetInventarNr", ausleihe.getGeraet().getInventarNr()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{reservierungsNr}")
    public ResponseEntity<?> deleteReservationRequest(@PathVariable Integer reservierungsNr, HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        boolean geloescht = dbaccessReservierungsverwaltung.loescheFremdeReservierung(reservierungsNr);
        if (!geloescht) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservierung nicht gefunden"));
        }

        return ResponseEntity.ok(Map.of("message", "Reservierungsantrag gelöscht"));
    }

    private Map<String, Object> toReservationResponse(Reservierung reservierung) {
        int freieGeraete = dbaccessReservierungsverwaltung.findeVerfuegbareGeraeteFuerReservierung(reservierung).size();
        return Map.of(
                "reservierungsNr", reservierung.getReservierungsNr(),
                "mitarbeiterName", reservierung.getMitarbeiter().getVorname() + " " + reservierung.getMitarbeiter().getNachname(),
                "mitarbeiterPersonalNr", reservierung.getMitarbeiter().getPersonalNr(),
                "geraetetypId", reservierung.getGeraetetyp().getId(),
                "geraetetypName", reservierung.getGeraetetyp().getHersteller() + " " + reservierung.getGeraetetyp().getBezeichnung(),
                "ausleihdatum", reservierung.getAusleihdatum(),
                "rueckgabedatum", reservierung.getRueckgabedatum(),
                "availableCount", freieGeraete
        );
    }

    @PutMapping("/{reservierungsNr}")
    public ResponseEntity<?> updateReservationRequest(
            @PathVariable Integer reservierungsNr,
            @RequestBody UpdateReservationRequest updateRequest,
            HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
        }

        try {
            Reservierung reservierung = dbaccessReservierungsverwaltung.bearbeiteFremdeReservierung(
                    reservierungsNr,
                    updateRequest.ausleihdatum(),
                    updateRequest.rueckgabedatum()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Reservierungsantrag wurde aktualisiert",
                    "reservierungsNr", reservierung.getReservierungsNr()
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

    public record AcceptReservationRequest(Integer inventarNr) {
    }

    public record UpdateReservationRequest(LocalDate ausleihdatum, LocalDate rueckgabedatum) {
    }
}
