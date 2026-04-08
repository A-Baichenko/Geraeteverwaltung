const statusEl = document.getElementById('status');
const profileEl = document.getElementById('profile');
const logoutBtn = document.getElementById('logoutBtn');
const loginForm = document.getElementById('loginForm');

function setStatus(message, type) {
    statusEl.textContent = message || '';
    statusEl.className = `status ${type || ''}`;
}

function renderProfile(data) {
    document.getElementById('profileUsername').textContent = data.username;
    document.getElementById('profileRole').textContent = data.role;
    document.getElementById('profilePersonalNr').textContent = data.personalNr;
    document.getElementById('profileName').textContent = data.mitarbeiterName;
    document.getElementById('profileMessage').textContent = data.welcomeMessage;

    profileEl.style.display = 'block';
    logoutBtn.style.display = 'block';
}

function clearSession() {
    localStorage.removeItem('authToken');
    profileEl.style.display = 'none';
    logoutBtn.style.display = 'none';
}

async function fetchCurrentUser() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        return;
    }

    const response = await fetch('/api/auth/me', {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (!response.ok) {
        clearSession();
        setStatus('Session abgelaufen. Bitte neu anmelden.', 'error');
        return;
    }

    const data = await response.json();
    renderProfile(data);
    setStatus(`Angemeldet: ${data.welcomeMessage}`, 'success');
}

loginForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    setStatus('Anmeldung läuft ...');

    const formData = new FormData(loginForm);
    const payload = {
        username: formData.get('username'),
        password: formData.get('password')
    };

    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        const errorData = await response.json();
        clearSession();
        setStatus(errorData.error || 'Anmeldung fehlgeschlagen', 'error');
        return;
    }

    const data = await response.json();
    localStorage.setItem('authToken', data.token);
    renderProfile(data);
    setStatus(`Erfolgreich eingeloggt: ${data.welcomeMessage}`, 'success');
    loginForm.reset();
});

logoutBtn.addEventListener('click', () => {
    clearSession();
    setStatus('Du wurdest ausgeloggt.', 'success');
});

fetchCurrentUser();