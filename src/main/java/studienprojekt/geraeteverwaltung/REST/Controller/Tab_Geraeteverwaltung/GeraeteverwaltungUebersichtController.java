package studienprojekt.geraeteverwaltung.REST.Controller.Tab_Geraeteverwaltung;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Ausleiheverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Geraeteverwaltung;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.GeraetStatus;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraetetyp;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_Mitarbeiterverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Mitarbeiter;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.DBaccess_Raumverwaltung;
import studienprojekt.geraeteverwaltung.raumverwaltung.DBaccess.entity.Raum;

@RestController
@RequestMapping("/api/geraeteverwaltung/device-management")
public class GeraeteverwaltungUebersichtController {

    private final DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung;
    private final DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung;
    private final DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung;
    private final DBaccess_Raumverwaltung dbaccessRaumverwaltung;
    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;

    public GeraeteverwaltungUebersichtController(
            DBaccess_Geraeteverwaltung dbaccessGeraeteverwaltung,
            DBaccess_Ausleiheverwaltung dbaccessAusleiheverwaltung,
            DBaccess_Mitarbeiterverwaltung dbaccessMitarbeiterverwaltung,
            DBaccess_Raumverwaltung dbaccessRaumverwaltung,
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung) {
        this.dbaccessGeraeteverwaltung = dbaccessGeraeteverwaltung;
        this.dbaccessAusleiheverwaltung = dbaccessAusleiheverwaltung;
        this.dbaccessMitarbeiterverwaltung = dbaccessMitarbeiterverwaltung;
        this.dbaccessRaumverwaltung = dbaccessRaumverwaltung;
        this.jwtService = jwtService;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<?> getHierarchy(HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return forbidden();
        }
        return ResponseEntity.ok(Map.of("categories", baueKategorien(null, null)));
    }

    @GetMapping("/hierarchy-html")
    public ResponseEntity<?> getHierarchyHtml(@RequestParam(required = false) String query,
                                              @RequestParam(defaultValue = "all") String status,
                                              HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return forbidden();
        }

