package studienprojekt.geraeteverwaltung.REST.Controller.Reservieren;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Geraeteverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Reservierungsverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Reservierung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;

@RestController
@RequestMapping("/api/reservierung")
public class ReservierungController {

    private final DBaccess_Reservierungsverwaltung dbaccessReservierungsverwaltung;
    private final DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung;
    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;

    public ReservierungController(
            DBaccess_Reservierungsverwaltung dbaccessReservierungsverwaltung,
            DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung,
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung) {
        this.dbaccessReservierungsverwaltung = dbaccessReservierungsverwaltung;
        this.dbaccessGeraeteverwaltung = dbaccessGeraeteverwaltung;
        this.jwtService = jwtService;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
    }

    @GetMapping("/geraetetypen")
    public ResponseEntity<?> sucheGeraetetypen(@RequestParam(required = false) String suchbegriff) {
        try {
            var geraete = dbaccessGeraeteverwaltung.findeGeraetetypenNachFilter(suchbegriff);

            return ResponseEntity.ok(
                    geraete.stream()
                            .map(geraet -> Map.of(
                                    "id", geraet.getId(),
                                    "hersteller", geraet.getHersteller(),
                                    "bezeichnung", geraet.getBezeichnung(),
                                    "kategorie", geraet.getKategorie().getBezeichnung(),
                                    "anzeigeName", geraet.getHersteller() + " " + geraet.getBezeichnung()
                            ))
                            .toList()
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/reservierungen/me")
    public ResponseEntity<?> eigeneReservierungen(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nicht autorisiert"));
        }

        var reservierungen = dbaccessReservierungsverwaltung.findeReservierungsstatusFuerMitarbeiter(user.getMitarbeiter().getPersonalNr());
        return ResponseEntity.ok(
                reservierungen.stream()
                        .map(eintrag -> {
                            Reservierung reservierung = eintrag.reservierung();
                            Map<String, Object> response = new LinkedHashMap<>();
                            response.put("reservierungsNr", reservierung.getReservierungsNr());
                            response.put("ausleihdatum", reservierung.getAusleihdatum());
                            response.put("rueckgabedatum", reservierung.getRueckgabedatum());
                            response.put("geraetetypId", reservierung.getGeraetetyp().getId());
                            response.put("geraetetypName", reservierung.getGeraetetyp().getHersteller() + " " + reservierung.getGeraetetyp().getBezeichnung());
                            response.put("status", eintrag.istAktiv() ? "AKTIV" : "ABGESCHLOSSEN");
                            response.put("tatsaechlichesRueckgabedatum", eintrag.tatsaechlichesRueckgabedatum());
                            return response;
                        })
                        .toList()
        );
    }

    @GetMapping("/device-types/{geraetetypId}/unavailable-periods")
    public ResponseEntity<?> nichtVerfuegbareZeitraeume(
            @PathVariable Long geraetetypId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) Integer excludeReservierungId) {
        try {
            LocalDate startDatum = LocalDate.parse(start);
            LocalDate endDatum = LocalDate.parse(end);

            var zeitraeume = dbaccessReservierungsverwaltung.findeNichtVerfuegbareZeitraeume(
                    geraetetypId,
                    startDatum,
                    endDatum,
                    excludeReservierungId
            );

            return ResponseEntity.ok(zeitraeume.stream()
                    .map(zeitraum -> Map.of(
                            "start", zeitraum.start(),
                            "end", zeitraum.ende()
                    ))
                    .toList());
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ungültiges Datumsformat"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/reservierungen")
    public ResponseEntity<?> reservieren(@RequestBody ReservierungRequest request, HttpServletRequest httpRequest) {
        AppUser user = authenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nicht autorisiert"));
        }

        if (!user.getMitarbeiter().getPersonalNr().equals(request.personalNr())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Reservierungen nur für den eigenen Benutzer erlaubt"));
        }

        try {
            Reservierung reservierung = dbaccessReservierungsverwaltung.reserviereGeraet(
                    request.geraetetypId(),
                    request.personalNr(),
                    request.ausleihdatum(),
                    request.rueckgabedatum()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Reservierung erfolgreich erstellt",
                    "reservierungsNr", reservierung.getReservierungsNr()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Für diesen Gerätetyp ist im gewählten Zeitraum kein freies Gerät verfügbar."
            ));
        }
    }

    @PutMapping("/reservierungen/{reservierungsNr}")
    public ResponseEntity<?> updateReservierung(
            @PathVariable Integer reservierungsNr,
            @RequestBody ReservierungUpdateRequest request,
            HttpServletRequest httpRequest) {
        AppUser user = authenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nicht autorisiert"));
        }

        try {
            Reservierung reservierung = dbaccessReservierungsverwaltung.bearbeiteEigeneReservierung(
                    reservierungsNr,
                    user.getMitarbeiter().getPersonalNr(),
                    request.ausleihdatum(),
                    request.rueckgabedatum()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Reservierung erfolgreich aktualisiert",
                    "reservierungsNr", reservierung.getReservierungsNr()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Für diesen Gerätetyp ist im gewählten Zeitraum kein freies Gerät verfügbar."
            ));
        }
    }

    @DeleteMapping("/reservierungen/{reservierungsNr}")
    public ResponseEntity<?> deleteReservierung(@PathVariable Integer reservierungsNr, HttpServletRequest httpRequest) {
        AppUser user = authenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nicht autorisiert"));
        }

        boolean geloescht = dbaccessReservierungsverwaltung.loescheEigeneReservierung(
                reservierungsNr,
                user.getMitarbeiter().getPersonalNr()
        );

        if (!geloescht) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservierung nicht gefunden"));
        }

        return ResponseEntity.ok(Map.of("message", "Reservierung gelöscht"));
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

    public record ReservierungRequest(
            Long geraetetypId,
            Integer personalNr,
            LocalDate ausleihdatum,
            LocalDate rueckgabedatum
    ) {
    }

    public record ReservierungUpdateRequest(
            LocalDate ausleihdatum,
            LocalDate rueckgabedatum
    ) {
    }
}