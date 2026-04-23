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
                                                   type="text"
                                                   placeholder="z. B. 1001">
                                        </label>

                                        <label class="ga-field">
                                            <span>Serien-Nr.:</span>
                                            <input id="ga-create-serial-number"
                                                   name="serialNumber"
                                                   type="text"
                                                   placeholder="z. B. 555123">
                                        </label>

                                        <label class="ga-field">
                                            <span>Kaufdatum:</span>
                                            <input id="ga-create-purchase-date"
                                                   name="purchaseDate"
                                                   type="date"
                                                   placeholder="tt.mm.jjjj">
                                        </label>

                                        <label class="ga-field">
                                            <span>Gerätetyp:</span>
                                            <div class="ga-picker-field">
                                                <input id="ga-create-device-type"
                                                       name="deviceType"
                                                       type="text"
                                                       placeholder="Gerätetyp auswählen"
                                                       readonly>
                                                <button type="button"
                                                        class="ga-icon-button"
                                                        data-action="open-create-search"
                                                        data-search-target="deviceType"
                                                        aria-label="Gerätetyp auswählen">⌕</button>
                                            </div>
                                        </label>

                                        <label class="ga-field ga-checkbox-field">
                                            <span>Ausleihbar:</span>
                                            <input id="ga-create-lendable"
                                                   name="lendable"
                                                   type="checkbox">
                                        </label>

                                        <label class="ga-field">
                                            <span>Mitarbeiter (optional):</span>
                                            <div class="ga-picker-field">
                                                <input id="ga-create-employee"
                                                       name="employee"
                                                       type="text"
                                                       placeholder="Optional: Mitarbeiter auswählen"
                                                       readonly>
                                                <button type="button"
                                                        class="ga-icon-button"
                                                        data-action="open-create-search"
                                                        data-search-target="employee"
                                                        aria-label="Mitarbeiter auswählen">⌕</button>
                                            </div>
                                        </label>

                                        <label class="ga-field">
                                            <span>Raum (optional):</span>
                                            <div class="ga-picker-field">
                                                <input id="ga-create-room"
                                                       name="room"
                                                       type="text"
                                                       placeholder="Optional: Raum auswählen"
                                                       readonly>
                                                <button type="button"
                                                        class="ga-icon-button"
                                                        data-action="open-create-search"
                                                        data-search-target="room"
                                                        aria-label="Raum auswählen">⌕</button>
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

                    <section class="fz-accordion-section" data-section="assignFixed">
                                  <button type="button" class="fz-accordion-trigger" data-action="toggle-section" data-section-key="assignFixed">
                                      <span>Fest zuordnen</span><span class="fz-chevron">▾</span>
                                  </button>
                                  <div class="fz-accordion-content">
                                      <p id="fz-global-message" class="fz-error-text"></p>
                
                                      <div class="fz-grid">
                                          <section>
                                              <div class="fz-toolbar">
                                                  <div class="fz-search-box">
                                                      <input id="fz-search-input"
                                                             name="fixedAssignmentSearch"
                                                             type="text"
                                                             placeholder="Suche">
                                                  </div>
                                                  <button type="button" class="fz-add-button" data-action="open-fixed-editor">＋</button>
                                              </div>
                
                                              <h3>Liste fest zugeordneter Geräte</h3>
                                              <ul id="fz-assignment-list" class="fz-list"></ul>
                                              <p id="fz-list-placeholder" class="fz-placeholder">Noch keine festen Zuordnungen vorhanden.</p>
                                          </section>
                
                                          <section id="fz-editor-panel" class="fz-slide-panel">
                                              <h3>Zuordnung</h3>
                
                                              <div class="fz-fields">
                                                  <label class="fz-field">
                                                      <span>Inv Nr:</span>
                                                      <input id="fz-inventar-nr"
                                                             name="inventarNr"
                                                             type="number"
                                                             placeholder="z. B. 1001">
                                                  </label>
                
                                                  <label class="fz-field">
                                                      <span>Mitarbeiter:</span>
                                                      <input id="fz-mitarbeiter-nr"
                                                             name="mitarbeiterNr"
                                                             type="number"
                                                             placeholder="Personal-Nr.">
                                                  </label>
                
                                                  <label class="fz-field">
                                                      <span>Raum:</span>
                                                      <input id="fz-raum-nr"
                                                             name="raumNr"
                                                             type="number"
                                                             placeholder="Raum-Nr.">
                                                  </label>
                                              </div>
                
                                              <div class="fz-actions">
                                                  <button type="button" data-action="save-fixed-assignment">OK</button>
                                                  <button type="button" class="fz-secondary-button" data-action="clear-fixed-assignment">Zuordnung aufheben</button>
                                                  <button type="button" class="fz-secondary-button" data-action="close-fixed-editor">Abbrechen</button>
                                              </div>
                                          </section>
                                      </div>
                                  </div>
                              </section>

                    <section class="al-accordion-section" data-section="lend">
                                  <button type="button" class="al-accordion-trigger" data-action="toggle-section" data-section-key="lend">
                                      <span>Ausleihe</span><span class="al-chevron">▾</span>
                                  </button>
                                  <div class="al-accordion-content">
                                      <p id="al-global-message" class="al-error-text"></p>
                
                                      <div class="al-grid">
                                          <section>
                                              <h3>Ausleihen</h3>
                
                                              <div class="al-fields">
                                                  <label class="al-field">
                                                      <span>Ausleihdatum:</span>
                                                      <input id="al-lend-date"
                                                             name="lendDate"
                                                             type="date">
                                                  </label>
                
                                                  <label class="al-field">
                                                      <span>Rückgabedatum:</span>
                                                      <div class="al-picker-field">
                                                          <input id="al-return-date"
                                                                 name="returnDate"
                                                                 type="date">
                                                      </div>
                                                  </label>
                
                                                  <label class="al-field">
                                                      <span>Gerät:</span>
                                                      <div class="al-picker-field">
                                                          <input id="al-device-type"
                                                                 name="deviceType"
                                                                 type="text"
                                                                 placeholder="Gerät auswählen"
                                                                 readonly>
                                                          <button type="button"
                                                                  class="al-icon-button"
                                                                  data-action="open-lend-search"
                                                                  data-search-target="deviceType"
                                                                  aria-label="Gerät auswählen">⌕</button>
                                                      </div>
                                                  </label>
                
                                                  <label class="al-field">
                                                      <span>Mitarbeiter:</span>
                                                      <div class="al-picker-field">
                                                          <input id="al-employee"
                                                                 name="employee"
                                                                 type="text"
                                                                 placeholder="Mitarbeiter auswählen"
                                                                 readonly>
                                                          <button type="button"
                                                                  class="al-icon-button"
                                                                  data-action="open-lend-search"
                                                                  data-search-target="employee"
                                                                  aria-label="Mitarbeiter auswählen">⌕</button>
                                                      </div>
                                                  </label>
                                              </div>
                
                                              <div class="al-actions">
                                                  <button type="button" data-action="create-lend">OK</button>
                                                  <button type="button" class="al-secondary-button" data-action="reset-lend">Zurücksetzen</button>
                                              </div>
                                          </section>
                
                                          <section id="al-search-panel" class="al-slide-panel">
                                              <div class="al-panel-header">
                                                  <h3>Suche</h3>
                                                  <button type="button"
                                                          class="al-icon-button"
                                                          data-action="close-lend-search">✕</button>
                                              </div>
                
                                              <p id="al-search-title" class="al-section-subtitle">Suche</p>
                
                                              <div class="al-search-box">
                                                  <input id="al-search-input"
                                                         name="lendSearch"
                                                         type="text"
                                                         placeholder="Filter eingeben">
                                              </div>
                
                                              <ul id="al-search-results" class="al-list"></ul>
                                              <p id="al-search-error" class="al-error-text"></p>
                                              <p id="al-search-placeholder" class="al-placeholder">Noch keine Einträge vorhanden.</p>
                                          </section>
                                      </div>
                                  </div>
                              </section>

                    <section class="ao-accordion-section" data-section="lendOverview">
                                            <button type="button" class="ao-accordion-trigger" data-action="toggle-section" data-section-key="lendOverview">
                                                <span>Ausleihe Übersicht</span><span class="ao-chevron">▾</span>
                                            </button>
                                            <div class="ao-accordion-content">
                                                <p id="ao-global-message" class="ao-error-text"></p>
                
                                                <div class="ao-grid">
                                                    <section>
                                                        <div class="ao-search-box">
                                                            <input id="ao-search-input"
                                                                   name="lendOverviewSearch"
                                                                   type="text"
                                                                   placeholder="Mitarbeiter suchen">
                                                                   
                                                                   <select id="ao-return-filter" name="lendOverviewReturnFilter">
                                                                        <option value="all">Alle</option>
                                                                        <option value="open">Noch nicht zurückgegeben</option>
                                                                        <option value="done">Schon zurückgegeben</option>
                                                                   </select>
                                                            <button type="button" data-action="clear-overview-search">✕</button>
                                                        </div>
                
                                                        <ul id="ao-overview-list" class="ao-list"></ul>
                                                        <p id="ao-overview-placeholder" class="ao-placeholder">Noch keine Ausleihen vorhanden.</p>
                                                        <div id="ao-pagination" class="ao-pagination"></div>
                                                    </section>
                                                </div>
                                            </div>
                                        </section>

                    <section class="gv-accordion-section" data-section="deviceManagement">
                        <button type="button" class="gv-accordion-trigger" data-action="toggle-section" data-section-key="deviceManagement">
                            <span>Geräteverwaltung</span><span class="gv-chevron">▾</span>
                        </button>
                        <div class="gv-accordion-content">
                                                    <p id="dm-global-error" class="error-text"></p>
                                                    <div class="dm-toolbar">
                                                        <label class="dm-toolbar-field">
                                                            <span>Suche</span>
                                                            <input id="dm-search-input" type="search" placeholder="Nach Gerätetyp oder Gerät suchen">
                                                        </label>
                                                        <label class="dm-toolbar-field">
                                                            <span>Status</span>
                                                            <select id="dm-status-filter">
                                                                <option value="all">Alle</option>
                                                                <option value="verfuegbar">Verfügbar</option>
                                                                <option value="reserviert">Reserviert</option>
                                                                <option value="ausgeliehen">Ausgeliehen</option>
                                                                <option value="fest_zugeordnet">Fest zugeordnet</option>
                                                                <option value="wartung_defekt">In Wartung / Defekt</option>
                                                            </select>
                                                        </label>
                                                    </div>
                                                    <div id="dm-tree" class="dm-tree"></div>
                                                </div>
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
                
                <aside id="dm-device-modal" class="gv-device-modal">
                                    <div class="gv-device-modal-card">
                                        <div class="gv-device-modal-header">
                                            <h3 id="dm-modal-title">Gerät bearbeiten</h3>
                                            <button type="button" class="icon-button" data-action="dm-close-modal">✕</button>
                                        </div>
                                        <p id="dm-modal-error" class="error-text"></p>
                                        <div id="dm-edit-form" class="gv-fields">
                                            <label class="gv-field">
                                                <span>Seriennummer</span>
                                                <input id="dm-edit-serial-number" type="number" min="1">
                                            </label>
                                            <label class="gv-field">
                                                <span>Kaufdatum</span>
                                                <input id="dm-edit-purchase-date" type="date">
                                            </label>
                                            <label class="gv-field ga-checkbox-field">
                                                <span>Ausleihbar</span>
                                                <input id="dm-edit-lendable" type="checkbox">
                                            </label>
                                        </div>
                                        <div id="dm-move-form" class="gv-fields">
                                            <label class="gv-field">
                                                <span>Gerätetyp</span>
                                                <select id="dm-move-device-type"></select>
                                            </label>
                                            <label class="gv-field">
                                                <span>Mitarbeiter</span>
                                                <select id="dm-move-employee"></select>
                                            </label>
                                            <label class="gv-field">
                                                <span>Raum</span>
                                                <select id="dm-move-room"></select>
                                            </label>
                                        </div>
                                        <div class="gv-device-modal-actions">
                                            <button type="button" data-action="dm-save-modal">Speichern</button>
                                            <button type="button" data-action="dm-close-modal">Abbrechen</button>
                                        </div>
                                    </div>
                                </aside>
                """;
    }
}