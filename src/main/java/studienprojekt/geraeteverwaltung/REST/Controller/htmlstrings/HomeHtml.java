package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class HomeHtml {

    private HomeHtml() {
    }

    public static String content() {
        return """
                <section id="home-overview" data-module="home-overview">
                    <header class="section-header">
                        <h2>Home (Übersicht)</h2>
                        <p class="section-subtitle">Statusübersicht der Geräteverwaltung.</p>
                    </header>

                    <div id="hm-overview-app" class="hm-overview-app">
                        <div class="hm-board">
                            <section class="hm-column" data-column="available">
                                <div class="hm-column-header">
                                    <h3>Verfügbar</h3>
                                </div>
                                <div class="hm-search-box">
                                    <input id="hm-search-available" type="text" placeholder="Suche">
                                </div>
                                <ul id="hm-list-available" class="hm-list"></ul>
                                <p id="hm-placeholder-available" class="hm-placeholder">Keine Einträge vorhanden.</p>
                            </section>

                            <section class="hm-column" data-column="reserved">
                                <div class="hm-column-header">
                                    <h3>Reserviert</h3>
                                </div>
                                <div class="hm-search-box">
                                    <input id="hm-search-reserved" type="text" placeholder="Suche">
                                </div>
                                <ul id="hm-list-reserved" class="hm-list"></ul>
                                <p id="hm-placeholder-reserved" class="hm-placeholder">Keine Einträge vorhanden.</p>
                            </section>

                            <section class="hm-column" data-column="lent">
                                <div class="hm-column-header">
                                    <h3>Ausgeliehen</h3>
                                </div>
                                <div class="hm-search-box">
                                    <input id="hm-search-lent" type="text" placeholder="Suche">
                                </div>
                                <ul id="hm-list-lent" class="hm-list"></ul>
                                <p id="hm-placeholder-lent" class="hm-placeholder">Keine Einträge vorhanden.</p>
                            </section>

                            <section class="hm-column" data-column="notLendable">
                                <div class="hm-column-header">
                                    <h3>Fest zuordnen</h3>
                                </div>
                                <div class="hm-search-box">
                                    <input id="hm-search-not-lendable" type="text" placeholder="Suche">
                                </div>
                                <ul id="hm-list-not-lendable" class="hm-list"></ul>
                                <p id="hm-placeholder-not-lendable" class="hm-placeholder">Keine Einträge vorhanden.</p>
                            </section>
                        </div>
                    </div>
                </section>
                """;
    }
}