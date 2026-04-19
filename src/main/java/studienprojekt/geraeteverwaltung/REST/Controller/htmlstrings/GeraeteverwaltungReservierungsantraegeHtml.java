package studienprojekt.geraeteverwaltung.REST.Controller.htmlstrings;

public final class GeraeteverwaltungReservierungsantraegeHtml {

    private GeraeteverwaltungReservierungsantraegeHtml() {
    }

    public static String content() {
        return """
                <div class="gv-accordion-layout">
                    <section class="ga-accordion-section" data-section="createDevices">
                        <button type="button" class="ga-accordion-trigger" data-action="toggle-section" data-section-key="createDevices">
                            <span>Geräteanlegen</span><span class="ga-chevron">▾</span>
                        </button>
                        <div class="ga-accordion-content">
                            <p id="ga-create-device-error" class="ga-error-text"></p>

                            <div class="ga-grid">
                                <section>
                                    <h3>Geräte anlegen</h3>

                                    <div class="ga-fields">
                                        <label class="ga-field">
                                            <span>Inventar-Nr.:</span>
                                            <input id="ga-create-inventory-number"
                                                   name="inventoryNumber"
                                                   type="text">
                                                   placeholder="Filter eingeben">
                                        </label>

                                        <label class="ga-field">
                                            <span>Serien-Nr.:</span>
                                            <input id="ga-create-serial-number"
                                                   name="serialNumber"
                                                   type="text">
                                        </label>

                                        <label class="ga-field">
                                            <span>Kaufdatum:</span>
                                            <input id="ga-create-purchase-date"
                                                   name="purchaseDate"
                                                   type="date">
                                        </label>

                                        <label class="ga-field">
                                            <span>Gerätetyp:</span>
                                            <div class="ga-picker-field">
                                                <input id="ga-create-device-type"
                                                       name="deviceType"
                                                       type="text"
                                                       readonly>
                                                <button type="button"
                                                        class="ga-icon-button"
                                                        data-action="open-create-search"
                                                        data-search-target="deviceType"
                                                        aria-label="Gerätetyp suchen">⌕</button>
                                            </div>
                                        </label>

                                        <label class="ga-field ga-checkbox-field">
                                            <span>Ausleihbar:</span>
                                            <input id="ga-create-lendable"
                                                   name="lendable"
                                                   type="checkbox">
                                        </label>

                                        <label class="ga-field">
                                            <span>Mitarbeiter:</span>
                                            <div class="ga-picker-field">
                                                <input id="ga-create-employee"
                                                       name="employee"
                                                       type="text"
                                                       readonly>
                                                <button type="button"
                                                        class="ga-icon-button"
                                                        data-action="open-create-search"
                                                        data-search-target="employee"
                                                        aria-label="Mitarbeiter suchen">⌕</button>
                                            </div>
                                        </label>

                                        <label class="ga-field">
                                            <span>Raum:</span>
                                            <div class="ga-picker-field">
                                                <input id="ga-create-room"
                                                       name="room"
                                                       type="text"
                                                       readonly>
                                                <button type="button"
                                                        class="ga-icon-button"
                                                        data-action="open-create-search"
                                                        data-search-target="room"
                                                        aria-label="Raum suchen">⌕</button>
                                            </div>
                                        </label>
                                    </div>

                                    <div class="ga-actions">
                                        <button type="button" data-action="create-device">Speichern</button>
                                        <button type="button" class="ga-secondary-button" data-action="reset-create-device">Zurücksetzen</button>
                                    </div>
                                </section>

                                <section id="ga-create-search-panel" class="ga-slide-panel">
                                              <div class="ga-panel-header">
                                                  <h3>Suche</h3>
                                                  <button type="button"
                                                          class="ga-icon-button"
                                                          data-action="close-create-search">✕</button>
                                              </div>
                
                                              <p id="ga-create-search-title" class="ga-section-subtitle">Suche</p>
                
                                              <div class="ga-search-box">
                                                  <input id="ga-create-search-input"
                                                         name="createSearch"
                                                         type="text"
                                                         placeholder="Filter eingeben">
                                              </div>
                
                                              <ul id="ga-create-search-results" class="ga-list"></ul>
                                              <p id="ga-create-search-error" class="ga-error-text"></p>
                                              <p id="ga-create-search-placeholder" class="ga-placeholder">Noch keine Einträge vorhanden.</p>
                                          </section>
                            </div>
                        </div>
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