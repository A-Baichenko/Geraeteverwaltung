package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings.reservierungausleihe;

public final class Ausleihe {

    private Ausleihe() {
    }

    public static String content() {
        return """
            <div class="page-wrapper" style="display:flex; justify-content:center; padding:2rem;">
                <div class="card" style="width: 700px; padding: 2rem;">

                    <h2 style="text-align:center; margin-bottom:2rem;">
                        Ausleihen
                    </h2>

                    <div style="display:flex; flex-direction:column; gap:1.25rem;">

                        <div>
                            <label for="mitarbeiter" style="display:block; margin-bottom:0.4rem;">Mitarbeiter</label>
                            <input
                                id="mitarbeiter"
                                type="text"
                                readonly
                                placeholder="Mitarbeiter wird automatisch gesetzt"
                                style="width:100%; padding:0.65rem;"
                            />
                        </div>

                        <div>
                            <label for="raum" style="display:block; margin-bottom:0.4rem;">Raum</label>
                            <input
                                id="raum"
                                type="text"
                                readonly
                                placeholder="Raum auswählen"
                                style="width:100%; padding:0.65rem; cursor:pointer;"
                            />
                        </div>

                        <div>
                            <label for="geraet" style="display:block; margin-bottom:0.4rem;">Gerät</label>
                            <input
                                id="geraet"
                                type="text"
                                readonly
                                placeholder="Gerät auswählen"
                                style="width:100%; padding:0.65rem; cursor:pointer;"
                            />
                        </div>

                        <div>
                            <label for="ausleihdatum" style="display:block; margin-bottom:0.4rem;">Ausleihdatum</label>
                            <input
                                id="ausleihdatum"
                                type="date"
                                style="width:100%; padding:0.65rem;"
                            />
                        </div>

                        <div>
                            <label for="rueckgabedatum" style="display:block; margin-bottom:0.4rem;">Rückgabedatum</label>
                            <input
                                id="rueckgabedatum"
                                type="date"
                                style="width:100%; padding:0.65rem;"
                            />
                        </div>

                        <div style="display:flex; justify-content:center; gap:1rem; margin-top:1rem;">
                            <button id="btn-ausleihe-speichern" type="button">Ausleihe speichern</button>
                            <button id="back-to-reservierung-menu" type="button">Zurück</button>
                        </div>

                    </div>
                </div>
            </div>
            """;
    }
}