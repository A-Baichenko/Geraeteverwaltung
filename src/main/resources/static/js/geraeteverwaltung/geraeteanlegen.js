const createDeviceState = {
    createDeviceModal: false,
    currentSearchTarget: null,
    selectedDeviceType: null,
    selectedEmployee: null,
    selectedRoom: null,
    managementModal: null,
    categories: [],
    loading: {
        action: false,
        search: false,
        management: false
    },
    error: null,
    success: false,
    searchError: null,
    managementError: null,
    managementSuccess: false,
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

async function fetchCategories(token) {
    const response = await fetch('/api/geraeteverwaltung/create-devices/categories', {
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ([]));
    if (!response.ok) {
        throw new Error(data.error || 'Kategorien konnten nicht geladen werden.');
    }

    return Array.isArray(data) ? data : [];
}

async function submitCreateDeviceType(token, payload) {
    const response = await fetch('/api/geraeteverwaltung/create-devices/device-types', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(payload)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Gerätetyp konnte nicht angelegt werden.');
    }

    return data;
}

async function submitCreateCategory(token, payload) {
    const response = await fetch('/api/geraeteverwaltung/create-devices/categories', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(payload)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Kategorie konnte nicht angelegt werden.');
    }

    return data;
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

    return !!root.querySelector('#ga-create-device-modal') && !!root.querySelector('#ga-create-management-modal');
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function notifyDeviceDataChanged() {
    window.dispatchEvent(new CustomEvent('geraeteverwaltung:device-data-changed'));
}

function parseIntegerOrNull(value) {
    if (value == null || String(value).trim() === '') {
        return null;
    }

    const parsed = Number.parseInt(String(value).trim(), 10);
    return Number.isNaN(parsed) ? null : parsed;
}

function resetManagementForm(root) {
    const manufacturer = root.querySelector('#ga-device-type-manufacturer');
    const deviceTypeName = root.querySelector('#ga-device-type-name');
    const category = root.querySelector('#ga-device-type-category');
    const categoryName = root.querySelector('#ga-category-name');

    if (manufacturer) manufacturer.value = '';
    if (deviceTypeName) deviceTypeName.value = '';
    if (category) category.value = '';
    if (categoryName) categoryName.value = '';

    createDeviceState.managementError = null;
    createDeviceState.managementSuccess = false;
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
    createDeviceState.success = false;
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

function createDeviceTypePayload(root) {
    return {
        hersteller: root.querySelector('#ga-device-type-manufacturer')?.value?.trim() || '',
        bezeichnung: root.querySelector('#ga-device-type-name')?.value?.trim() || '',
        kategorieId: parseIntegerOrNull(root.querySelector('#ga-device-type-category')?.value)
    };
}

function createCategoryPayload(root) {
    return {
        bezeichnung: root.querySelector('#ga-category-name')?.value?.trim() || ''
    };
}

function validateDeviceTypePayload(payload) {
    if (!payload.hersteller) {
        return 'Hersteller ist erforderlich.';
    }
    if (!payload.bezeichnung) {
        return 'Bezeichnung ist erforderlich.';
    }
    if (payload.kategorieId == null) {
        return 'Kategorie ist erforderlich.';
    }
    return null;
}

function validateCategoryPayload(payload) {
    if (!payload.bezeichnung) {
        return 'Kategorie-Bezeichnung ist erforderlich.';
    }
    return null;
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

    const createModal = root.querySelector('#ga-create-device-modal');
    if (createModal) {
        createModal.classList.toggle('is-open', createDeviceState.createDeviceModal);
        createModal.setAttribute('aria-hidden', createDeviceState.createDeviceModal ? 'false' : 'true');
    }

    const errorNode = root.querySelector('#ga-create-device-error');
    if (errorNode) {
        errorNode.textContent = createDeviceState.error || '';
        errorNode.style.display = createDeviceState.error ? 'block' : 'none';
        errorNode.classList.toggle('ga-success-text', Boolean(createDeviceState.success));
        errorNode.classList.toggle('ga-error-text', !createDeviceState.success);
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

    const modal = root.querySelector('#ga-create-management-modal');
    if (modal) {
        modal.classList.toggle('is-open', Boolean(createDeviceState.managementModal));
        modal.setAttribute('aria-hidden', createDeviceState.managementModal ? 'false' : 'true');
    }

    const modalTitle = root.querySelector('#ga-management-modal-title');
    const modalSubtitle = root.querySelector('#ga-management-modal-subtitle');
    const deviceTypeForm = root.querySelector('#ga-device-type-form');
    const categoryForm = root.querySelector('#ga-category-form');
    const categorySelect = root.querySelector('#ga-device-type-category');
    const modalMessage = root.querySelector('#ga-management-modal-message');
    const modalSaveButton = root.querySelector('[data-action="save-create-management-modal"]');

    if (modalTitle) {
        modalTitle.textContent = createDeviceState.managementModal === 'category' ? 'Kategorie anlegen' : 'Gerätetyp anlegen';
    }
    if (modalSubtitle) {
        modalSubtitle.textContent = createDeviceState.managementModal === 'category'
            ? 'Neue Kategorie für Gerätetypen erfassen.'
            : 'Neuen Gerätetyp eindeutig mit Hersteller, Bezeichnung und Kategorie erfassen.';
    }
    if (deviceTypeForm) {
        deviceTypeForm.style.display = createDeviceState.managementModal === 'deviceType' ? 'grid' : 'none';
    }
    if (categoryForm) {
        categoryForm.style.display = createDeviceState.managementModal === 'category' ? 'grid' : 'none';
    }
    if (categorySelect) {
        const selectedValue = categorySelect.value;
        categorySelect.innerHTML = '<option value="">Kategorie auswählen</option>' + createDeviceState.categories.map((category) => `
            <option value="${category.id ?? ''}">${escapeHtml(category.bezeichnung || category.label || 'Ohne Bezeichnung')}</option>
        `).join('');
        categorySelect.value = selectedValue;
    }
    if (modalMessage) {
        modalMessage.textContent = createDeviceState.managementError || '';
        modalMessage.style.display = createDeviceState.managementError ? 'block' : 'none';
        modalMessage.classList.toggle('ga-success-text', Boolean(createDeviceState.managementSuccess));
        modalMessage.classList.toggle('ga-error-text', !createDeviceState.managementSuccess);
    }
    if (modalSaveButton) {
        modalSaveButton.disabled = createDeviceState.loading.management;
        modalSaveButton.textContent = createDeviceState.loading.management ? 'Speichern …' : 'Speichern';
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
        createDeviceState.success = false;
    } catch (error) {
        createDeviceState.error = error.message;
        createDeviceState.success = false;
    }

    createDeviceState.createDeviceModal = false;
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
            if (action === 'open-create-device-modal') {
                createDeviceState.createDeviceModal = true;
                createDeviceState.currentSearchTarget = null;
                createDeviceState.searchResults = [];
                createDeviceState.searchError = null;
                createDeviceState.error = null;
                createDeviceState.success = false;
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'close-create-device-modal') {
                createDeviceState.createDeviceModal = false;
                createDeviceState.currentSearchTarget = null;
                createDeviceState.searchResults = [];
                createDeviceState.searchError = null;
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'open-create-search') {
                createDeviceState.currentSearchTarget = target.dataset.searchTarget;
                createDeviceState.searchResults = [];
                createDeviceState.searchError = null;
                createDeviceState.error = null;
                createDeviceState.success = false;
                createDeviceState.createDeviceModal = true;

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

            if (action === 'open-create-device-type-modal') {
                createDeviceState.managementModal = 'deviceType';
                createDeviceState.managementError = null;
                createDeviceState.managementSuccess = false;
                createDeviceState.categories = await fetchCategories(token);
                resetManagementForm(root);
                createDeviceState.managementModal = 'deviceType';
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'open-create-category-modal') {
                createDeviceState.managementModal = 'category';
                createDeviceState.managementError = null;
                createDeviceState.managementSuccess = false;
                resetManagementForm(root);
                createDeviceState.managementModal = 'category';
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'close-create-management-modal') {
                createDeviceState.managementModal = null;
                resetManagementForm(root);
                renderCreateDeviceView(root);
                return;
            }

            if (action === 'save-create-management-modal') {
                if (createDeviceState.loading.management || !createDeviceState.managementModal) {
                    return;
                }

                const isDeviceType = createDeviceState.managementModal === 'deviceType';
                const payload = isDeviceType ? createDeviceTypePayload(root) : createCategoryPayload(root);
                const validationError = isDeviceType ? validateDeviceTypePayload(payload) : validateCategoryPayload(payload);
                if (validationError) {
                    createDeviceState.managementError = validationError;
                    createDeviceState.managementSuccess = false;
                    renderCreateDeviceView(root);
                    return;
                }

                createDeviceState.loading.management = true;
                createDeviceState.managementError = null;
                createDeviceState.managementSuccess = false;
                renderCreateDeviceView(root);

                if (isDeviceType) {
                    const result = await submitCreateDeviceType(token, payload);
                    createDeviceState.selectedDeviceType = {
                        id: result.id,
                        label: result.label || `${payload.hersteller} ${payload.bezeichnung}`
                    };
                    createDeviceState.currentSearchTarget = null;
                    createDeviceState.searchResults = [];
                    createDeviceState.managementModal = null;
                    resetManagementForm(root);
                    createDeviceState.error = result.message || 'Gerätetyp wurde angelegt.';
                    createDeviceState.success = true;
                    notifyDeviceDataChanged();
                } else {
                    const result = await submitCreateCategory(token, payload);
                    resetManagementForm(root);
                    createDeviceState.managementModal = 'deviceType';
                    createDeviceState.categories = await fetchCategories(token);
                    createDeviceState.managementError = result.message || 'Kategorie wurde angelegt. Du kannst sie jetzt für den Gerätetyp auswählen.';
                    createDeviceState.managementSuccess = true;
                    const categorySelect = root.querySelector('#ga-device-type-category');
                    if (categorySelect && result.id != null) {
                        renderCreateDeviceView(root);
                        categorySelect.value = String(result.id);
                    }
                    notifyDeviceDataChanged();
                }
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
                    createDeviceState.success = false;
                    renderCreateDeviceView(root);
                    return;
                }

                createDeviceState.loading.action = true;
                createDeviceState.error = null;
                createDeviceState.success = false;
                renderCreateDeviceView(root);

                const result = await submitCreateDevice(token, payload);

                resetCreateForm(root);
                createDeviceState.error = result.message || 'Gerät wurde angelegt.';
                createDeviceState.success = true;
                notifyDeviceDataChanged();
                renderCreateDeviceView(root);
                return;
            }
        } catch (error) {
            createDeviceState.error = error.message;
            createDeviceState.success = false;
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