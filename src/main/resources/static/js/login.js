const statusEl = document.getElementById('status');
const loginForm = document.getElementById('loginForm');

function setStatus(message, type) {
    statusEl.textContent = message || '';
    statusEl.className = `status ${type || ''}`;
}

function storeSession(data) {
    localStorage.setItem('authToken', data.token);
    localStorage.setItem('userRole', data.role);
    localStorage.setItem('username', data.username);
}

function redirectToPage() {
    window.setTimeout(() => {
        window.location.href = '/page.html';
    }, 500);
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
        localStorage.removeItem('authToken');
        localStorage.removeItem('userRole');
        localStorage.removeItem('username');
        setStatus(errorData.error || 'Anmeldung fehlgeschlagen', 'error');
        return;
    }

    const data = await response.json();
    storeSession(data);
    setStatus('Erfolgreich eingeloggt. Weiterleitung ...', 'success');
    loginForm.reset();
    redirectToPage();
});