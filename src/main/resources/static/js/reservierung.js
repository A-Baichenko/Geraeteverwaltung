const state = {
    form: {
        reservierungsNr: null,
        personalNr: null,
        mitarbeiterName: '',
        geraetetypId: null,
        geraetetypName: '',
        ausleihdatum: '',
        rueckgabedatum: ''
    },
    kalender: {
        unavailablePeriods: []
    },
    suche: {
        suchbegriff: '',
        ergebnisse: []
    },
    liste: {
        reservierungen: []
    }
};

let ausleihdatumPicker = null;
let rueckgabedatumPicker = null;

function authHeaders(token) {
    return {
        Authorization: `Bearer ${token}`
    };
}

async function ladeCurrentMitarbeiter(token) {
    const response = await fetch('/api/auth/me', { headers: authHeaders(token) });
    if (!response.ok) {
        return null;
    }
    return response.json();
}

async function ladeGeraetetypen(token, suchbegriff = '') {
    const url = suchbegriff.trim()
        ? `/api/reservierung/geraetetypen?suchbegriff=${encodeURIComponent(suchbegriff)}`
        : '/api/reservierung/geraetetypen';

    const response = await fetch(url, { headers: authHeaders(token) });
    if (!response.ok) {
        return [];
    }
    return response.json();
}

async function ladeEigeneReservierungen(token) {
    const response = await fetch('/api/reservierung/reservierungen/me', {
        headers: authHeaders(token)
    });

    if (!response.ok) {
        return [];
    }
    return response.json();
}

async function ladeNichtVerfuegbareZeitraeume(token) {
    if (!state.form.geraetetypId) {
        state.kalender.unavailablePeriods = [];
        aktualisiereDatepickerSperren();
        return;
    }

    const heute = new Date();
    const start = heute.toISOString().slice(0, 10);
    const endeDatum = new Date(heute);
    endeDatum.setMonth(endeDatum.getMonth() + 12);
    const end = endeDatum.toISOString().slice(0, 10);

    const url = new URL(`/api/reservierung/device-types/${state.form.geraetetypId}/unavailable-periods`, window.location.origin);
    url.searchParams.set('start', start);
    url.searchParams.set('end', end);
    if (state.form.reservierungsNr) {
        url.searchParams.set('excludeReservierungId', String(state.form.reservierungsNr));
    }

    const response = await fetch(url.toString(), { headers: authHeaders(token) });
    if (!response.ok) {
        state.kalender.unavailablePeriods = [];
        aktualisiereDatepickerSperren();
        return;
    }

    state.kalender.unavailablePeriods = await response.json();
    aktualisiereDatepickerSperren();
}

function leseFormular() {
    return {
        ausleihdatum: document.getElementById('ausleihdatum')?.value || '',
        rueckgabedatum: document.getElementById('rueckgabedatum')?.value || ''
    };
}

function schreibeFormular() {
    const mitarbeiterInput = document.getElementById('mitarbeiter');
    const geraetetypInput = document.getElementById('geraetetyp');
    const ausleihdatumInput = document.getElementById('ausleihdatum');
    const rueckgabedatumInput = document.getElementById('rueckgabedatum');
    const speichernButton = document.getElementById('btn-reservierung-speichern');
    const abbrechenButton = document.getElementById('btn-reservierung-abbrechen');

    if (mitarbeiterInput) mitarbeiterInput.value = state.form.mitarbeiterName;
    if (geraetetypInput) geraetetypInput.value = state.form.geraetetypName;
    if (ausleihdatumInput) ausleihdatumInput.value = state.form.ausleihdatum;
    if (rueckgabedatumInput) rueckgabedatumInput.value = state.form.rueckgabedatum;

    if (speichernButton) {
        speichernButton.textContent = state.form.reservierungsNr ? 'Reservierung aktualisieren' : 'Reservieren';
    }

    if (abbrechenButton) {
        abbrechenButton.classList.toggle('hidden', !state.form.reservierungsNr);
    }

    synchronisierePickerWerte();
}

function resetFormular() {
    state.form.reservierungsNr = null;
    state.form.geraetetypId = null;
    state.form.geraetetypName = '';
    state.form.ausleihdatum = '';
    state.form.rueckgabedatum = '';
    state.kalender.unavailablePeriods = [];
    schreibeFormular();
    aktualisiereDatepickerSperren();
}

function rendereGeraetetypListe() {
    const list = document.getElementById('geraetetyp-liste');
    if (!list) {
        return;
    }

    if (!state.suche.ergebnisse.length) {
        list.innerHTML = '<li class="empty-list-item">Keine Gerätetypen gefunden.</li>';
        return;
    }

    list.innerHTML = state.suche.ergebnisse.map((typ) => `
        <li>
            <button
                type="button"
                class="list-item-button ${state.form.geraetetypId === typ.id ? 'active-selection' : ''}"
                data-geraetetyp-id="${typ.id}"
                data-geraetetyp-name="${typ.anzeigeName}"
            >
                <span>${typ.anzeigeName}</span>
                <small>${typ.kategorie ?? ''}</small>
            </button>
        </li>
    `).join('');
}

