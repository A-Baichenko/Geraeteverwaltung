package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class ReservierungAusleiheHtml {

    private ReservierungAusleiheHtml() {
    }

    public static String content() {
        return """
            <div class="page-wrapper">
                <div class="board reservation-board">
                    <div class="menu-item-wrapper">
                        <div class="card-item clickable-card" id="btn-ausleihen">
                            <b>Ausleihen</b>
                        </div>
                    </div>

                    <div class="menu-item-wrapper">
                        <div class="card-item">
                            <b>Reservieren</b>
                        </div>
                    </div>

                    <div class="menu-item-wrapper">
                        <div class="card-item">
                            <b>Fest ausleihen</b>
                        </div>
                    </div>
                </div>
            </div>
            """;
    }
}