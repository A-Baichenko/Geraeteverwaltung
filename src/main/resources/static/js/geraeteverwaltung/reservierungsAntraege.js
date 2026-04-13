const defaultFieldConfig = [
    { key: 'mitarbeiterName', label: 'Mitarbeiter', readonly: true },
    { key: 'geraetetypName', label: 'Gerätetyp', readonly: true },
    { key: 'ausleihdatum', label: 'Ausleihdatum', readonly: true },
    { key: 'rueckgabedatum', label: 'Rückgabedatum', readonly: true },
    { key: 'selectedDeviceLabel', label: 'Ausgewähltes Gerät', readonly: true }
];

const managerState = {
    requests: [],
    fieldConfig: defaultFieldConfig,
    layoutHtml: null,
    loading: {
        requests: false,
        action: false
    },
    error: null,
    activeRequest: null,
    activeFormDraft: null,
    openSection: 'reservationRequests',
    deviceSelection: {
        isOpen: false,
        items: [],
        selectedDevice: null,
        pendingDevice: null,
        loading: false,
        error: null
    }
};

function authHeaders(token) {
    return { Authorization: `Bearer ${token}` };
}

async function fetchManagerViewConfig(token) {
    const response = await fetch('/api/geraeteverwaltung/reservation-requests/view-config', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'View-Konfiguration konnte nicht geladen werden.');
    }

    return response.json();
}

async function fetchReservationRequests(token) {
    const response = await fetch('/api/geraeteverwaltung/reservation-requests', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'Reservierungsanträge konnten nicht geladen werden.');
    }

    return response.json();
}

async function fetchReservationRequestById(token, reservationId) {
    const response = await fetch(`/api/geraeteverwaltung/reservation-requests/${reservationId}`, {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'Reservierungsantrag konnte nicht geöffnet werden.');
    }

    return response.json();
}

