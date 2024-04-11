package com.conceptbreakdowntool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.swing.JOptionPane;
import java.util.Random;


public class MainApplicationWindow extends JFrame {
    private ConceptBreakdownToolUI startupUI;
    private DatabaseManager dbManager; // Use the passed DatabaseManager instance
    private JTable dataTable;
    private JLabel feedbackLabel;
    private JPanel panel;

    public MainApplicationWindow(DatabaseManager dbManager, ConceptBreakdownToolUI startupUI) {
        this.startupUI = startupUI;
        this.dbManager = dbManager;
        this.dbManager.setUIUpdateListener(this::updateUI); // Setup UIUpdateListener

        setTitle("Concept Breakdown Tool");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(5, 0));

        JButton menuButton = new JButton("Menu");
        JPopupMenu menuPopup = new JPopupMenu();
        JMenuItem addMenuItem = new JMenuItem("Add");
        JMenuItem printMenuItem = new JMenuItem("Print");
        JMenuItem recommendMenuItem = new JMenuItem("Recommend");
        menuPopup.add(addMenuItem);
        menuPopup.add(printMenuItem);
        menuPopup.add(recommendMenuItem);

        menuButton.addActionListener(e -> menuPopup.show(menuButton, menuButton.getWidth() / 2, menuButton.getHeight() / 2));

        JButton instructionsButton = new JButton("Instructions");
        JButton backToStartupButton = new JButton("Home");

        Dimension standardDimension = new Dimension(100, 25);
        menuButton.setPreferredSize(standardDimension);
        instructionsButton.setPreferredSize(standardDimension);
        backToStartupButton.setPreferredSize(standardDimension);

