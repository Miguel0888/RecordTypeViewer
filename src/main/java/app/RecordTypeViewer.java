package app;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class RecordTypeViewer extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JTextArea jsonStructureArea;
    private JPanel filterPanel;
    private List<JComboBox<String>> filterCombos;
    private List<String[]> allData = new ArrayList<>();
    private JsonArray filterDefinitions;

    // Standard JSON-Vorbelegung
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

    public RecordTypeViewer() {
        setTitle("Record Type Viewer");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tabellenmodell und Tabelle
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Erste Toolbar (Dateioperationen)
        JToolBar fileToolBar = new JToolBar();
        fileToolBar.setFloatable(false);

        JButton openFileButton = new JButton("Open File");
        openFileButton.addActionListener(e -> openFile());
        JButton applyFiltersButton = new JButton("Apply Filters");
        applyFiltersButton.addActionListener(e -> applyFilters());

        fileToolBar.add(openFileButton);
        fileToolBar.add(applyFiltersButton);
        statusLabel = new JLabel("No file loaded...");
        fileToolBar.add(statusLabel);

        // Zweite Toolbar (JSON-Definition laden)
        JToolBar configToolBar = new JToolBar();
        configToolBar.setFloatable(false);

        JLabel configLabel = new JLabel("Define Data Structure:");
        JButton loadConfigButton = new JButton("Load JSON");
        loadConfigButton.addActionListener(e -> loadJsonConfig());
        configToolBar.add(configLabel);
        configToolBar.add(loadConfigButton);

        // Dritte Toolbar (Filter)
        JToolBar filterToolBar = new JToolBar();
        filterToolBar.setFloatable(false);
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterToolBar.add(filterPanel);

        // Panel für die Toolbars
        JPanel toolBarPanel = new JPanel(new GridLayout(3, 1));
        toolBarPanel.add(fileToolBar);
        toolBarPanel.add(configToolBar);
        toolBarPanel.add(filterToolBar);

        // Tabs für die Tabelle und die JSON-Struktur
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Table View", tableScrollPane);

        jsonStructureArea = new JTextArea(10, 50);
        jsonStructureArea.setText(DEFAULT_JSON); // **JSON beim Start vorbelegen**
        JScrollPane jsonScrollPane = new JScrollPane(jsonStructureArea);
        tabbedPane.addTab("Define Structure (JSON)", jsonScrollPane);

        // Layout setzen
        add(toolBarPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        filterCombos = new ArrayList<>();
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadData(file);
        }
    }

    private void loadData(File file) {
        allData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allData.add(new String[]{line});
            }
            updateTable(allData);
            statusLabel.setText("File loaded: " + file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<String[]> data) {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"Raw Data"});
        for (String[] row : data) {
            tableModel.addRow(row);
        }
    }

    private void loadJsonConfig() {
        try {
            String jsonText = jsonStructureArea.getText();
            Gson gson = new Gson();
            JsonObject rootNode = JsonParser.parseString(jsonText).getAsJsonObject();

            filterDefinitions = rootNode.getAsJsonArray("definition");
            setupDynamicFilterCombos(rootNode);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid JSON configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupDynamicFilterCombos(JsonObject rootNode) {
        filterPanel.removeAll();
        filterCombos.clear();

        JsonArray filters = rootNode.getAsJsonArray("filters");
        for (int i = 0; i < filters.size(); i++) {
            String filterName = filters.get(i).getAsString();
            JComboBox<String> comboBox = new JComboBox<>();
            comboBox.addItem(""); // Leerer Eintrag zum Abwählen
            extractColumnData(i).forEach(comboBox::addItem);

            filterCombos.add(comboBox);
            filterPanel.add(new JLabel(filterName + ":"));
            filterPanel.add(comboBox);
        }
        filterPanel.revalidate();
        filterPanel.repaint();
    }

    private List<String> extractColumnData(int index) {
        Set<String> uniqueValues = new HashSet<>();
        int start = filterDefinitions.get(index).getAsJsonObject().get("start").getAsInt();
        int end = filterDefinitions.get(index).getAsJsonObject().get("end").getAsInt();

        for (String[] row : allData) {
            if (row[0].length() >= end || end == -1) {
                uniqueValues.add(row[0].substring(start - 1, end == -1 ? row[0].length() : end).trim());
            }
        }
        return new ArrayList<>(uniqueValues);
    }

    private void applyFilters() {
        List<String[]> filteredData = new ArrayList<>();
        outerLoop:
        for (String[] row : allData) {
            for (int i = 0; i < filterCombos.size(); i++) {
                String selectedFilter = (String) filterCombos.get(i).getSelectedItem();
                if (selectedFilter != null && !selectedFilter.isEmpty()) {
                    int start = filterDefinitions.get(i).getAsJsonObject().get("start").getAsInt();
                    int end = filterDefinitions.get(i).getAsJsonObject().get("end").getAsInt();

                    if (!row[0].substring(start - 1, end == -1 ? row[0].length() : end).trim().equals(selectedFilter)) {
                        continue outerLoop;
                    }
                }
            }
            filteredData.add(row);
        }
        updateTable(filteredData);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RecordTypeViewer().setVisible(true);
        });
    }
}