        GeraetStatus filterStatus = parseStatus(status);
        List<Map<String, Object>> categories = baueKategorien(query, filterStatus);
        String html = renderHierarchyHtml(categories, query, status);
        return ResponseEntity.ok(Map.of("html", html));
    }

    @GetMapping("/move-options")
    public ResponseEntity<?> getMoveOptions(HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return forbidden();
        }

        return ResponseEntity.ok(Map.of(
                "deviceTypes", dbaccessGeraeteverwaltung.findeGeraetetypenNachFilter(null).stream()
                        .map(typ -> Map.of("id", typ.getId(), "label", typ.getHersteller() + " " + typ.getBezeichnung()))
                        .toList(),
                "employees", dbaccessMitarbeiterverwaltung.findeMitarbeiterNachFilter(null).stream()
                        .map(person -> Map.of("id", person.getPersonalNr(), "label", person.getVorname() + " " + person.getNachname()))
                        .toList(),
                "rooms", dbaccessRaumverwaltung.findeRaeumeNachFilter(null).stream()
                        .map(raum -> Map.of("id", raum.getRaumNr(), "label", "Raum " + raum.getRaumNr() + " - " + raum.getGebaeude()))
                        .toList()
        ));
    }

    @PutMapping("/devices/{inventarNr}")
    public ResponseEntity<?> editDevice(@PathVariable Integer inventarNr,
                                        @RequestBody EditDeviceRequest body,
                                        HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return forbidden();
        }

        if (body.serienNr() == null || body.kaufdatum() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Seriennummer und Kaufdatum sind Pflichtfelder"));
        }

        try {
            dbaccessGeraeteverwaltung.aktualisiereGeraet(inventarNr, body.serienNr(), body.kaufdatum(), Boolean.TRUE.equals(body.ausleihbar()));
            return ResponseEntity.ok(Map.of("message", "Gerät wurde bearbeitet"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/devices/{inventarNr}/move")
    public ResponseEntity<?> moveDevice(@PathVariable Integer inventarNr,
                                        @RequestBody MoveDeviceRequest body,
                                        HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return forbidden();
        }

        Geraetetyp geraetetyp = dbaccessGeraeteverwaltung.sucheGeraetetypById(body.geraetetypId());
        Mitarbeiter mitarbeiter = body.mitarbeiterPersonalNr() != null
                ? dbaccessMitarbeiterverwaltung.sucheMitarbeiter(body.mitarbeiterPersonalNr())
                : null;
        Raum raum = body.raumNr() != null
                ? dbaccessRaumverwaltung.sucheRaum(body.raumNr())
                : null;

        try {
            dbaccessGeraeteverwaltung.verschiebeGeraet(inventarNr, geraetetyp, mitarbeiter, raum);
            return ResponseEntity.ok(Map.of("message", "Gerät wurde verschoben"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/devices/{inventarNr}")
    public ResponseEntity<?> deleteDevice(@PathVariable Integer inventarNr,
                                          HttpServletRequest request) {
        if (authenticatedManagerOrAdmin(request) == null) {
            return forbidden();
        }

        if (dbaccessGeraeteverwaltung.zaehleAusleihenZuGeraet(inventarNr) > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Gerät kann nicht gelöscht werden, da bereits Ausleihen vorhanden sind."));
        }

        try {
            dbaccessGeraeteverwaltung.loescheGeraet(inventarNr);
            return ResponseEntity.ok(Map.of("message", "Gerät wurde gelöscht"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private List<Map<String, Object>> baueKategorien(String query, GeraetStatus statusFilter) {
        LocalDate heute = LocalDate.now();
        dbaccessGeraeteverwaltung.synchronisiereStatus(heute);
        Map<Integer, String> aktiveAusleiher = dbaccessAusleiheverwaltung.findeAktiveAusleiherJeInventar(heute);
        List<Geraet> geraete = dbaccessGeraeteverwaltung.findeGeraeteMitDetailsNachFilter(query, statusFilter);

        Map<Long, KategorieNode> kategorien = new LinkedHashMap<>();

        for (Geraet geraet : geraete) {
            Long kategorieId = geraet.getGeraetetyp().getKategorie().getId();
            KategorieNode kategorieNode = kategorien.computeIfAbsent(kategorieId,
                    ignored -> new KategorieNode(kategorieId, geraet.getGeraetetyp().getKategorie().getBezeichnung(), new LinkedHashMap<>()));

            Long typId = geraet.getGeraetetyp().getId();
            GeraetetypNode typNode = kategorieNode.geraetetypen().computeIfAbsent(typId,
                    ignored -> new GeraetetypNode(
                            typId,
                            geraet.getGeraetetyp().getHersteller(),
                            geraet.getGeraetetyp().getBezeichnung(),
                            new ArrayList<>()));

            String status = geraet.getStatus().name().toLowerCase();

            Map<String, Object> geraetData = new LinkedHashMap<>();
            geraetData.put("inventarNr", geraet.getInventarNr());
            geraetData.put("serienNr", geraet.getSerienNr());
            geraetData.put("kaufdatum", geraet.getKaufdatum() != null ? geraet.getKaufdatum().toString() : "");
            String mitarbeiterName = aktiveAusleiher.get(geraet.getInventarNr());
            if (mitarbeiterName == null && geraet.getStaendigerNutzer() != null) {
                mitarbeiterName = geraet.getStaendigerNutzer().getVorname() + " " + geraet.getStaendigerNutzer().getNachname();
            }
            geraetData.put("mitarbeiter", mitarbeiterName != null ? mitarbeiterName : "");
            geraetData.put("mitarbeiterPersonalNr", geraet.getStaendigerNutzer() != null ? geraet.getStaendigerNutzer().getPersonalNr() : null);
            geraetData.put("raum", geraet.getStandort() != null ? geraet.getStandort().getRaumNr() : null);
            geraetData.put("geraetetypId", geraet.getGeraetetyp().getId());
            geraetData.put("status", status);
            geraetData.put("istAusleihbar", geraet.isIstAusleihbar());

            typNode.geraete().add(geraetData);
        }

        return kategorien.values().stream()
                .sorted(Comparator.comparing(KategorieNode::name, String.CASE_INSENSITIVE_ORDER))
                .map(kategorie -> Map.of(
                        "id", kategorie.id(),
                        "name", kategorie.name(),
                        "types", kategorie.geraetetypen().values().stream()
                                .sorted(Comparator.comparing(GeraetetypNode::displayName, String.CASE_INSENSITIVE_ORDER))
                                .map(typ -> Map.of(
                                        "id", typ.id(),
                                        "name", typ.displayName(),
                                        "devices", typ.geraete().stream()
                                                .sorted(Comparator.comparingInt(v -> (Integer) v.get("inventarNr")))
                                                .toList()
                                ))
                                .toList()
                ))
                .toList();
    }

    private String renderHierarchyHtml(List<Map<String, Object>> categories, String query, String statusFilter) {
        StringBuilder html = new StringBuilder();

        for (Map<String, Object> category : categories) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> types = (List<Map<String, Object>>) category.get("types");
            StringBuilder typeHtml = new StringBuilder();

            for (Map<String, Object> type : types) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> devices = (List<Map<String, Object>>) type.get("devices");
                StringBuilder deviceHtml = new StringBuilder();

                for (Map<String, Object> device : devices) {
                    String deviceStatus = String.valueOf(device.get("status"));

                    deviceHtml.append("""
                            <article class="dm-device-card">
                                <div class="dm-device-main">
                                    <div><strong>Inventar:</strong> %s</div>
                                    <div><strong>Seriennummer:</strong> %s</div>
                                    <div><strong>Kaufdatum:</strong> %s</div>
                                    <div><strong>Mitarbeiter:</strong> %s</div>
                                    <div><strong>Raum:</strong> %s</div>
                                    <div><strong>Status:</strong> <span class="dm-status dm-status-%s">%s</span></div>
                                </div>
                                <div class="dm-device-actions">
                                    <button type="button" class="icon-button" data-action="dm-open-move" data-inventar-nr="%s" data-serien-nr="%s" data-kaufdatum="%s" data-geraetetyp-id="%s" data-mitarbeiter-personal-nr="%s" data-raum="%s" data-ist-ausleihbar="%s" title="Verschieben">⇆</button>
                                    <button type="button" class="icon-button" data-action="dm-open-edit" data-inventar-nr="%s" data-serien-nr="%s" data-kaufdatum="%s" data-geraetetyp-id="%s" data-mitarbeiter-personal-nr="%s" data-raum="%s" data-ist-ausleihbar="%s" title="Bearbeiten">✎</button>
                                    <button type="button" class="icon-button" data-action="dm-delete-device" data-inventar-nr="%s" title="Löschen">🗑</button>
                                </div>
                            </article>
                            """.formatted(
                            escapeHtml(String.valueOf(device.get("inventarNr"))),
                            escapeHtml(String.valueOf(device.get("serienNr"))),
                            escapeHtml(String.valueOf(device.get("kaufdatum"))),
                            escapeHtml(String.valueOf(device.get("mitarbeiter"))),
                            escapeHtml(String.valueOf(device.get("raum"))),
                            escapeHtml(deviceStatus),
                            escapeHtml(statusLabel(deviceStatus)),
                            escapeHtml(String.valueOf(device.get("inventarNr"))),
                            escapeHtml(String.valueOf(device.get("serienNr"))),
                            escapeHtml(String.valueOf(device.get("kaufdatum"))),
                            escapeHtml(String.valueOf(device.get("geraetetypId"))),
                            escapeHtml(String.valueOf(device.get("mitarbeiterPersonalNr"))),
                            escapeHtml(String.valueOf(device.get("raum"))),
                            escapeHtml(String.valueOf(device.get("istAusleihbar"))),
                            escapeHtml(String.valueOf(device.get("inventarNr"))),
                            escapeHtml(String.valueOf(device.get("serienNr"))),
                            escapeHtml(String.valueOf(device.get("kaufdatum"))),
                            escapeHtml(String.valueOf(device.get("geraetetypId"))),
                            escapeHtml(String.valueOf(device.get("mitarbeiterPersonalNr"))),
                            escapeHtml(String.valueOf(device.get("raum"))),
                            escapeHtml(String.valueOf(device.get("istAusleihbar"))),
                            escapeHtml(String.valueOf(device.get("inventarNr")))
                    ));
                }

                if (!deviceHtml.isEmpty()) {
                    typeHtml.append("""
                            <details class="dm-level dm-type" open>
                                <summary class="dm-row dm-type-row">%s</summary>
                                <div class="dm-device-list">%s</div>
                            </details>
                            """.formatted(
                            escapeHtml(String.valueOf(type.get("name"))),
                            deviceHtml
                    ));
                }
            }

            if (!typeHtml.isEmpty()) {
                html.append("""
                        <details class="dm-level dm-category" open>
                            <summary class="dm-row dm-category-row"><strong>%s</strong></summary>
                            %s
                        </details>
                        """.formatted(
                        escapeHtml(String.valueOf(category.get("name"))),
                        typeHtml
                ));
            }
        }

        if (html.isEmpty()) {
            return "<p class=\"placeholder\">Keine passenden Geräte gefunden.</p>";
        }
        return html.toString();
    }

    private GeraetStatus parseStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }

        return switch (status.toLowerCase()) {
            case "verfuegbar" -> GeraetStatus.VERFUEGBAR;
            case "reserviert" -> GeraetStatus.RESERVIERT;
            case "ausgeliehen" -> GeraetStatus.AUSGELIEHEN;
            case "fest_zugeordnet" -> GeraetStatus.FEST_ZUGEORDNET;
            case "wartung_defekt" -> GeraetStatus.WARTUNG_DEFEKT;
            default -> null;
        };
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "verfuegbar" -> "Verfügbar";
            case "reserviert" -> "Reserviert";
            case "ausgeliehen" -> "Ausgeliehen";
            case "fest_zugeordnet" -> "Fest zugeordnet";
            case "wartung_defekt" -> "In Wartung / Defekt";
            default -> status;
        };
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Nur für Geräteverwalter oder Admin verfügbar"));
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

    private record KategorieNode(Long id, String name, Map<Long, GeraetetypNode> geraetetypen) {
    }

    private record GeraetetypNode(Long id, String hersteller, String bezeichnung, List<Map<String, Object>> geraete) {
        private String displayName() {
            return (hersteller + " " + bezeichnung).trim();
        }
    }

    public record EditDeviceRequest(Integer serienNr, LocalDate kaufdatum, Boolean ausleihbar) {
    }

    public record MoveDeviceRequest(Long geraetetypId, Integer mitarbeiterPersonalNr, Integer raumNr) {
    }
}