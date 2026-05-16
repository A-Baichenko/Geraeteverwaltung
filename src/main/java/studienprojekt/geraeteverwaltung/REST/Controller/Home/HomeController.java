package studienprojekt.geraeteverwaltung.REST.Controller.Home;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studienprojekt.geraeteverwaltung.REST.Service.JwtService;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Ausleihe;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Geraet;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.entity.Reservierung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.DBaccess_AppUserverwaltung;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.Role;

@RestController
@RequestMapping("/api/home")
@Transactional
public class HomeController {

    private final JwtService jwtService;
    private final DBaccess_AppUserverwaltung dbaccessAppUserverwaltung;

    @PersistenceContext
    private EntityManager em;

    public HomeController(
            JwtService jwtService,
            DBaccess_AppUserverwaltung dbaccessAppUserverwaltung) {
        this.jwtService = jwtService;
        this.dbaccessAppUserverwaltung = dbaccessAppUserverwaltung;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(HttpServletRequest request) {
        if (authenticatedUser(request) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nicht berechtigt"));
        }

        List<Geraet> alleGeraete = em.createQuery(
                        "SELECT g FROM Geraet g " +
                                "LEFT JOIN FETCH g.geraetetyp " +
                                "LEFT JOIN FETCH g.standort " +
                                "ORDER BY g.inventarNr",
                        Geraet.class)
                .getResultList();

        List<Reservierung> offeneReservierungen = em.createQuery(
                        "SELECT r FROM Reservierung r " +
                                "JOIN FETCH r.mitarbeiter " +
                                "JOIN FETCH r.geraetetyp " +
                                "WHERE NOT EXISTS (" +
                                "SELECT a.ausleiheNr FROM Ausleihe a WHERE a.reservierung = r" +
                                ") ORDER BY r.ausleihdatum",
                        Reservierung.class)
                .getResultList();

        List<Ausleihe> aktiveAusleihen = em.createQuery(
                        "SELECT a FROM Ausleihe a " +
                                "JOIN FETCH a.mitarbeiter " +
                                "JOIN FETCH a.geraet g " +
                                "JOIN FETCH g.geraetetyp " +
                                "WHERE a.tatsaechlichesRueckgabedatum IS NULL " +
                                "ORDER BY a.ausleihdatum",
                        Ausleihe.class)
                .getResultList();

        List<Geraet> freieAusleihbareGeraete = alleGeraete.stream()
                .filter(Geraet::isIstAusleihbar)
                .filter(g -> !istAktivAusgeliehen(g, aktiveAusleihen))
                .sorted(Comparator.comparing(Geraet::getInventarNr))
                .toList();

        Map<Long, Long> reservierungenProTyp = offeneReservierungen.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getGeraetetyp().getId(),
                        Collectors.counting()
                ));

        Map<Long, Integer> bereitsVerwendeteReservierungsSlots = new LinkedHashMap<>();

        List<Map<String, Object>> available = freieAusleihbareGeraete.stream()
                .filter(g -> {
                    Long typId = g.getGeraetetyp().getId();
                    long blockierteAnzahl = reservierungenProTyp.getOrDefault(typId, 0L);
                    int bereitsVerwendet = bereitsVerwendeteReservierungsSlots.getOrDefault(typId, 0);

                    if (bereitsVerwendet < blockierteAnzahl) {
                        bereitsVerwendeteReservierungsSlots.put(typId, bereitsVerwendet + 1);
                        return false;
                    }

                    return true;
                })
                .map(g -> createItem(
                        "Inventar #" + g.getInventarNr(),
                        g.getGeraetetyp().getHersteller() + " " + g.getGeraetetyp().getBezeichnung(),
                        g.getStandort() != null ? "Raum " + g.getStandort().getRaumNr() : null,
                        null
                ))
                .toList();

        List<Map<String, Object>> reserved = offeneReservierungen.stream()
                .map(r -> createItem(
                        r.getMitarbeiter().getVorname() + " " + r.getMitarbeiter().getNachname(),
                        r.getGeraetetyp().getHersteller() + " " + r.getGeraetetyp().getBezeichnung(),
                        "Von " + r.getAusleihdatum(),
                        "Bis " + r.getRueckgabedatum()
                ))
                .toList();

        List<Map<String, Object>> lent = aktiveAusleihen.stream()
                .map(a -> createItem(
                        a.getMitarbeiter().getVorname() + " " + a.getMitarbeiter().getNachname(),
                        "Inventar #" + a.getGeraet().getInventarNr(),
                        "Ausleihdatum: " + a.getAusleihdatum(),
                        "Rückgabe: " + a.getVereinbartesRueckgabedatum()
                ))
                .toList();

        List<Map<String, Object>> notLendable = alleGeraete.stream()
                .filter(g -> !g.isIstAusleihbar())
                .map(g -> createItem(
                        "Inventar #" + g.getInventarNr(),
                        g.getGeraetetyp().getHersteller() + " " + g.getGeraetetyp().getBezeichnung(),
                        g.getStandort() != null ? "Raum " + g.getStandort().getRaumNr() : null,
                        "Nicht-ausleihbar"
                ))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", available);
        response.put("reserved", reserved);
        response.put("lent", lent);
        response.put("notLendable", notLendable);

        return ResponseEntity.ok(response);
    }

    private boolean istAktivAusgeliehen(Geraet geraet, List<Ausleihe> aktiveAusleihen) {
        return aktiveAusleihen.stream()
                .anyMatch(a -> a.getGeraet().getInventarNr().equals(geraet.getInventarNr()));
    }

    private Map<String, Object> createItem(String title, String subtitle, String meta1, String meta2) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("subtitle", subtitle);
        item.put("meta1", meta1);
        item.put("meta2", meta2);
        return item;
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

        AppUser user = dbaccessAppUserverwaltung.sucheNachUsername(claims.getSubject());
        if (user == null) {
            return null;
        }

        return user.getRole() == Role.ADMIN
                || user.getRole() == Role.GERAETE_VERWALTER
                || user.getRole() == Role.PERSONEN_VERWALTER
                || user.getRole() == Role.RAUM_VERWALTER
                ? user
                : null;
    }
}