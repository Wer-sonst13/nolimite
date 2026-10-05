# NoLimite Launcher

## Variante A: Auf deinem PC (Windows)
1. Node.js installieren: https://nodejs.org
2. `build.bat` doppelklicken
3. Im Ordner `dist` liegt `NoLimite Setup x.x.x.exe` -> installieren, fertig.

## Variante B: Fertige .exe von GitHub bauen lassen (ohne alles lokal)
1. Neues GitHub-Repo erstellen und diesen Ordner hochladen (inkl. `.github`)
2. Tab "Actions" -> "Build NoLimite" abwarten
3. Unten bei "Artifacts" `NoLimite-Setup` herunterladen, entpacken, installieren.

Entwicklung: `npm install` und `npm start`

## Auto-Update
1. In `package.json` bei `build.publish` `DEIN-GITHUB-NAME` durch deinen GitHub-Namen ersetzen (Repo muss öffentlich sein, Name `nolimite`).
2. Neue Version veröffentlichen: `version` in `package.json` erhöhen (z. B. 0.1.1), committen, dann
   `git tag v0.1.1` und `git push --tags`.
3. GitHub baut das Setup und legt es unter "Releases" ab. Jeder installierte Launcher findet es beim Start,
   lädt es und aktualisiert sich selbst.
Die erste Installation macht man einmal über die Setup-Datei aus dem Release.
