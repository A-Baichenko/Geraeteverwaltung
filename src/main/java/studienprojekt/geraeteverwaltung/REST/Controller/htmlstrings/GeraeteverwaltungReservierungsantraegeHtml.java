package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class GeraeteverwaltungReservierungsantraegeHtml {

    private GeraeteverwaltungReservierungsantraegeHtml() {
    }

    public static String content() {
        return """
                <div class="gv-accordion-layout">
                    <section class="gv-accordion-section" data-section="createDevices">
                        <button type="button" class="gv-accordion-trigger" data-action="toggle-section" data-section-key="createDevices">
                            <span>Geräteanlegen</span><span class="gv-chevron">▾</span>
                        </button>
                        <div class="gv-accordion-content"><p class="placeholder">Bereich vorhanden, Funktion folgt später.</p></div>
                    </section>

                    <section class="gv-accordion-section is-open" data-section="reservationRequests">
                        <button type="button" class="gv-accordion-trigger" data-action="toggle-section" data-section-key="reservationRequests">
                            <span>Reservierungsanträge</span><span class="gv-chevron">▾</span>
                        </button>
                        <div class="gv-accordion-content">
                            <p id="gv-global-error" class="error-text"></p>
                            <div class="gv-grid">
                                <section>
                                    <h3>Reservierungsanträge</h3>
                                    <ul id="gv-request-list" class="gv-list"></ul>
                                </section>
                                <section id="gv-details-panel" class="gv-slide-panel">
                                    <h3>Antragsdetails</h3>
                                    <div id="gv-fields" class="gv-fields"></div>
                                    <div class="gv-actions">
                                        <button type="button" data-action="open-device-selection">Gerät auswählen</button>
                                        <button type="button" data-action="edit-request">Bearbeiten</button>
                                        <button type="button" class="danger-button" data-action="delete-request">Löschen</button>
                                        <button type="button" data-action="accept-request">Ausleihen</button>
                                    </div>
                                    <p id="gv-action-error" class="error-text"></p>
                                    <p id="gv-details-placeholder" class="placeholder">Bitte einen Reservierungsantrag auswählen.</p>
                                </section>
                            </div>
                        </div>
                    </section>

                    <section class="gv-accordion-section" data-section="assignFixed">
                        <button type="button" class="gv-accordion-trigger" data-action="toggle-section" data-section-key="assignFixed">
                            <span>Fest zuordnen</span><span class="gv-chevron">▾</span>
                        </button>
                        <div class="gv-accordion-content"><p class="placeholder">Bereich vorhanden, Funktion folgt später.</p></div>
                    </section>

                    <section class="gv-accordion-section" data-section="lend">
                        <button type="button" class="gv-accordion-trigger" data-action="toggle-section" data-section-key="lend">
                            <span>Ausleihe</span><span class="gv-chevron">▾</span>
                        </button>
                        <div class="gv-accordion-content"><p class="placeholder">Bereich vorhanden, Funktion folgt später.</p></div>
                    </section>

                    <section class="gv-accordion-section" data-section="lendOverview">
                        <button type="button" class="gv-accordion-trigger" data-action="toggle-section" data-section-key="lendOverview">
                            <span>Ausleihe Übersicht</span><span class="gv-chevron">▾</span>
                        </button>
                        <div class="gv-accordion-content"><p class="placeholder">Bereich vorhanden, Funktion folgt später.</p></div>
                    </section>

                    <section class="gv-accordion-section" data-section="deviceManagement">
                        <button type="button" class="gv-accordion-trigger" data-action="toggle-section" data-section-key="deviceManagement">
                            <span>Geräteverwaltung</span><span class="gv-chevron">▾</span>
                        </button>
                        <div class="gv-accordion-content"><p class="placeholder">Bereich vorhanden, Funktion folgt später.</p></div>
                    </section>
                </div>

                <aside id="gv-device-modal" class="gv-device-modal">
                    <div class="gv-device-modal-card">
                        <div class="gv-device-modal-header">
                            <h3>Verfügbare Geräte auswählen</h3>
                            <button type="button" class="icon-button" data-action="close-device-selection">✕</button>
                        </div>
                        <p id="gv-device-type-name"></p>
                        <ul id="gv-device-list" class="gv-device-list"></ul>
                        <div class="gv-device-modal-actions">
                            <button type="button" data-action="confirm-device-selection">OK</button>
                            <button type="button" data-action="close-device-selection">Abbrechen</button>
                        </div>
                    </div>
                </aside>
                """;
    }
}
