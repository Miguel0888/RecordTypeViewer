package app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class RecordTypeViewer extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JTextArea jsonStructureArea;
    private JPanel filterPanel, filterInputPanel;
    private JButton filterInputButton;
    private List<JCheckBox> filterCheckBoxes = new ArrayList<>();
    private RecordTypeService recordTypeService;

    public RecordTypeViewer() {
        setTitle("Record Type Viewer");
        setSize(1024, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Service für die Datenverarbeitung
        recordTypeService = new RecordTypeService(this);

        // Tabellen für Table View
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        JTabbedPane tabbedPane = setUpToolbar(tableScrollPane);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JTabbedPane setUpToolbar(JScrollPane tableScrollPane) {
        // Erste Toolbar (Dateioperationen)
        JToolBar fileToolBar = new JToolBar();
        fileToolBar.setFloatable(false);
        JButton openFileButton = new JButton("Open File");
        openFileButton.addActionListener(e -> openFile());
        JButton exportDataButton = new JButton("Export All Data");  // 🆕 Export-Button
        exportDataButton.addActionListener(e -> recordTypeService.exportAllData());
        statusLabel = new JLabel("No file loaded...");
        fileToolBar.add(openFileButton);
        fileToolBar.add(exportDataButton);  // 🆕 Button rechts in der ersten Zeile
        fileToolBar.add(Box.createHorizontalGlue());
        fileToolBar.add(statusLabel);

        // Zweite Toolbar (JSON-Definition + Remove Selected Row)
        JToolBar configToolBar = new JToolBar();
        configToolBar.setFloatable(false);
        JButton loadConfigButton = new JButton("Load JSON");
        loadConfigButton.addActionListener(e -> loadJsonConfig());
        JButton removeSelectedRowButton = new JButton("Remove Selected Row");  // 🆕 Entfernen-Button
        removeSelectedRowButton.addActionListener(e -> removeSelectedRow());
        configToolBar.add(loadConfigButton);
        configToolBar.add(Box.createHorizontalGlue());
        configToolBar.add(removeSelectedRowButton);  // 🆕 Button ganz rechts in der zweiten Zeile


        // Dritte Toolbar (Final-Filter + Apply Button)
        JToolBar filterToolBar = new JToolBar();
        filterToolBar.setFloatable(false);
        JButton applyFiltersButton = new JButton("Apply Filters");
        applyFiltersButton.addActionListener(e -> applyFilters());
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterToolBar.add(applyFiltersButton);
        filterToolBar.add(filterPanel);

        // Vierte Toolbar (Filter Input)
        JToolBar filterInputToolBar = new JToolBar();
        filterInputToolBar.setFloatable(false);
        filterInputButton = new JButton("Filter Input");
        filterInputButton.setEnabled(false);
        filterInputButton.addActionListener(e -> applyInputFilter());
        filterInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterInputToolBar.add(filterInputButton);
        filterInputToolBar.add(filterInputPanel);

        // Panel für Toolbars
        JPanel toolBarPanel = new JPanel(new GridLayout(4, 1));
        toolBarPanel.add(fileToolBar);
        toolBarPanel.add(configToolBar);
        toolBarPanel.add(filterToolBar);
        toolBarPanel.add(filterInputToolBar);

        // Tabs für die verschiedenen Ansichten
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Table View", tableScrollPane);
        jsonStructureArea = new JTextArea(10, 50);
        jsonStructureArea.setText(recordTypeService.getDefaultJson());
        JScrollPane jsonScrollPane = new JScrollPane(jsonStructureArea);
        tabbedPane.addTab("Define Structure (JSON)", jsonScrollPane);

        // Layout setzen
        add(toolBarPanel, BorderLayout.NORTH);
        return tabbedPane;
    }

    // 🆕 Methode zum Entfernen der aktuell markierten Zeile
    private void removeSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            recordTypeService.removeRowFromData(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
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

    private void applyInputFilter() {
        recordTypeService.applyInputFilter(filterCheckBoxes);
    }

    public void updateTable(List<String[]> data) {
        tableModel.setRowCount(0);

        // Stelle sicher, dass die Spaltenüberschrift gesetzt ist
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

    public JPanel getFilterInputPanel() {
        return filterInputPanel;
    }

    public JButton getFilterInputButton() {
        return filterInputButton;
    }

    public List<JCheckBox> getFilterCheckBoxes() {
        return filterCheckBoxes;
    }


    public JTextArea getJsonStructureArea() {
        return jsonStructureArea;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RecordTypeViewer().setVisible(true));
    }

    public List<String[]> getTableData() {
        List<String[]> tableData = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            tableData.add(new String[]{(String) table.getValueAt(i, 0)});
        }
        return tableData;
    }

}
