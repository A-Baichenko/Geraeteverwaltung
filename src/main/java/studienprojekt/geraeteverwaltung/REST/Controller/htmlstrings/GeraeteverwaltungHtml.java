package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class GeraeteverwaltungHtml {

    private GeraeteverwaltungHtml() {
    }

    public static String content() {
        return """
                <section id="geraeteverwaltung-reservation-requests" data-module="geraeteverwalten-reservierungsantraege">
                    <header class="section-header">
                        <h2>Geräteverwalten → Reservierungsanträge annehmen</h2>
                        <p class="section-subtitle">Basisstruktur ist sichtbar. Aktuell ist nur der Bereich „Reservierungsanträge“ aktiv.</p>
                    </header>
                    <div id="gv-manager-app" class="gv-manager-app"></div>
                </section>
                """;
    }
}
