const managerState = {
    roleContext: null,
    moduleConfig: null,
    initializing: false,
    requests: [],
    loading: {
        config: false,
        requests: false,
        action: false,
        devices: false
    },
    error: null,
    activeRequest: null,
    activeFormDraft: null,
    deviceSelection: {
        isOpen: false,
        items: [],
        selectedDevice: null,
        loading: false,
        error: null
    }
};

function authHeaders(token) {
    return {
        Authorization: `Bearer ${token}`
    };
}

async function fetchManagerViewConfig(token) {
    const response = await fetch('/api/geraeteverwaltung/reservation-requests/view-config', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        throw new Error('View-Konfiguration konnte nicht geladen werden.');
    }

    return response.json();
}

async function fetchReservationRequests(token) {
    const response = await fetch('/api/geraeteverwaltung/reservation-requests', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        throw new Error('Reservierungsanträge konnten nicht geladen werden.');
    }

    return response.json();
}

async function fetchReservationRequestById(token, reservationId) {
    const response = await fetch(`/api/geraeteverwaltung/reservation-requests/${reservationId}`, {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        throw new Error('Reservierungsantrag konnte nicht geöffnet werden.');
    }

    return response.json();
}

async function fetchAvailableDevices(token, reservationId) {
    const response = await fetch(`/api/geraeteverwaltung/reservation-requests/${reservationId}/available-devices`, {
        headers: authHeaders(token)
    });

    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || 'Verfügbare Geräte konnten nicht geladen werden.');
    }

    return data;
}

async function acceptReservationRequest(token, reservationId, inventarNr) {
    const response = await fetch(`/api/geraeteverwaltung/reservation-requests/${reservationId}/accept`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify({ inventarNr })
    });

    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || 'Antrag konnte nicht angenommen werden.');
    }

    return data;
}

async function deleteReservationRequest(token, reservationId) {
    const response = await fetch(`/api/geraeteverwaltung/reservation-requests/${reservationId}`, {
        method: 'DELETE',
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || 'Antrag konnte nicht gelöscht werden.');
    }
}

function mapFieldValue(fieldKey, request, selectedDevice) {
    if (!request) {
        return '—';
    }

    const map = {
        mitarbeiterName: request.mitarbeiterName,
        geraetetypName: request.geraetetypName,
        ausleihdatum: request.ausleihdatum,
        rueckgabedatum: request.rueckgabedatum,
        selectedDeviceLabel: selectedDevice?.label || 'Noch kein Gerät gewählt'
    };

    return map[fieldKey] ?? '—';
}

function isGuardedForCurrentRole(config, tabState) {
    const activeRole = config?.roleContext?.activeRole;
    const activeModule = config?.roleContext?.module;
    const activeSubArea = config?.roleContext?.subArea;

    return tabState.activeTabKey === 'geraeteverwaltung'
        && activeRole === 'GERAETE_VERWALTER'
        && activeModule === 'geraeteverwalten'
        && activeSubArea === 'reservierungsantraege';
}

