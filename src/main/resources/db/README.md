## Flyway – Verwendung

### 1. Migration erstellen
- Neue SQL-Datei im `db/migration`-Ordner anlegen
- Namensschema einhalten:  
  `V<Version>__<beschreibung>.sql`

**Beispiel:**

---

### 2. Änderungen definieren
- SQL-Befehle in die Datei schreiben (CREATE, ALTER, INSERT, …)
- Migrationen **nicht nachträglich ändern**, sobald sie ausgeführt wurden

---

### 3. Migration ausführen
- Flyway wird beim Start der Anwendung oder manuell ausgeführt
- Führt automatisch alle neuen Migrationen in der richtigen Reihenfolge aus

---

### 4. Status prüfen
- Flyway speichert ausgeführte Migrationen in der Tabelle `flyway_schema_history`
- Bereits ausgeführte Migrationen werden nicht erneut ausgeführt

---

### Wichtige Regeln
- Migrationen sind **immutable** (nicht ändern, sondern neue erstellen)
- Versionsnummern müssen eindeutig und aufsteigend sein
- Jede DB-Änderung erfolgt über eine Migration