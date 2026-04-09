package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class ReservierungAusleiheHtml {

    private ReservierungAusleiheHtml() {
    }

    public static String content() {
        return """
            <div class="page-wrapper">
                <div class="board" style="display: flex; justify-content: center; gap: 2rem; border: none;">
                    <div style="text-align: center;">
                        <div class="card-item" id="btn-ausleihen" style="cursor: pointer;">
                            <b>Ausleihen</b>
                        </div>
                    </div>

                    <div style="text-align: center;">
                        <div class="card-item">
                            <b>Reservieren</b>
                        </div>
                    </div>

                    <div style="text-align: center;">
                        <div class="card-item">
                            <b>Fest ausleihen</b>
                        </div>
                    </div>
                </div>
            </div>
            """;
    }
}