function renderManagerView(root) {
    if (!root) {
        return;
    }

    if (managerState.loading.config || managerState.loading.requests) {
        root.innerHTML = '<div class="placeholder">Reservierungsanträge werden geladen …</div>';
        return;
    }

    if (managerState.error) {
        root.innerHTML = `<div class="placeholder">${managerState.error}</div>`;
        return;
    }

    if (!managerState.moduleConfig) {
        root.innerHTML = '<div class="placeholder">Keine Konfiguration für die aktuelle Rolle verfügbar.</div>';
        return;
    }

    const requestListHtml = managerState.requests.length
        ? managerState.requests.map((request) => `
            <li class="gv-item">
                <button type="button" data-action="open-request" data-request-id="${request.reservierungsNr}">
                    <strong>${request.mitarbeiterName}</strong>
                    <span>${request.geraetetypName}</span>
                    <small>${request.ausleihdatum} bis ${request.rueckgabedatum}</small>
                    <small>Verfügbar: ${request.availableCount}</small>
                </button>
            </li>
        `).join('')
        : '<li class="empty-list-item">Keine offenen Reservierungsanträge vorhanden.</li>';

    const detailsOpenClass = managerState.activeRequest ? 'is-open' : '';
    const selectedDevice = managerState.deviceSelection.selectedDevice;

    const fieldConfig = managerState.moduleConfig.fieldConfig || [];
    const fieldsHtml = fieldConfig.map((field) => `
        <div class="gv-field">
            <label>${field.label}</label>
            <input type="text" value="${mapFieldValue(field.key, managerState.activeFormDraft, selectedDevice)}" ${field.readonly ? 'readonly' : ''} />
        </div>
    `).join('');

    const actionDisabled = !managerState.activeRequest || managerState.loading.action;
    const acceptDisabled = actionDisabled || !selectedDevice;

    const deviceOverlayClass = managerState.deviceSelection.isOpen ? 'is-open' : '';
    const deviceItems = managerState.deviceSelection.items || [];

    const deviceListHtml = managerState.deviceSelection.loading
        ? '<li class="empty-list-item">Geräte werden geladen …</li>'
        : deviceItems.length
            ? deviceItems.map((device) => `
                <li>
                    <button type="button" class="list-item-button" data-action="select-device" data-inventar-nr="${device.inventarNr}" data-device-label="${device.label}">
                        <strong>${device.label}</strong>
                    </button>
                </li>
            `).join('')
            : '<li class="empty-list-item">Keine verfügbaren Geräte gefunden.</li>';

    root.innerHTML = `
        <div class="gv-grid">
            <section>
                <h3>Reservierungsanträge</h3>
                <ul class="gv-list">${requestListHtml}</ul>
            </section>

            <section class="gv-slide-panel ${detailsOpenClass}">
                <h3>Antrag</h3>
                ${managerState.activeRequest ? `
                    <div class="gv-fields">${fieldsHtml}</div>
                    <div class="gv-actions">
                        <button type="button" data-action="open-device-selection" ${actionDisabled ? 'disabled' : ''}>Gerät auswählen</button>
                        <button type="button" data-action="edit-request" ${actionDisabled ? 'disabled' : ''}>Bearbeiten</button>
                        <button type="button" class="danger-button" data-action="delete-request" ${actionDisabled ? 'disabled' : ''}>Löschen</button>
                        <button type="button" data-action="accept-request" ${acceptDisabled ? 'disabled' : ''}>Annehmen</button>
                    </div>
                    ${managerState.deviceSelection.error ? `<p class="error-text">${managerState.deviceSelection.error}</p>` : ''}
                ` : '<p class="placeholder">Bitte einen Reservierungsantrag öffnen.</p>'}
            </section>
        </div>

        <aside class="gv-device-overlay ${deviceOverlayClass}">
            <div class="gv-device-overlay-header">
                <button type="button" class="back-button" data-action="back-to-form">← Zurück</button>
                <h3>Verfügbare Geräte</h3>
            </div>
            <p>${managerState.activeRequest ? managerState.activeRequest.geraetetypName : ''}</p>
            ${managerState.deviceSelection.error ? `<p class="error-text">${managerState.deviceSelection.error}</p>` : ''}
            <ul class="gv-device-list">${deviceListHtml}</ul>
        </aside>
    `;
}

async function initializeManagerFlow(token, getState, pageContent) {
    const tabState = getState();
    if (tabState.activeTabKey !== 'geraeteverwaltung') {
        return;
    }

    const root = pageContent.querySelector('#gv-manager-app');
    if (!root || root.dataset.initialized === 'true' || managerState.initializing) {
        return;
    }

    managerState.initializing = true;
    managerState.loading.config = true;
    managerState.loading.requests = true;
    managerState.error = null;
    renderManagerView(root);

    try {
        const config = await fetchManagerViewConfig(token);
        managerState.roleContext = config.roleContext;
        managerState.moduleConfig = config;

        if (!isGuardedForCurrentRole(config, tabState)) {
            root.innerHTML = '<div class="placeholder">Dieser Bereich ist nur für Geräteverwalter im Modul Reservierungsanträge verfügbar.</div>';
            root.dataset.initialized = 'true';
            return;
        }

        managerState.requests = await fetchReservationRequests(token);
    } catch (error) {
        managerState.error = error.message;
    } finally {
        managerState.loading.config = false;
        managerState.loading.requests = false;
        managerState.initializing = false;
    }

    renderManagerView(root);
    root.dataset.initialized = 'true';
}

