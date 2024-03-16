package com.conceptbreakdowntool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.List;


public class MainApplicationWindow extends JFrame {
    private DatabaseManager dbManager; // Placeholder for your DatabaseManager
    private JTable dataTable;
    private JLabel feedbackLabel;
    private JPanel panel;

    public MainApplicationWindow(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setTitle("Main Application Window");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(createInstructionAndStartupPanel());
        topPanel.add(createCommandPanel());
        add(topPanel, BorderLayout.NORTH);

        setupDataTable();
        setupFeedbackLabel();
    }

    private JPanel createInstructionAndStartupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton instructionsButton = new JButton("Instructions");
        JButton backToStartupButton = new JButton("Back to Startup");
        panel.add(instructionsButton, BorderLayout.WEST);
        panel.add(backToStartupButton, BorderLayout.EAST);
        return panel;
    }

    private JPanel createCommandPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        String[] commands = {"Add", "Update", "Remove", "Print", "Recommend", "Load"};
        for (String cmd : commands) {
            JButton button = new JButton(cmd);
            button.addActionListener(this::handleCommand);
            panel.add(button);
        }
        return panel;
    }

    private void setupDataTable() {
        dataTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Category Name", "Actions"}, 0));
        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);
        dataTable.setRowHeight(40); // Adjust row height for buttons

        // Initialize and set up your table buttons here
        setupTableButtons();

        // Optionally, call updateDataTable() here if you want to populate the table at startup
        // (assuming dbManager is already loaded with data)
        // updateDataTable();
    }


    private void setupFeedbackLabel() {
        feedbackLabel = new JLabel("Ready.", SwingConstants.CENTER);
        add(feedbackLabel, BorderLayout.SOUTH);
    }

    private void setupTableButtons() {
        TableColumn actionCol = dataTable.getColumnModel().getColumn(2); // Adjust the index as necessary
        actionCol.setCellRenderer(new ButtonPanelRenderer());
        actionCol.setCellEditor(new ButtonPanelEditor(new JCheckBox())); // Use your custom editor
    }





    private void updateDataTable() {
        DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
        model.setRowCount(0); // Clear existing data

        // Populate the table with actual data from DatabaseManager
        for (Category category : dbManager.getCategories()) {
            model.addRow(new Object[]{
                    category.getId(),
                    category.getTopic(),
                    createButtonPanel(category.getId()) // Use a method that creates a panel with the buttons for each row
            });
        }
    }


    private JPanel createButtonPanel(int categoryId) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewButton = new JButton("View");
        JButton modifyButton = new JButton("Modify");
        JButton removeButton = new JButton("Remove");

        // Adding action listeners to each button
        viewButton.addActionListener(e -> viewAction(categoryId));
        modifyButton.addActionListener(e -> modifyAction(categoryId));
        removeButton.addActionListener(e -> removeAction(categoryId));

        // Add the buttons to the panel
        panel.add(viewButton);
        panel.add(modifyButton);
        panel.add(removeButton);

        return panel;
    }


    private void viewAction(int categoryId) {
        Category category = dbManager.getCategoryById(categoryId);
        if (category != null) {
            // Create a dialog to display the details
            JDialog detailsDialog = new JDialog();
            detailsDialog.setTitle("Category and Concept Details");
            detailsDialog.setLayout(new BorderLayout());

            // Create a panel for category details
            JPanel categoryPanel = new JPanel(new GridLayout(0, 1));
            categoryPanel.add(new JLabel("Category ID: " + category.getId()));
            categoryPanel.add(new JLabel("Category Topic: " + category.getTopic()));

            detailsDialog.add(categoryPanel, BorderLayout.NORTH);

            // Get the concepts by category ID
            List<Concept> concepts = dbManager.getConceptsByCategoryId(categoryId);

            // For each concept, display its topic, details, and components
            for (Concept concept : concepts) {
                JPanel conceptPanel = new JPanel(new GridLayout(0, 1));
                conceptPanel.add(new JLabel("Concept Topic: " + concept.getTopic()));
                conceptPanel.add(new JLabel("Concept Details: " + concept.getDetails()));

                // Create a table for components
                String[] columnNames = {"Component", "Description"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);
                JTable componentsTable = new JTable(model);

                // Populate the table with components of the current concept
                List<Component> components = concept.getComponents();
                for (Component comp : components) {
                    model.addRow(new Object[]{comp.getTopic(), comp.getDetails()});
                }

                // Add the concept panel and its components table to the dialog
                detailsDialog.add(conceptPanel, BorderLayout.CENTER);
                detailsDialog.add(new JScrollPane(componentsTable), BorderLayout.SOUTH);

                // Adjustments for multiple concepts
                // Note: You might need to create a more complex layout if multiple concepts are to be displayed properly
            }

            detailsDialog.pack();
            detailsDialog.setLocationRelativeTo(this);
            detailsDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Category not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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

            int option = JOptionPane.showConfirmDialog(null, message, "Modify Category", JOptionPane.OK_CANCEL_OPTION);
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



    // Inside MainApplicationWindow.java

    private void removeAction(int categoryId) {
        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this category?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean removedCategory = dbManager.removeCategoryById(categoryId); // This method should return the removed Category
            updateDataTable(); // Refresh the table view
            // To implement undo, save the removedCategory object and offer to re-add it if the user chooses to undo
        }
    }



    private void handleCommand(ActionEvent e) {
        feedbackLabel.setText("Command: " + e.getActionCommand());
    }

    public void loadFile(File selectedFile) {
        dbManager.loadDataFromFile(selectedFile.getAbsolutePath());
        updateDataTable();
    }


    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof JPanel) { // Check if the value is indeed a JPanel
                return (JPanel) value;
            } else {
                // It's not a JPanel, so handle accordingly, maybe log a warning or return a placeholder
                this.setText("Invalid"); // Placeholder action, adjust as necessary
                return this;
            }
        }
    }

    class ButtonPanelRenderer implements TableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Ensure the value is indeed a JPanel before casting
            if (value instanceof JPanel) {
                // Correct handling, returning the JPanel as is
                return (JPanel) value;
            } else {
                // Fallback handling for values that are not JPanels
                return new JLabel(value == null ? "" : value.toString());
            }
        }
    }


    class ButtonPanelEditor extends DefaultCellEditor {
        protected JPanel panel;

        public ButtonPanelEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Assuming the value is your JPanel with buttons
            if (value instanceof JPanel) {
                this.panel = (JPanel) value;
                // You can customize your panel or buttons here if needed
                return this.panel;
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            // Here, return the value you want to be associated with the cell.
            // This could be a specific value from the buttons or the panel itself.
            return panel;
        }
    }






    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout());
        private Object editorValue;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.add(new JButton("View"));
            panel.add(new JButton("Modify"));
            panel.add(new JButton("Remove"));
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof JPanel) {
                this.panel = (JPanel) value;
                return this.panel;
            }
            return null; // Or a new JPanel() as a fallback to avoid null returns.
        }


        @Override
        public Object getCellEditorValue() {
            return editorValue;
        }
    }

    private void loadConceptsFromFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Category:")) {
                    // Parse and add category
                    String[] parts = line.split(",", 2);
                    int id = Integer.parseInt(parts[0].split(": ")[1]);
                    String name = parts[1];
                    dbManager.addCategory(new Category(id, name));
                } else if (line.startsWith("Concept:")) {
                    // Parse and add concept
                    String[] parts = line.split(",", 4);
                    int id = Integer.parseInt(parts[0].split(": ")[1]);
                    String name = parts[1];
                    String categoryName = parts[2];
                    String detail = parts[3];
                    dbManager.addConcept(new Concept(id, name, categoryName, detail));
                } else if (line.startsWith("Component:")) {
                    // Parse and add component
                    String[] parts = line.split(",", 3);
                    String name = parts[0].split(": ")[1];
                    String detail = parts[2];
                    dbManager.addComponent(new Component(name, detail));
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found: " + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error parsing ID from file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // After loading, update the table
        updateDataTable();
    }


    private JDialog createDetailsViewForCategory(Category category) {
        JDialog detailsDialog = new JDialog(this, "Category Details", true);
        detailsDialog.setLayout(new BorderLayout());

        // Assuming you have a method getConceptsByCategory in DatabaseManager
        List<Concept> concepts = dbManager.getConceptsByCategory(category.getTopic());
        String[] columnNames = {"Concept ID", "Concept Topic", "Details"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (Concept concept : concepts) {
            model.addRow(new Object[]{concept.getId(), concept.getTopic(), concept.getDetails()});
        }

        JTable detailsTable = new JTable(model);
        detailsDialog.add(new JScrollPane(detailsTable), BorderLayout.CENTER);
        detailsDialog.setSize(500, 400); // Set size as per your need
        detailsDialog.setLocationRelativeTo(this); // Center on screen
        return detailsDialog;
    }

    private void modifyCategory(int categoryId) {
        Category category = dbManager.getCategoryById(categoryId);
        if (category != null) {
            // Prompt for a new name
            String newName = JOptionPane.showInputDialog(this, "Enter new name for the category:", category.getTopic());
            if (newName != null && !newName.trim().isEmpty()) {
                // Update the category name
                dbManager.updateCategoryName(categoryId, newName);
                // Refresh data views as necessary
                updateDataTable(); // Assuming this method exists to refresh the UI with updated data
            }
        } else {
            JOptionPane.showMessageDialog(this, "Category not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



}
