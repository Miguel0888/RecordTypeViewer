package app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class RecordTypeViewer extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JTextArea jsonStructureArea;
    private JPanel filterPanel;
    private RecordTypeService recordTypeService;

    public RecordTypeViewer() {
        setTitle("Record Type Viewer");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Service für die Datenverarbeitung
        recordTypeService = new RecordTypeService(this);

        // Tabellen für Table View
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

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

        // Dritte Toolbar (Final-Filter + Apply Button)
        JToolBar filterToolBar = new JToolBar();
        filterToolBar.setFloatable(false);
        JButton applyFiltersButton = new JButton("Apply Filters");
        applyFiltersButton.addActionListener(e -> applyFilters());
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterToolBar.add(applyFiltersButton);
        filterToolBar.add(filterPanel);

        // Panel für Toolbars
        JPanel toolBarPanel = new JPanel(new GridLayout(3, 1));
        toolBarPanel.add(fileToolBar);
        toolBarPanel.add(configToolBar);
        toolBarPanel.add(filterToolBar);

        // Tabs für die verschiedenen Ansichten
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Table View", tableScrollPane);
        jsonStructureArea = new JTextArea(10, 50);
        jsonStructureArea.setText(recordTypeService.getDefaultJson());
        JScrollPane jsonScrollPane = new JScrollPane(jsonStructureArea);
        tabbedPane.addTab("Define Structure (JSON)", jsonScrollPane);

        // Layout setzen
        add(toolBarPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            recordTypeService.loadData(file);
        }
    }

    private void loadJsonConfig() {
        recordTypeService.loadJsonConfig(jsonStructureArea.getText());
    }

    private void applyFilters() {
        recordTypeService.applyFilters();
    }

    public void updateTable(List<String[]> data) {
        tableModel.setRowCount(0);

        // ✅ Stelle sicher, dass die Spaltenüberschrift gesetzt ist
        if (tableModel.getColumnCount() == 0) {
            tableModel.setColumnIdentifiers(new String[]{"Raw Data"});
        }

        for (String[] row : data) {
            tableModel.addRow(row);
        }
    }

    public void updateStatus(String message) {
        statusLabel.setText(message);
    }

    public JPanel getFilterPanel() {
        return filterPanel;
    }

    public JTextArea getJsonStructureArea() {
        return jsonStructureArea;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RecordTypeViewer().setVisible(true));
    }
}
