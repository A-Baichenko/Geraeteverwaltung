const fixedAssignmentState = {
    openSection: '',
    items: [],
    searchTerm: '',
    currentSearchTarget: null,
    searchResults: [],
    searchError: null,
    loading: {
        list: false,
        action: false,
        search: false
    },
    message: null,
    success: false,
    editor: {
        isOpen: false,
        inventarNr: '',
        selectedDeviceType: null,
        selectedEmployee: null,
        selectedRoom: null
    }
};

function authHeaders(token) {
    return {
        Authorization: `Bearer ${token}`
    };
}

async function fetchFixedAssignmentsViewConfig(token) {
    const response = await fetch('/api/geraeteverwaltung/fixed-assignments/view-config', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'View-Konfiguration konnte nicht geladen werden.');
    }

    return response.json();
}

async function fetchFixedAssignments(token, query = '') {
    const endpoint = query.trim()
        ? `/api/geraeteverwaltung/fixed-assignments?query=${encodeURIComponent(query)}`
        : '/api/geraeteverwaltung/fixed-assignments';

    const response = await fetch(endpoint, {
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ([]));
    if (!response.ok) {
        throw new Error(data.error || 'Feste Zuordnungen konnten nicht geladen werden.');
    }

    return Array.isArray(data) ? data : [];
}

async function fetchFixedAssignmentById(token, inventarNr) {
    const response = await fetch(`/api/geraeteverwaltung/fixed-assignments/${inventarNr}`, {
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Zuordnung konnte nicht geladen werden.');
    }

    return data;
}

async function fetchFixedSearchResults(token, target, query = '') {
    let endpoint = '';

    if (target === 'deviceType') {
        endpoint = `/api/geraeteverwaltung/fixed-assignments/device-types?query=${encodeURIComponent(query)}`;
    } else if (target === 'employee') {
        endpoint = `/api/geraeteverwaltung/fixed-assignments/employees?query=${encodeURIComponent(query)}`;
    } else if (target === 'room') {
        endpoint = `/api/geraeteverwaltung/fixed-assignments/rooms?query=${encodeURIComponent(query)}`;
    } else {
        return [];
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

async function saveFixedAssignment(token, payload) {
    const hasInventory = payload.inventarNr != null;
    const endpoint = hasInventory
        ? `/api/geraeteverwaltung/fixed-assignments/${payload.inventarNr}`
        : '/api/geraeteverwaltung/fixed-assignments';

    const response = await fetch(endpoint, {
        method: hasInventory ? 'PUT' : 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(payload)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Zuordnung konnte nicht gespeichert werden.');
    }

    return data;
}

async function clearFixedAssignment(token, inventarNr) {
    const response = await fetch(`/api/geraeteverwaltung/fixed-assignments/${inventarNr}`, {
        method: 'DELETE',
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Zuordnung konnte nicht aufgehoben werden.');
    }

    return data;
}

function ensureFixedLayout(root) {
    if (!root) {
        return false;
    }

    return !!root.querySelector('.fz-accordion-section[data-section="assignFixed"]');
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

function resetFixedEditor() {
    fixedAssignmentState.editor = {
        isOpen: false,
        inventarNr: '',
        selectedDeviceType: null,
        selectedEmployee: null,
        selectedRoom: null
    };
    fixedAssignmentState.currentSearchTarget = null;
    fixedAssignmentState.searchResults = [];
    fixedAssignmentState.searchError = null;
}

function createFixedPayload() {
    return {
        inventarNr: parseIntegerOrNull(fixedAssignmentState.editor.inventarNr),
        geraetetypId: fixedAssignmentState.editor.selectedDeviceType?.id ?? null,
        mitarbeiterPersonalNr: fixedAssignmentState.editor.selectedEmployee?.personalNr ?? fixedAssignmentState.editor.selectedEmployee?.id ?? null,
        raumNr: fixedAssignmentState.editor.selectedRoom?.raumNr ?? fixedAssignmentState.editor.selectedRoom?.id ?? null
    };
}

function renderFixedAssignmentsView(root) {
    if (!root || !ensureFixedLayout(root)) {
        return;
    }

    root.querySelectorAll('.fz-accordion-section').forEach((section) => {
        section.classList.toggle('is-open', section.dataset.section === fixedAssignmentState.openSection);
    });

    const messageNode = root.querySelector('#fz-global-message');
    if (messageNode) {
        messageNode.textContent = fixedAssignmentState.message || '';
        messageNode.style.display = fixedAssignmentState.message ? 'block' : 'none';
        messageNode.classList.toggle('fz-success-text', Boolean(fixedAssignmentState.success));
        messageNode.classList.toggle('fz-error-text', !fixedAssignmentState.success);
    }

    const searchInput = root.querySelector('#fz-search-input');
    if (searchInput) {
        searchInput.value = fixedAssignmentState.searchTerm;
    }

    const list = root.querySelector('#fz-assignment-list');
    const placeholder = root.querySelector('#fz-list-placeholder');

    if (list) {
        if (fixedAssignmentState.loading.list) {
            list.innerHTML = '<li class="fz-placeholder">Zuordnungen werden geladen …</li>';
        } else if (!fixedAssignmentState.items.length) {
            list.innerHTML = '';
        } else {
            list.innerHTML = fixedAssignmentState.items.map((item) => `
                <li class="fz-item">
                    <button type="button" data-action="open-fixed-item" data-inventar-nr="${item.inventarNr}">
                        <strong>Inventar #${escapeHtml(item.inventarNr)}</strong>
                        <span>Mitarbeiter: ${escapeHtml(item.mitarbeiterLabel || '—')}</span>
                        <span>Raum: ${escapeHtml(item.raumLabel || '—')}</span>
                    </button>
                </li>
            `).join('');
        }
    }

    if (placeholder) {
        placeholder.style.display = !fixedAssignmentState.loading.list && fixedAssignmentState.items.length === 0 ? 'block' : 'none';
    }

    const panel = root.querySelector('#fz-editor-panel');
    if (panel) {
        panel.classList.toggle('is-open', fixedAssignmentState.editor.isOpen);
    }

    const deviceInput = root.querySelector('#fz-device-type');
    const employeeInput = root.querySelector('#fz-employee');
    const roomInput = root.querySelector('#fz-room');

    if (deviceInput) {
        deviceInput.value = fixedAssignmentState.editor.inventarNr
            ? `Inventar #${fixedAssignmentState.editor.inventarNr}`
            : fixedAssignmentState.editor.selectedDeviceType?.label || '';
    }
    if (employeeInput) {
        employeeInput.value = fixedAssignmentState.editor.selectedEmployee?.label || '';
    }
    if (roomInput) {
        roomInput.value = fixedAssignmentState.editor.selectedRoom?.label || '';
    }

    const searchPanel = root.querySelector('#fz-search-panel');
    if (searchPanel) {
        searchPanel.classList.toggle('is-open', !!fixedAssignmentState.currentSearchTarget);
    }

    const searchTitle = root.querySelector('#fz-search-title');
    if (searchTitle) {
        if (fixedAssignmentState.currentSearchTarget === 'deviceType') {
            searchTitle.textContent = 'Gerätetyp auswählen';
        } else if (fixedAssignmentState.currentSearchTarget === 'employee') {
            searchTitle.textContent = 'Mitarbeiter auswählen';
        } else if (fixedAssignmentState.currentSearchTarget === 'room') {
            searchTitle.textContent = 'Raum auswählen';
        } else {
            searchTitle.textContent = 'Suche';
        }
    }

    const searchErrorNode = root.querySelector('#fz-search-error');
    if (searchErrorNode) {
        searchErrorNode.textContent = fixedAssignmentState.searchError || '';
        searchErrorNode.style.display = fixedAssignmentState.searchError ? 'block' : 'none';
    }

    const searchList = root.querySelector('#fz-search-results');
    const searchPlaceholder = root.querySelector('#fz-search-placeholder');

    if (searchList) {
        if (fixedAssignmentState.loading.search) {
            searchList.innerHTML = '<li class="fz-placeholder">Einträge werden geladen …</li>';
        } else if (!fixedAssignmentState.searchResults.length) {
            searchList.innerHTML = '';
        } else {
            searchList.innerHTML = fixedAssignmentState.searchResults.map((item) => `
                <li class="fz-item">
                    <button type="button"
                            data-action="select-fixed-search-result"
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
        if (!fixedAssignmentState.currentSearchTarget) {
            searchPlaceholder.textContent = 'Noch keine Einträge vorhanden.';
            searchPlaceholder.style.display = fixedAssignmentState.searchResults.length === 0 ? 'block' : 'none';
        } else if (fixedAssignmentState.loading.search) {
            searchPlaceholder.style.display = 'none';
        } else if (fixedAssignmentState.searchResults.length === 0) {
            searchPlaceholder.textContent = 'Keine Ergebnisse gefunden.';
            searchPlaceholder.style.display = 'block';
        } else {
            searchPlaceholder.style.display = 'none';
        }
    }
}

async function loadFixedAssignments(token, root) {
    fixedAssignmentState.loading.list = true;
    fixedAssignmentState.message = null;
    fixedAssignmentState.success = false;
    renderFixedAssignmentsView(root);

    try {
        fixedAssignmentState.items = await fetchFixedAssignments(token, fixedAssignmentState.searchTerm);
    } catch (error) {
        fixedAssignmentState.items = [];
        fixedAssignmentState.message = error.message;
        fixedAssignmentState.success = false;
    } finally {
        fixedAssignmentState.loading.list = false;
        renderFixedAssignmentsView(root);
    }
}

async function loadFixedSearchList(token, root, target, query = '') {
    fixedAssignmentState.loading.search = true;
    fixedAssignmentState.searchError = null;
    renderFixedAssignmentsView(root);

    try {
        fixedAssignmentState.searchResults = await fetchFixedSearchResults(token, target, query);
    } catch (error) {
        fixedAssignmentState.searchError = error.message;
        fixedAssignmentState.searchResults = [];
    } finally {
        fixedAssignmentState.loading.search = false;
        renderFixedAssignmentsView(root);
    }
}

async function initializeFixedAssignmentsFlow(token, getState, pageContent) {
    const tabState = getState();
    if (tabState.activeTabKey !== 'geraeteverwaltung') {
        return null;
    }

    const root = pageContent.querySelector('#gv-manager-app');
    if (!root || !ensureFixedLayout(root)) {
        return null;
    }

    if (root.dataset.fixedAssignmentsInitialized === 'true') {
        renderFixedAssignmentsView(root);
        return root;
    }

    try {
        await fetchFixedAssignmentsViewConfig(token);
        fixedAssignmentState.message = null;
        fixedAssignmentState.success = false;
    } catch (error) {
        fixedAssignmentState.message = error.message;
        fixedAssignmentState.success = false;
    }

    fixedAssignmentState.openSection = '';
    renderFixedAssignmentsView(root);
    root.dataset.fixedAssignmentsInitialized = 'true';

    return root;
}

export function registerFestZuordnenHandlers({ pageContent, getToken, redirectToLogin, getState }) {
    let initializedRoot = null;

    async function ensureInitialized() {
        const token = getToken();
        if (!token) {
            return null;
        }

        const root = pageContent.querySelector('#gv-manager-app');
        if (!root || !ensureFixedLayout(root)) {
            return null;
        }

        if (initializedRoot !== root) {
            initializedRoot = root;
            root.dataset.fixedAssignmentsInitialized = '';
            await initializeFixedAssignmentsFlow(token, getState, pageContent);
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
            if (action === 'toggle-section' && target.dataset.sectionKey === 'assignFixed') {
                fixedAssignmentState.openSection = fixedAssignmentState.openSection === 'assignFixed' ? '' : 'assignFixed';
                renderFixedAssignmentsView(root);

                if (fixedAssignmentState.openSection === 'assignFixed') {
                    await loadFixedAssignments(token, root);
                }
                return;
            }

            if (action === 'open-fixed-editor') {
                resetFixedEditor();
                fixedAssignmentState.editor.isOpen = true;
                fixedAssignmentState.message = null;
                fixedAssignmentState.success = false;
                renderFixedAssignmentsView(root);
                return;
            }

            if (action === 'open-fixed-item') {
                const item = await fetchFixedAssignmentById(token, Number(target.dataset.inventarNr));
                fixedAssignmentState.editor = {
                    isOpen: true,
                    inventarNr: String(item.inventarNr ?? ''),
                    selectedDeviceType: null,
                    selectedEmployee: item.mitarbeiterPersonalNr != null
                        ? { id: Number(item.mitarbeiterPersonalNr), personalNr: Number(item.mitarbeiterPersonalNr), label: item.mitarbeiterLabel || `Personal-Nr. ${item.mitarbeiterPersonalNr}` }
                        : null,
                    selectedRoom: item.raumNr != null
                        ? { id: Number(item.raumNr), raumNr: Number(item.raumNr), label: item.raumLabel || `Raum ${item.raumNr}` }
                        : null
                };
                fixedAssignmentState.currentSearchTarget = null;
                fixedAssignmentState.searchResults = [];
                fixedAssignmentState.searchError = null;
                fixedAssignmentState.message = null;
                fixedAssignmentState.success = false;
                renderFixedAssignmentsView(root);
                return;
            }

            if (action === 'close-fixed-editor') {
                resetFixedEditor();
                renderFixedAssignmentsView(root);
                return;
            }

            if (action === 'open-fixed-search') {
                if (!fixedAssignmentState.editor.isOpen) {
                    fixedAssignmentState.editor.isOpen = true;
                }

                fixedAssignmentState.currentSearchTarget = target.dataset.searchTarget;
                fixedAssignmentState.searchResults = [];
                fixedAssignmentState.searchError = null;
                fixedAssignmentState.message = null;
                fixedAssignmentState.success = false;
                fixedAssignmentState.openSection = 'assignFixed';

                const searchInput = root.querySelector('#fz-picker-search-input');
                if (searchInput) {
                    searchInput.value = '';
                }

                renderFixedAssignmentsView(root);
                await loadFixedSearchList(token, root, fixedAssignmentState.currentSearchTarget, '');
                return;
            }

            if (action === 'close-fixed-search') {
                fixedAssignmentState.currentSearchTarget = null;
                fixedAssignmentState.searchResults = [];
                fixedAssignmentState.searchError = null;
                renderFixedAssignmentsView(root);
                return;
            }

            if (action === 'select-fixed-search-result') {
                const selectedItem = {
                    id: target.dataset.resultId ? Number(target.dataset.resultId) : null,
                    label: target.dataset.resultLabel || '',
                    personalNr: target.dataset.personalNr ? Number(target.dataset.personalNr) : null,
                    raumNr: target.dataset.raumNr ? Number(target.dataset.raumNr) : null
                };

                if (fixedAssignmentState.currentSearchTarget === 'deviceType') {
                    fixedAssignmentState.editor.inventarNr = '';
                    fixedAssignmentState.editor.selectedDeviceType = selectedItem;
                }

                if (fixedAssignmentState.currentSearchTarget === 'employee') {
                    fixedAssignmentState.editor.selectedEmployee = selectedItem;
                }

                if (fixedAssignmentState.currentSearchTarget === 'room') {
                    fixedAssignmentState.editor.selectedRoom = selectedItem;
                }

                fixedAssignmentState.currentSearchTarget = null;
                fixedAssignmentState.searchResults = [];
                fixedAssignmentState.searchError = null;
                renderFixedAssignmentsView(root);
                return;
            }

            if (action === 'save-fixed-assignment') {
                if (fixedAssignmentState.loading.action) {
                    return;
                }

                const payload = createFixedPayload();

                if (payload.inventarNr == null && payload.geraetetypId == null) {
                    fixedAssignmentState.message = 'Bitte ein Gerät oder einen Gerätetyp auswählen.';
                    fixedAssignmentState.success = false;
                    renderFixedAssignmentsView(root);
                    return;
                }

                if (payload.mitarbeiterPersonalNr == null && payload.raumNr == null) {
                    fixedAssignmentState.message = 'Bitte Mitarbeiter oder Raum auswählen.';
                    fixedAssignmentState.success = false;
                    renderFixedAssignmentsView(root);
                    return;
                }

                fixedAssignmentState.loading.action = true;
                renderFixedAssignmentsView(root);

                const result = await saveFixedAssignment(token, payload);
                fixedAssignmentState.message = result.message || 'Zuordnung gespeichert.';
                fixedAssignmentState.success = true;
                await loadFixedAssignments(token, root);
                resetFixedEditor();
                renderFixedAssignmentsView(root);
                return;
            }

            if (action === 'clear-fixed-assignment') {
                const inventarNr = parseIntegerOrNull(fixedAssignmentState.editor.inventarNr);
                if (inventarNr == null) {
                    fixedAssignmentState.message = 'Zum Aufheben bitte eine bestehende feste Zuordnung aus der Liste auswählen.';
                    fixedAssignmentState.success = false;
                    renderFixedAssignmentsView(root);
                    return;
                }

                fixedAssignmentState.loading.action = true;
                renderFixedAssignmentsView(root);

                const result = await clearFixedAssignment(token, inventarNr);
                fixedAssignmentState.message = result.message || 'Zuordnung aufgehoben.';
                fixedAssignmentState.success = true;
                await loadFixedAssignments(token, root);
                resetFixedEditor();
                renderFixedAssignmentsView(root);
                return;
            }
        } catch (error) {
            fixedAssignmentState.message = error.message;
            fixedAssignmentState.success = false;
            renderFixedAssignmentsView(root);
        } finally {
            fixedAssignmentState.loading.action = false;
            renderFixedAssignmentsView(root);
        }
    });

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        if (!target || (target.id !== 'fz-search-input' && target.id !== 'fz-picker-search-input')) {
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

        if (target.id === 'fz-search-input') {
            fixedAssignmentState.searchTerm = target.value || '';

            if (fixedAssignmentState.openSection === 'assignFixed') {
                await loadFixedAssignments(token, root);
            }
            return;
        }

        if (target.id === 'fz-picker-search-input' && fixedAssignmentState.currentSearchTarget) {
            await loadFixedSearchList(token, root, fixedAssignmentState.currentSearchTarget, target.value || '');
        }
    });
}