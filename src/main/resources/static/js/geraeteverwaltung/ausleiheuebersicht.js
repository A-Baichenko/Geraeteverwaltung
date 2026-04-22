const overviewState = {
    openSection: '',
    items: [],
    searchTerm: '',
    currentPage: 1,
    pageSize: 5,
    loading: {
        list: false,
        action: false
    },
    message: null,
    success: false
};

function authHeaders(token) {
    return {
        Authorization: `Bearer ${token}`
    };
}

async function fetchOverviewViewConfig(token) {
    const response = await fetch('/api/geraeteverwaltung/lend-overview/view-config', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'View-Konfiguration konnte nicht geladen werden.');
    }

    return response.json();
}

async function fetchLendOverviewItems(token, query = '') {
    const endpoint = query.trim()
        ? `/api/geraeteverwaltung/lend-overview?query=${encodeURIComponent(query)}`
        : '/api/geraeteverwaltung/lend-overview';

    const response = await fetch(endpoint, {
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ([]));
    if (!response.ok) {
        throw new Error(data.error || 'Ausleihen konnten nicht geladen werden.');
    }

    return Array.isArray(data) ? data : [];
}

async function confirmReturn(token, ausleiheNr) {
    const response = await fetch(`/api/geraeteverwaltung/lend-overview/${ausleiheNr}/return`, {
        method: 'POST',
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Rückgabe konnte nicht bestätigt werden.');
    }

    return data;
}

function ensureOverviewLayout(root) {
    if (!root) {
        return false;
    }

    return !!root.querySelector('.ao-accordion-section[data-section="lendOverview"]');
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function getFilteredItems() {
    return overviewState.items;
}

function getTotalPages() {
    const totalItems = getFilteredItems().length;
    return Math.max(1, Math.ceil(totalItems / overviewState.pageSize));
}

function getPagedItems() {
    const items = getFilteredItems();
    const startIndex = (overviewState.currentPage - 1) * overviewState.pageSize;
    const endIndex = startIndex + overviewState.pageSize;
    return items.slice(startIndex, endIndex);
}

function normalizeCurrentPage() {
    const totalPages = getTotalPages();
    if (overviewState.currentPage > totalPages) {
        overviewState.currentPage = totalPages;
    }
    if (overviewState.currentPage < 1) {
        overviewState.currentPage = 1;
    }
}

function renderOverviewView(root) {
    if (!root || !ensureOverviewLayout(root)) {
        return;
    }

    root.querySelectorAll('.ao-accordion-section').forEach((section) => {
        section.classList.toggle('is-open', section.dataset.section === overviewState.openSection);
    });

    const messageNode = root.querySelector('#ao-global-message');
    if (messageNode) {
        messageNode.textContent = overviewState.message || '';
        messageNode.style.display = overviewState.message ? 'block' : 'none';
        messageNode.classList.toggle('ao-success-text', Boolean(overviewState.success));
        messageNode.classList.toggle('ao-error-text', !overviewState.success);
    }

    const list = root.querySelector('#ao-overview-list');
    const placeholder = root.querySelector('#ao-overview-placeholder');
    const searchInput = root.querySelector('#ao-search-input');
    const pagination = root.querySelector('#ao-pagination');

    if (searchInput) {
        searchInput.value = overviewState.searchTerm;
    }

    normalizeCurrentPage();

    const pagedItems = getPagedItems();
    const totalPages = getTotalPages();

    if (list) {
        if (overviewState.loading.list) {
            list.innerHTML = '<li class="ao-placeholder">Ausleihen werden geladen …</li>';
        } else if (!overviewState.items.length) {
            list.innerHTML = '';
        } else {
            list.innerHTML = pagedItems.map((item) => `
                <li class="ao-item">
                    <div class="ao-item-header">
                        <div class="ao-item-title">${escapeHtml(item.mitarbeiterName || '—')}</div>
                    </div>

                    <div class="ao-item-fields">
                        <small>Gerät: ${escapeHtml(item.geraetLabel || '—')}</small>
                        <small>Ausleihdatum: ${escapeHtml(item.ausleihdatum || '—')}</small>
                        <small>Rückgabedatum: ${escapeHtml(item.vereinbartesRueckgabedatum || '—')}</small>
                        <small>Tatsächliches Rückgabedatum: ${escapeHtml(item.tatsaechlichesRueckgabedatum || 'Heute bei Bestätigung')}</small>
                    </div>

                    <div class="ao-actions">
                        <button type="button"
                                class="ao-success-button"
                                data-action="confirm-return"
                                data-ausleihe-id="${item.ausleiheNr}"
                                ${item.zurueckgegeben ? 'disabled' : ''}>
                            Erfolgreich
                        </button>
                    </div>
                </li>
            `).join('');
        }
    }

    if (placeholder) {
        placeholder.style.display = !overviewState.loading.list && overviewState.items.length === 0 ? 'block' : 'none';
    }

    if (pagination) {
        if (overviewState.loading.list || overviewState.items.length === 0) {
            pagination.innerHTML = '';
        } else {
            pagination.innerHTML = `
                <button type="button"
                        data-action="overview-prev-page"
                        ${overviewState.currentPage <= 1 ? 'disabled' : ''}>
                    Zurück
                </button>
                <span>Seite ${overviewState.currentPage} von ${totalPages}</span>
                <button type="button"
                        data-action="overview-next-page"
                        ${overviewState.currentPage >= totalPages ? 'disabled' : ''}>
                    Weiter
                </button>
            `;
        }
    }
}

async function loadOverviewItems(token, root) {
    overviewState.loading.list = true;
    overviewState.message = null;
    overviewState.success = false;
    renderOverviewView(root);

    try {
        overviewState.items = await fetchLendOverviewItems(token, overviewState.searchTerm);
    } catch (error) {
        overviewState.items = [];
        overviewState.message = error.message;
        overviewState.success = false;
    } finally {
        overviewState.loading.list = false;
        renderOverviewView(root);
    }
}

async function initializeOverviewFlow(token, getState, pageContent) {
    const tabState = getState();
    if (tabState.activeTabKey !== 'geraeteverwaltung') {
        return null;
    }

    const root = pageContent.querySelector('#gv-manager-app');
    if (!root || !ensureOverviewLayout(root)) {
        return null;
    }

    if (root.dataset.overviewInitialized === 'true') {
        renderOverviewView(root);
        return root;
    }

    try {
        await fetchOverviewViewConfig(token);
        overviewState.message = null;
        overviewState.success = false;
    } catch (error) {
        overviewState.message = error.message;
        overviewState.success = false;
    }

    overviewState.openSection = '';
    renderOverviewView(root);
    root.dataset.overviewInitialized = 'true';

    return root;
}

export function registerAusleiheUebersichtHandlers({ pageContent, getToken, redirectToLogin, getState }) {
    let initializedRoot = null;

    async function ensureInitialized() {
        const token = getToken();
        if (!token) {
            return null;
        }

        const root = pageContent.querySelector('#gv-manager-app');
        if (!root || !ensureOverviewLayout(root)) {
            return null;
        }

        if (initializedRoot !== root) {
            initializedRoot = root;
            root.dataset.overviewInitialized = '';
            await initializeOverviewFlow(token, getState, pageContent);
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
            if (action === 'toggle-section' && target.dataset.sectionKey === 'lendOverview') {
                overviewState.openSection = overviewState.openSection === 'lendOverview' ? '' : 'lendOverview';
                renderOverviewView(root);

                if (overviewState.openSection === 'lendOverview') {
                    overviewState.currentPage = 1;
                    await loadOverviewItems(token, root);
                }
                return;
            }

            if (action === 'clear-overview-search') {
                overviewState.searchTerm = '';
                overviewState.currentPage = 1;
                await loadOverviewItems(token, root);
                return;
            }

            if (action === 'overview-prev-page') {
                if (overviewState.currentPage > 1) {
                    overviewState.currentPage -= 1;
                    renderOverviewView(root);
                }
                return;
            }

            if (action === 'overview-next-page') {
                if (overviewState.currentPage < getTotalPages()) {
                    overviewState.currentPage += 1;
                    renderOverviewView(root);
                }
                return;
            }

            if (action === 'confirm-return') {
                if (overviewState.loading.action) {
                    return;
                }

                overviewState.loading.action = true;
                overviewState.message = null;
                overviewState.success = false;
                renderOverviewView(root);

                const result = await confirmReturn(token, Number(target.dataset.ausleiheId));
                overviewState.message = result.message || 'Rückgabe bestätigt.';
                overviewState.success = true;
                await loadOverviewItems(token, root);
                return;
            }
        } catch (error) {
            overviewState.message = error.message;
            overviewState.success = false;
            renderOverviewView(root);
        } finally {
            overviewState.loading.action = false;
            renderOverviewView(root);
        }
    });

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        if (!target || target.id !== 'ao-search-input') {
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

        overviewState.searchTerm = target.value || '';
        overviewState.currentPage = 1;

        if (overviewState.openSection === 'lendOverview') {
            await loadOverviewItems(token, root);
        }
    });
}