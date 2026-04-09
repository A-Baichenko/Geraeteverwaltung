package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.reservierungausleihe;

public final class GeraetSucheHtml {

    private GeraetSucheHtml() {
    }

    public static String content() {
        return """
            <div class="page-wrapper" style="display:flex; justify-content:center;">
                <div class="card" style="width: 900px; padding: 2rem;">

                    <h2 style="text-align:center; margin-bottom: 1.5rem;">
                        Gerät suchen
                    </h2>

                    <div style="display:flex; gap:1rem; margin-bottom:1.5rem;">
                        <input
                            id="geraet-suche-input"
                            placeholder="Gerät, Hersteller oder Kategorie filtern..."
                            style="flex:1; padding:0.5rem;"
                        />
                    </div>

                    <table style="width:100%; border-collapse: collapse;">
                        <thead>
                            <tr style="background:#f0f0f0;">
                                <th style="padding:0.5rem; border:1px solid #ccc;">Hersteller</th>
                                <th style="padding:0.5rem; border:1px solid #ccc;">Bezeichnung</th>
                                <th style="padding:0.5rem; border:1px solid #ccc;">Kategorie</th>
                            </tr>
                        </thead>
                        <tbody id="geraet-tabelle-body"></tbody>
                    </table>

                    <div style="margin-top:2rem; text-align:center;">
                        <button id="back-to-form" type="button">Zurück</button>
                    </div>
                </div>
            </div>
            """;
    }
}