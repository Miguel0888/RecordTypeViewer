# **Record Type Viewer (RTV)**
**Ein Tool zur Analyse und Filterung von Datendateien mit Satzarten.**  
Mit **Record Type Viewer (RTV)** können Benutzer Dateien mit fest definierten Satzarten laden, analysieren und gezielt filtern.

---

## **🚀 Installation & Download**
Die neueste Version von RTV findest du unter **GitHub Releases**:  
👉 **[Hier klicken, um die neueste Version herunterzuladen!](https://github.com/DEIN_GITHUB_USER/DEIN_REPO/releases)**

Dort kannst du das **fertige JAR-File (RTV-<VERSION>.jar)** herunterladen.

---

## **💻 How to launch the program?**
### **Windows**
1. **[Win] + [R]** drücken
2. `cmd` eintippen und [ENTER] drücken
3. In den Download-Ordner wechseln:
   ```sh
   cd \ 
   cd <YOUR DOWNLOAD FOLDER>
   ```
4. Programm starten:
   ```sh
   java -jar RTV-<VERSION>.jar
   ```
5. Falls ein Fehler auftritt, überprüfe deine **Java Path Environment Variable**.  
   RTV ist **kompatibel mit Java 8**!

---

## **🖥️ Benutzeranleitung**
### **🔹 Hauptfunktionen**
- **Dateien mit Satzarten laden**
- **Daten in einer Tabelle anzeigen**
- **Flexible Filterung nach Satzarten über Dropdowns**
- **Vorfilterung über eine zusätzliche Checkbox-Leiste (Pre-Filter View)**
- **Datenmanipulation über "Filter Input"**
- **Live-Update der Filtermöglichkeiten nach jeder Manipulation**

---

## **📝 Bedienung**
### **1️⃣ Datei laden**
- Klicke auf **"Open File"** und wähle eine Datei aus.
- Nach dem Laden erscheint der Inhalt in der **Table View**.
- Falls das Format nicht passt, prüfe dein JSON-Filter-Setup.

### **2️⃣ Datenstruktur definieren (JSON)**
- Gehe auf den Tab **"Define Structure (JSON)"**.
- Falls nötig, passe die Satzarten im JSON an.
- Lade die neue Struktur mit **"Load JSON"**.

### **3️⃣ Datensätze filtern (DropDown-Filter)**
- In der dritten Toolbar-Zeile findest du **Filter-Dropdowns**.
- Wähle dort die gewünschten Filterkriterien aus.
- Klicke auf **"Apply Filters"**, um die Tabelle zu aktualisieren.

### **4️⃣ Pre-Filter aktivieren (Checkboxen)**
- In der vierten Toolbar-Zeile befinden sich **Checkboxen für jede Satzart**.
- Aktiviere eine oder mehrere Checkboxen.
- **Sobald mindestens eine Checkbox aktiv ist, wird der Button "Filter Input" aktiviert.**
- Durch Klicken auf **"Filter Input"** wird `allData` so reduziert, dass nur noch Zeilen enthalten sind, die in der aktuellen Ansicht vorkommen.

---

## **🎯 Beispiele**
### **📌 Beispiel 1: Nur relevante IDs beibehalten**
1. Wähle im **Prefix-Dropdown** eine bestimmte Satzart aus.
2. Wähle im **ID-Dropdown** eine spezifische ID.
3. Klicke auf **"Apply Filters"**, um die Ansicht zu aktualisieren.
4. Setze in der vierten Toolbar-Zeile den Haken bei **"ID"**.
5. Klicke auf **"Filter Input"**, um `allData` dauerhaft auf diese IDs zu reduzieren.

### **📌 Beispiel 2: Mehrere Satzarten filtern**
1. Wähle eine Satzart im **Type-Dropdown**.
2. Setze Haken bei **"Type"** und **"Payload"** in der vierten Toolbar.
3. Klicke auf **"Filter Input"** – jetzt bleiben nur noch Zeilen übrig, die sowohl **die gewählte Satzart als auch das passende Payload-Feld haben.**

---

## **🔧 Fehlerbehebung**
### **🚫 Problem: "No File Loaded"**
✅ Lösung: Datei erneut laden oder ein anderes Verzeichnis ausprobieren.  
✅ Prüfe, ob das **JSON-Format korrekt geladen** wurde.

### **🚫 Problem: Java-Fehlermeldung beim Start**
✅ Lösung: Prüfe, ob Java 8 installiert ist:
```sh
java -version
```
Falls nicht, installiere Java 8.

### **🚫 Problem: Filter zeigen keine Werte**
✅ Lösung: Stelle sicher, dass du **zuerst eine Datei lädst**.  
✅ Prüfe, ob du nach **"Load JSON"** die **richtige Satzarten-Definition hast**.

---

## **📜 Lizenz & Mitwirkung**
**Record Type Viewer (RTV)** ist ein Open-Source-Projekt.  
Du kannst dazu beitragen, indem du Fehler meldest oder Pull Requests erstellst.

---

## **📧 Support**
Für Fragen oder Probleme:  
✉️ **[Kontakt aufnehmen](mailto:DEINE_EMAIL@example.com)**  
🐞 **[Issue auf GitHub erstellen](https://github.com/DEIN_GITHUB_USER/DEIN_REPO/issues)**

---

### **🚀 Viel Spaß mit dem Record Type Viewer!** 🎉  