function rendereReservierungsliste() {
    const list = document.getElementById('meine-reservierungen-liste');
    if (!list) {
        return;
    }

    if (!state.liste.reservierungen.length) {
        list.innerHTML = '<li class="empty-list-item">Noch keine Reservierungen vorhanden.</li>';
        return;
    }

    list.innerHTML = state.liste.reservierungen.map((eintrag) => `
        <li class="reservation-item">
            <div>
                <strong>${eintrag.geraetetypName}</strong>
                <div>${eintrag.ausleihdatum} bis ${eintrag.rueckgabedatum}</div>
            </div>
            <div class="reservation-actions">
                <button type="button" data-edit-id="${eintrag.reservierungsNr}">Bearbeiten</button>
                <button type="button" class="danger-button" data-delete-id="${eintrag.reservierungsNr}">Löschen</button>
            </div>
        </li>
    `).join('');
}

function validiereForm(formular) {
    if (!state.form.personalNr) {
        return 'Mitarbeiter konnte nicht automatisch erkannt werden.';
    }
    if (!state.form.geraetetypId) {
        return 'Bitte zuerst einen Gerätetyp auswählen.';
    }
    if (!formular.ausleihdatum || !formular.rueckgabedatum) {
        return 'Bitte Ausleihdatum und Rückgabedatum eingeben.';
    }
    if (formular.ausleihdatum > formular.rueckgabedatum) {
        return 'Startdatum darf nicht nach dem Rückgabedatum liegen.';
    }
    if (ueberschneidetGesperrteZeitraeume(formular.ausleihdatum, formular.rueckgabedatum)) {
        return 'Für diesen Gerätetyp ist im gewählten Zeitraum kein freies Gerät verfügbar.';
    }
    return null;
}

async function speichereReservierung(token) {
    const formular = leseFormular();
    const fehler = validiereForm(formular);
    if (fehler) {
        alert(fehler);
        return;
    }

    const requestBody = {
        geraetetypId: Number(state.form.geraetetypId),
        personalNr: Number(state.form.personalNr),
        ausleihdatum: formular.ausleihdatum,
        rueckgabedatum: formular.rueckgabedatum
    };

    const istBearbeitung = Boolean(state.form.reservierungsNr);
    const url = istBearbeitung
        ? `/api/reservierung/reservierungen/${state.form.reservierungsNr}`
        : '/api/reservierung/reservierungen';

    const response = await fetch(url, {
        method: istBearbeitung ? 'PUT' : 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders(token)
        },
        body: JSON.stringify(requestBody)
    });

    const data = await response.json();
    if (!response.ok) {
        alert(data.error || 'Reservierung konnte nicht gespeichert werden.');
        return;
    }

    alert(istBearbeitung ? 'Reservierung aktualisiert.' : 'Reservierung erstellt.');
    await initialisiereTab(token, false);
    resetFormular();
}

async function loescheReservierung(token, reservierungsNr) {
    const bestaetigt = window.confirm('Reservierung wirklich löschen?');
    if (!bestaetigt) {
        return;
    }

    const response = await fetch(`/api/reservierung/reservierungen/${reservierungsNr}`, {
        method: 'DELETE',
        headers: authHeaders(token)
    });

    if (!response.ok) {
        const data = await response.json();
        alert(data.error || 'Reservierung konnte nicht gelöscht werden.');
        return;
    }

    await initialisiereTab(token, false);
    if (state.form.reservierungsNr === reservierungsNr) {
        resetFormular();
    }
}

function bearbeiteReservierung(reservierungsNr) {
    const reservierung = state.liste.reservierungen.find((eintrag) => eintrag.reservierungsNr === reservierungsNr);
    if (!reservierung) {
        return;
    }

    state.form.reservierungsNr = reservierung.reservierungsNr;
    state.form.geraetetypId = reservierung.geraetetypId;
    state.form.geraetetypName = reservierung.geraetetypName;
    state.form.ausleihdatum = reservierung.ausleihdatum;
    state.form.rueckgabedatum = reservierung.rueckgabedatum;

    schreibeFormular();
    rendereGeraetetypListe();
}

