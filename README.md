# Geraeteverwaltung

## 📖 Projektbeschreibung

Die **Geraeteverwaltung** ist eine dockerisierte Anwendung zur Verwaltung von Geräten, Räumen und Benutzern.  
Alle benötigten Dienste werden mithilfe von Docker-Containern bereitgestellt und orchestriert.

Die Anwendung besitzt ein rollenbasiertes Zugriffssystem mit bereits vorkonfigurierten Testbenutzern.

---

# 🚀 Voraussetzungen

Stelle sicher, dass folgende Software installiert ist:

- Docker **20.10+**
- Docker Compose **2.0+**
- Git

## Installation prüfen

```bash
docker --version
docker compose version
git --version
```

---

# 📥 Repository klonen

```bash
git clone https://github.com/A-Baichenko/Geraeteverwaltung.git
```

Anschließend in das Projektverzeichnis wechseln:

```bash
cd Geraeteverwaltung
```

---

# ⚙️ Environment-Datei erstellen

Erstelle im Hauptverzeichnis eine `.env` Datei:

```env
#--- App ---
APP_PORT=8080
SPRING_PROFILES_ACTIVE=local

#--- Database ---
MYSQL_IMAGE_TAG=8.4
DB_NAME=geraeteverwaltung
DB_USER=app
DB_PASSWORD=app
DB_ROOT_PASSWORD=root
DB_PORT_HOST=3306
DB_PORT_CONTAINER=3306

#--- Flyway ---
FLYWAY_IMAGE_TAG=11
```

---

# ⚙️ Anwendung starten

## Container bauen und starten

```bash
docker compose up --build
```

Die Anwendung startet nun alle benötigten Container automatisch.

---

# 🛑 Anwendung stoppen

```bash
docker compose down
```

---

# 🔄 Anwendung neu starten

```bash
docker compose down
docker compose up --build
```

---

# 📦 Docker-Container anzeigen

```bash
docker ps
```

Alle laufenden Container werden angezeigt.

---

# 📜 Logs anzeigen

## Logs aller Container

```bash
docker compose logs
```

## Live-Logs anzeigen

```bash
docker compose logs -f
```

## Logs eines bestimmten Containers

```bash
docker logs <container-name>
```

Beispiel:

```bash
docker logs backend
```

---

# 👤 Testbenutzer

Die Anwendung enthält vorkonfigurierte Testaccounts.

| Rolle | Benutzername | Passwort |
|---|---|---|
| Admin | test1admin | test |
| Geraete_Verwalter | test2geraete | test |
| Raum_Verwalter | test3raum | test |
| Personen_Verwalter | test4personen | test |
| Benutzer | test5mitarbeiter | test |

> Die Zugangsdaten können je nach Konfiguration angepasst werden.

---

# 🌐 Zugriff auf die Anwendung

Nach dem Start ist die Anwendung erreichbar unter:

```text
http://localhost:8080
```

---

# 🧹 Container und Volumes vollständig löschen

```bash
docker compose down -v
```

Dadurch werden:
- Container
- Netzwerke
- Volumes
- gespeicherte Daten

entfernt.

---

# 🛠️ Häufige Probleme

## Port bereits belegt

Fehler:

```text
Bind for 0.0.0.0:8080 failed: port is already allocated
```

### Lösung

- Andere Anwendung auf dem Port beenden
- Oder den Port in der `docker-compose.yml` ändern

---

## Änderungen werden nicht übernommen

Container komplett neu bauen:

```bash
docker compose up --build --force-recreate
```

---

# 📂 Projektstruktur

```text
Geraeteverwaltung/
│
├── backend/
├── frontend/
├── docker-compose.yml
├── README.md
└── .env
```

---

# 🧑‍💻 Entwickler

Projekt von:

- Andreas Baichenko
- Kevin Wengler

Repository:

- https://github.com/A-Baichenko/Geraeteverwaltung