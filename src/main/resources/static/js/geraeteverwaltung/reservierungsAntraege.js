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
    isEditing: false,
    openSections: new Set(['reservationRequests']),
    deviceSelection: {
        isOpen: false,
        items: [],
        selectedDevice: null,
        pendingDevice: null,
        loading: false,
        error: null
    },
    deviceManagement: {
        hierarchyHtml: '',
        loading: false,
        error: null,
        search: '',
        statusFilter: 'all',
        moveOptions: null,
        modal: {
            isOpen: false,
            mode: null,
            device: null,
            error: null
        }
    }
};

const managerAccordionSections = new Set(['reservationRequests', 'assignFixed', 'lendOverview', 'deviceManagement']);

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

async function updateReservationRequest(token, reservationId, payload) {
    const response = await fetch(`/api/geraeteverwaltung/reservation-requests/${reservationId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(payload)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Antrag konnte nicht bearbeitet werden.');
    }

    return data;
}

async function fetchDeviceHierarchyHtml(token, query = '', status = 'all') {
    const response = await fetch(`/api/geraeteverwaltung/device-management/hierarchy-html?query=${encodeURIComponent(query)}&status=${encodeURIComponent(status)}`, {
        headers: authHeaders(token)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Gerätehierarchie konnte nicht geladen werden.');
    }
    return data;
}

async function fetchMoveOptions(token) {
    const response = await fetch('/api/geraeteverwaltung/device-management/move-options', {
        headers: authHeaders(token)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Optionen konnten nicht geladen werden.');
    }
    return data;
}

async function editDevice(token, inventarNr, payload) {
    const response = await fetch(`/api/geraeteverwaltung/device-management/devices/${inventarNr}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(payload)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Gerät konnte nicht bearbeitet werden.');
    }
    return data;
}

async function moveDevice(token, inventarNr, payload) {
    const response = await fetch(`/api/geraeteverwaltung/device-management/devices/${inventarNr}/move`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(payload)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Gerät konnte nicht verschoben werden.');
    }
    return data;
}

