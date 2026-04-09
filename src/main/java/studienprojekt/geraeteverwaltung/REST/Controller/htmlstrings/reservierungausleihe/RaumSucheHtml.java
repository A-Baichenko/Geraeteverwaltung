package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.reservierungausleihe;

public final class RaumSucheHtml {

    private RaumSucheHtml() {
    }

    public static String content() {
        return """
            <div class="search-page">
                <div class="card search-card-small">
                    
                    <h2 class="search-title">
                        Raum suchen
                    </h2>

                    <div class="search-bar">
                        <input
                            id="raum-suche-input"
                            placeholder="Raum oder Gebäude filtern..."
                            class="search-input"
                        />
                    </div>

                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Raum Nr</th>
                                <th>Gebäude</th>
                            </tr>
                        </thead>
                        <tbody id="raum-tabelle-body"></tbody>
                    </table>

                    <div class="center-actions">
                        <button id="back-to-form" type="button">Zurück</button>
                    </div>
                </div>
            </div>
            """;
    }
}