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
    private JTable table, preFilterTable;
    private DefaultTableModel tableModel, preFilterTableModel;
    private JLabel statusLabel;
    private JTextArea jsonStructureArea;
    private JPanel filterPanel, preFilterPanel;
    private List<JComboBox<String>> filterCombos, preFilterCombos;
    private List<String[]> allData = new ArrayList<>();
    private List<String[]> filteredData = new ArrayList<>();
    private JsonArray filterDefinitions;
    private Set<String> selectedIDs = new HashSet<>();

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

        // Tabellen für Table View und Pre-Filter View
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        preFilterTableModel = new DefaultTableModel();
        preFilterTable = new JTable(preFilterTableModel);
        JScrollPane preFilterTableScrollPane = new JScrollPane(preFilterTable);

        // Erste Toolbar (Dateioperationen)
        JToolBar fileToolBar = new JToolBar();
        fileToolBar.setFloatable(false);

        JButton openFileButton = new JButton("Open File");
        openFileButton.addActionListener(e -> openFile());
        statusLabel = new JLabel("No file loaded...");
        fileToolBar.add(openFileButton);
        fileToolBar.add(statusLabel);

        // Zweite Toolbar (JSON-Definition laden)
        JToolBar configToolBar = new JToolBar();
        configToolBar.setFloatable(false);

        JButton loadConfigButton = new JButton("Load JSON");
        loadConfigButton.addActionListener(e -> loadJsonConfig());
        configToolBar.add(loadConfigButton);

        JLabel configLabel = new JLabel("Define Data Structure:");
        configToolBar.add(configLabel);

        // Dritte Toolbar (Filter + Apply Button)
        JToolBar filterToolBar = new JToolBar();
        filterToolBar.setFloatable(false);
        JButton applyFiltersButton = new JButton("Apply Filters");
        applyFiltersButton.addActionListener(e -> applyFilters());
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterToolBar.add(applyFiltersButton);
        filterToolBar.add(filterPanel);

        // Vierte Toolbar für Pre-Filter
        JToolBar preFilterToolBar = new JToolBar();
        preFilterToolBar.setFloatable(false);
        JButton applyPreFilterButton = new JButton("Apply Pre-Filter");
        applyPreFilterButton.addActionListener(e -> applyPreFilter());
        preFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        preFilterToolBar.add(applyPreFilterButton);
        preFilterToolBar.add(preFilterPanel);

        // Panel für Toolbars
        JPanel toolBarPanel = new JPanel(new GridLayout(3, 1));
        toolBarPanel.add(fileToolBar);
        toolBarPanel.add(configToolBar);
        toolBarPanel.add(filterToolBar);

        // Tabs für die verschiedenen Ansichten
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Table View", tableScrollPane);
        jsonStructureArea = new JTextArea(10, 50);
        jsonStructureArea.setText(DEFAULT_JSON);
        JScrollPane jsonScrollPane = new JScrollPane(jsonStructureArea);
        tabbedPane.addTab("Define Structure (JSON)", jsonScrollPane);

        JPanel preFilterPanelContainer = new JPanel(new BorderLayout());
        preFilterPanelContainer.add(preFilterToolBar, BorderLayout.NORTH);
        preFilterPanelContainer.add(preFilterTableScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Pre-Filter View", preFilterPanelContainer);

        // Layout setzen
        add(toolBarPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        filterCombos = new ArrayList<>();
        preFilterCombos = new ArrayList<>();
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
        filteredData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allData.add(new String[]{line});
            }
            filteredData.addAll(allData);
            updateTable(filteredData);
            statusLabel.setText("File loaded: " + file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<String[]> data) {
        tableModel.setRowCount(0);
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
            statusLabel.setText("JSON loaded!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid JSON configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyPreFilter() {
        filteredData.clear();
        filteredData.addAll(allData);  // Simulation einer Vorfilterung
        updateTable(filteredData);
    }

    private void applyFilters() {
        List<String[]> finalFilteredData = new ArrayList<>();
        for (String[] row : filteredData) {
            finalFilteredData.add(row);
        }
        updateTable(finalFilteredData);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RecordTypeViewer().setVisible(true);
        });
    }
}