async function deleteDevice(token, inventarNr) {
    const response = await fetch(`/api/geraeteverwaltung/device-management/devices/${inventarNr}`, {
        method: 'DELETE',
        headers: authHeaders(token)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Gerät konnte nicht gelöscht werden.');
    }
    return data;
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
        section.classList.toggle('is-open', managerState.openSections.has(section.dataset.section));
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
                    <input
                        type="${field.key === 'ausleihdatum' || field.key === 'rueckgabedatum' ? 'date' : 'text'}"
                        value="${mapFieldValue(field.key, managerState.activeFormDraft, selectedDevice)}"
                        data-field-key="${field.key}"
                        ${field.readonly || !managerState.isEditing ? 'readonly' : ''}
                    />
                </div>
            `).join('');
        }
    }

    if (actionError) {
        actionError.textContent = managerState.deviceSelection.error || '';
        actionError.style.display = managerState.deviceSelection.error ? 'block' : 'none';
    }

    const actionDisabled = !managerState.activeRequest || managerState.loading.action;
    const actionDisabledInEditMode = actionDisabled || managerState.isEditing;

    const openDeviceButton = root.querySelector('[data-action="open-device-selection"]');
    const editButton = root.querySelector('[data-action="edit-request"]');
    const deleteButton = root.querySelector('[data-action="delete-request"]');
    const acceptButton = root.querySelector('[data-action="accept-request"]');

    if (openDeviceButton) {
        openDeviceButton.disabled = actionDisabledInEditMode;
    }
    if (editButton) {
        editButton.textContent = managerState.isEditing ? 'Speichern' : 'Bearbeiten';
        editButton.disabled = actionDisabled;
    }
    if (deleteButton) {
        deleteButton.disabled = actionDisabledInEditMode;
    }
    if (acceptButton) {
        acceptButton.disabled = actionDisabledInEditMode || !selectedDevice;
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
    renderDeviceManagement(root);
}

function renderDeviceManagement(root) {
    const errorNode = root.querySelector('#dm-global-error');
    if (errorNode) {
        errorNode.textContent = managerState.deviceManagement.error || '';
        errorNode.style.display = managerState.deviceManagement.error ? 'block' : 'none';
    }

    const searchInput = root.querySelector('#dm-search-input');
    if (searchInput && searchInput.value !== managerState.deviceManagement.search) {
        searchInput.value = managerState.deviceManagement.search;
    }

    const statusFilter = root.querySelector('#dm-status-filter');
    if (statusFilter && statusFilter.value !== managerState.deviceManagement.statusFilter) {
        statusFilter.value = managerState.deviceManagement.statusFilter;
    }

    const tree = root.querySelector('#dm-tree');
    if (!tree) {
        return;
    }

    if (managerState.deviceManagement.loading) {
        tree.innerHTML = '<p class="placeholder">Gerätehierarchie wird geladen …</p>';
    } else {
        tree.innerHTML = managerState.deviceManagement.hierarchyHtml || '<p class="placeholder">Keine passenden Geräte gefunden.</p>';
    }

    const modal = root.querySelector('#dm-device-modal');
    const modalError = root.querySelector('#dm-modal-error');
    const editForm = root.querySelector('#dm-edit-form');
    const moveForm = root.querySelector('#dm-move-form');
    const modalTitle = root.querySelector('#dm-modal-title');

    if (modal) {
        modal.classList.toggle('is-open', managerState.deviceManagement.modal.isOpen);
    }
    if (modalError) {
        modalError.textContent = managerState.deviceManagement.modal.error || '';
        modalError.style.display = managerState.deviceManagement.modal.error ? 'block' : 'none';
    }

    const isEdit = managerState.deviceManagement.modal.mode === 'edit';
    const isMove = managerState.deviceManagement.modal.mode === 'move';
    if (editForm) editForm.style.display = isEdit ? 'grid' : 'none';
    if (moveForm) moveForm.style.display = isMove ? 'grid' : 'none';
    if (modalTitle) modalTitle.textContent = isEdit ? 'Gerät bearbeiten' : 'Gerät verschieben';

    if (isEdit && managerState.deviceManagement.modal.device) {
        root.querySelector('#dm-edit-serial-number').value = managerState.deviceManagement.modal.device.serienNr ?? '';
        root.querySelector('#dm-edit-purchase-date').value = managerState.deviceManagement.modal.device.kaufdatum || '';
        root.querySelector('#dm-edit-lendable').checked = !!managerState.deviceManagement.modal.device.istAusleihbar;
    }

    if (isMove && managerState.deviceManagement.modal.device && managerState.deviceManagement.moveOptions) {
        const { deviceTypes = [], employees = [], rooms = [] } = managerState.deviceManagement.moveOptions;
        root.querySelector('#dm-move-device-type').innerHTML = deviceTypes.map((entry) => `
            <option value="${entry.id}" ${String(entry.id) === String(managerState.deviceManagement.modal.device.geraetetypId) ? 'selected' : ''}>${entry.label}</option>
        `).join('');
        root.querySelector('#dm-move-employee').innerHTML = `<option value="">—</option>${employees.map((entry) => `
            <option value="${entry.id}" ${String(entry.id) === String(managerState.deviceManagement.modal.device.mitarbeiterPersonalNr) ? 'selected' : ''}>${entry.label}</option>
        `).join('')}`;
        root.querySelector('#dm-move-room').innerHTML = `<option value="">—</option>${rooms.map((entry) => `
            <option value="${entry.id}" ${String(entry.id) === String(managerState.deviceManagement.modal.device.raum) ? 'selected' : ''}>${entry.label}</option>
        `).join('')}`;
    }
}

async function refreshDeviceManagementHtml(token) {
    const data = await fetchDeviceHierarchyHtml(
        token,
        managerState.deviceManagement.search,
        managerState.deviceManagement.statusFilter
    );
    managerState.deviceManagement.hierarchyHtml = data.html || '';
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
    managerState.deviceManagement.loading = true;
    renderManagerView(root);

    try {
        managerState.requests = await fetchReservationRequests(token);
        await refreshDeviceManagementHtml(token);
        managerState.deviceManagement.error = null;
    } catch (error) {
        managerState.error = error.message;
        managerState.requests = [];
        managerState.deviceManagement.error = error.message;
        managerState.deviceManagement.hierarchyHtml = '';
    } finally {
        managerState.loading.requests = false;
        managerState.deviceManagement.loading = false;
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
            if (action === 'toggle-section' && managerAccordionSections.has(target.dataset.sectionKey)) {
                const sectionKey = target.dataset.sectionKey;
                if (managerState.openSections.has(sectionKey)) {
                    managerState.openSections.delete(sectionKey);
                } else {
                    managerState.openSections.add(sectionKey);
                }
            }

            if (action === 'open-request') {
                managerState.openSections.add('reservationRequests');
                managerState.deviceSelection.error = null;
                managerState.deviceSelection.selectedDevice = null;
                managerState.isEditing = false;
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
                const preselectedInventarNr = Number(devicePayload.preselectedDeviceInventarNr);
                if (preselectedInventarNr) {
                    const matchingDevice = managerState.deviceSelection.items.find(
                        (device) => Number(device.inventarNr) === preselectedInventarNr
                    );
                    if (matchingDevice) {
                        managerState.deviceSelection.pendingDevice = {
                            inventarNr: Number(matchingDevice.inventarNr),
                            label: matchingDevice.label
                        };
                        managerState.deviceSelection.selectedDevice = {
                            inventarNr: Number(matchingDevice.inventarNr),
                            label: matchingDevice.label
                        };
                    }
                }
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
                managerState.isEditing = false;
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
                managerState.isEditing = false;
                managerState.deviceSelection.selectedDevice = null;
            }

            if (action === 'edit-request') {
                if (!managerState.activeRequest) {
                    return;
                }

                if (!managerState.isEditing) {
                    managerState.isEditing = true;
                    managerState.deviceSelection.error = null;
                    return;
                }

                managerState.loading.action = true;
                renderManagerView(root);

                await updateReservationRequest(token, managerState.activeRequest.reservierungsNr, {
                    ausleihdatum: managerState.activeFormDraft.ausleihdatum,
                    rueckgabedatum: managerState.activeFormDraft.rueckgabedatum
                });

                managerState.requests = await fetchReservationRequests(token);
                managerState.activeRequest = await fetchReservationRequestById(token, managerState.activeRequest.reservierungsNr);
                managerState.activeFormDraft = { ...managerState.activeRequest };
                managerState.isEditing = false;
                managerState.deviceSelection.error = null;
            }

            if (action === 'dm-open-edit' || action === 'dm-open-move') {
                managerState.deviceManagement.modal.isOpen = true;
                managerState.deviceManagement.modal.mode = action === 'dm-open-edit' ? 'edit' : 'move';
                managerState.deviceManagement.modal.device = {
                    inventarNr: Number(target.dataset.inventarNr),
                    serienNr: target.dataset.serienNr ? Number(target.dataset.serienNr) : null,
                    kaufdatum: target.dataset.kaufdatum || '',
                    geraetetypId: target.dataset.geraetetypId ? Number(target.dataset.geraetetypId) : null,
                    mitarbeiterPersonalNr: target.dataset.mitarbeiterPersonalNr && target.dataset.mitarbeiterPersonalNr !== 'null'
                        ? Number(target.dataset.mitarbeiterPersonalNr)
                        : null,
                    raum: target.dataset.raum && target.dataset.raum !== 'null'
                        ? Number(target.dataset.raum)
                        : null,
                    istAusleihbar: target.dataset.istAusleihbar === 'true'
                };
                managerState.deviceManagement.modal.error = null;
                if (managerState.deviceManagement.modal.mode === 'move' && !managerState.deviceManagement.moveOptions) {
                    managerState.deviceManagement.moveOptions = await fetchMoveOptions(token);
                }
            }

            if (action === 'dm-close-modal') {
                managerState.deviceManagement.modal.isOpen = false;
                managerState.deviceManagement.modal.error = null;
            }

            if (action === 'dm-delete-device') {
                const inventarNr = Number(target.dataset.inventarNr);
                if (!inventarNr || !window.confirm('Gerät wirklich löschen?')) {
                    return;
                }
                await deleteDevice(token, inventarNr);
                await refreshDeviceManagementHtml(token);
            }

            if (action === 'dm-save-modal') {
                const modalState = managerState.deviceManagement.modal;
                if (!modalState.device?.inventarNr) {
                    return;
                }

                if (modalState.mode === 'edit') {
                    await editDevice(token, modalState.device.inventarNr, {
                        serienNr: Number(root.querySelector('#dm-edit-serial-number').value),
                        kaufdatum: root.querySelector('#dm-edit-purchase-date').value,
                        ausleihbar: root.querySelector('#dm-edit-lendable').checked
                    });
                }

                if (modalState.mode === 'move') {
                    await moveDevice(token, modalState.device.inventarNr, {
                        geraetetypId: Number(root.querySelector('#dm-move-device-type').value),
                        mitarbeiterPersonalNr: root.querySelector('#dm-move-employee').value ? Number(root.querySelector('#dm-move-employee').value) : null,
                        raumNr: root.querySelector('#dm-move-room').value ? Number(root.querySelector('#dm-move-room').value) : null
                    });
                }

                await refreshDeviceManagementHtml(token);
                managerState.deviceManagement.modal.isOpen = false;
            }
        } catch (error) {
            if (String(action || '').startsWith('dm-')) {
                if (managerState.deviceManagement.modal.isOpen) {
                    managerState.deviceManagement.modal.error = error.message;
                } else {
                    managerState.deviceManagement.error = error.message;
                }
            } else {
                managerState.deviceSelection.error = error.message;
            }
        } finally {
            managerState.loading.action = false;
            managerState.deviceSelection.loading = false;
            renderManagerView(root);
        }
    });

    let deviceSearchDebounce;

    pageContent.addEventListener('input', (event) => {
        if (event.target.id === 'dm-search-input') {
            managerState.deviceManagement.search = event.target.value || '';
            const token = getToken();
            const root = pageContent.querySelector('#gv-manager-app');
            clearTimeout(deviceSearchDebounce);
            deviceSearchDebounce = setTimeout(async () => {
                if (!token || !root) return;
                try {
                    managerState.deviceManagement.loading = true;
                    renderManagerView(root);
                    await refreshDeviceManagementHtml(token);
                    managerState.deviceManagement.error = null;
                } catch (error) {
                    managerState.deviceManagement.error = error.message;
                } finally {
                    managerState.deviceManagement.loading = false;
                    renderManagerView(root);
                }
            }, 200);
            return;
        }
        const target = event.target.closest('[data-field-key]');
        if (!target || !managerState.activeFormDraft || !managerState.isEditing) {
            return;
        }

        managerState.activeFormDraft[target.dataset.fieldKey] = target.value;
    });

    pageContent.addEventListener('change', (event) => {
        if (event.target.id !== 'dm-status-filter') {
            return;
        }
        managerState.deviceManagement.statusFilter = event.target.value || 'all';
        const token = getToken();
        const root = pageContent.querySelector('#gv-manager-app');
        if (!token || !root) return;
        (async () => {
            try {
                managerState.deviceManagement.loading = true;
                renderManagerView(root);
                await refreshDeviceManagementHtml(token);
                managerState.deviceManagement.error = null;
            } catch (error) {
                managerState.deviceManagement.error = error.message;
            } finally {
                managerState.deviceManagement.loading = false;
                renderManagerView(root);
            }
        })();
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