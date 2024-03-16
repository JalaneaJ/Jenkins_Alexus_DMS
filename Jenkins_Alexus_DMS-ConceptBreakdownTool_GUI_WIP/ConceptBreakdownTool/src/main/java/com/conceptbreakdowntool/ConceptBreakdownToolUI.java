package com.conceptbreakdowntool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class ConceptBreakdownToolUI extends JFrame {
    private DatabaseManager dbManager;
    private MainApplicationWindow mainWindow;

    public ConceptBreakdownToolUI(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.mainWindow = new MainApplicationWindow(dbManager);
        setTitle("Concept Breakdown Tool");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        initComponents();
    }

    private void initComponents() {
        int gap = 25;
        int startY = (getHeight() - 3 * gap - 100) / 2;

        JLabel heading = new JLabel("Concept Breakdown Tool", SwingConstants.CENTER);
        heading.setFont(new Font("Serif", Font.BOLD, 24));
        heading.setBounds((getWidth() - 300) / 2, startY, 300, 30);

        JTextField inputField = new JTextField();
        inputField.setBounds((getWidth() - 300) / 2, startY + 30 + gap, 300, 30);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setBounds(0, startY + 60 + 2 * gap, getWidth(), 35);
        buttonsPanel.setOpaque(false);

        JButton createFileButton = new JButton("Create File");
        createFileButton.addActionListener(e -> createFile());


        JButton instructionsButton = new JButton("Instructions");
        instructionsButton.addActionListener(e -> showInstructions());


        JButton loadFileButton = new JButton("Load File");
        loadFileButton.addActionListener(e -> {
            if (!mainWindow.isVisible()) {
                mainWindow.setVisible(true); // Make sure mainWindow is made visible here
            }
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(mainWindow);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                mainWindow.loadFile(selectedFile); // Correctly passing the selected file to load
            }


        });


        buttonsPanel.add(createFileButton);
        buttonsPanel.add(instructionsButton);
        buttonsPanel.add(loadFileButton);

        add(heading);
        add(inputField);
        add(buttonsPanel);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DatabaseManager dbManager = new DatabaseManager(); // Initialize your DatabaseManager here
            MainApplicationWindow mainWindow = new MainApplicationWindow(dbManager); // Initialize MainApplicationWindow here
            ConceptBreakdownToolUI frame = new ConceptBreakdownToolUI(dbManager);
            frame.setVisible(true);
        });
    }

    private void showInstructions() {
        String instructionsText = "Welcome to the Concept Breakdown Tool Tutorial!\n" +
                "\n" +
                "This guide will walk you through the process of adding Categories, Concepts, and Components to your project using the Concept Breakdown Tool.\n" +
                "\n" +
                "# Step 1: Adding Categories\n" +
                "Categories are broad classifications under which Concepts and Components are organized.\n" +
                "- To add a new Category, choose the 'Add' option and select 'Category'.\n" +
                "- You will be prompted to enter a unique ID and the name for the Category.\n" +
                "Example: \"Category: 1, UX Design Skills\"\n" +
                "\n" +
                "# Step 2: Adding Concepts\n" +
                "Concepts are the main ideas or topics within a Category.\n" +
                "- To add a new Concept, choose 'Add' and select 'Concept'.\n" +
                "- Specify if the Concept belongs to an existing Category. If so, enter the Category Name.\n" +
                "- Provide a unique ID, the Concept Topic, and a brief Detail about the Concept.\n" +
                "Example: \"Concept: 101, Wireframing, UX Design Skills, Basics of wireframing\"\n" +
                "\n" +
                "# Step 3: Adding Components\n" +
                "Components are specific elements or aspects that make up a Concept.\n" +
                "- To add a new Component, choose 'Add' and select 'Component'.\n" +
                "- You will be asked to enter the Concept ID to which the Component belongs.\n" +
                "- Provide the Component Topic and Details.\n" +
                "Example: \"Component: Organizing Systems, How to organize content effectively\"\n" +
                "\n" +
                "Remember, each entry in the file should be on a new line, following the format provided in the examples above.\n" +
                "\n" +
                "# Tips for Successful File Creation:\n" +
                "- Ensure each ID is unique within its category (Category ID, Concept ID).\n" +
                "- Be descriptive yet concise in your details to facilitate understanding and organization.\n" +
                "- Review your entries for accuracy before saving your file.\n" +
                "\n" +
                "Thank you for using the Concept Breakdown Tool! We hope this tutorial helps you get started with organizing and managing your concepts effectively.\n";



        // Display the instructions in a JOptionPane dialog
        JOptionPane.showMessageDialog(this, instructionsText, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    private void createFile() {
        // Prompt the user to enter a file name
        String fileName = JOptionPane.showInputDialog(this, "Enter a name for the tutorial file:", "Create Tutorial File", JOptionPane.PLAIN_MESSAGE);
        if (fileName == null || fileName.trim().isEmpty()) {
            return; // User cancelled or entered an empty name
        }
        if (!fileName.endsWith(".txt")) {
            fileName += ".txt"; // Ensure the file has a .txt extension
        }

        // Define the path and create a File object
        File tutorialFile = new File(fileName);

        // Tutorial content generated by generateTutorialContent
        String content = generateTutorialContent();

        // Write the tutorial content to the file and then load it
        try (FileWriter writer = new FileWriter(tutorialFile)) {
            writer.write(content);
            JOptionPane.showMessageDialog(this, "Tutorial file created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Load the created tutorial file into the main application window
            if (mainWindow != null) {
                mainWindow.setVisible(true);
                mainWindow.loadFile(tutorialFile); // Corrected to pass the File object
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to create the tutorial file:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private String generateTutorialContent() {
        return
                "Category: 0,Tutorial\n" +
                        "Concept: 1,Adding Categories,Tutorial,To add a new Category, select 'Add' and choose 'Category'. Enter a unique ID and the category name." +
                        "Component: Step-by-Step Process,Choose 'Add' and select 'Category'. Provide an ID and name." +
                        "Component: Example Entry,Category: 1, UX Design Skills" +
                        "Concept: 2,Adding Concepts,Tutorial,Concepts are ideas or topics within a Category. To add, select 'Add', then 'Concept'. Specify if the Concept belongs to an existing Category by entering the Category Name. Provide an ID, Topic, and Details." +
                        "Component: Detailed Instructions,Select 'Add', choose 'Concept', and fill in the required information." +
                        "Component: Example Entry,Concept: 101, Wireframing, UX Design Skills, Basics of wireframing" +
                        "Concept: 3,Adding Components,Tutorial,Components are elements that make up a Concept. To add a Component, choose 'Add' and select 'Component'. You will need to specify the Concept ID it belongs to and provide details." +
                        "Component: Step-by-Step Guide,Choose 'Add', then 'Component'. Link to a Concept ID and provide the details." +
                        "Component: Example Entry,Component: Organizing Systems, How to organize content effectively.";
    }



    private void saveToFile(String content) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Append ".txt" if not present
            if (!fileToSave.getAbsolutePath().endsWith(".txt")) {
                fileToSave = new File(fileToSave + ".txt");
            }
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(content);
                JOptionPane.showMessageDialog(this, "File was saved successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error writing to file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



}
