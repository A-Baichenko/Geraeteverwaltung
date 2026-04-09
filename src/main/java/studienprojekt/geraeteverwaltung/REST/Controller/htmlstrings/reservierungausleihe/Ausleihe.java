package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.reservierungausleihe;

public final class Ausleihe {

    private Ausleihe() {
    }

    public static String content() {
        return """
            <div class="form-page">
                <div class="card form-card">

                    <h2 class="page-title">
                        Ausleihen
                    </h2>

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
                            <label for="raum" class="form-label">Raum</label>
                            <input
                                id="raum"
                                type="text"
                                readonly
                                placeholder="Raum auswählen"
                                class="form-input clickable-input"
                            />
                        </div>

                        <div>
                            <label for="geraet" class="form-label">Gerät</label>
                            <input
                                id="geraet"
                                type="text"
                                readonly
                                placeholder="Gerät auswählen"
                                class="form-input clickable-input"
                            />
                        </div>

                        <div>
                            <label for="ausleihdatum" class="form-label">Ausleihdatum</label>
                            <input
                                id="ausleihdatum"
                                type="date"
                                class="form-input"
                            />
                        </div>

                        <div>
                            <label for="rueckgabedatum" class="form-label">Rückgabedatum</label>
                            <input
                                id="rueckgabedatum"
                                type="date"
                                class="form-input"
                            />
                        </div>

                        <div class="button-row">
                            <button id="btn-ausleihe-speichern" type="button">Ausleihe speichern</button>
                            <button id="back-to-reservierung-menu" type="button">Zurück</button>
                        </div>

                    </div>
                </div>
            </div>
            """;
    }
}