        JPanel leftPanel = new JPanel();
        leftPanel.add(menuButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(instructionsButton);
        rightPanel.add(backToStartupButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        this.setLayout(new BorderLayout());
        this.add(topPanel, BorderLayout.NORTH);

        addMenuItem.addActionListener(e -> {
            showAddOptionsDialog();
            refreshTableData(); // Refresh table data after adding a category
        });
        printMenuItem.addActionListener(e -> showPrintDialog());
        recommendMenuItem.addActionListener(e -> recommendDiagram());
        backToStartupButton.addActionListener(e -> {
            this.setVisible(false); // Hide the MainApplicationWindow
            startupUI.setVisible(true); // Show the ConceptBreakdownToolUI
        });
        instructionsButton.addActionListener(e -> showInstructions());

        setupDataTable();
        setupFeedbackLabel();
    }

    private void setupDataTable() {
        dataTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Category Name", "Actions"}, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return (column == 2) ? JPanel.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only the button column should be editable
            }
        });

        TableColumn actionColumn = dataTable.getColumnModel().getColumn(2);
        actionColumn.setCellRenderer(new ButtonPanelRenderer());

        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);
        dataTable.setRowHeight(40); // Adjust row height to accommodate buttons

        setupTableButtons();
        refreshTableData(); // This method should call updateDataTable() method
    }



    private void setupFeedbackLabel() {
        feedbackLabel = new JLabel("Ready.", SwingConstants.CENTER);
        add(feedbackLabel, BorderLayout.SOUTH);
    }

    private void setupTableButtons() {
        int actionColumnIndex = 2;
        TableColumn actionColumn = dataTable.getColumnModel().getColumn(actionColumnIndex);

        actionColumn.setCellRenderer(new TableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof JPanel) {
                    return (JPanel) value;
                } else {
                    return new JLabel("Not a Panel");
                }
            }
        });

        // Set the custom editor for the action column
        actionColumn.setCellEditor(new ButtonPanelEditor(new JCheckBox()));
    }



    private void updateDataTable() {
        DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
        model.setRowCount(0); // Clear existing data

        for (Category category : dbManager.getCategories()) {
            model.addRow(new Object[]{category.getId(), category.getTopic(), "Category"});

            // Retrieve and add concepts for this category
            List<Concept> concepts = dbManager.getConceptsByCategoryId(category.getId());
            for (Concept concept : concepts) {
                model.addRow(new Object[]{concept.getId(), concept.getTopic(), "Concept"});

                // Retrieve and add components for this concept
                List<Component> components = dbManager.getComponentsByConceptId(concept.getId());
                for (Component component : components) {
                    model.addRow(new Object[]{component.getTopic(), component.getDetails(), "Component"});
                }
            }
        }
    }


    // An enum to define the type of entity the buttons are for
    enum EntityType {
        CATEGORY,
        CONCEPT,
        COMPONENT
    }

    private JPanel createButtonPanel(int id, EntityType type, String topic) { // Added 'topic' parameter for COMPONENT case
        System.out.println("Creating button panel for " + type + " ID: " + id);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewButton = new JButton("View");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Remove");

        // Set action listeners based on the type of entity
        if (type == EntityType.CATEGORY) {
            viewButton.addActionListener(e -> viewCategoryAction(id));
            updateButton.addActionListener(e -> updateCategoryAction(id));
            deleteButton.addActionListener(e -> deleteCategoryAction(id));
        } else if (type == EntityType.CONCEPT) {
            viewButton.addActionListener(e -> viewConceptAction(id));
            updateButton.addActionListener(e -> updateConceptAction(id));
            deleteButton.addActionListener(e -> deleteConceptAction(id));
        } else if (type == EntityType.COMPONENT) {
            // Use the passed 'topic' for COMPONENT actions
            viewButton.addActionListener(e -> viewComponentAction(topic));
            updateButton.addActionListener(e -> updateComponentAction(topic));
            deleteButton.addActionListener(e -> deleteComponentAction(topic));
        }

        panel.add(viewButton);
        panel.add(updateButton);
        panel.add(deleteButton);

        return panel;
    }


    private void viewAction(int categoryId) {
        Category category = dbManager.getCategoryById(categoryId);
        if (category == null) {
            JOptionPane.showMessageDialog(this, "Category with ID: " + categoryId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Stop the method if category is null
        }

        List<Concept> concepts = dbManager.getConceptsByCategoryId(categoryId);
        JDialog categoryDialog = new JDialog();
        categoryDialog.setTitle(category.getTopic()); // Window title should be category name
        categoryDialog.setLayout(new BorderLayout());

        JPanel conceptsPanel = new JPanel();
        conceptsPanel.setLayout(new BoxLayout(conceptsPanel, BoxLayout.Y_AXIS));

        for (Concept concept : concepts) {
            JPanel conceptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel conceptLabel = new JLabel(concept.getTopic());
            JButton viewButton = new JButton("View");
            JButton modifyButton = new JButton("Update");
            JButton removeButton = new JButton("Remove");

            // Add actions for buttons
            viewButton.addActionListener(e -> viewConceptDetails(concept));
            modifyButton.addActionListener(e -> modifyConceptDetails(concept.getId()));
            removeButton.addActionListener(e -> {
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete the concept '" + concept.getTopic() + "'?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    boolean removed = dbManager.deleteConcept(concept.getId());
                    if (removed) {
                        conceptsPanel.remove(conceptPanel);
                        conceptsPanel.revalidate();
                        conceptsPanel.repaint();
                        JOptionPane.showMessageDialog(this, "Concept removed successfully.", "Removal Successful", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove the concept.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            conceptPanel.add(conceptLabel);
            conceptPanel.add(viewButton);
            conceptPanel.add(modifyButton);
            conceptPanel.add(removeButton);
            conceptsPanel.add(conceptPanel);
        }

        categoryDialog.add(new JScrollPane(conceptsPanel), BorderLayout.CENTER);
        categoryDialog.setSize(600, 400);
        categoryDialog.setLocationRelativeTo(null); // Center on screen
        categoryDialog.setVisible(true);
    }

    private void modifyAction(int categoryId) {
        Category category = dbManager.getCategoryById(categoryId);
        if (category != null) {
            JTextField categoryIdField = new JTextField(String.valueOf(category.getId()));
            JTextField categoryNameField = new JTextField(category.getTopic());

            Object[] message = {
                    "Category ID:", categoryIdField,
                    "Category Name:", categoryNameField,
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Update Category", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                int newCategoryId = Integer.parseInt(categoryIdField.getText());
                String newCategoryName = categoryNameField.getText();

                // Validate and update category
                // Note: This requires adjusting your data model to support changing IDs, which might involve more complexity,
                // such as updating references in related concepts/components.
            }
        } else {
            JOptionPane.showMessageDialog(this, "Category not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeAction(int categoryId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this category?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            boolean success = dbManager.deleteCategory(categoryId);
            if(success) {
                JOptionPane.showMessageDialog(this, "Category removed successfully.");
                refreshTableData(); // Refresh the data in the table to show that the category has been removed

                // Find the corresponding row in the table and remove it
                DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    if(categoryId == (Integer) model.getValueAt(i, 0)) {
                        model.removeRow(i);
                        break; // Break the loop after removing the row
                    }
                }

                // Notify the table that the data model has changed
                dataTable.setModel(model);

            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove category.");
            }
        }
    }


    private void handleCommand(ActionEvent e) {
        feedbackLabel.setText("Command: " + e.getActionCommand());
    }

    public void loadFile(File selectedFile) {
        try {
            boolean loaded = dbManager.loadDataFromFile(selectedFile.getAbsolutePath());
            if (loaded) {
                refreshTableData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load data from file.", "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "General Error: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    class ButtonPanelRenderer implements TableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            System.out.println("Rendering row: " + row + ", column: " + column); // Debug print
            System.out.println("Value class: " + value.getClass().getName()); // Check the class of the value

            if (value instanceof JPanel) {
                return (JPanel) value;
            } else {
                return new JLabel("Not a Panel");
            }
        }
    }


    class ButtonPanelEditor extends DefaultCellEditor {
        protected JPanel panel;

        public ButtonPanelEditor(JCheckBox checkBox) {
            super(checkBox);
            this.clickCountToStart = 1; // This makes the editor immediately active on a single click
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof JPanel) {
                this.panel = (JPanel) value;
                return this.panel;
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return this.panel;
        }
    }

    // This method is called when you want to view the details of a specific concepts
    private void viewConceptDetails(Concept concept) {
        // Create a JDialog to show the concept details
        JDialog conceptDetailsDialog = new JDialog(this, "Concept Details", true);
        conceptDetailsDialog.setLayout(new BorderLayout());

        // Main panel that will contain all the details
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Concept topic and details
        JLabel conceptNameLabel = new JLabel("Concept: " + concept.getTopic());
        conceptNameLabel.setFont(new Font("Serif", Font.BOLD, 18));
        JLabel conceptDetailsLabel = new JLabel("<html><p style='width: 300px;'>" + concept.getDetails() + "</p></html>");

        // Add the labels to the panel
        contentPanel.add(conceptNameLabel);
        contentPanel.add(conceptDetailsLabel);

        // A separator for visual structure
        JSeparator separator = new JSeparator();
        contentPanel.add(separator);

        // Create a table to display the components
        String[] columnNames = {"Component Topic", "Description"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table cells not editable
            }
        };
        JTable componentsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(componentsTable);

        // Fill the table with the concept's components
        List<Component> components = dbManager.getComponentsByConceptId(concept.getId()); // Fetch the components
        for (Component comp : components) {
            model.addRow(new Object[]{comp.getTopic(), comp.getDetails()});
        }

        // Add table to the content panel
        contentPanel.add(scrollPane);

        // Add the main content panel to the dialog
        conceptDetailsDialog.add(contentPanel, BorderLayout.CENTER);

        // Set the size of the dialog and make it visible
        conceptDetailsDialog.setSize(new Dimension(400, 300));
        conceptDetailsDialog.setLocationRelativeTo(null); // Center the dialog
        conceptDetailsDialog.setVisible(true);
    }

    private void showAddOptionsDialog() {
        String[] options = {"Category", "Concept", "Component"};
        int choice = JOptionPane.showOptionDialog(this,
                "What would you like to add?",
                "Select Type",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        switch (choice) {
            case 0: // Category
                showAddCategoryDialog();
                break;
            case 1: // Concept
                showAddConceptDialog();
                break;
            case 2: // Component
                showAddComponentDialog();
                break;
            default:
                // User closed dialog or clicked cancel
                break;
        }
    }

    private void showAddCategoryDialog() {
        JTextField idField = new JTextField();
        JTextField topicField = new JTextField();
        Object[] message = {
                "Category ID:", idField,
                "Category Topic:", topicField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Category", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int id = Integer.parseInt(idField.getText().trim()); // Add validation as necessary
            String topic = topicField.getText().trim();
            dbManager.addCategory(new Category(id, topic)); // Assuming this method exists in your DatabaseManager
            if(dbManager.getCategory(id) != null) {
                refreshTableData(); // Refresh data to reflect changes
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add category.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddConceptDialog() {
        JTextField idField = new JTextField();
        JTextField topicField = new JTextField();
        JTextField detailsField = new JTextField();
        JComboBox<String> categoryComboBox = new JComboBox<>();

        // Fill the JComboBox with existing categories
        for (Category category : dbManager.getCategories()) {
            categoryComboBox.addItem(category.getTopic());
        }

        Object[] message = {
                "Concept ID:", idField,
                "Concept Topic:", topicField,
                "Category:", categoryComboBox,
                "Details:", detailsField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Concept", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int id = Integer.parseInt(idField.getText().trim()); // Add validation as necessary
            String topic = topicField.getText().trim();
            String categoryName = (String) categoryComboBox.getSelectedItem();
            String details = detailsField.getText().trim();

            // As addConcept() now doesn't return a boolean, no need to check for a return value
            dbManager.addConcept(new Concept(id, topic, categoryName, details));
            // updateUI() method will now be called within the addConcept method if necessary
        }
    }

    private void showAddComponentDialog() {
        JTextField topicField = new JTextField();
        JTextArea detailsField = new JTextArea(5, 20); // Provides more space for details
        JScrollPane detailsScrollPane = new JScrollPane(detailsField); // Make the TextArea scrollable

        JComboBox<Concept> conceptComboBox = new JComboBox<>();
        // Fill the JComboBox with existing concepts
        for (Concept concept : dbManager.getConcepts()) {
            conceptComboBox.addItem(concept);
        }

        // Ensuring that the combo box is set to display the concept's topic
        conceptComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Concept) {
                    setText(((Concept) value).getTopic());
                }
                return this;
            }
        });

        Object[] message = {
                "Concept:", conceptComboBox,
                "Component Topic:", topicField,
                "Details:", detailsScrollPane
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Component", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String topic = topicField.getText().trim();
            String details = detailsField.getText().trim();
            Concept selectedConcept = (Concept) conceptComboBox.getSelectedItem();
            if (selectedConcept != null) {
                int newComponentId = dbManager.generateNewComponentId(); // Generate a new ID for the component
                dbManager.addComponent(new Component(topic, details, selectedConcept.getId()));
                // Assuming addComponent updates the UI, no need to explicitly call updateUI here
            }
        }
    }

    private void showPrintDialog() {
        // Create a new JDialog
        JDialog printDialog = new JDialog(this, "Print Data", true);
        printDialog.setLayout(new BorderLayout());

        // Set up the table model
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Category");
        tableModel.addColumn("Concept");
        tableModel.addColumn("Details");

        // Fetch data from the database
        String sql = "SELECT c.Concept_ID, cat.Category_Topic, c.Concept_Topic, comp.Component_Description " +
                "FROM Concept c " +
                "JOIN Category cat ON c.Category_ID = cat.Category_ID " +
                "JOIN Component comp ON c.Concept_ID = comp.Concept_ID";

        try (Connection conn = dbManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Add rows to the table model
                tableModel.addRow(new Object[]{
                        rs.getInt("Concept_ID"),
                        rs.getString("Category_Topic"),
                        rs.getString("Concept_Topic"),
                        rs.getString("Component_Description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading print data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit the method if there's an error
        }

        // Create the table
        JTable dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable); // Make table scrollable
        dataTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        dataTable.setFillsViewportHeight(true);

        // Add table to dialog
        printDialog.add(scrollPane, BorderLayout.CENTER);

        // Configure and display the dialog
        printDialog.pack();
        printDialog.setLocationRelativeTo(this); // Center the dialog
        printDialog.setVisible(true);
    }

    private static final String[] DIAGRAM_TYPES = {
            "Flowchart",
            "Venn Diagram",
            "UML Diagram",
            "Mind Map",
            "Fishbone Diagram",
            "Concept Map",
            "Affinity Diagram",
            "Relationship Diagram"
    };

    // Method to recommend a diagram
    private void recommendDiagram() {
        Random random = new Random();
        int index = random.nextInt(DIAGRAM_TYPES.length);
        String recommendedDiagram = DIAGRAM_TYPES[index];
        JOptionPane.showMessageDialog(this, "Recommended diagram type: " + recommendedDiagram, "Diagram Recommendation", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInstructions() {
        String instructionsText = "<html>" +
                "<head>" +
                "<style type='text/css'>" +
                "body { font-family: Helvetica; }" +
                "h1 { margin-top: 0; margin-bottom: 10px; font-weight: bold; }" +
                "h2 { margin-top: 10px; margin-bottom: 5px; font-weight: bold; }" +
                "ul { margin-top: 0;}" +
                "li { margin-bottom: 4px;}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>Welcome to the Concept Breakdown Tool!</h1>" +
                "<p>This guide will walk you through the process of adding Categories, Concepts, and Components to your project using the Concept Breakdown Tool.</p>" +
                "<h2>Starting Up</h2>" +
                "<ul>" +
                "<li><u>Launch the Application:</u> Open the Concept Breakdown Tool on your computer.</li>" +
                "<li><u>Main Window:</u> Upon launching, you will be greeted by the main window. Here, you can create new files, load existing ones, or access instructions.</li>" +
                "</ul>" +
                "<h2>Creating and Managing Concepts</h2>" +
                "<ul>" +
                "<li><u>Adding Categories:</u> To begin organizing your concepts, start by adding Categories. Select the 'Add Category' option from the main menu. Enter a name for your new category and confirm. This category will serve as a high-level organization for your concepts.</li>" +
                "<li><u>Adding Concepts:</u> With categories in place, you can add Concepts to them. Choose 'Add Concept' from the menu, select the relevant category, and provide a name and description for your concept. These concepts are the core ideas you'll be working with.</li>" +
                "<li><u>Adding Components:</u> Components are detailed elements under each Concept. Add a Component by selecting a concept and specifying the component's details. This allows you to break down concepts into manageable, focused parts.</li>" +
                "</ul>" +
                "<h2>Managing Your Data</h2>" +
                "<ul>" +
                "<li><u>Updating Entries:</u> Select any category, concept, or component to update its details. This is essential for keeping your project's information accurate and current.</li>" +
                "<li><u>Removing Entries:</u> Should you need to remove an entry, simply select it and choose the remove option. Confirm your decision to keep your workspace organized.</li>" +
                "</ul>" +
                "<h2>Exporting and Importing Data</h2>" +
                "<p>Your work can be exported to a file for backup or sharing purposes. Conversely, you can import data from a previously saved file to continue your work or collaborate with others.</p>" +
                "<h2>Conclusion</h2>" +
                "<p>The Concept Breakdown Tool is designed to be intuitive and easy to use. With these instructions and tips, you should be well on your way to effectively managing your projects and studies. If you encounter any issues or have feedback, please don't hesitate to reach out for support.</p>" +
                "</body></html>";

        // Display the instructions in a JOptionPane dialog with HTML formatting
        JOptionPane.showMessageDialog(this, instructionsText, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    private void modifyConceptDetails(int conceptId) {
        Concept concept = dbManager.getConcept(conceptId);
        if (concept != null) {
            JTextField topicField = new JTextField(concept.getTopic());
            JTextArea detailsArea = new JTextArea(5, 20); // Set rows and columns to make the text area larger
            detailsArea.setText(concept.getDetails());
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);

            JScrollPane detailsScroll = new JScrollPane(detailsArea);
            detailsScroll.setPreferredSize(new Dimension(300, 100)); // Set only the scroll pane's preferred size

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.add(new JLabel("Topic:"));
            panel.add(topicField);
            panel.add(Box.createRigidArea(new Dimension(0, 5))); // Add a little spacer
            panel.add(new JLabel("Details:"));
            panel.add(detailsScroll);

            int result = JOptionPane.showConfirmDialog(this, panel, "Update Concept", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String newTopic = topicField.getText().trim();
                String newDetails = detailsArea.getText().trim();

                boolean updated = dbManager.updateConcept(conceptId, newTopic, newDetails);
                if (updated) {
                    JOptionPane.showMessageDialog(this, "Concept updated successfully.", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                    // Refresh your UI here if necessary
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update the concept.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Concept not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to get the ID of the selected concept from the UI, specifically from a JTable

    private void showConceptDetails() {
        int conceptId = getSelectedConceptIdFromUI(); // This is a placeholder, you would actually get the ID from your UI component
        Concept concept = dbManager.getConcept(conceptId); // Fetch the concept from the database

        if (concept != null) {
            viewConceptDetails(concept); // Call the method to display the concept details in a dialog
        } else {
            JOptionPane.showMessageDialog(this, "Concept with ID " + conceptId + " not found.", "Concept Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    public interface UIUpdateListener {
        void updateUI();
    }

    private UIUpdateListener uiUpdateListener;

    public void setUIUpdateListener(UIUpdateListener listener) {
        this.uiUpdateListener = listener;
    }

    private int getSelectedConceptIdFromUI() {
        int selectedRow = dataTable.getSelectedRow(); // Get the index of the selected row

        if (selectedRow >= 0) { // Check if a row is actually selected
            // Assume the ID is in the first column of the table model
            int conceptId = (Integer) dataTable.getModel().getValueAt(selectedRow, 0);
            return conceptId;
        } else {
            // No row is selected, or an error occurred
            JOptionPane.showMessageDialog(this, "Please select a concept from the table.", "No Concept Selected", JOptionPane.WARNING_MESSAGE);
            return -1; // Return an invalid ID to indicate that no valid selection was made
        }
    }

    private void refreshTableData() {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
            model.setRowCount(0); // Clear existing rows

            if (dbManager.getCategories().isEmpty()) {
                System.out.println("No categories to display.");
                feedbackLabel.setText("No data available.");
                return; // Early return if no categories exist
            }

            for (Category category : dbManager.getCategories()) {
                model.addRow(new Object[]{
                        category.getId(),
                        category.getTopic(),
                        createButtonPanel(category.getId(), EntityType.CATEGORY, null)
                });

                List<Concept> concepts = dbManager.getConceptsByCategoryId(category.getId());
                for (Concept concept : concepts) {
                    model.addRow(new Object[]{
                            concept.getId(),
                            "  " + concept.getTopic(), // Indent for visibility under the category
                            createButtonPanel(concept.getId(), EntityType.CONCEPT, null)
                    });

                    List<Component> components = dbManager.getComponentsByConceptId(concept.getId());
                    for (Component component : components) {
                        model.addRow(new Object[]{
                                "", // Components do not have an ID in this scenario
                                "    " + component.getTopic(), // Further indent for visibility under the concept
                                createButtonPanel(-1, EntityType.COMPONENT, component.getTopic()) // Correctly pass the component's topic here
                        });
                    }
                }
            }

            dataTable.setModel(model);
            System.out.println("Table data refreshed.");
            feedbackLabel.setText("Data loaded.");
        });
    }

    private void updateUI() {
        // SwingUtilities.invokeLater is used to update the table in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Make sure the method updateDataTable() updates the data model and UI accordingly
            updateDataTable();
            dataTable.revalidate();
            dataTable.repaint();
        });
    }

    private void viewComponentAction(String componentTopic) {
        // Here, you would implement the logic to view the details of a component.
        // For example, showing a dialog with component details.
        Component component = dbManager.getComponent(componentTopic);
        if (component != null) {
            JOptionPane.showMessageDialog(this, "Component Details:\n" + component.getDetails(), "View Component", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Component with topic " + componentTopic + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateComponentAction(String componentTopic) {
        // This method would prompt the user to enter new details for the component and then update it.
        Component component = dbManager.getComponent(componentTopic);
        if (component != null) {
            String newDetails = JOptionPane.showInputDialog(this, "Enter new details for the component:", component.getDetails());
            if (newDetails != null && !newDetails.isEmpty()) {
                // Assume there is a method in dbManager to update the component's details.
                dbManager.updateComponent(component.getConceptId(), componentTopic, componentTopic, newDetails);
                refreshTableData(); // Refresh table to show the updated details
            }
        } else {
            JOptionPane.showMessageDialog(this, "Component with topic " + componentTopic + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteComponentAction(String componentTopic) {
        // Prompt the user to confirm the deletion of the component.
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the component with topic: " + componentTopic + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Assume dbManager has a method to delete a component by topic.
            boolean success = dbManager.deleteComponent(componentTopic);
            if (success) {
                JOptionPane.showMessageDialog(this, "Component deleted successfully.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshTableData(); // Refresh table to remove the deleted component
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the component.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewCategoryAction(int categoryId) {
        Category category = dbManager.getCategoryById(categoryId);
        if (category == null) {
            JOptionPane.showMessageDialog(this, "Category with ID " + categoryId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "View Category: " + category.getTopic());
        dialog.setLayout(new BorderLayout());

        JPanel conceptsPanel = new JPanel();
        conceptsPanel.setLayout(new BoxLayout(conceptsPanel, BoxLayout.Y_AXIS));

        List<Concept> concepts = dbManager.getConceptsByCategoryId(categoryId);
        if (concepts.isEmpty()) {
            conceptsPanel.add(new JLabel("No concepts found for this category."));
        } else {
            for (Concept concept : concepts) {
                JPanel conceptPanel = new JPanel();
                conceptPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                JLabel conceptLabel = new JLabel(concept.getTopic());
                JButton viewButton = new JButton("View");
                JButton updateButton = new JButton("Update");
                JButton removeButton = new JButton("Remove");

                // Attach listeners to buttons
                viewButton.addActionListener(e -> viewConceptDetails(concept)); // Now passing the Concept object
                updateButton.addActionListener(e -> updateConceptAction(concept.getId()));
                removeButton.addActionListener(e -> deleteConceptAction(concept.getId()));

                // Add components to the concept panel
                conceptPanel.add(conceptLabel);
                conceptPanel.add(viewButton);
                conceptPanel.add(updateButton);
                conceptPanel.add(removeButton);

                // Add concept panel to the main concepts panel
                conceptsPanel.add(conceptPanel);
            }
        }

        JScrollPane scrollPane = new JScrollPane(conceptsPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateCategoryAction(int categoryId) {
        Category category = dbManager.getCategory(categoryId);
        if (category != null) {
            String newTopic = JOptionPane.showInputDialog(this, "Enter new topic for the category:", category.getTopic());
            if (newTopic != null && !newTopic.isEmpty()) {
                dbManager.updateCategory(categoryId, newTopic);
                refreshTableData(); // Refresh table to show the updated topic
            }
        } else {
            JOptionPane.showMessageDialog(this, "Category with ID " + categoryId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCategoryAction(int categoryId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the category with ID: " + categoryId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dbManager.deleteCategory(categoryId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Category deleted successfully.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshTableData(); // Refresh table to remove the deleted category
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the category.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewConceptAction(int conceptId) {
        Concept concept = dbManager.getConcept(conceptId);
        if (concept != null) {
            JOptionPane.showMessageDialog(this, "Concept Details:\n" + concept.getTopic() + "\n" + concept.getDetails(), "View Concept", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Concept with ID " + conceptId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateConceptAction(int conceptId) {
        Concept concept = dbManager.getConcept(conceptId);
        if (concept != null) {
            String newTopic = JOptionPane.showInputDialog(this, "Enter new topic for the concept:", concept.getTopic());
            String newDetails = JOptionPane.showInputDialog(this, "Enter new details for the concept:", concept.getDetails());
            if (newTopic != null && !newTopic.isEmpty() && newDetails != null && !newDetails.isEmpty()) {
                dbManager.updateConcept(conceptId, newTopic, newDetails);
                refreshTableData(); // Refresh table to show the updated concept
            }
        } else {
            JOptionPane.showMessageDialog(this, "Concept with ID " + conceptId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteConceptAction(int conceptId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the concept with ID: " + conceptId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dbManager.deleteConcept(conceptId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Concept deleted successfully.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshTableData(); // Refresh table to remove the deleted concept
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the concept.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


}
