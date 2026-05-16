const lendState = {
    openSection: '',
    currentSearchTarget: null,
    selectedDeviceType: null,
    selectedEmployee: null,
    loading: {
        action: false,
        search: false
    },
    message: null,
    success: false,
    searchError: null,
    searchResults: []
};

function authHeaders(token) {
    return {
        Authorization: `Bearer ${token}`
    };
}

async function fetchLendViewConfig(token) {
    const response = await fetch('/api/geraeteverwaltung/lend/view-config', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'View-Konfiguration konnte nicht geladen werden.');
    }

    return response.json();
}

async function fetchLendSearchResults(token, target, query = '') {
    let endpoint = '';

    if (target === 'deviceType') {
        endpoint = `/api/geraeteverwaltung/lend/device-types?query=${encodeURIComponent(query)}`;
    } else if (target === 'employee') {
        endpoint = `/api/geraeteverwaltung/lend/employees?query=${encodeURIComponent(query)}`;
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

async function submitLend(token, payload) {
    const response = await fetch('/api/geraeteverwaltung/lend', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(payload)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Ausleihe konnte nicht erstellt werden.');
    }

    return data;
}

function ensureLendLayout(root) {
    if (!root) {
        return false;
    }

    return !!root.querySelector('.al-accordion-section[data-section="lend"]');
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function resetLendForm(root) {
    const lendDate = root.querySelector('#al-lend-date');
    const returnDate = root.querySelector('#al-return-date');
    const deviceType = root.querySelector('#al-device-type');
    const employee = root.querySelector('#al-employee');
    const searchInput = root.querySelector('#al-search-input');

    const today = new Date().toISOString().slice(0, 10);

    if (lendDate) lendDate.value = today;
    if (returnDate) returnDate.value = '';
    if (deviceType) deviceType.value = '';
    if (employee) employee.value = '';
    if (searchInput) searchInput.value = '';

    lendState.selectedDeviceType = null;
    lendState.selectedEmployee = null;
    lendState.currentSearchTarget = null;
    lendState.searchResults = [];
    lendState.message = null;
    lendState.success = false;
    lendState.searchError = null;
}

function createLendPayload(root) {
    const lendDate = root.querySelector('#al-lend-date')?.value || null;
    const returnDate = root.querySelector('#al-return-date')?.value || null;

    return {
        geraetetypId: lendState.selectedDeviceType?.id ?? null,
        personalNr: lendState.selectedEmployee?.personalNr ?? lendState.selectedEmployee?.id ?? null,
        ausleihdatum: lendDate,
        rueckgabedatum: returnDate
    };
}

function validateLendPayload(payload) {
    if (!payload.ausleihdatum) {
        return 'Ausleihdatum ist erforderlich.';
    }
    if (!payload.rueckgabedatum) {
        return 'Rückgabedatum ist erforderlich.';
    }
    if (!payload.geraetetypId) {
        return 'Gerät ist erforderlich.';
    }
    if (!payload.personalNr) {
        return 'Mitarbeiter ist erforderlich.';
    }
    if (payload.ausleihdatum > payload.rueckgabedatum) {
        return 'Ausleihdatum darf nicht nach dem Rückgabedatum liegen.';
    }
    return null;
}

function renderLendView(root) {
    if (!root || !ensureLendLayout(root)) {
        return;
    }

    root.querySelectorAll('.al-accordion-section').forEach((section) => {
        section.classList.toggle('is-open', section.dataset.section === lendState.openSection);
    });

    const messageNode = root.querySelector('#al-global-message');
    if (messageNode) {
        messageNode.textContent = lendState.message || '';
        messageNode.style.display = lendState.message ? 'block' : 'none';
        messageNode.classList.toggle('al-success-text', Boolean(lendState.success));
        messageNode.classList.toggle('al-error-text', !lendState.success);
    }

    const deviceTypeInput = root.querySelector('#al-device-type');
    const employeeInput = root.querySelector('#al-employee');

    if (deviceTypeInput) {
        deviceTypeInput.value = lendState.selectedDeviceType?.label || '';
    }
    if (employeeInput) {
        employeeInput.value = lendState.selectedEmployee?.label || '';
    }

    const panel = root.querySelector('#al-search-panel');
    if (panel) {
        panel.classList.toggle('is-open', !!lendState.currentSearchTarget);
    }

    const searchTitle = root.querySelector('#al-search-title');
    if (searchTitle) {
        if (lendState.currentSearchTarget === 'deviceType') {
            searchTitle.textContent = 'Gerät auswählen';
        } else if (lendState.currentSearchTarget === 'employee') {
            searchTitle.textContent = 'Mitarbeiter auswählen';
        } else {
            searchTitle.textContent = 'Suche';
        }
    }

    const searchErrorNode = root.querySelector('#al-search-error');
    if (searchErrorNode) {
        searchErrorNode.textContent = lendState.searchError || '';
        searchErrorNode.style.display = lendState.searchError ? 'block' : 'none';
    }

    const searchList = root.querySelector('#al-search-results');
    const placeholder = root.querySelector('#al-search-placeholder');

    if (searchList) {
        if (lendState.loading.search) {
            searchList.innerHTML = '<li class="al-placeholder">Einträge werden geladen …</li>';
        } else if (!lendState.searchResults.length) {
            searchList.innerHTML = '';
        } else {
            searchList.innerHTML = lendState.searchResults.map((item) => `
                <li class="al-item">
                    <button type="button"
                            data-action="select-lend-search-result"
                            data-result-id="${item.id ?? ''}"
                            data-result-label="${escapeHtml(item.label || '')}"
                            data-personal-nr="${item.personalNr ?? ''}">
                        ${escapeHtml(item.label || 'Ohne Bezeichnung')}
                    </button>
                </li>
            `).join('');
        }
    }

    if (placeholder) {
        if (!lendState.currentSearchTarget) {
            placeholder.textContent = 'Noch keine Einträge vorhanden.';
            placeholder.style.display = lendState.searchResults.length === 0 ? 'block' : 'none';
        } else if (lendState.loading.search) {
            placeholder.style.display = 'none';
        } else if (lendState.searchResults.length === 0) {
            placeholder.textContent = 'Keine Ergebnisse gefunden.';
            placeholder.style.display = 'block';
        } else {
            placeholder.style.display = 'none';
        }
    }
}

async function loadLendSearchList(token, root, target, query = '') {
    lendState.loading.search = true;
    lendState.searchError = null;
    renderLendView(root);

    try {
        lendState.searchResults = await fetchLendSearchResults(token, target, query);
    } catch (error) {
        lendState.searchError = error.message;
        lendState.searchResults = [];
    } finally {
        lendState.loading.search = false;
        renderLendView(root);
    }
}

async function initializeLendFlow(token, getState, pageContent) {
    const tabState = getState();
    if (tabState.activeTabKey !== 'geraeteverwaltung') {
        return null;
    }

    const root = pageContent.querySelector('#gv-manager-app');
    if (!root || !ensureLendLayout(root)) {
        return null;
    }

    if (root.dataset.lendInitialized === 'true') {
        renderLendView(root);
        return root;
    }

    try {
        await fetchLendViewConfig(token);
        lendState.message = null;
        lendState.success = false;
    } catch (error) {
        lendState.message = error.message;
        lendState.success = false;
    }

    lendState.openSection = '';
    renderLendView(root);
    resetLendForm(root);
    root.dataset.lendInitialized = 'true';

    return root;
}

export function registerAusleiheHandlers({ pageContent, getToken, redirectToLogin, getState }) {
    let initializedRoot = null;

    async function ensureInitialized() {
        const token = getToken();
        if (!token) {
            return null;
        }

        const root = pageContent.querySelector('#gv-manager-app');
        if (!root || !ensureLendLayout(root)) {
            return null;
        }

        if (initializedRoot !== root) {
            initializedRoot = root;
            root.dataset.lendInitialized = '';
            await initializeLendFlow(token, getState, pageContent);
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
            if (action === 'toggle-section' && target.dataset.sectionKey === 'lend') {
                lendState.openSection = lendState.openSection === 'lend' ? '' : 'lend';
                renderLendView(root);
                return;
            }

            if (action === 'open-lend-search') {
                if (target.dataset.searchTarget === 'returnInfo') {
                    return;
                }

                lendState.currentSearchTarget = target.dataset.searchTarget;
                lendState.searchResults = [];
                lendState.searchError = null;
                lendState.message = null;
                lendState.success = false;
                lendState.openSection = 'lend';

                const searchInput = root.querySelector('#al-search-input');
                if (searchInput) {
                    searchInput.value = '';
                }

                renderLendView(root);
                await loadLendSearchList(token, root, lendState.currentSearchTarget, '');
                return;
            }

            if (action === 'close-lend-search') {
                lendState.currentSearchTarget = null;
                lendState.searchResults = [];
                lendState.searchError = null;
                renderLendView(root);
                return;
            }

            if (action === 'select-lend-search-result') {
                const selectedItem = {
                    id: target.dataset.resultId ? Number(target.dataset.resultId) : null,
                    label: target.dataset.resultLabel || '',
                    personalNr: target.dataset.personalNr ? Number(target.dataset.personalNr) : null
                };

                if (lendState.currentSearchTarget === 'deviceType') {
                    lendState.selectedDeviceType = selectedItem;
                }

                if (lendState.currentSearchTarget === 'employee') {
                    lendState.selectedEmployee = selectedItem;
                }

                lendState.currentSearchTarget = null;
                lendState.searchResults = [];
                lendState.searchError = null;
                renderLendView(root);
                return;
            }

            if (action === 'reset-lend') {
                resetLendForm(root);
                renderLendView(root);
                return;
            }

            if (action === 'create-lend') {
                if (lendState.loading.action) {
                    return;
                }

                const payload = createLendPayload(root);
                const validationError = validateLendPayload(payload);
                if (validationError) {
                    lendState.message = validationError;
                    lendState.success = false;
                    renderLendView(root);
                    return;
                }

                lendState.loading.action = true;
                lendState.message = null;
                lendState.success = false;
                renderLendView(root);

                const result = await submitLend(token, payload);

                resetLendForm(root);
                lendState.message = result.message || 'Ausleihe wurde erstellt.';
                lendState.success = true;
                renderLendView(root);
                return;
            }
        } catch (error) {
            lendState.message = error.message;
            lendState.success = false;
            renderLendView(root);
        } finally {
            lendState.loading.action = false;
            renderLendView(root);
        }
    });

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        if (!target || target.id !== 'al-search-input') {
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

        if (!lendState.currentSearchTarget) {
            return;
        }

        await loadLendSearchList(token, root, lendState.currentSearchTarget, target.value || '');
    });
}