export function registerGeraeteverwaltungHandlers({
                                                      pageContent,
                                                      getToken,
                                                      redirectToLogin,
                                                      getState
                                                  }) {
    pageContent.addEventListener('click', async (event) => {
        const target = event.target.closest('[data-action]');
        if (!target) {
            return;
        }

        const token = getToken();
        if (!token) {
            redirectToLogin();
            return;
        }

        const root = pageContent.querySelector('#gv-manager-app');
        if (!root) {
            return;
        }

        const action = target.dataset.action;

        try {
            if (action === 'open-request') {
                managerState.deviceSelection.error = null;
                managerState.deviceSelection.selectedDevice = null;
                managerState.activeRequest = await fetchReservationRequestById(token, Number(target.dataset.requestId));
                managerState.activeFormDraft = { ...managerState.activeRequest };
            }

            if (action === 'open-device-selection') {
                if (!managerState.activeRequest) {
                    return;
                }

                managerState.deviceSelection.loading = true;
                managerState.deviceSelection.isOpen = true;
                managerState.deviceSelection.error = null;
                renderManagerView(root);

                const devicePayload = await fetchAvailableDevices(token, managerState.activeRequest.reservierungsNr);
                managerState.deviceSelection.items = devicePayload.devices || [];
            }

            if (action === 'select-device') {
                managerState.deviceSelection.selectedDevice = {
                    inventarNr: Number(target.dataset.inventarNr),
                    label: target.dataset.deviceLabel
                };
                managerState.deviceSelection.isOpen = false;
                managerState.deviceSelection.error = null;
            }

            if (action === 'back-to-form') {
                managerState.deviceSelection.isOpen = false;
                managerState.deviceSelection.error = null;
            }

            if (action === 'accept-request') {
                if (!managerState.activeRequest || !managerState.deviceSelection.selectedDevice) {
                    return;
                }

                managerState.loading.action = true;
                renderManagerView(root);

                await acceptReservationRequest(
                    token,
                    managerState.activeRequest.reservierungsNr,
                    managerState.deviceSelection.selectedDevice.inventarNr
                );

                managerState.requests = await fetchReservationRequests(token);
                managerState.activeRequest = null;
                managerState.activeFormDraft = null;
                managerState.deviceSelection = {
                    isOpen: false,
                    items: [],
                    selectedDevice: null,
                    loading: false,
                    error: null
                };
            }

            if (action === 'delete-request') {
                if (!managerState.activeRequest) {
                    return;
                }

                await deleteReservationRequest(token, managerState.activeRequest.reservierungsNr);
                managerState.requests = await fetchReservationRequests(token);
                managerState.activeRequest = null;
                managerState.activeFormDraft = null;
                managerState.deviceSelection.selectedDevice = null;
            }

            if (action === 'edit-request') {
                managerState.deviceSelection.error = 'Bearbeiten ist als Handler vorbereitet, die Editor-Ansicht folgt im nächsten Schritt.';
            }
        } catch (error) {
            managerState.deviceSelection.error = error.message;
        } finally {
            managerState.loading.action = false;
            managerState.deviceSelection.loading = false;
            renderManagerView(root);
        }
    });

    const observer = new MutationObserver(async () => {
        const token = getToken();
        if (!token) {
            return;
        }
        await initializeManagerFlow(token, getState, pageContent);
    });

    observer.observe(pageContent, { childList: true });
}