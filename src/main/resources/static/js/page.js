import { registerReservierungAusleiheHandlers } from './reservierung.js';
import { registerGeraeteverwaltungHandlers } from './geraeteverwaltung/reservierungsAntraege.js';
import { registerMitarbeiterverwaltungHandlers } from './mitarbeiterverwaltung.js';
import { registerRaumverwaltungHandlers } from './raumverwaltung.js';

const topNav = document.getElementById('topNav');
const pageContent = document.getElementById('pageContent');

const tabs = [
    { key: 'home', label: 'Home (Übersicht)' },
    { key: 'reservierung', label: 'Reservieren' },
    { key: 'geraeteverwaltung', label: 'Geräteverwaltung' },
    { key: 'mitarbeiterverwaltung', label: 'Mitarbeiterverwaltung' },
    { key: 'raumverwaltung', label: 'Raumverwaltung' },
    { key: 'placeholder', label: '—' }
];

let activeTabKey = null;
let allowedTabKeys = [];

function getToken() {
    return localStorage.getItem('authToken');
}

function redirectToLogin() {
    window.location.href = '/index.html';
}

async function getTabConfig(token) {
    const response = await fetch('/api/page/tab-config', {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error('Tab-Konfiguration konnte nicht geladen werden');
    }

    return response.json();
}

async function loadTabHtml(tabKey, token) {
    if (tabKey === 'placeholder') {
        return '<div class="placeholder">Platzhalter für zukünftige Erweiterungen.</div>';
    }

    const url = `/api/page/content/${tabKey}`;

    const response = await fetch(url, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error(`Inhalt für Tab ${tabKey} konnte nicht geladen werden`);
    }

    const data = await response.json();
    return data.html;
}

function renderNav(allowedTabs, onTabSelected) {
    topNav.innerHTML = '';

    tabs.forEach((tab) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'tab';
        button.textContent = tab.label;

        const isAllowed = allowedTabs.includes(tab.key);
        const isPlaceholder = tab.key === 'placeholder';

        if (tab.key === activeTabKey) {
            button.classList.add('active');
        }

        if (!isAllowed || isPlaceholder) {
            button.classList.add('disabled');
            button.disabled = true;
        } else {
            button.addEventListener('click', () => onTabSelected(tab.key));
        }

        topNav.appendChild(button);
    });
}

async function switchTab(tabKey, token, allowedTabs) {
    if (!allowedTabs.includes(tabKey)) {
        return;
    }

    activeTabKey = tabKey;
    renderNav(allowedTabs, (nextTabKey) => switchTab(nextTabKey, token, allowedTabs));

    try {
        pageContent.innerHTML = await loadTabHtml(tabKey, token);
    } catch (error) {
        console.error(error);
        pageContent.innerHTML = '<div class="placeholder">Inhalte konnten nicht geladen werden.</div>';
    }
}

function firstAllowedTab(allowedTabs) {
    for (const tab of tabs) {
        if (allowedTabs.includes(tab.key)) {
            return tab.key;
        }
    }
    return null;
}

async function init() {
    const token = getToken();
    if (!token) {
        redirectToLogin();
        return;
    }

    try {
        const config = await getTabConfig(token);
        allowedTabKeys = config.allowedTabs || [];

        const initialTab = firstAllowedTab(allowedTabKeys);
        if (!initialTab) {
            pageContent.innerHTML = '<div class="placeholder">Keine freigeschalteten Tabs gefunden.</div>';
            renderNav([], () => {});
            return;
        }

        await switchTab(initialTab, token, allowedTabKeys);
    } catch (error) {
        console.error(error);
        localStorage.removeItem('authToken');
        localStorage.removeItem('userRole');
        redirectToLogin();
    }
}

registerReservierungAusleiheHandlers({
    pageContent,
    getToken,
    redirectToLogin,
    getState: () => ({
        activeTabKey,
        allowedTabKeys
    })
});

registerGeraeteverwaltungHandlers({
    pageContent,
    getToken,
    redirectToLogin,
    getState: () => ({
        activeTabKey,
        allowedTabKeys
    })
});

registerMitarbeiterverwaltungHandlers({
    pageContent,
    getToken,
    redirectToLogin,
    getState: () => ({
        activeTabKey,
        allowedTabKeys
    })
});

registerRaumverwaltungHandlers({
    pageContent,
    getToken,
    redirectToLogin,
    getState: () => ({
        activeTabKey,
        allowedTabKeys
    })
});

init();