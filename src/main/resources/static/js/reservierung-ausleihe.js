let selectedPersonalNr = null;
let selectedRaumNr = null;
let selectedGeraetetypId = null;

let selectedMitarbeiterName = '';
let selectedRaumName = '';
let selectedGeraetName = '';

async function ladeCurrentMitarbeiter(token) {
    const response = await fetch('/api/auth/me', {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (!response.ok) {
        return null;
    }

    return response.json();
}

async function setzeCurrentMitarbeiter(token) {
    const mitarbeiterInput = document.getElementById('mitarbeiter');
    if (!mitarbeiterInput) {
        return;
    }

    const data = await ladeCurrentMitarbeiter(token);
    if (!data) {
        mitarbeiterInput.value = '';
        return;
    }

    selectedPersonalNr = data.personalNr;
    selectedMitarbeiterName = data.mitarbeiterName;
    mitarbeiterInput.value = data.mitarbeiterName;
}

async function sucheRaeume(suchbegriff, token) {
    const url = suchbegriff && suchbegriff.trim()
        ? `/api/reservierung-ausleihe/raeume?suchbegriff=${encodeURIComponent(suchbegriff)}`
        : '/api/reservierung-ausleihe/raeume';

    const response = await fetch(url, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (!response.ok) {
        return [];
    }

    return response.json();
}

async function sucheGeraete(suchbegriff, token) {
    const url = suchbegriff && suchbegriff.trim()
        ? `/api/reservierung-ausleihe/geraete?suchbegriff=${encodeURIComponent(suchbegriff)}`
        : '/api/reservierung-ausleihe/geraete';

    const response = await fetch(url, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (!response.ok) {
        return [];
    }

    return response.json();
}

function renderRaumTabelle(raeume) {
    const tableBody = document.getElementById('raum-tabelle-body');
    if (!tableBody) {
        return;
    }

    if (!raeume || raeume.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="2" class="table-empty-cell">
                    Keine Räume gefunden
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = raeume.map((raum) => `
        <tr
            class="select-raum-row table-select-row"
            data-raum-nr="${raum.raumNr}"
            data-raum-name="${raum.anzeigeName}"
        >
            <td>${raum.raumNr}</td>
            <td>${raum.gebaeude}</td>
        </tr>
    `).join('');
}

function renderGeraetTabelle(geraete) {
    const tableBody = document.getElementById('geraet-tabelle-body');
    if (!tableBody) {
        return;
    }

    if (!geraete || geraete.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="3" class="table-empty-cell">
                    Keine Geräte gefunden
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = geraete.map((geraet) => `
        <tr
            class="select-geraet-row table-select-row"
            data-geraetetyp-id="${geraet.id}"
            data-geraet-name="${geraet.anzeigeName}"
        >
            <td>${geraet.hersteller}</td>
            <td>${geraet.bezeichnung}</td>
            <td>${geraet.kategorie ?? ''}</td>
        </tr>
    `).join('');
}

async function speichereAusleihe(token) {
    const mitarbeiterInput = document.getElementById('mitarbeiter');
    const ausleihdatumInput = document.getElementById('ausleihdatum');
    const rueckgabedatumInput = document.getElementById('rueckgabedatum');

    const ausleihdatum = ausleihdatumInput?.value;
    const rueckgabedatum = rueckgabedatumInput?.value;

    if (!mitarbeiterInput || !ausleihdatumInput || !rueckgabedatumInput) {
        alert('Formular nicht vollständig geladen.');
        return;
    }

    if (!selectedPersonalNr) {
        alert('Mitarbeiter konnte nicht automatisch erkannt werden.');
        return;
    }

    if (!selectedGeraetetypId) {
        alert('Bitte zuerst ein Gerät auswählen.');
        return;
    }

    if (!ausleihdatum || !rueckgabedatum) {
        alert('Bitte Ausleihdatum und Rückgabedatum eingeben.');
        return;
    }

    const response = await fetch('/api/reservierung-ausleihe/ausleihen', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
            geraetetypId: Number(selectedGeraetetypId),
            personalNr: Number(selectedPersonalNr),
            ausleihdatum,
            rueckgabedatum
        })
    });

    const data = await response.json();

    if (!response.ok) {
        alert(data.error || 'Fehler beim Speichern der Ausleihe.');
        return;
    }

    alert('Ausleihe erfolgreich gespeichert.');
    selectedRaumNr = null;
    selectedGeraetetypId = null;
    selectedRaumName = '';
    selectedGeraetName = '';
}

function schreibeAuswahlInsFormular() {
    const mitarbeiterInput = document.getElementById('mitarbeiter');
    const raumInput = document.getElementById('raum');
    const geraetInput = document.getElementById('geraet');

    if (mitarbeiterInput && selectedMitarbeiterName) {
        mitarbeiterInput.value = selectedMitarbeiterName;
    }

    if (raumInput && selectedRaumName) {
        raumInput.value = selectedRaumName;
    }

    if (geraetInput && selectedGeraetName) {
        geraetInput.value = selectedGeraetName;
    }
}

export function registerReservierungAusleiheHandlers({
                                                         pageContent,
                                                         getToken,
                                                         redirectToLogin,
                                                         getState,
                                                         setState,
                                                         switchTab
                                                     }) {
    pageContent.addEventListener('click', async (event) => {
        const target = event.target.closest(
            '#btn-ausleihen, #back-to-reservierung-menu, #btn-ausleihe-speichern, #raum, #geraet, #back-to-form, .select-raum-row, .select-geraet-row'
        );

        if (!target) {
            return;
        }

        const token = getToken();
        if (!token) {
            redirectToLogin();
            return;
        }

        const state = getState();

        if (target.id === 'btn-ausleihen') {
            setState({
                currentSubView: 'ausleihe',
                currentAusleiheView: 'form'
            });
            await switchTab(state.activeTabKey, token, state.allowedTabKeys);
            setTimeout(async () => {
                schreibeAuswahlInsFormular();
                await setzeCurrentMitarbeiter(token);
            }, 0);
            return;
        }

        if (target.id === 'raum') {
            setState({
                currentSubView: 'ausleihe',
                currentAusleiheView: 'raum'
            });
            await switchTab(state.activeTabKey, token, state.allowedTabKeys);

            const raeume = await sucheRaeume('', token);
            renderRaumTabelle(raeume);
            return;
        }

        if (target.id === 'geraet') {
            setState({
                currentSubView: 'ausleihe',
                currentAusleiheView: 'geraet'
            });
            await switchTab(state.activeTabKey, token, state.allowedTabKeys);

            const geraete = await sucheGeraete('', token);
            renderGeraetTabelle(geraete);
            return;
        }

        if (target.id === 'back-to-form') {
            setState({
                currentSubView: 'ausleihe',
                currentAusleiheView: 'form'
            });
            await switchTab(state.activeTabKey, token, state.allowedTabKeys);
            setTimeout(async () => {
                schreibeAuswahlInsFormular();
                await setzeCurrentMitarbeiter(token);
            }, 0);
            return;
        }

        if (target.id === 'back-to-reservierung-menu') {
            setState({
                currentSubView: 'menu',
                currentAusleiheView: 'form'
            });
            await switchTab(state.activeTabKey, token, state.allowedTabKeys);
            return;
        }

        if (target.classList.contains('select-raum-row')) {
            selectedRaumNr = Number(target.dataset.raumNr);
            selectedRaumName = target.dataset.raumName || '';

            setState({
                currentSubView: 'ausleihe',
                currentAusleiheView: 'form'
            });

            await switchTab(state.activeTabKey, token, state.allowedTabKeys);
            setTimeout(async () => {
                schreibeAuswahlInsFormular();
                await setzeCurrentMitarbeiter(token);
            }, 0);
            return;
        }

        if (target.classList.contains('select-geraet-row')) {
            selectedGeraetetypId = Number(target.dataset.geraetetypId);
            selectedGeraetName = target.dataset.geraetName || '';

            setState({
                currentSubView: 'ausleihe',
                currentAusleiheView: 'form'
            });

            await switchTab(state.activeTabKey, token, state.allowedTabKeys);
            setTimeout(async () => {
                schreibeAuswahlInsFormular();
                await setzeCurrentMitarbeiter(token);
            }, 0);
            return;
        }

        if (target.id === 'btn-ausleihe-speichern') {
            await speichereAusleihe(token);
        }
    });

    pageContent.addEventListener('input', async (event) => {
        const target = event.target;
        const token = getToken();

        if (!token) {
            return;
        }

        if (target.id === 'raum-suche-input') {
            const raeume = await sucheRaeume(target.value, token);
            renderRaumTabelle(raeume);
            return;
        }

        if (target.id === 'geraet-suche-input') {
            const geraete = await sucheGeraete(target.value, token);
            renderGeraetTabelle(geraete);
        }
    });
}