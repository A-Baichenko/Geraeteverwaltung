package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class GeraeteverwaltungHtml {

    private GeraeteverwaltungHtml() {
    }

    public static String content() {
        return """
                <section id="geraeteverwaltung-reservation-requests" data-module="geraeteverwalten-reservierungsantraege">
                    <header class="section-header">
                        <h2>Geräteverwalten</h2> 
                    </header>
                    <div id="gv-manager-app" class="gv-manager-app"></div>
                </section>
                """;
    }
}
