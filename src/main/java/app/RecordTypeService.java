package app;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class RecordTypeService {
    private RecordTypeViewer ui;
    private List<String[]> allData = new ArrayList<>();
    private List<String[]> filteredData = new ArrayList<>();
    private JsonArray filterDefinitions;

    // Standard JSON wird jetzt hier gespeichert
    private static final String DEFAULT_JSON = "{\n" +
            "  \"filters\": [\"Prefix\", \"ID\", \"Operation\", \"Stelle\", \"Type\", \"Payload\"],\n" +
            "  \"definition\": [\n" +
            "    {\"name\": \"Prefix\", \"start\": 1, \"end\": 5},\n" +
            "    {\"name\": \"ID\", \"start\": 6, \"end\": 13},\n" +
            "    {\"name\": \"Operation\", \"start\": 14, \"end\": 18},\n" +
            "    {\"name\": \"Stelle\", \"start\": 19, \"end\": 36},\n" +
            "    {\"name\": \"Type\", \"start\": 37, \"end\": 39},\n" +
            "    {\"name\": \"Payload\", \"start\": 40, \"end\": -1}\n" +
            "  ]\n" +
            "}";

    public RecordTypeService(RecordTypeViewer ui) {
        this.ui = ui;
    }

    public void loadData(File file) {
        allData.clear();
        filteredData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allData.add(new String[]{line});
            }
            filteredData.addAll(allData);

            // ✅ Falls JSON noch nicht geladen wurde, Standard-JSON laden
            if (filterDefinitions == null) {
                loadJsonConfig(ui.getJsonStructureArea().getText());
            }

            // ✅ Tabelle aktualisieren
            ui.updateTable(filteredData);

            // ✅ Jetzt die Comboboxen füllen, damit sie korrekt angezeigt werden
            updateFilterCombosFromData();

            // ✅ Status aktualisieren
            ui.updateStatus("File loaded: " + file.getName());

        } catch (IOException e) {
            JOptionPane.showMessageDialog(ui, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFilterCombosFromData() {
        if (filterDefinitions == null) return;

        JPanel filterPanel = ui.getFilterPanel();
        filterPanel.removeAll();  // ✅ Entfernt alle alten Comboboxen

        // Neue Comboboxen erstellen und füllen
        for (int i = 0; i < filterDefinitions.size(); i++) {
            JsonObject definition = filterDefinitions.get(i).getAsJsonObject();
            String filterName = definition.get("name").getAsString();

            JComboBox<String> comboBox = new JComboBox<>();
            comboBox.addItem(""); // Leerer Eintrag

            // Werte aus `allData` extrahieren und hinzufügen
            Set<String> uniqueValues = extractColumnData(i);
            for (String value : uniqueValues) {
                comboBox.addItem(value);
            }

            filterPanel.add(new JLabel(filterName + ":"));
            filterPanel.add(comboBox);
        }

        // UI aktualisieren
        filterPanel.revalidate();
        filterPanel.repaint();
    }

    public void loadJsonConfig(String jsonText) {
        try {
            Gson gson = new Gson();
            JsonObject rootNode = JsonParser.parseString(jsonText).getAsJsonObject();
            filterDefinitions = rootNode.getAsJsonArray("definition");

            // ✅ Komplett neue Filter-Comboboxen aufbauen
            setupDynamicFilterCombos();

            // ✅ Filter mit Werten aus `allData` befüllen
            updateFilterCombosFromData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui, "Invalid JSON configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFilterInputButtonState() {
        boolean anySelected = ui.getFilterCheckBoxes().stream().anyMatch(JCheckBox::isSelected);
        ui.getFilterInputButton().setEnabled(anySelected);
    }

    private void setupDynamicFilterCombos() {
        // ✅ 1. Alte Filter-Comboboxen und Checkboxen entfernen
        JPanel filterPanel = ui.getFilterPanel();
        JPanel filterInputPanel = ui.getFilterInputPanel();

        filterPanel.removeAll();         // Entferne alle alten Filter-Dropdowns
        filterInputPanel.removeAll();    // Entferne alle alten Checkboxen
        ui.getFilterCheckBoxes().clear(); // Lösche die Liste der Checkboxen

        // ✅ 2. Neue Filter-Comboboxen und Checkboxen erstellen
        for (int i = 0; i < filterDefinitions.size(); i++) {
            JsonObject definition = filterDefinitions.get(i).getAsJsonObject();
            String filterName = definition.get("name").getAsString();

            // 🔹 Erstelle Combobox für den Filter
            JComboBox<String> comboBox = new JComboBox<>();
            comboBox.addItem("");  // Leerer Eintrag zum Abwählen
            filterPanel.add(new JLabel(filterName + ":"));
            filterPanel.add(comboBox);

            // 🔹 Erstelle Checkbox für "Filter Input"
            JCheckBox checkBox = new JCheckBox(filterName);
            checkBox.addActionListener(e -> updateFilterInputButtonState());

            ui.getFilterCheckBoxes().add(checkBox);
            filterInputPanel.add(checkBox);
        }

        // ✅ 3. UI-Update
        filterPanel.revalidate();
        filterPanel.repaint();
        filterInputPanel.revalidate();
        filterInputPanel.repaint();
    }


    private void updateFilterCombos() {
        JPanel filterPanel = ui.getFilterPanel();
        Component[] components = filterPanel.getComponents();

        int comboIndex = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JComboBox) {
                JComboBox<String> comboBox = (JComboBox<String>) components[i];
                comboBox.removeAllItems();
                comboBox.addItem(""); // Leerer Eintrag

                // Werte aus `allData` extrahieren
                Set<String> uniqueValues = extractColumnData(comboIndex);
                for (String value : uniqueValues) {
                    comboBox.addItem(value);
                }
                comboIndex++;
            }
        }
    }

    private Set<String> extractColumnData(int index) {
        Set<String> uniqueValues = new HashSet<>();
        if (filterDefinitions == null) return uniqueValues;

        int start = filterDefinitions.get(index).getAsJsonObject().get("start").getAsInt();
        int end = filterDefinitions.get(index).getAsJsonObject().get("end").getAsInt();

        for (String[] row : allData) {
            if (row[0].length() >= start) {  // Verhindert OutOfBounds-Fehler
                String value;
                if (end == -1 || end > row[0].length()) {
                    value = row[0].substring(start - 1).trim();  // Bis zum Ende des Strings
                } else {
                    value = row[0].substring(start - 1, end).trim();
                }
                uniqueValues.add(value);
            }
        }
        return uniqueValues;
    }

    public void applyFilters() {
        List<String[]> finalFilteredData = new ArrayList<>();
        JPanel filterPanel = ui.getFilterPanel();
        Component[] components = filterPanel.getComponents();

        outerLoop:
        for (String[] row : allData) {
            int comboIndex = 0;
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof JComboBox) {
                    JComboBox<String> comboBox = (JComboBox<String>) components[i];
                    String selectedFilter = (String) comboBox.getSelectedItem();
                    if (selectedFilter != null && !selectedFilter.isEmpty()) {
                        int start = filterDefinitions.get(comboIndex).getAsJsonObject().get("start").getAsInt();
                        int end = filterDefinitions.get(comboIndex).getAsJsonObject().get("end").getAsInt();
                        String rowValue = row[0].substring(start - 1, Math.min(end, row[0].length())).trim();

                        if (!rowValue.equals(selectedFilter)) {
                            continue outerLoop;
                        }
                    }
                    comboIndex++;
                }
            }
            finalFilteredData.add(row);
        }
        ui.updateTable(finalFilteredData);
    }

    public void applyInputFilter(List<JCheckBox> filterCheckBoxes) {
        Set<Integer> selectedIndices = new HashSet<>();

        // ✅ 1. Sammle Indexe der aktivierten Satzarten (Spalten)
        for (int i = 0; i < filterCheckBoxes.size(); i++) {
            if (filterCheckBoxes.get(i).isSelected()) {
                selectedIndices.add(i);
            }
        }

        if (selectedIndices.isEmpty()) {
            return; // Keine aktiven Filter -> Keine Änderungen an allData
        }

        // ✅ 2. Werte aus der aktuellen Table View extrahieren
        Map<Integer, Set<String>> validValues = new HashMap<>();

        for (Integer index : selectedIndices) {
            validValues.put(index, new HashSet<>());
        }

        for (String[] row : ui.getTableData()) {  // Holt alle angezeigten Zeilen aus Table View
            for (Integer index : selectedIndices) {
                int start = filterDefinitions.get(index).getAsJsonObject().get("start").getAsInt();
                int end = filterDefinitions.get(index).getAsJsonObject().get("end").getAsInt();
                String value = row[0].substring(start - 1, Math.min(end, row[0].length())).trim();
                validValues.get(index).add(value);
            }
        }

        // ✅ 3. Neue Liste für `allData` erstellen
        List<String[]> newAllData = new ArrayList<>();

        // ✅ 4. Jede Zeile aus `allData` prüfen
        for (String[] row : allData) {
            boolean rowMatches = true; // Muss für alle gewählten Satzarten passen

            for (Integer index : selectedIndices) {
                int start = filterDefinitions.get(index).getAsJsonObject().get("start").getAsInt();
                int end = filterDefinitions.get(index).getAsJsonObject().get("end").getAsInt();
                String value = row[0].substring(start - 1, Math.min(end, row[0].length())).trim();

                // ✅ Bedingung: Mindestens eine Zeile aus Table View muss diesen Wert enthalten
                boolean foundInView = false;
                for (String[] tableRow : ui.getTableData()) {
                    String tableValue = tableRow[0].substring(start - 1, Math.min(end, tableRow[0].length())).trim();
                    if (tableValue.equals(value)) {
                        foundInView = true;
                        break;
                    }
                }

                if (!foundInView) {
                    rowMatches = false;
                    break; // Falls eine Satzart nicht vorkommt, verwerfe Zeile
                }
            }

            if (rowMatches) {
                newAllData.add(row); // ✅ Nur behalten, wenn ALLE Satzarten in einer Zeile der View vorkommen
            }
        }

        // ✅ 5. `allData` mit den gefilterten Daten überschreiben
        allData.clear();
        allData.addAll(newAllData);

        // ✅ 6. UI aktualisieren
        updateFilterCombos();
        ui.updateTable(allData);
    }

    public String getDefaultJson() {
        return DEFAULT_JSON;
    }

    public void removeRowFromData(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < allData.size()) {
            allData.remove(rowIndex);
            ui.updateTable(allData);  // Aktualisiere UI nach Entfernung
        }
    }

    public void exportAllData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Export File");
        int userSelection = fileChooser.showSaveDialog(ui);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                for (String[] row : allData) {
                    writer.write(row[0]);  // Schreibe jede Zeile von allData in die Datei
                    writer.newLine();
                }
                JOptionPane.showMessageDialog(ui, "Data successfully exported!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(ui, "Error exporting data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
