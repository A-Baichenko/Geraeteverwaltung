const state = {
    items: [],
    search: '',
    editPersonalNr: null
};

function authHeaders(token) {
    return { Authorization: `Bearer ${token}` };
}

async function ladeMitarbeiter(token) {
    const url = state.search.trim()
        ? `/api/mitarbeiterverwaltung?suchbegriff=${encodeURIComponent(state.search.trim())}`
        : '/api/mitarbeiterverwaltung';

    const response = await fetch(url, { headers: authHeaders(token) });
    const data = await response.json().catch(() => []);
    if (!response.ok) {
        throw new Error(data.error || 'Mitarbeiter konnten nicht geladen werden.');
    }

    state.items = Array.isArray(data) ? data : [];
}

async function speichereMitarbeiter(token) {
    const personalNrInput = document.getElementById('mv-personalnr');
    const vornameInput = document.getElementById('mv-vorname');
    const nachnameInput = document.getElementById('mv-nachname');
    const anredeInput = document.getElementById('mv-anrede');

    const payload = {
        personalNr: Number(personalNrInput?.value),
        vorname: vornameInput?.value?.trim(),
        nachname: nachnameInput?.value?.trim(),
        anrede: anredeInput?.value
    };

    const istBearbeitung = state.editPersonalNr !== null;
    const response = await fetch(
        istBearbeitung ? `/api/mitarbeiterverwaltung/${state.editPersonalNr}` : '/api/mitarbeiterverwaltung',
        {
            method: istBearbeitung ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders(token)
            },
            body: JSON.stringify(istBearbeitung
                ? { vorname: payload.vorname, nachname: payload.nachname, anrede: payload.anrede }
                : payload)
        }
    );

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Mitarbeiter konnte nicht gespeichert werden.');
    }
}

async function loescheMitarbeiter(token, personalNr) {
    const response = await fetch(`/api/mitarbeiterverwaltung/${personalNr}`, {
        method: 'DELETE',
        headers: authHeaders(token)
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.error || 'Mitarbeiter konnte nicht gelöscht werden.');
    }
}

function resetForm() {
    state.editPersonalNr = null;
    const personalNrInput = document.getElementById('mv-personalnr');
    const vornameInput = document.getElementById('mv-vorname');
    const nachnameInput = document.getElementById('mv-nachname');
    const anredeInput = document.getElementById('mv-anrede');
    const hint = document.getElementById('mv-form-hint');

    if (personalNrInput) {
        personalNrInput.value = '';
        personalNrInput.readOnly = false;
    }
    if (vornameInput) {
        vornameInput.value = '';
    }
    if (nachnameInput) {
        nachnameInput.value = '';
    }
    if (anredeInput) {
        anredeInput.value = 'HERR';
    }
    if (hint) {
        hint.textContent = '';
    }
}

function fillForm(item) {
    state.editPersonalNr = item.personalNr;

    const personalNrInput = document.getElementById('mv-personalnr');
    const vornameInput = document.getElementById('mv-vorname');
    const nachnameInput = document.getElementById('mv-nachname');
    const anredeInput = document.getElementById('mv-anrede');
    const hint = document.getElementById('mv-form-hint');

    if (personalNrInput) {
        personalNrInput.value = String(item.personalNr);
        personalNrInput.readOnly = true;
    }
    if (vornameInput) {
        vornameInput.value = item.vorname || '';
    }
    if (nachnameInput) {
        nachnameInput.value = item.nachname || '';
    }
    if (anredeInput) {
        anredeInput.value = item.anrede || 'HERR';
    }
    if (hint) {
        hint.textContent = `Bearbeiten aktiv für Personalnummer ${item.personalNr}.`;
    }
}

function renderListe() {
    const list = document.getElementById('mv-result-list');
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
                <strong>${item.vorname} ${item.nachname}</strong>
                <small>${item.anrede} • Personalnummer ${item.personalNr}</small>
            </div>
            <div class="management-item-actions">
                <button type="button" data-action="mv-edit" data-personal-nr="${item.personalNr}">Bearbeiten</button>
                <button type="button" class="danger-button" data-action="mv-delete" data-personal-nr="${item.personalNr}">Löschen</button>
            </div>
        </li>
    `).join('');
}

async function initIfNeeded(token) {
    const root = document.getElementById('mv-root');
    if (!root || root.dataset.initialized === 'true') {
        return;
    }

    root.dataset.initialized = 'true';
    resetForm();
    await ladeMitarbeiter(token);
    renderListe();
}

export function registerMitarbeiterverwaltungHandlers({ pageContent, getToken, redirectToLogin, getState }) {
    pageContent.addEventListener('click', async (event) => {
        const target = event.target.closest('[data-action^="mv-"]');
        if (!target) {
            return;
        }

        const token = getToken();
        if (!token) {
            redirectToLogin();
            return;
        }

        try {
            if (target.dataset.action === 'mv-reset') {
                resetForm();
                return;
            }

            if (target.dataset.action === 'mv-save') {
                await speichereMitarbeiter(token);
                await ladeMitarbeiter(token);
                renderListe();
                resetForm();
                return;
            }

            if (target.dataset.action === 'mv-edit') {
                const item = state.items.find((entry) => String(entry.personalNr) === target.dataset.personalNr);
                if (item) {
                    fillForm(item);
                }
                return;
            }

            if (target.dataset.action === 'mv-delete') {
                if (!window.confirm('Mitarbeiter wirklich löschen?')) {
                    return;
                }
                await loescheMitarbeiter(token, Number(target.dataset.personalNr));
                await ladeMitarbeiter(token);
                renderListe();
                if (String(state.editPersonalNr) === target.dataset.personalNr) {
                    resetForm();
                }
            }
        } catch (error) {
            alert(error.message || 'Aktion fehlgeschlagen.');
        }
    });

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        if (target.id !== 'mv-search') {
            return;
        }

        const token = getToken();
        if (!token) {
            return;
        }

        try {
            state.search = target.value || '';
            await ladeMitarbeiter(token);
            renderListe();
        } catch (error) {
            alert(error.message || 'Suche fehlgeschlagen.');
        }
    });

    const observer = new MutationObserver(async () => {
        if (getState().activeTabKey !== 'mitarbeiterverwaltung') {
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
            const list = document.getElementById('mv-result-list');
            if (list) {
                list.innerHTML = `<li class="empty-list-item">${error.message || 'Fehler beim Laden.'}</li>`;
            }
        }
    });

    observer.observe(pageContent, { childList: true, subtree: true });
}