async function fetchAvailableDevices(token, reservationId) {
    const response = await fetch(`/api/geraeteverwaltung/reservation-requests/${reservationId}/available-devices`, {
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ({}));
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

    const data = await response.json().catch(() => ({}));
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
        const data = await response.json().catch(() => ({}));
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

function ensureLayout(root) {
    if (!root || root.dataset.layoutInjected === 'true') {
        return;
    }

    if (!managerState.layoutHtml) {
        root.innerHTML = '<div class="placeholder">View-Konfiguration konnte nicht geladen werden.</div>';
        return;
    }

    root.innerHTML = managerState.layoutHtml;
    root.dataset.layoutInjected = 'true';
}

function renderManagerView(root) {
    ensureLayout(root);
    if (!root || root.dataset.layoutInjected !== 'true') {
        return;
    }

    root.querySelectorAll('.gv-accordion-section').forEach((section) => {
        section.classList.toggle('is-open', section.dataset.section === managerState.openSection);
    });

    const globalError = root.querySelector('#gv-global-error');
    if (globalError) {
        globalError.textContent = managerState.error || '';
        globalError.style.display = managerState.error ? 'block' : 'none';
    }

    const requestList = root.querySelector('#gv-request-list');
    if (requestList) {
        if (managerState.loading.requests) {
            requestList.innerHTML = '<li class="empty-list-item">Reservierungsanträge werden geladen …</li>';
        } else if (!managerState.requests.length) {
            requestList.innerHTML = '<li class="empty-list-item">Keine offenen Reservierungsanträge vorhanden.</li>';
        } else {
            requestList.innerHTML = managerState.requests.map((request) => `
                <li class="gv-item">
                    <button type="button" data-action="open-request" data-request-id="${request.reservierungsNr}">
                        <strong>${request.mitarbeiterName}</strong>
                        <span>${request.geraetetypName}</span>
                        <small>${request.ausleihdatum} bis ${request.rueckgabedatum}</small>
                        <small>Verfügbar: ${request.availableCount}</small>
                    </button>
                </li>
            `).join('');
        }
    }

    const detailsPanel = root.querySelector('#gv-details-panel');
    const fields = root.querySelector('#gv-fields');
    const detailsPlaceholder = root.querySelector('#gv-details-placeholder');
    const actionError = root.querySelector('#gv-action-error');

    const selectedDevice = managerState.deviceSelection.selectedDevice;

    if (!managerState.activeRequest) {
        if (fields) {
            fields.innerHTML = '';
        }
        if (detailsPlaceholder) {
            detailsPlaceholder.style.display = 'block';
        }
        if (detailsPanel) {
            detailsPanel.classList.remove('is-open');
        }
    } else {
        if (detailsPlaceholder) {
            detailsPlaceholder.style.display = 'none';
        }
        if (detailsPanel) {
            detailsPanel.classList.add('is-open');
        }
        if (fields) {
            fields.innerHTML = managerState.fieldConfig.map((field) => `
                <div class="gv-field">
                    <label>${field.label}</label>
                    <input type="text" value="${mapFieldValue(field.key, managerState.activeFormDraft, selectedDevice)}" readonly />
                </div>
            `).join('');
        }
    }

    if (actionError) {
        actionError.textContent = managerState.deviceSelection.error || '';
        actionError.style.display = managerState.deviceSelection.error ? 'block' : 'none';
    }

    const actionDisabled = !managerState.activeRequest || managerState.loading.action;

    const openDeviceButton = root.querySelector('[data-action="open-device-selection"]');
    const editButton = root.querySelector('[data-action="edit-request"]');
    const deleteButton = root.querySelector('[data-action="delete-request"]');
    const acceptButton = root.querySelector('[data-action="accept-request"]');

    if (openDeviceButton) {
        openDeviceButton.disabled = actionDisabled;
    }
    if (editButton) {
        editButton.disabled = actionDisabled;
    }
    if (deleteButton) {
        deleteButton.disabled = actionDisabled;
    }
    if (acceptButton) {
        acceptButton.disabled = actionDisabled || !selectedDevice;
    }

    const modal = root.querySelector('#gv-device-modal');
    const modalList = root.querySelector('#gv-device-list');
    const deviceTypeName = root.querySelector('#gv-device-type-name');

    if (deviceTypeName) {
        deviceTypeName.textContent = managerState.activeRequest?.geraetetypName || '';
    }

    if (modal) {
        modal.classList.toggle('is-open', managerState.deviceSelection.isOpen);
    }

    if (modalList) {
        if (managerState.deviceSelection.loading) {
            modalList.innerHTML = '<li class="empty-list-item">Geräte werden geladen …</li>';
        } else if (!managerState.deviceSelection.items.length) {
            modalList.innerHTML = '<li class="empty-list-item">Keine verfügbaren Geräte gefunden.</li>';
        } else {
            modalList.innerHTML = managerState.deviceSelection.items.map((device) => {
                const checked = String(managerState.deviceSelection.pendingDevice?.inventarNr) === String(device.inventarNr)
                    ? 'checked'
                    : '';

                return `
                    <li>
                        <label class="device-option">
                            <input
                                type="radio"
                                name="selectedDevice"
                                data-action="choose-device-radio"
                                data-inventar-nr="${device.inventarNr}"
                                data-device-label="${device.label}"
                                ${checked}
                            />
                            <span>${device.label}</span>
                        </label>
                    </li>
                `;
            }).join('');
        }
    }
}

async function initializeManagerFlow(token, getState, pageContent) {
    const tabState = getState();
    if (tabState.activeTabKey !== 'geraeteverwaltung') {
        return;
    }

    const root = pageContent.querySelector('#gv-manager-app');
    if (!root || root.dataset.initialized === 'true') {
        return;
    }

    try {
        const viewConfig = await fetchManagerViewConfig(token);
        managerState.fieldConfig = viewConfig.fieldConfig || defaultFieldConfig;
        managerState.layoutHtml = viewConfig.layoutHtml;
        managerState.error = null;
    } catch (error) {
        managerState.error = error.message;
    }

    managerState.loading.requests = true;
    renderManagerView(root);

    try {
        managerState.requests = await fetchReservationRequests(token);
    } catch (error) {
        managerState.error = error.message;
        managerState.requests = [];
    } finally {
        managerState.loading.requests = false;
    }

    renderManagerView(root);
    root.dataset.initialized = 'true';
}

export function registerGeraeteverwaltungHandlers({ pageContent, getToken, redirectToLogin, getState }) {
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
            if (action === 'toggle-section') {
                const sectionKey = target.dataset.sectionKey;
                managerState.openSection = managerState.openSection === sectionKey ? '' : sectionKey;
            }

            if (action === 'open-request') {
                managerState.openSection = 'reservationRequests';
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
                managerState.deviceSelection.pendingDevice = managerState.deviceSelection.selectedDevice;
                renderManagerView(root);

                const devicePayload = await fetchAvailableDevices(token, managerState.activeRequest.reservierungsNr);
                managerState.deviceSelection.items = devicePayload.devices || [];
            }

            if (action === 'choose-device-radio') {
                managerState.deviceSelection.pendingDevice = {
                    inventarNr: Number(target.dataset.inventarNr),
                    label: target.dataset.deviceLabel
                };
            }

            if (action === 'confirm-device-selection') {
                if (managerState.deviceSelection.pendingDevice) {
                    managerState.deviceSelection.selectedDevice = managerState.deviceSelection.pendingDevice;
                }
                managerState.deviceSelection.isOpen = false;
            }

            if (action === 'close-device-selection') {
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
                    pendingDevice: null,
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
                managerState.deviceSelection.error = 'Bearbeiten folgt im nächsten Schritt.';
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

    observer.observe(pageContent, { childList: true, subtree: true });
}