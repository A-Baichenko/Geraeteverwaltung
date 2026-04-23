package studienprojekt.geraeteverwaltung.geraeteverwaltung;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.geraeteverwaltung.DBaccess.DBaccess_Reservierungsverwaltung;

@Component
public class ReservationCleanupScheduler {

    private final DBaccess_Reservierungsverwaltung reservierungsverwaltung;

    public ReservationCleanupScheduler(DBaccess_Reservierungsverwaltung reservierungsverwaltung) {
        this.reservierungsverwaltung = reservierungsverwaltung;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    @Transactional
    public void loescheAlteReservierungen() {
        reservierungsverwaltung.loescheAbgeschlosseneAlteReservierungen(LocalDate.now());
    }
}
