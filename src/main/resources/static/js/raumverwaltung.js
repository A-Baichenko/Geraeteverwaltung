const state = {
    items: [],
    search: '',
    editRaumNr: null
};

function authHeaders(token) {
    return { Authorization: `Bearer ${token}` };
}

async function ladeRaeume(token) {
    const url = state.search.trim()
        ? `/api/raumverwaltung?suchbegriff=${encodeURIComponent(state.search.trim())}`
        : '/api/raumverwaltung';

    const response = await fetch(url, { headers: authHeaders(token) });
    const data = await response.json().catch(() => []);
    if (!response.ok) {
        throw new Error(data.error || 'Räume konnten nicht geladen werden.');
    }

    state.items = Array.isArray(data) ? data : [];
}

async function speichereRaum(token) {
    const gebaeudeInput = document.getElementById('rv-gebaeude');
    const raumNrInput = document.getElementById('rv-raum-nr');

    const payload = {
        gebaeude: gebaeudeInput?.value?.trim(),
        raumNr: Number(raumNrInput?.value)
    };

    const istBearbeitung = state.editRaumNr !== null;
    const response = await fetch(
        istBearbeitung ? `/api/raumverwaltung/${state.editRaumNr}` : '/api/raumverwaltung',
        {
            method: istBearbeitung ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders(token)
            },
            body: JSON.stringify(istBearbeitung ? { gebaeude: payload.gebaeude } : payload)
        }
    );

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Raum konnte nicht gespeichert werden.');
    }
}

async function loescheRaum(token, raumNr) {
    const response = await fetch(`/api/raumverwaltung/${raumNr}`, {
        method: 'DELETE',
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Raum konnte nicht gelöscht werden.');
    }
}

function resetForm() {
    state.editRaumNr = null;

    const gebaeudeInput = document.getElementById('rv-gebaeude');
    const raumNrInput = document.getElementById('rv-raum-nr');
    const hint = document.getElementById('rv-form-hint');

    if (gebaeudeInput) {
        gebaeudeInput.value = '';
    }
    if (raumNrInput) {
        raumNrInput.value = '';
        raumNrInput.readOnly = false;
    }
    if (hint) {
        hint.textContent = '';
    }
}

function fillForm(item) {
    state.editRaumNr = item.raumNr;

    const gebaeudeInput = document.getElementById('rv-gebaeude');
    const raumNrInput = document.getElementById('rv-raum-nr');
    const hint = document.getElementById('rv-form-hint');

    if (gebaeudeInput) {
        gebaeudeInput.value = item.gebaeude || '';
    }
    if (raumNrInput) {
        raumNrInput.value = String(item.raumNr);
        raumNrInput.readOnly = true;
    }
    if (hint) {
        hint.textContent = `Bearbeiten aktiv für Raum ${item.raumNr}.`;
    }
}

function renderListe() {
    const list = document.getElementById('rv-result-list');
    if (!list) {
        return;
    }

    if (!state.items.length) {
        list.innerHTML = '<li class="empty-list-item">Keine Treffer gefunden.</li>';
        return;
    }

    list.innerHTML = state.items.map((item) => `
        <li class="management-item">
            <div>
                <strong>${item.gebaeude}</strong>
                <small>Raumnummer ${item.raumNr}</small>
            </div>
            <div class="management-item-actions">
                <button type="button" data-action="rv-edit" data-raum-nr="${item.raumNr}">Bearbeiten</button>
                <button type="button" class="danger-button" data-action="rv-delete" data-raum-nr="${item.raumNr}">Löschen</button>
            </div>
        </li>
    `).join('');
}

async function initIfNeeded(token) {
    const root = document.getElementById('rv-root');
    if (!root || root.dataset.initialized === 'true') {
        return;
    }

    root.dataset.initialized = 'true';
    resetForm();
    await ladeRaeume(token);
    renderListe();
}

export function registerRaumverwaltungHandlers({ pageContent, getToken, redirectToLogin, getState }) {
    pageContent.addEventListener('click', async (event) => {
        const target = event.target.closest('[data-action^="rv-"]');
        if (!target) {
            return;
        }

        const token = getToken();
        if (!token) {
            redirectToLogin();
            return;
        }

        try {
            if (target.dataset.action === 'rv-reset') {
                resetForm();
                return;
            }

            if (target.dataset.action === 'rv-save') {
                await speichereRaum(token);
                await ladeRaeume(token);
                renderListe();
                resetForm();
                return;
            }

            if (target.dataset.action === 'rv-edit') {
                const item = state.items.find((entry) => String(entry.raumNr) === target.dataset.raumNr);
                if (item) {
                    fillForm(item);
                }
                return;
            }

            if (target.dataset.action === 'rv-delete') {
                if (!window.confirm('Raum wirklich löschen?')) {
                    return;
                }
                await loescheRaum(token, Number(target.dataset.raumNr));
                await ladeRaeume(token);
                renderListe();
                if (String(state.editRaumNr) === target.dataset.raumNr) {
                    resetForm();
                }
            }
        } catch (error) {
            alert(error.message || 'Aktion fehlgeschlagen.');
        }
    });

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        if (target.id !== 'rv-search') {
            return;
        }

        const token = getToken();
        if (!token) {
            return;
        }

        try {
            state.search = target.value || '';
            await ladeRaeume(token);
            renderListe();
        } catch (error) {
            alert(error.message || 'Suche fehlgeschlagen.');
        }
    });

    const observer = new MutationObserver(async () => {
        if (getState().activeTabKey !== 'raumverwaltung') {
            return;
        }

        const token = getToken();
        if (!token) {
            redirectToLogin();
            return;
        }

        try {
            await initIfNeeded(token);
        } catch (error) {
            const list = document.getElementById('rv-result-list');
            if (list) {
                list.innerHTML = `<li class="empty-list-item">${error.message || 'Fehler beim Laden.'}</li>`;
            }
        }
    });

    observer.observe(pageContent, { childList: true, subtree: true });
}