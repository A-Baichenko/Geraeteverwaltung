package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class RaumverwaltungHtml {

    private RaumverwaltungHtml() {
    }

    public static String content() {
        return """
                <section class="management-layout" id="rv-root">
                                    <div class="management-panel">
                                        <h2>Anlegen</h2>
                                        <div class="management-form-grid">
                                            <label for="rv-gebaeude">Gebäude</label>
                                            <input id="rv-gebaeude" type="text" />
                
                                            <label for="rv-raum-nr">Raumnummer</label>
                                            <input id="rv-raum-nr" type="number" min="1" />
                                        </div>
                                        <div class="management-actions">
                                            <button type="button" data-action="rv-reset">Reset</button>
                                            <button type="button" data-action="rv-save">OK</button>
                                        </div>
                                        <p id="rv-form-hint" class="management-hint"></p>
                                    </div>
                
                                    <div class="management-panel">
                                        <h2>Suche</h2>
                                        <input id="rv-search" type="search" placeholder="Suche nach Gebäude oder Raumnummer" />
                                        <ul id="rv-result-list" class="management-result-list"></ul>
                                    </div>
                </section>
                """;
    }
}
