package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.reservierungausleihe;

public final class GeraetSucheHtml {

    private GeraetSucheHtml() {
    }

    public static String content() {
        return """
            <div class="search-page">
                <div class="card search-card-large">

                    <h2 class="search-title">
                        Gerät suchen
                    </h2>

                    <div class="search-bar">
                        <input
                            id="geraet-suche-input"
                            placeholder="Gerät, Hersteller oder Kategorie filtern..."
                            class="search-input"
                        />
                    </div>

                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Hersteller</th>
                                <th>Bezeichnung</th>
                                <th>Kategorie</th>
                            </tr>
                        </thead>
                        <tbody id="geraet-tabelle-body"></tbody>
                    </table>

                    <div class="center-actions">
                        <button id="back-to-form" type="button">Zurück</button>
                    </div>
                </div>
            </div>
            """;
    }
}