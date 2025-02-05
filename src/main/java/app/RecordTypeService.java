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

            // ✅ Status-Label aktualisieren
            ui.updateStatus("File loaded: " + file.getName());

            // ✅ Tabelle richtig aktualisieren
            ui.updateTable(filteredData);

            // ✅ Filter-Comboboxen mit neuen Daten füllen
            if (filterDefinitions != null) {
                updateFilterCombos();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(ui, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadJsonConfig(String jsonText) {
        try {
            Gson gson = new Gson();
            JsonObject rootNode = JsonParser.parseString(jsonText).getAsJsonObject();
            filterDefinitions = rootNode.getAsJsonArray("definition");

            setupDynamicFilterCombos();
            setupFilterInputCheckboxes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui, "Invalid JSON configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupFilterInputCheckboxes() {
        JPanel filterInputPanel = ui.getFilterInputPanel();
        filterInputPanel.removeAll();
        ui.getFilterCheckBoxes().clear();

        for (int i = 0; i < filterDefinitions.size(); i++) {
            JsonObject definition = filterDefinitions.get(i).getAsJsonObject();
            String filterName = definition.get("name").getAsString();

            JCheckBox checkBox = new JCheckBox(filterName);
            checkBox.addActionListener(e -> updateFilterInputButtonState());
            ui.getFilterCheckBoxes().add(checkBox);
            filterInputPanel.add(checkBox);
        }

        filterInputPanel.revalidate();
        filterInputPanel.repaint();
    }

    private void updateFilterInputButtonState() {
        boolean anySelected = ui.getFilterCheckBoxes().stream().anyMatch(JCheckBox::isSelected);
        ui.getFilterInputButton().setEnabled(anySelected);
    }

    private void setupDynamicFilterCombos() {
        JPanel filterPanel = ui.getFilterPanel();
        filterPanel.removeAll();

        for (int i = 0; i < filterDefinitions.size(); i++) {
            JsonObject definition = filterDefinitions.get(i).getAsJsonObject();
            String filterName = definition.get("name").getAsString();

            JComboBox<String> comboBox = new JComboBox<>();
            comboBox.addItem("");  // Leerer Eintrag zum Abwählen
            filterPanel.add(new JLabel(filterName + ":"));
            filterPanel.add(comboBox);
        }
        filterPanel.revalidate();
        filterPanel.repaint();
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

        // Prüfe, welche Checkboxen aktiviert sind
        for (int i = 0; i < filterCheckBoxes.size(); i++) {
            if (filterCheckBoxes.get(i).isSelected()) {
                selectedIndices.add(i);
            }
        }

        if (selectedIndices.isEmpty()) {
            return;
        }

        // Mappe die gültigen Werte aus der aktuellen Table View
        Map<Integer, Set<String>> validValues = new HashMap<>();
        for (Integer index : selectedIndices) {
            validValues.put(index, new HashSet<>());
        }

        // Durchlaufe alle Zeilen in der aktuellen Table View und speichere gültige Werte
        for (String[] row : ui.getTableData()) {  // Holt die aktuell sichtbaren Daten aus Table View
            for (Integer index : selectedIndices) {
                int start = filterDefinitions.get(index).getAsJsonObject().get("start").getAsInt();
                int end = filterDefinitions.get(index).getAsJsonObject().get("end").getAsInt();
                String value = row[0].substring(start - 1, Math.min(end, row[0].length())).trim();
                validValues.get(index).add(value);
            }
        }

        // Neue gefilterte Liste erstellen
        List<String[]> newAllData = new ArrayList<>();
        for (String[] row : allData) {
            boolean allMatch = true;  // Muss für alle gewählten Satzarten gültig sein

            for (Integer index : selectedIndices) {
                int start = filterDefinitions.get(index).getAsJsonObject().get("start").getAsInt();
                int end = filterDefinitions.get(index).getAsJsonObject().get("end").getAsInt();
                String value = row[0].substring(start - 1, Math.min(end, row[0].length())).trim();

                // Falls der Wert nicht in der aktuellen Ansicht vorkam, entferne Zeile aus `allData`
                if (!validValues.get(index).contains(value)) {
                    allMatch = false;
                    break;  // Falls eine Satzart nicht passt -> Zeile verwerfen
                }
            }

            if (allMatch) {
                newAllData.add(row);  // Falls alle Satzarten gültig -> Behalte Zeile
            }
        }

        // Überschreibe `allData` mit den gefilterten Daten
        allData.clear();
        allData.addAll(newAllData);

        // UI-Update
        updateFilterCombos();
        ui.updateTable(allData);
    }


    public String getDefaultJson() {
        return DEFAULT_JSON;
    }
}