function initialisiereDatepicker() {
    if (typeof window.flatpickr !== 'function') {
        return;
    }

    const commonConfig = {
        dateFormat: 'Y-m-d',
        locale: 'de',
        disableMobile: true
    };

    ausleihdatumPicker?.destroy();
    rueckgabedatumPicker?.destroy();

    ausleihdatumPicker = window.flatpickr('#ausleihdatum', {
        ...commonConfig,
        onChange: (_, dateStr) => {
            state.form.ausleihdatum = dateStr || '';
        }
    });

    rueckgabedatumPicker = window.flatpickr('#rueckgabedatum', {
        ...commonConfig,
        onChange: (_, dateStr) => {
            state.form.rueckgabedatum = dateStr || '';
        }
    });

    synchronisierePickerWerte();
    aktualisiereDatepickerSperren();
}

function synchronisierePickerWerte() {
    if (ausleihdatumPicker) {
        ausleihdatumPicker.setDate(state.form.ausleihdatum || null, false);
    }
    if (rueckgabedatumPicker) {
        rueckgabedatumPicker.setDate(state.form.rueckgabedatum || null, false);
    }
}

function aktualisiereDatepickerSperren() {
    if (!ausleihdatumPicker || !rueckgabedatumPicker) {
        return;
    }

    const disableConfig = state.kalender.unavailablePeriods.map((periode) => ({
        from: periode.start,
        to: periode.end
    }));

    ausleihdatumPicker.set('disable', disableConfig);
    rueckgabedatumPicker.set('disable', disableConfig);
}

function ueberschneidetGesperrteZeitraeume(start, ende) {
    if (!start || !ende) {
        return false;
    }

    return state.kalender.unavailablePeriods.some((periode) => !(ende < periode.start || start > periode.end));
}

async function initialisiereTab(token, includeMitarbeiter = true) {
    if (includeMitarbeiter || !state.form.personalNr) {
        const user = await ladeCurrentMitarbeiter(token);
        if (user) {
            state.form.personalNr = user.personalNr;
            state.form.mitarbeiterName = user.mitarbeiterName;
        }
    }

    state.suche.ergebnisse = await ladeGeraetetypen(token, state.suche.suchbegriff);
    state.liste.reservierungen = await ladeEigeneReservierungen(token);
    await ladeNichtVerfuegbareZeitraeume(token);

    schreibeFormular();
    rendereGeraetetypListe();
    rendereReservierungsliste();
}

export function registerReservierungAusleiheHandlers({
                                                         pageContent,
                                                         getToken,
                                                         redirectToLogin,
                                                         getState
                                                     }) {
    pageContent.addEventListener('click', async (event) => {
        const target = event.target.closest(
            '#btn-reservierung-speichern, #btn-reservierung-abbrechen, [data-geraetetyp-id], [data-edit-id], [data-delete-id]'
        );

        if (!target) {
            return;
        }

        const token = getToken();
        if (!token) {
            redirectToLogin();
            return;
        }

        if (target.id === 'btn-reservierung-speichern') {
            await speichereReservierung(token);
            return;
        }

        if (target.id === 'btn-reservierung-abbrechen') {
            resetFormular();
            return;
        }

        if (target.dataset.geraetetypId) {
            state.form.geraetetypId = Number(target.dataset.geraetetypId);
            state.form.geraetetypName = target.dataset.geraetetypName || '';
            state.form.ausleihdatum = '';
            state.form.rueckgabedatum = '';
            await ladeNichtVerfuegbareZeitraeume(token);
            schreibeFormular();
            rendereGeraetetypListe();
            return;
        }

        if (target.dataset.editId) {
            bearbeiteReservierung(Number(target.dataset.editId));
            return;
        }

        if (target.dataset.deleteId) {
            await loescheReservierung(token, Number(target.dataset.deleteId));
            await ladeNichtVerfuegbareZeitraeume(token);
        }
    });

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        const token = getToken();

        if (!token) {
            return;
        }

        if (target.id === 'geraetetyp-suche-input') {
            state.suche.suchbegriff = target.value || '';
            state.suche.ergebnisse = await ladeGeraetetypen(token, state.suche.suchbegriff);
            rendereGeraetetypListe();
            return;
        }

        if (target.id === 'ausleihdatum' || target.id === 'rueckgabedatum') {
            state.form[target.id] = target.value || '';
            if (state.form.geraetetypId) {
                await ladeNichtVerfuegbareZeitraeume(token);
            }
        }
    });

    const observer = new MutationObserver(async () => {
        const tabIstAktiv = getState().activeTabKey === 'reservierung';
        const reservierungsListe = document.getElementById('meine-reservierungen-liste');
        if (!tabIstAktiv || !reservierungsListe || reservierungsListe.dataset.initialized === 'true') {
            return;
        }

        reservierungsListe.dataset.initialized = 'true';
        const token = getToken();
        if (!token) {
            redirectToLogin();
            return;
        }

        await initialisiereTab(token);
        initialisiereDatepicker();
    });

    observer.observe(pageContent, { childList: true, subtree: true });
}