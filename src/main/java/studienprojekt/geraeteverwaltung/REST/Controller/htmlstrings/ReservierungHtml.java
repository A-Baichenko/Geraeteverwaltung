package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class ReservierungHtml {

    private ReservierungHtml() {
    }

    public static String content() {
        return """
            <div class="reservation-tab">
                <h2 class="reservation-tab-title">Reservieren</h2>

                <div class="reservation-grid">
                    <section class="card reservation-column">
                        <h3>Reservierungsformular</h3>

                        <div class="form-layout">
                            <div>
                                <label for="mitarbeiter" class="form-label">Mitarbeiter</label>
                                <input
                                    id="mitarbeiter"
                                    type="text"
                                    readonly
                                    placeholder="Mitarbeiter wird automatisch gesetzt"
                                    class="form-input"
                                />
                            </div>

                            <div>
                                <label for="geraetetyp" class="form-label">Gerätetyp</label>
                                <input
                                    id="geraetetyp"
                                    type="text"
                                    readonly
                                    placeholder="Gerätetyp aus der Suche wählen"
                                    class="form-input"
                                />
                            </div>

                            <div>
                                <label for="ausleihdatum" class="form-label">Ausleihdatum</label>
                                <input id="ausleihdatum" type="date" class="form-input" />
                            </div>

                            <div>
                                <label for="rueckgabedatum" class="form-label">Rückgabedatum</label>
                                <input id="rueckgabedatum" type="date" class="form-input" />
                            </div>
                            <p class="reservation-note">
                                Nicht verfügbare Zeiträume werden nach Auswahl eines Gerätetyps im Kalender ausgegraut und gesperrt.
                            </p>
                            <button id="btn-reservierung-speichern" type="button">Reservieren</button>
                            <button id="btn-reservierung-abbrechen" type="button" class="secondary-button hidden">Bearbeitung abbrechen</button>
                        </div>
                    </section>

                    <section class="card reservation-column">
                        <h3>Gerätetyp-Suche</h3>

                        <div class="search-bar">
                            <input
                                id="geraetetyp-suche-input"
                                placeholder="Gerätetyp, Hersteller oder Kategorie suchen..."
                                class="search-input"
                            />
                        </div>

                        <ul id="geraetetyp-liste" class="selection-list"></ul>
                    </section>

                    <section class="card reservation-column">
                        <h3>Eigene Reservierungen</h3>
                        <ul id="meine-reservierungen-liste" class="selection-list"></ul>
                    </section>
                </div>
            </div>
            """;
    }
}