/**
 Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Apr 11th 2024
 ClassName: MainApplicationWindow

 Purpose: Serves as the primary usher interface for the Concept Breakdown Tool application that manages categorises, concepts, and components in a structured database.

 - CONSTRUCTOR AND INITIALIZATION: Responsible for setting up the main window, database connection, and UI Components.
     *Constructor(MainApplicationWindow): Initializes the main window; Sets up the database manager connection; Prepare the UI components.
     *initComponents(): Sets up the components of the UI (Buttons, Labels, Panels).
 - UI COMPONENTS SETUP: Configures the main table, label at the bottom of the window, and the buttons.
     * setupDataTable(): Configures the main table to display data with action buttons.
     * setupFeedbackLabel(): Initializes a label at the bottom of the window to provide feedback to the user.
     * setupTableButtons(): Configured buttons within the table cells for actions like view, update, and delete.
 - ACTION HANDLERS: Handles multiple options that allows data to load, display, and print onto the main window frame.
     * loadFile(): Loads data from a selected file into the application.
     * showAddOptionsDialog(): Displays a dialog for selecting whether to add a category, concept, or component.
     * showAddCategoryDialog(): Dialogues for adding a category.
     * showAddConceptDialog(): Dialogues for adding a concept.
     * showAddComponentDialog(): Dialogues for adding a component.
     * showPrintDialog(): Shows a dialog with printable content from the database.
     * recommendDiagram: Randomly recommends a diagram type.
     * showInstructions(): Displays instructions for using the application.
 - DATA MANIPULATION METHODS: Updates the database and GUI.
    * updateDataTable(): Refreshes and updates the data shown in the main table.
    * refreshTableData(): Refreshes the data in the table.
    * updateUI(): Updates the UI elements, primarily the data table, to ensure they reflect the current state of data within the database.
    * modifyConceptDetails(): Allows updating the details of a selected concept.
- ENTITY ACTION METHODS: Handles action specific to categorises, concepts, and components.
    * viewCategoryAction(), viewConceptAction(), viewComponentAction(): Methods to view categories, concepts, and components.
    * updateCategoryAction(), updateConceptAction(), updateComponentAction(): Updates the categories, concepts, and components.
    * deleteCategoryAction(), deleteConceptAction(), deleteComponentAction(): Deletes the categories, concepts, and components.
 - UTILITY CLASSES
    * ButtonPanelRenderer: Customizes the cell rendered for embedding buttons within table cells.
    * ButtonPanelEditor: Customizes the cell editor for embedding buttons within table cells.
 - DIALOG AND DETAIL VIEWING
    * viewConceptDetails(): Opens a detailed view for a specific concept, including its components.
 - NESTED INTERFACES/CLASSES
    * EntityType(Enum): Defines types of entities to streamline handling in UI components.
    * UIUpdateListener(Interface): Provides an interface for updating the GUI.

 @author Alexus Jenkins
 @version 5.0
 **/

