package studienprojekt.geraeteverwaltung.REST.Controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Ausleiheverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Geraeteverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Reservierungsverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Ausleihe;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Reservierung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_Mitarbeiterverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.DBaccess_Raumverwaltung;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

@RestController
@RequestMapping("/api/reservierung-ausleihe")
public class ReservierungAusleiheController {

    private final DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung;
    private final DBaccess_Reservierungsverwaltung dbaccessReservierungsverwaltung;
    private final DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung;
    private final DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung;
    private final DBaccess_Raumverwaltung dbaccessRaumverwaltung;
    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;

    public ReservierungAusleiheController(
            DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung,
            DBaccess_Reservierungsverwaltung dbaccessReservierungsverwaltung,
            DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung,
            DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung,
            DBaccess_Raumverwaltung dbaccessRaumverwaltung,
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung) {
        this.dbaccessAusleiheverwaltung = dbaccessAusleiheverwaltung;
        this.dbaccessReservierungsverwaltung = dbaccessReservierungsverwaltung;
        this.dbaccessGeraeteverwaltung = dbaccessGeraeteverwaltung;
        this.dbaccessMitarbeiterverwaltung = dbaccessMitarbeiterverwaltung;
        this.dbaccessRaumverwaltung = dbaccessRaumverwaltung;
        this.jwtService = jwtService;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
    }

