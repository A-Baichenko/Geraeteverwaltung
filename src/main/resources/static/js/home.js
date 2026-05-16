const homeState = {
    layoutInjected: false,
    items: {
        available: [],
        reserved: [],
        lent: [],
        notLendable: []
    },
    filters: {
        available: '',
        reserved: '',
        lent: '',
        notLendable: ''
    },
    loading: false,
    error: null
};

function authHeaders(token) {
    return {
        Authorization: `Bearer ${token}`
    };
}

async function fetchHomeOverview(token) {
    const response = await fetch('/api/home/overview', {
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Übersicht konnte nicht geladen werden.');
    }

    return data;
}

function ensureHomeLayout(root) {
    return !!root?.querySelector('#hm-overview-app');
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function filterItems(items, searchTerm) {
    const term = (searchTerm || '').trim().toLowerCase();
    if (!term) {
        return items;
    }

    return items.filter((item) => {
        const haystack = [
            item.title,
            item.subtitle,
            item.meta1,
            item.meta2
        ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase();

        return haystack.includes(term);
    });
}

function renderColumn(root, key, listId, placeholderId) {
    const list = root.querySelector(`#${listId}`);
    const placeholder = root.querySelector(`#${placeholderId}`);

    if (!list || !placeholder) {
        return;
    }

    const filtered = filterItems(homeState.items[key], homeState.filters[key]);

    if (homeState.loading) {
        list.innerHTML = '<li class="hm-placeholder">Einträge werden geladen …</li>';
        placeholder.style.display = 'none';
        return;
    }

    if (!filtered.length) {
        list.innerHTML = '';
        placeholder.style.display = 'block';
        return;
    }

    placeholder.style.display = 'none';
    list.innerHTML = filtered.map((item) => `
        <li class="hm-item">
            <strong>${escapeHtml(item.title)}</strong>
            ${item.subtitle ? `<div>${escapeHtml(item.subtitle)}</div>` : ''}
            ${item.meta1 ? `<small>${escapeHtml(item.meta1)}</small>` : ''}
            ${item.meta2 ? `<small>${escapeHtml(item.meta2)}</small>` : ''}
        </li>
    `).join('');
}

function renderHomeOverview(root) {
    if (!root || !ensureHomeLayout(root)) {
        return;
    }

    renderColumn(root, 'available', 'hm-list-available', 'hm-placeholder-available');
    renderColumn(root, 'reserved', 'hm-list-reserved', 'hm-placeholder-reserved');
    renderColumn(root, 'lent', 'hm-list-lent', 'hm-placeholder-lent');
    renderColumn(root, 'notLendable', 'hm-list-not-lendable', 'hm-placeholder-not-lendable');
}

async function loadHomeOverview(root, token) {
    homeState.loading = true;
    homeState.error = null;
    renderHomeOverview(root);

    try {
        const data = await fetchHomeOverview(token);
        homeState.items.available = Array.isArray(data.available) ? data.available : [];
        homeState.items.reserved = Array.isArray(data.reserved) ? data.reserved : [];
        homeState.items.lent = Array.isArray(data.lent) ? data.lent : [];
        homeState.items.notLendable = Array.isArray(data.notLendable) ? data.notLendable : [];
    } catch (error) {
        homeState.error = error.message;
        homeState.items.available = [];
        homeState.items.reserved = [];
        homeState.items.lent = [];
        homeState.items.notLendable = [];
    } finally {
        homeState.loading = false;
        renderHomeOverview(root);
    }
}

export function registerHomeOverviewHandlers({ pageContent, getToken, redirectToLogin, getState }) {
    let initializedRoot = null;

    async function ensureInitialized() {
        const tabState = getState();
        if (tabState.activeTabKey !== 'home') {
            return null;
        }

        const root = pageContent.querySelector('#hm-overview-app')?.closest('#home-overview');
        if (!root || !ensureHomeLayout(root)) {
            return null;
        }

        if (initializedRoot !== root) {
            initializedRoot = root;

            const token = getToken();
            if (!token) {
                redirectToLogin();
                return null;
            }

            await loadHomeOverview(root, token);
        }

        return root;
    }

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        if (!target) {
            return;
        }

        const root = await ensureInitialized();
        if (!root) {
            return;
        }

        if (target.id === 'hm-search-available') {
            homeState.filters.available = target.value || '';
        }

        if (target.id === 'hm-search-reserved') {
            homeState.filters.reserved = target.value || '';
        }

        if (target.id === 'hm-search-lent') {
            homeState.filters.lent = target.value || '';
        }

        if (target.id === 'hm-search-not-lendable') {
            homeState.filters.notLendable = target.value || '';
        }

        renderHomeOverview(root);
    });

    const observer = new MutationObserver(async () => {
        const token = getToken();
        if (!token) {
            return;
        }

        await ensureInitialized();
    });

    observer.observe(pageContent, { childList: true, subtree: true });
}