package com.conceptbreakdowntool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
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
    private DatabaseManager dbManager;
    private JTable dataTable;
    private JLabel feedbackLabel;
    private JPanel panel;

    /**
     CONSTRUCTOR AND INITIALIZATION: Responsible for setting up the main window, database connection, and UI Components.
     **/
    /**
     Constructor(MainApplicationWindow): Initializes the main window; Sets up the database manager connection; Prepare the UI components.
     * @param dbManager the database manager to handle data operations
     * @param startupUI the initial startup user interface
     **/
    public MainApplicationWindow(DatabaseManager dbManager, ConceptBreakdownToolUI startupUI) {
        this.startupUI = startupUI;
        this.dbManager = dbManager;
        this.dbManager.setUIUpdateListener(this::updateUI);

        setTitle("Concept Breakdown Tool");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     initComponents(): Sets up the components of the UI (Buttons, Labels, Panels).
     **/
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

    /**
     setupDataTable(): Configures the main table to display data with action buttons.
     **/
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

    /**
     setupFeedbackLabel(): Initializes a label at the bottom of the window to provide feedback to the user.
     **/
    private void setupFeedbackLabel() {
        feedbackLabel = new JLabel("Ready.", SwingConstants.CENTER);
        add(feedbackLabel, BorderLayout.SOUTH);
    }

    /**
     setupTableButtons(): Configured buttons within the table cells for actions like view, update, and delete.
     **/
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


    /**
     ACTION HANDLERS: Handles multiple options that allows data to load, display, and print onto the main window frame.
     **/

    /**
     loadFile(): Loads data from a selected file into the application.
     * @param selectedFile the file selected by the user to load
     **/
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

    /**
     showAddOptionsDialog(): Displays a dialog for selecting whether to add a category, concept, or component.
    **/
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

    /**
     showAddCategoryDialog(): Dialogues for adding a category.
     **/
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

    /**
     showAddConceptDialog(): Dialogues for adding a concept.
     **/
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

    /**
     showAddComponentDialog(): Dialogues for adding a component.
     **/
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

    /**
     showPrintDialog(): Shows a dialog with printable content from the database.
     **/
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

    /**
     recommendDiagram: Randomly recommends a diagram type.
     **/
    private void recommendDiagram() {
        Random random = new Random();
        int index = random.nextInt(DIAGRAM_TYPES.length);
        String recommendedDiagram = DIAGRAM_TYPES[index];
        JOptionPane.showMessageDialog(this, "Recommended diagram type: " + recommendedDiagram, "Diagram Recommendation", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     showInstructions(): Displays instructions for using the application.
     **/
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
                "<p>This guide will walk you through the process of viewing, adding, updating, and removing categories, concepts, and components to your database.</p>" +
                "<h2>Starting Up</h2>" +
                "<ul>" +
                "<li><u>Launch the Application:</u> Open the Concept Breakdown Tool project on your computer and enter the path to the database file.</li>" +
                "<li><u>Create File:</u> Click the 'Create File' button and it will create a .db file.</li>" +
                "</ul>" +
                "<li><u>Load File:</u> Click the 'Load File' button and load your .db file.</li>" +
                "</ul>" +
                "<h2>Navigation and Buttons</h2>" +
                "<ul>" +
                "<li><u>Menu:</u> Click the 'Menu' button and it will show three buttons - 'Add', 'Print', and 'Recommend'.</li>" +
                "<li><u>Add:</u> It will add a category, concept, and/or component to your .db file. You cannot create a concept without a category and you cannot create a component without a concept.</li>" +
                "<li><u>Print:</u> Displays a table with all of the content from the .db file.</li>" +
                "<li><u>Recommend:</u> It will recommend you a diagram to transform your data into a visual aid.</li>" +
                "<li><u>View:</u> You will join another window where you can view and/or modify concepts and/or components.</li>" +
                "<li><u>Updates:</u> You will be able to modify the ID, Topic, and Description of a category, concept, and/or component.</li>" +
                "<li><u>Remove:</u> You will be able to remove a category, concept, or component.</li>" +
                "</ul>" +
                "<h2>Conclusion</h2>" +
                "<p>The Concept Breakdown Tool is designed to progressively improve visual-spatial learners experience with learning concepts. If you encounter any issues, reach out to alexusjenkins@uiuxdesign.us</p>" +
                "</body></html>";

        // Display the instructions in a JOptionPane dialog with HTML formatting
        JOptionPane.showMessageDialog(this, instructionsText, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }


    /**
     DATA MANIPULATION METHODS: Updates the database and GUI.
     **/
    /**
     updateDataTable(): Refreshes and updates the data shown in the main table.
     **/
    private void updateDataTable() {
        DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
        model.setRowCount(0); // Clear existing data

        for (Category category : dbManager.getCategories()) {
            JPanel buttonPanel = createButtonPanel(category.getId(), EntityType.CATEGORY, category.getTopic());
            model.addRow(new Object[]{category.getId(), category.getTopic(), buttonPanel});
        }

        dataTable.setModel(model);
        System.out.println("Table data refreshed.");
    }

    /**
     refreshTableData(): Refreshes the data in the table.
     **/
    private void refreshTableData() {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
            model.setRowCount(0); // Clear existing rows

            // Load only categories into the table
            for (Category category : dbManager.getCategories()) {
                model.addRow(new Object[]{
                        category.getId(),
                        category.getTopic(),
                        createButtonPanel(category.getId(), EntityType.CATEGORY, category.getTopic())
                });
            }

            dataTable.setModel(model);
            System.out.println("Table data refreshed.");
            feedbackLabel.setText("Data loaded.");
        });
    }

    /**
     updateUI(): Updates the UI elements, primarily the data table, to ensure they reflect the current state of data within the database.
     **/
    private void updateUI() {
        // SwingUtilities.invokeLater is used to update the table in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Make sure the method updateDataTable() updates the data model and UI accordingly
            updateDataTable();
            dataTable.revalidate();
            dataTable.repaint();
        });
    }

    /**
     modifyConceptDetails(): Allows updating the details of a selected concept.
     * @param conceptId the ID of the concept to be updated
     **/
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


    /**
     ENTITY ACTION METHODS: Handles action specific to categorises, concepts, and components.
     **/
    /**
     viewCategoryAction(): Methods to view categories, concepts, and components.
     * @param categoryId the ID of the category to view
     **/
    private void viewCategoryAction(int categoryId) {
        Category category = dbManager.getCategoryById(categoryId);
        if (category == null) {
            JOptionPane.showMessageDialog(this, "Category with ID " + categoryId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "View Category: " + category.getTopic(), true);
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

                viewButton.addActionListener(e -> viewConceptDetails(concept));
                updateButton.addActionListener(e -> updateConceptAction(concept.getId()));
                removeButton.addActionListener(e -> handleRemoveAction(dialog, concept, conceptsPanel, conceptPanel, categoryId));

                conceptPanel.add(conceptLabel);
                conceptPanel.add(viewButton);
                conceptPanel.add(updateButton);
                conceptPanel.add(removeButton);

                conceptsPanel.add(conceptPanel);
            }
        }

        JScrollPane scrollPane = new JScrollPane(conceptsPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     viewConceptAction(): Methods to view categories, concepts, and components.
     * @param conceptId the ID of the concept to view
     **/
    private void viewConceptAction(int conceptId) {
        Concept concept = dbManager.getConcept(conceptId);
        if (concept != null) {
            viewConceptDetails(concept);
        } else {
            JOptionPane.showMessageDialog(this, "Concept with ID " + conceptId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     viewComponentAction(): Methods to view categories, concepts, and components.
     * @param componentTopic the topic of the component to view
     **/
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

    /**
     updateCategoryAction(): Updates the categories, concepts, and components.
     * @param categoryId the ID of the category to update
     **/
    private void updateCategoryAction(int categoryId) {
        // Fetch the current category details
        Category category = dbManager.getCategoryById(categoryId);
        if (category == null) {
            JOptionPane.showMessageDialog(this, "Category with ID " + categoryId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show dialog with current details for editing
        JTextField idField = new JTextField(String.valueOf(category.getId()));
        JTextField nameField = new JTextField(category.getTopic());
        Object[] message = {
                "Category ID:", idField,
                "Category Name:", nameField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Update Category", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int newId = Integer.parseInt(idField.getText().trim());
                String newName = nameField.getText().trim();

                // Check if the ID has changed and if so, validate it
                if (newId != categoryId && dbManager.getCategoryById(newId) != null) {
                    JOptionPane.showMessageDialog(this, "Category ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Now update the category in the database
                boolean updated = dbManager.updateCategory(newId, newName);
                if (updated) {
                    JOptionPane.showMessageDialog(this, "Category updated successfully.", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                    refreshTableData(); // Refresh data to show updated values
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update the category.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for category ID.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     updateConceptAction(): Updates the categories, concepts, and components.
     * @param conceptId the ID of the concept to update
     **/
    private void updateConceptAction(int conceptId) {
        Object[] options = {"Concept", "Component"};
        int choice = JOptionPane.showOptionDialog(this,
                "Are you updating the Concept or Component?",
                "Update Options",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        switch (choice) {
            case 0: // Update Concept
                modifyConceptDetails(conceptId);
                break;
            case 1: // Update Component
                updateComponentForConcept(conceptId);
                break;
            default:
                break;
        }
    }

    /**
     updateComponentAction(): Updates the categories, concepts, and components.
     * @param componentTopic the topic of the component to update
     **/
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

    /**
     deleteCategoryAction(): Deletes the categories, concepts, and components.
     * @param categoryId the ID of the category to delete
     **/
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

    /**
     deleteConceptAction(): Deletes the categories, concepts, and components.
     * @param conceptId the ID of the concept to delete
     **/
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

    /**
     deleteComponentAction(): Deletes the categories, concepts, and components.
     * @param componentTopic the topic of the component to delete
     **/
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

    /**
     UTILITY CLASSES
     **/
    /**
     ButtonPanelRenderer: Customizes the cell rendered for embedding buttons within table cells.**/
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

    /**
     ButtonPanelEditor: Customizes the cell editor for embedding buttons within table cells.**/
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

    /**
        DIALOG AND DETAIL VIEWING:
     **/
    /**
     viewConceptDetails(): Opens a detailed view for a specific concept, including its components.
     * @param concept the Concept object whose details are to be displayed
     **/
    private void viewConceptDetails(Concept concept) {
        JDialog detailsDialog = new JDialog(this, "Concept Details", true);
        detailsDialog.setLayout(new BorderLayout(10, 10)); // Margins between components

        // Panel for concept name and details
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(concept.getTopic());
        JTextArea detailsArea = new JTextArea(2, 5); // Set preferred size by rows and columns
        detailsArea.setText(concept.getDetails());
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);

        // Add components to the details panel
        detailPanel.add(nameLabel);
        detailPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Space between nameLabel and detailsArea
        detailPanel.add(detailsScrollPane);

        // Table to display components
        String[] columnNames = {"Component Topic", "Component Description"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable componentsTable = new JTable(model);
        componentsTable.setFillsViewportHeight(true);

        // Fill the table with component data
        List<Component> components = dbManager.getComponentsByConceptId(concept.getId());
        for (Component comp : components) {
            model.addRow(new Object[]{comp.getTopic(), comp.getDetails()});
        }

        JScrollPane tableScrollPane = new JScrollPane(componentsTable);

        // Add panels to the dialog
        detailsDialog.add(detailPanel, BorderLayout.NORTH);
        detailsDialog.add(tableScrollPane, BorderLayout.CENTER);

        // Configure and display the dialog
        detailsDialog.pack();
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setVisible(true);
    }

    /**
     NESTED INTERFACES CLASSES
     **/

    /**
     EntityType(Enum): Defines types of entities to streamline handling in UI components.
     **/
    enum EntityType {
        CATEGORY,
        CONCEPT,
        COMPONENT
    }

    /**
     createButtonPanel(): Creates a JPanel with buttons based on category, concept, or component.
     * @param id the entity ID
     * @param type the EntityType (CATEGORY, CONCEPT, COMPONENT)
     * @param topic the topic of the entity (used for COMPONENT)
     * @return JPanel containing action buttons
     **/
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


    /**UIUpdateListener():Provides an interface for updating the GUI. **/
    public interface UIUpdateListener {
        void updateUI();
    }

    /**
     updateComponentForConcept(): Displays a dialog that will allow the user to update component based on concept ID.
     * @param conceptId The ID of the concept whose components are to be updated.
     **/
    private void updateComponentForConcept(int conceptId) {
        // Fetch the Concept to get its Components
        Concept concept = dbManager.getConcept(conceptId);
        if (concept == null) {
            JOptionPane.showMessageDialog(this, "Concept not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get the list of Components for this Concept
        List<Component> components = dbManager.getComponentsByConceptId(conceptId);
        if (components.isEmpty()) {
            JOptionPane.showMessageDialog(this, "This Concept has no Components to update.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Let the user select which Component to update
        Component selectedComponent = (Component) JOptionPane.showInputDialog(this,
                "Select the Component to update:",
                "Select Component",
                JOptionPane.QUESTION_MESSAGE,
                null,
                components.toArray(new Component[0]),
                components.get(0));

        if (selectedComponent != null) {
            // Proceed to update the selected Component
            updateComponentDetails(selectedComponent);
        }
    }
    /**
     updateComponentDetails(): Displays a dialog that will allow users to update component details.
     * @param component The component object to be updated.
     **/
    private void updateComponentDetails(Component component) {
        JTextField topicField = new JTextField(component.getTopic());
        JTextArea detailsArea = new JTextArea(5, 20);
        detailsArea.setText(component.getDetails());
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setPreferredSize(new Dimension(300, 100));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel("Topic:"));
        panel.add(topicField);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        panel.add(new JLabel("Details:"));
        panel.add(detailsScroll);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Component", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String newTopic = topicField.getText().trim();
            String newDetails = detailsArea.getText().trim();

            boolean updated = dbManager.updateComponent(component.getConceptId(), component.getTopic(), newTopic, newDetails);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Component updated successfully.", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshTableData(); // Refresh the table to show the updated data
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update the component.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    /**
     handleRemoveAction(): Removes concepts or components along with a confirmation dialog for deleting the content.
     * @param dialog The parent dialog from which the action was triggered.
     * @param concept The concept object from which a component might be removed.
     * @param conceptsPanel The panel displaying the concepts, to be updated on removal.
     * @param conceptPanel The specific panel of the concept being removed.
     * @param categoryId The ID of the category under which the concept is categorized.
     **/
    private void handleRemoveAction(JDialog dialog, Concept concept, JPanel conceptsPanel, JPanel conceptPanel, int categoryId) {
        // Define removal options
        Object[] options = {"Concept", "Component"};
        int response = JOptionPane.showOptionDialog(
                dialog,
                "Do you want to remove the entire concept or just a component?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (response == JOptionPane.YES_OPTION) {
            // Confirm and delete the entire concept
            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "Are you sure you want to delete the entire concept: " + concept.getTopic() + "?",
                    "Confirm Concept Deletion",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                boolean removed = dbManager.deleteConcept(concept.getId());
                if (removed) {
                    conceptsPanel.remove(conceptPanel);
                    conceptsPanel.revalidate();
                    conceptsPanel.repaint();
                    JOptionPane.showMessageDialog(dialog, "Concept removed successfully.", "Removal Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to remove the concept.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (response == JOptionPane.NO_OPTION) {
            // Confirm and delete a component
            List<Component> componentList = dbManager.getComponentsByConceptId(concept.getId());
            Component[] componentsArray = componentList.toArray(new Component[0]);
            Component selectedComponent = (Component) JOptionPane.showInputDialog(
                    dialog,
                    "Select the Component to delete:",
                    "Select Component",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    componentsArray,
                    componentsArray[0]
            );

            if (selectedComponent != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        dialog,
                        "Are you sure you want to delete the component: " + selectedComponent.getTopic() + "?",
                        "Confirm Component Deletion",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean removed = dbManager.deleteComponent(selectedComponent.getTopic());
                    if (removed) {
                        JOptionPane.showMessageDialog(dialog, "Component removed successfully.", "Removal Successful", JOptionPane.INFORMATION_MESSAGE);
                        // Refresh the concept panel to remove the component.
                        viewCategoryAction(categoryId);
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to remove the component.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

}
