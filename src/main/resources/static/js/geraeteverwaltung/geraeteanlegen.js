const createDeviceState = {
    openSection: '',
    currentSearchTarget: null,
    selectedDeviceType: null,
    selectedEmployee: null,
    selectedRoom: null,
    loading: {
        action: false,
        search: false
    },
    error: null,
    searchError: null,
    searchResults: []
};

function authHeaders(token) {
    return {
        Authorization: `Bearer ${token}`
    };
}

async function fetchCreateDevicesViewConfig(token) {
    const response = await fetch('/api/geraeteverwaltung/create-devices/view-config', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'View-Konfiguration konnte nicht geladen werden.');
    }

    return response.json();
}

async function fetchSearchResults(token, target, query = '') {
    let endpoint = '';

    if (target === 'deviceType') {
        endpoint = `/api/geraeteverwaltung/create-devices/device-types?query=${encodeURIComponent(query)}`;
    } else if (target === 'employee') {
        endpoint = `/api/geraeteverwaltung/create-devices/employees?query=${encodeURIComponent(query)}`;
    } else if (target === 'room') {
        endpoint = `/api/geraeteverwaltung/create-devices/rooms?query=${encodeURIComponent(query)}`;
    } else {
        throw new Error('Unbekanntes Suchziel.');
    }

    const response = await fetch(endpoint, {
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ([]));
    if (!response.ok) {
        throw new Error(data.error || 'Suche fehlgeschlagen.');
    }

    return Array.isArray(data) ? data : [];
}

async function submitCreateDevice(token, payload) {
    const response = await fetch('/api/geraeteverwaltung/create-devices', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(payload)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Gerät konnte nicht angelegt werden.');
    }

    return data;
}

function ensureCreateLayout(root) {
    if (!root) {
        return false;
    }

    return !!root.querySelector('.ga-accordion-section[data-section="createDevices"]');
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function parseIntegerOrNull(value) {
    if (value == null || String(value).trim() === '') {
        return null;
    }

    const parsed = Number.parseInt(String(value).trim(), 10);
    return Number.isNaN(parsed) ? null : parsed;
}

function resetCreateForm(root) {
    const inventory = root.querySelector('#ga-create-inventory-number');
    const serial = root.querySelector('#ga-create-serial-number');
    const purchaseDate = root.querySelector('#ga-create-purchase-date');
    const lendable = root.querySelector('#ga-create-lendable');
    const deviceType = root.querySelector('#ga-create-device-type');
    const employee = root.querySelector('#ga-create-employee');
    const room = root.querySelector('#ga-create-room');
    const searchInput = root.querySelector('#ga-create-search-input');

    if (inventory) inventory.value = '';
    if (serial) serial.value = '';
    if (purchaseDate) purchaseDate.value = '';
    if (lendable) lendable.checked = false;
    if (deviceType) deviceType.value = '';
    if (employee) employee.value = '';
    if (room) room.value = '';
    if (searchInput) searchInput.value = '';

    createDeviceState.selectedDeviceType = null;
    createDeviceState.selectedEmployee = null;
    createDeviceState.selectedRoom = null;
    createDeviceState.currentSearchTarget = null;
    createDeviceState.searchResults = [];
    createDeviceState.error = null;
    createDeviceState.searchError = null;
}

function createPayload(root) {
    const inventarNr = parseIntegerOrNull(root.querySelector('#ga-create-inventory-number')?.value);
    const serienNr = parseIntegerOrNull(root.querySelector('#ga-create-serial-number')?.value);
    const kaufdatum = root.querySelector('#ga-create-purchase-date')?.value || null;
    const ausleihbar = !!root.querySelector('#ga-create-lendable')?.checked;
    const geraetetypId = createDeviceState.selectedDeviceType?.id ?? null;
    const mitarbeiterPersonalNr = createDeviceState.selectedEmployee?.personalNr ?? createDeviceState.selectedEmployee?.id ?? null;
    const raumNr = createDeviceState.selectedRoom?.raumNr ?? createDeviceState.selectedRoom?.id ?? null;

    return {
        inventarNr,
        serienNr,
        kaufdatum,
        geraetetypId,
        ausleihbar,
        mitarbeiterPersonalNr,
        raumNr
    };
}

function validatePayload(payload) {
    if (payload.inventarNr == null) {
        return 'Inventar-Nr. ist erforderlich.';
    }
    if (payload.serienNr == null) {
        return 'Serien-Nr. ist erforderlich.';
    }
    if (!payload.kaufdatum) {
        return 'Kaufdatum ist erforderlich.';
    }
    if (payload.geraetetypId == null) {
        return 'Gerätetyp ist erforderlich.';
    }
    return null;
}

function renderCreateDeviceView(root) {
    if (!root || !ensureCreateLayout(root)) {
        return;
    }

    root.querySelectorAll('.ga-accordion-section').forEach((section) => {
        section.classList.toggle('is-open', section.dataset.section === createDeviceState.openSection);
    });

    const errorNode = root.querySelector('#ga-create-device-error');
    if (errorNode) {
        errorNode.textContent = createDeviceState.error || '';
        errorNode.style.display = createDeviceState.error ? 'block' : 'none';
    }

    const searchErrorNode = root.querySelector('#ga-create-search-error');
    if (searchErrorNode) {
        searchErrorNode.textContent = createDeviceState.searchError || '';
        searchErrorNode.style.display = createDeviceState.searchError ? 'block' : 'none';
    }

    const deviceTypeInput = root.querySelector('#ga-create-device-type');
    const employeeInput = root.querySelector('#ga-create-employee');
    const roomInput = root.querySelector('#ga-create-room');

    if (deviceTypeInput) {
        deviceTypeInput.value = createDeviceState.selectedDeviceType?.label || '';
    }
    if (employeeInput) {
        employeeInput.value = createDeviceState.selectedEmployee?.label || '';
    }
    if (roomInput) {
        roomInput.value = createDeviceState.selectedRoom?.label || '';
    }

    const panel = root.querySelector('#ga-create-search-panel');
    if (panel) {
        panel.classList.toggle('is-open', !!createDeviceState.currentSearchTarget);
    }

    const searchTitle = root.querySelector('#ga-create-search-title');
    if (searchTitle) {
        if (createDeviceState.currentSearchTarget === 'deviceType') {
            searchTitle.textContent = 'Gerätetyp auswählen';
        } else if (createDeviceState.currentSearchTarget === 'employee') {
            searchTitle.textContent = 'Mitarbeiter auswählen';
        } else if (createDeviceState.currentSearchTarget === 'room') {
            searchTitle.textContent = 'Raum auswählen';
        } else {
            searchTitle.textContent = 'Suche';
        }
    }

    const searchPlaceholder = root.querySelector('#ga-create-search-placeholder');
    const searchList = root.querySelector('#ga-create-search-results');

    if (searchList) {
        if (createDeviceState.loading.search) {
            searchList.innerHTML = '<li class="ga-placeholder">Einträge werden geladen …</li>';
        } else if (!createDeviceState.searchResults.length) {
            searchList.innerHTML = '';
        } else {
            searchList.innerHTML = createDeviceState.searchResults.map((item) => `
                <li class="ga-item">
                    <button type="button"
                            data-action="select-create-search-result"
                            data-result-id="${item.id ?? ''}"
                            data-result-label="${escapeHtml(item.label || '')}"
                            data-personal-nr="${item.personalNr ?? ''}"
                            data-raum-nr="${item.raumNr ?? ''}">
                        ${escapeHtml(item.label || 'Ohne Bezeichnung')}
                    </button>
                </li>
            `).join('');
        }
    }

    if (searchPlaceholder) {
        if (!createDeviceState.currentSearchTarget) {
            searchPlaceholder.textContent = 'Noch keine Einträge geladen.';
            searchPlaceholder.style.display = createDeviceState.searchResults.length === 0 ? 'block' : 'none';
        } else if (createDeviceState.loading.search) {
            searchPlaceholder.style.display = 'none';
        } else if (createDeviceState.searchResults.length === 0) {
            searchPlaceholder.textContent = 'Keine Ergebnisse gefunden.';
            searchPlaceholder.style.display = 'block';
        } else {
            searchPlaceholder.style.display = 'none';
        }
    }

    const saveButton = root.querySelector('[data-action="create-device"]');
    const resetButton = root.querySelector('[data-action="reset-create-device"]');

    if (saveButton) {
        saveButton.disabled = createDeviceState.loading.action;
    }
    if (resetButton) {
        resetButton.disabled = createDeviceState.loading.action;
    }
}

async function loadSearchList(token, root, target, query = '') {
    createDeviceState.loading.search = true;
    createDeviceState.searchError = null;
    renderCreateDeviceView(root);

    try {
        createDeviceState.searchResults = await fetchSearchResults(token, target, query);
    } catch (error) {
        createDeviceState.searchError = error.message;
        createDeviceState.searchResults = [];
    } finally {
        createDeviceState.loading.search = false;
        renderCreateDeviceView(root);
    }
}

async function initializeCreateDevicesFlow(token, getState, pageContent) {
    const tabState = getState();
    if (tabState.activeTabKey !== 'geraeteverwaltung') {
        return null;
    }

    const root = pageContent.querySelector('#gv-manager-app');
    if (!root || !ensureCreateLayout(root)) {
        return null;
    }

    if (root.dataset.createDevicesInitialized === 'true') {
        renderCreateDeviceView(root);
        return root;
    }

    try {
        await fetchCreateDevicesViewConfig(token);
        createDeviceState.error = null;
    } catch (error) {
        createDeviceState.error = error.message;
    }

    createDeviceState.openSection = '';
    renderCreateDeviceView(root);
    root.dataset.createDevicesInitialized = 'true';

    return root;
}

export function registerGeraeteanlegenHandlers({ pageContent, getToken, redirectToLogin, getState }) {
    let initializedRoot = null;

    async function ensureInitialized() {
        const token = getToken();
        if (!token) {
            return null;
        }

        const root = pageContent.querySelector('#gv-manager-app');
        if (!root || !ensureCreateLayout(root)) {
            return null;
        }

        if (initializedRoot !== root) {
            initializedRoot = root;
            root.dataset.createDevicesInitialized = '';
            await initializeCreateDevicesFlow(token, getState, pageContent);
        }

        return root;
    }

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

        const root = await ensureInitialized();
        if (!root) {
            return;
        }

        const action = target.dataset.action;

        try {
            if (action === 'toggle-section' && target.dataset.sectionKey === 'createDevices') {
                createDeviceState.openSection =
                    createDeviceState.openSection === 'createDevices' ? '' : 'createDevices';
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'open-create-search') {
                createDeviceState.currentSearchTarget = target.dataset.searchTarget;
                createDeviceState.searchResults = [];
                createDeviceState.searchError = null;
                createDeviceState.openSection = 'createDevices';

                const searchInput = root.querySelector('#ga-create-search-input');
                if (searchInput) {
                    searchInput.value = '';
                }

                renderCreateDeviceView(root);
                await loadSearchList(token, root, createDeviceState.currentSearchTarget, '');
                return;
            }

            if (action === 'close-create-search') {
                createDeviceState.currentSearchTarget = null;
                createDeviceState.searchResults = [];
                createDeviceState.searchError = null;
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'select-create-search-result') {
                const selectedItem = {
                    id: target.dataset.resultId ? Number(target.dataset.resultId) : null,
                    label: target.dataset.resultLabel || '',
                    personalNr: target.dataset.personalNr ? Number(target.dataset.personalNr) : null,
                    raumNr: target.dataset.raumNr ? Number(target.dataset.raumNr) : null
                };

                if (createDeviceState.currentSearchTarget === 'deviceType') {
                    createDeviceState.selectedDeviceType = selectedItem;
                }

                if (createDeviceState.currentSearchTarget === 'employee') {
                    createDeviceState.selectedEmployee = selectedItem;
                }

                if (createDeviceState.currentSearchTarget === 'room') {
                    createDeviceState.selectedRoom = selectedItem;
                }

                createDeviceState.currentSearchTarget = null;
                createDeviceState.searchResults = [];
                createDeviceState.searchError = null;
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'reset-create-device') {
                resetCreateForm(root);
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'create-device') {
                if (createDeviceState.loading.action) {
                    return;
                }

                const payload = createPayload(root);
                const validationError = validatePayload(payload);
                if (validationError) {
                    createDeviceState.error = validationError;
                    renderCreateDeviceView(root);
                    return;
                }

                createDeviceState.loading.action = true;
                createDeviceState.error = null;
                renderCreateDeviceView(root);

                const result = await submitCreateDevice(token, payload);

                resetCreateForm(root);
                createDeviceState.error = result.message || 'Gerät wurde angelegt.';
                renderCreateDeviceView(root);
                return;
            }
        } catch (error) {
            createDeviceState.error = error.message;
            renderCreateDeviceView(root);
        } finally {
            createDeviceState.loading.action = false;
            renderCreateDeviceView(root);
        }
    });

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        if (!target || target.id !== 'ga-create-search-input') {
            return;
        }

        const token = getToken();
        if (!token) {
            return;
        }

        const root = await ensureInitialized();
        if (!root) {
            return;
        }

        if (!createDeviceState.currentSearchTarget) {
            return;
        }

        await loadSearchList(token, root, createDeviceState.currentSearchTarget, target.value || '');
    });
}