const topNav = document.getElementById('topNav');
const pageContent = document.getElementById('pageContent');
const tabs = [
    { key: 'home', label: 'Home' },
    { key: 'p1', label: 'Platzhalter 1' },
    { key: 'mitarbeiter', label: 'Mitarbeiter Verwalten' },
    { key: 'p2', label: 'Platzhalter 2' },
    { key: 'p3', label: 'Platzhalter 3' }
];

function getToken() {
    return localStorage.getItem('authToken');
}

function redirectToLogin() {
    window.location.href = '/index.html';
}

async function getCurrentUser(token) {
    const response = await fetch('/api/auth/me', {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error('Nicht eingeloggt');
    }

    return response.json();
}

async function loadHomeHtml(role, token) {
    const response = await fetch('/api/page/home-content', {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error('Home konnte nicht geladen werden');
    }

    const data = await response.json();
    if (role === 'ADMIN') {
        return data.html;
    }

    return '<div class="placeholder">Mitarbeiter Ansicht: Inhalt folgt aus dem Backend.</div>';
}

function renderNav(role, activeKey) {
    topNav.innerHTML = '';

    tabs.forEach((tab) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'tab';
        button.textContent = tab.label;

        if (tab.key === activeKey) {
            button.classList.add('active');
        }

        if (role === 'MITARBEITER' && tab.key !== 'mitarbeiter') {
            button.classList.add('disabled');
        }

        topNav.appendChild(button);
    });
}

async function init() {
    const token = getToken();
    if (!token) {
        redirectToLogin();
        return;
    }

    try {
        const user = await getCurrentUser(token);
        const role = user.role;
        const activeTab = role === 'MITARBEITER' ? 'mitarbeiter' : 'home';
        renderNav(role, activeTab);

        if (role === 'ADMIN') {
            const html = await loadHomeHtml(role, token);
            pageContent.innerHTML = html;
            return;
        }

        pageContent.innerHTML = '<div class="placeholder">Mitarbeiter Verwalten: Inhalte werden hier geladen.</div>';
    } catch (error) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userRole');
        redirectToLogin();
    }
}

init();