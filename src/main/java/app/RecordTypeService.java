package app;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.io.*;
import java.util.*;

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
            ui.updateTable(filteredData);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(ui, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadJsonConfig(String jsonText) {
        try {
            Gson gson = new Gson();
            JsonObject rootNode = JsonParser.parseString(jsonText).getAsJsonObject();
            filterDefinitions = rootNode.getAsJsonArray("definition");

            setupDynamicFilterCombos(rootNode);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui, "Invalid JSON configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupDynamicFilterCombos(JsonObject rootNode) {
        JPanel filterPanel = ui.getFilterPanel();
        filterPanel.removeAll();

        for (int i = 0; i < filterDefinitions.size(); i++) {
            JsonObject definition = filterDefinitions.get(i).getAsJsonObject();
            String filterName = definition.get("name").getAsString();

            JComboBox<String> comboBox = new JComboBox<>();
            comboBox.addItem("");
            filterPanel.add(new JLabel(filterName + ":"));
            filterPanel.add(comboBox);
        }
        filterPanel.revalidate();
        filterPanel.repaint();
    }

    public void applyPreFilter() {
        filteredData.clear();
        filteredData.addAll(allData);
        ui.updateTable(filteredData);
    }

    public void applyFilters() {
        List<String[]> finalFilteredData = new ArrayList<>(filteredData);
        ui.updateTable(finalFilteredData);
    }

    public String getDefaultJson() {
        return DEFAULT_JSON;
    }
}
