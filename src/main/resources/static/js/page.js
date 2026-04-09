import { registerReservierungAusleiheHandlers } from './reservierung-ausleihe.js';

const topNav = document.getElementById('topNav');
const pageContent = document.getElementById('pageContent');

const tabs = [
    { key: 'home', label: 'Home (Übersicht)' },
    { key: 'reservierung-ausleihe', label: 'Reservieren und Ausleihen' },
    { key: 'geraeteverwaltung', label: 'Geräteverwaltung' },
    { key: 'mitarbeiterverwaltung', label: 'Mitarbeiterverwaltung' },
    { key: 'raumverwaltung', label: 'Raumverwaltung' },
    { key: 'placeholder', label: '—' }
];

let activeTabKey = null;
let currentSubView = 'menu';
let currentAusleiheView = 'form';
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

    let url = `/api/page/content/${tabKey}`;

    if (tabKey === 'reservierung-ausleihe') {
        url += `?view=${currentSubView}&sub=${currentAusleiheView}`;
    }

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

    if (activeTabKey !== tabKey) {
        currentSubView = 'menu';
        currentAusleiheView = 'form';
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

pageContent.addEventListener('click', async (event) => {
    const target = event.target.closest(
        '#btn-ausleihen, #back-to-reservierung-menu, #btn-ausleihe-speichern, #raum, #geraet, #back-to-form'
    );

    if (!target) {
        return;
    }

    const token = getToken();
    if (!token) {
        redirectToLogin();
        return;
    }

    if (target.id === 'btn-ausleihen') {
        currentSubView = 'ausleihe';
        currentAusleiheView = 'form';
        await switchTab(activeTabKey, token, allowedTabKeys);
        return;
    }

    if (target.id === 'raum') {
        currentSubView = 'ausleihe';
        currentAusleiheView = 'raum';
        await switchTab(activeTabKey, token, allowedTabKeys);
        return;
    }

    if (target.id === 'geraet') {
        currentSubView = 'ausleihe';
        currentAusleiheView = 'geraet';
        await switchTab(activeTabKey, token, allowedTabKeys);
        return;
    }

    if (target.id === 'back-to-form') {
        currentSubView = 'ausleihe';
        currentAusleiheView = 'form';
        await switchTab(activeTabKey, token, allowedTabKeys);
        return;
    }

    if (target.id === 'back-to-reservierung-menu') {
        currentSubView = 'menu';
        currentAusleiheView = 'form';
        await switchTab(activeTabKey, token, allowedTabKeys);
        return;
    }
    
});

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
        currentSubView,
        currentAusleiheView,
        allowedTabKeys
    }),
    setState: ({ currentSubView: newSubView, currentAusleiheView: newAusleiheView }) => {
        if (newSubView !== undefined) {
            currentSubView = newSubView;
        }
        if (newAusleiheView !== undefined) {
            currentAusleiheView = newAusleiheView;
        }
    },
    switchTab
});

init();