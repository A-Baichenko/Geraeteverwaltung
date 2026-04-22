const fixedAssignmentState = {
    openSection: '',
    items: [],
    searchTerm: '',
    loading: {
        list: false,
        action: false
    },
    message: null,
    success: false,
    editor: {
        isOpen: false,
        inventarNr: '',
        mitarbeiterNr: '',
        raumNr: ''
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

async function saveFixedAssignment(token, payload) {
    const response = await fetch(`/api/geraeteverwaltung/fixed-assignments/${payload.inventarNr}`, {
        method: 'PUT',
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
        mitarbeiterNr: '',
        raumNr: ''
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

    const inventarInput = root.querySelector('#fz-inventar-nr');
    const mitarbeiterInput = root.querySelector('#fz-mitarbeiter-nr');
    const raumInput = root.querySelector('#fz-raum-nr');

    if (inventarInput) inventarInput.value = fixedAssignmentState.editor.inventarNr;
    if (mitarbeiterInput) mitarbeiterInput.value = fixedAssignmentState.editor.mitarbeiterNr;
    if (raumInput) raumInput.value = fixedAssignmentState.editor.raumNr;
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
                fixedAssignmentState.editor = {
                    isOpen: true,
                    inventarNr: '',
                    mitarbeiterNr: '',
                    raumNr: ''
                };
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
                    mitarbeiterNr: item.mitarbeiterPersonalNr != null ? String(item.mitarbeiterPersonalNr) : '',
                    raumNr: item.raumNr != null ? String(item.raumNr) : ''
                };
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

            if (action === 'save-fixed-assignment') {
                if (fixedAssignmentState.loading.action) {
                    return;
                }

                const payload = {
                    inventarNr: parseIntegerOrNull(root.querySelector('#fz-inventar-nr')?.value),
                    mitarbeiterPersonalNr: parseIntegerOrNull(root.querySelector('#fz-mitarbeiter-nr')?.value),
                    raumNr: parseIntegerOrNull(root.querySelector('#fz-raum-nr')?.value)
                };

                if (payload.inventarNr == null) {
                    fixedAssignmentState.message = 'Inventar-Nr. ist erforderlich.';
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
                const inventarNr = parseIntegerOrNull(root.querySelector('#fz-inventar-nr')?.value);
                if (inventarNr == null) {
                    fixedAssignmentState.message = 'Inventar-Nr. ist erforderlich.';
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
        if (!target || target.id !== 'fz-search-input') {
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

        fixedAssignmentState.searchTerm = target.value || '';

        if (fixedAssignmentState.openSection === 'assignFixed') {
            await loadFixedAssignments(token, root);
        }
    });
}