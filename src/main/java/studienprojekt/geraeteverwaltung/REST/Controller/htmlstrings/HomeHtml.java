package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class HomeHtml {

    private HomeHtml() {
    }

    public static String content() {
        return """
                <div class="board">\s
                    <article class="column">\s
                        <h2>Verfügbar</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Verfügbar">\s
                        <div class="card-item">Kategorie <span>Anz.</span></div>
                    </article>
                    <article class="column">\s
                        <h2>Reserviert</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Reserviert">\s
                        <div class="card-item">Gerätetyp <span>Anz.</span></div>
                    </article>
                    <article class="column">\s
                        <h2>Ausgeliehen</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Ausgeliehen">\s
                        <div class="card-item">Daten <span>id</span></div>
                    </article>
                    <article class="column">\s
                        <h2>Überfällig</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Überfällig">\s
                        <div class="card-item">Daten <span>id</span></div>
                    </article>
                    <article class="column">\s
                        <h2>Nicht-Ausleihbar</h2>
                        <input type="search" placeholder="Suche" aria-label="Suche Nicht-Ausleihbar">\s
                        <div class="card-item">Daten <span>id</span></div>
                    </article>
                </div>
               \s""";
    }
}
