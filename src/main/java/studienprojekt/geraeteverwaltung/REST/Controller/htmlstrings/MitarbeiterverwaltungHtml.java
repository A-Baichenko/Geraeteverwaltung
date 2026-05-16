package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class MitarbeiterverwaltungHtml {

    private MitarbeiterverwaltungHtml() {
    }

    public static String content() {
        return """
                <section class="management-layout" id="mv-root">
                                    <div class="management-panel">
                                        <h2>Anlegen</h2>
                                        <div class="management-form-grid">
                                            <label for="mv-personalnr">Personalnummer</label>
                                            <input id="mv-personalnr" type="number" min="1" />
                
                                            <label for="mv-vorname">Vorname</label>
                                            <input id="mv-vorname" type="text" />
                
                                            <label for="mv-nachname">Nachname</label>
                                            <input id="mv-nachname" type="text" />
                
                                            <label for="mv-anrede">Anrede</label>
                                            <select id="mv-anrede">
                                                <option value="HERR">Herr</option>
                                                <option value="FRAU">Frau</option>
                                                <option value="DIVERS">Divers</option>
                                            </select>
                                        </div>
                                        <div class="management-actions">
                                            <button type="button" data-action="mv-reset">Reset</button>
                                            <button type="button" data-action="mv-save">OK</button>
                                        </div>
                                        <p id="mv-form-hint" class="management-hint"></p>
                                    </div>
                
                                    <div class="management-panel">
                                        <h2>Suche</h2>
                                        <input id="mv-search" type="search" placeholder="Suche nach Name, Anrede oder Personalnummer" />
                                        <ul id="mv-result-list" class="management-result-list"></ul>
                                    </div>
                </section>
                """;
    }
}