    @PostMapping("/ausleihen")
    public ResponseEntity<?> ausleihen(@RequestBody AusleiheRequest request) {
        try {
            Ausleihe ausleihe = dbaccessAusleiheverwaltung.leiheGeraetAus(
                    request.geraetetypId(),
                    request.personalNr(),
                    request.ausleihdatum(),
                    request.rueckgabedatum()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Ausleihe erfolgreich erstellt",
                    "ausleiheNr", ausleihe.getAusleiheNr(),
                    "ausleihdatum", ausleihe.getAusleihdatum(),
                    "vereinbartesRueckgabedatum", ausleihe.getVereinbartesRueckgabedatum(),
                    "inventarNr", ausleihe.getGeraet().getInventarNr(),
                    "geraetetypId", ausleihe.getGeraet().getGeraetetyp().getId(),
                    "personalNr", ausleihe.getMitarbeiter().getPersonalNr()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @PostMapping("/reservieren")
    public ResponseEntity<?> reservieren(@RequestBody ReservierungRequest request) {
        try {
            Reservierung reservierung = dbaccessReservierungsverwaltung.reserviereGeraet(
                    request.geraetetypId(),
                    request.personalNr(),
                    request.ausleihdatum(),
                    request.rueckgabedatum()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Reservierung erfolgreich erstellt",
                    "reservierungsNr", reservierung.getReservierungsNr(),
                    "ausleihdatum", reservierung.getAusleihdatum(),
                    "rueckgabedatum", reservierung.getRueckgabedatum(),
                    "geraetetypId", reservierung.getGeraetetyp().getId(),
                    "personalNr", reservierung.getMitarbeiter().getPersonalNr()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @PostMapping("/rueckgabe")
    public ResponseEntity<?> rueckgabe(@RequestBody RueckgabeRequest request) {
        try {
            Ausleihe ausleihe = dbaccessAusleiheverwaltung.gibGeraetZurueck(
                    request.ausleiheNr(),
                    request.rueckgabeDatum()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Gerät erfolgreich zurückgegeben",
                    "ausleiheNr", ausleihe.getAusleiheNr(),
                    "rueckgabeDatum", ausleihe.getTatsaechlichesRueckgabedatum()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @GetMapping("/current-mitarbeiter")
    public ResponseEntity<?> currentMitarbeiter(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Nicht autorisiert"
            ));
        }

        Mitarbeiter mitarbeiter = null;

        try {
            mitarbeiter = dbaccessMitarbeiterverwaltung.findeNachName(user.getUsername());
        } catch (IllegalArgumentException ignored) {
        }

        if (mitarbeiter == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Kein Mitarbeiter zum eingeloggten Benutzer gefunden"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "personalNr", mitarbeiter.getPersonalNr(),
                "vorname", mitarbeiter.getVorname(),
                "nachname", mitarbeiter.getNachname(),
                "anzeigeName", mitarbeiter.getVorname() + " " + mitarbeiter.getNachname()
        ));
    }

    @GetMapping("/mitarbeiter")
    public ResponseEntity<?> sucheMitarbeiter(@RequestParam String suchbegriff) {
        try {
            Mitarbeiter mitarbeiter = null;

            try {
                Integer personalNr = Integer.valueOf(suchbegriff);
                mitarbeiter = dbaccessMitarbeiterverwaltung.sucheMitarbeiter(personalNr);
            } catch (NumberFormatException ignored) {
            }

            if (mitarbeiter == null) {
                mitarbeiter = dbaccessMitarbeiterverwaltung.findeNachName(suchbegriff);
            }

            if (mitarbeiter == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "error", "Kein passender Mitarbeiter gefunden"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "personalNr", mitarbeiter.getPersonalNr(),
                    "vorname", mitarbeiter.getVorname(),
                    "nachname", mitarbeiter.getNachname(),
                    "anzeigeName", mitarbeiter.getVorname() + " " + mitarbeiter.getNachname()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @GetMapping("/raum")
    public ResponseEntity<?> sucheRaum(@RequestParam String suchbegriff) {
        try {
            Raum raum = null;

            try {
                Integer raumNr = Integer.valueOf(suchbegriff);
                raum = dbaccessRaumverwaltung.sucheRaum(raumNr);
            } catch (NumberFormatException ignored) {
            }

            if (raum == null) {
                raum = dbaccessRaumverwaltung.findeNachGebaeude(suchbegriff);
            }

            if (raum == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "error", "Kein passender Raum gefunden"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "raumNr", raum.getRaumNr(),
                    "gebaeude", raum.getGebaeude(),
                    "anzeigeName", raum.getGebaeude() + " " + raum.getRaumNr()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @GetMapping("/geraetetyp")
    public ResponseEntity<?> sucheGeraetetyp(@RequestParam String suchbegriff) {
        try {
            Geraetetyp geraetetyp = dbaccessGeraeteverwaltung.sucheGeraetetyp(suchbegriff);

            if (geraetetyp == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "error", "Kein passender Gerätetyp gefunden"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "id", geraetetyp.getId(),
                    "bezeichnung", geraetetyp.getBezeichnung(),
                    "hersteller", geraetetyp.getHersteller(),
                    "anzeigeName", geraetetyp.getHersteller() + " " + geraetetyp.getBezeichnung()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @GetMapping("/ausleihe/{ausleiheNr}")
    public ResponseEntity<?> sucheAusleihe(@PathVariable Integer ausleiheNr) {
        Ausleihe ausleihe = dbaccessAusleiheverwaltung.sucheAusleihe(ausleiheNr);

        if (ausleihe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Ausleihe nicht gefunden"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "ausleiheNr", ausleihe.getAusleiheNr(),
                "ausleihdatum", ausleihe.getAusleihdatum(),
                "vereinbartesRueckgabedatum", ausleihe.getVereinbartesRueckgabedatum(),
                "tatsaechlichesRueckgabedatum", ausleihe.getTatsaechlichesRueckgabedatum(),
                "inventarNr", ausleihe.getGeraet().getInventarNr(),
                "geraetetypId", ausleihe.getGeraet().getGeraetetyp().getId(),
                "personalNr", ausleihe.getMitarbeiter().getPersonalNr()
        ));
    }

    @GetMapping("/raeume")
    public ResponseEntity<?> sucheRaeume(@RequestParam(required = false) String suchbegriff) {
        try {
            var raeume = dbaccessRaumverwaltung.findeRaeumeNachFilter(suchbegriff);

            return ResponseEntity.ok(
                    raeume.stream()
                            .map(raum -> Map.of(
                                    "raumNr", raum.getRaumNr(),
                                    "gebaeude", raum.getGebaeude(),
                                    "anzeigeName", raum.getGebaeude() + " " + raum.getRaumNr()
                            ))
                            .toList()
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @GetMapping("/geraete")
    public ResponseEntity<?> sucheGeraete(@RequestParam(required = false) String suchbegriff) {
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
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @GetMapping("/reservierung/{reservierungsNr}")
    public ResponseEntity<?> sucheReservierung(@PathVariable Integer reservierungsNr) {
        Reservierung reservierung = dbaccessReservierungsverwaltung.sucheReservierung(reservierungsNr);

        if (reservierung == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Reservierung nicht gefunden"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "reservierungsNr", reservierung.getReservierungsNr(),
                "ausleihdatum", reservierung.getAusleihdatum(),
                "rueckgabedatum", reservierung.getRueckgabedatum(),
                "geraetetypId", reservierung.getGeraetetyp().getId(),
                "personalNr", reservierung.getMitarbeiter().getPersonalNr()
        ));
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

    public record AusleiheRequest(
            Long geraetetypId,
            Integer personalNr,
            LocalDate ausleihdatum,
            LocalDate rueckgabedatum
    ) {
    }

    public record ReservierungRequest(
            Long geraetetypId,
            Integer personalNr,
            LocalDate ausleihdatum,
            LocalDate rueckgabedatum
    ) {
    }

    public record RueckgabeRequest(
            Integer ausleiheNr,
            LocalDate rueckgabeDatum
    ) {
    }
}