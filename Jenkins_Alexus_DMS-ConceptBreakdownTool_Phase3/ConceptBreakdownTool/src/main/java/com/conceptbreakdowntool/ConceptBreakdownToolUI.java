package com.conceptbreakdowntool;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class ConceptBreakdownToolUI extends JFrame {
    private DatabaseManager dbManager;
    private MainApplicationWindow mainWindow;

    public ConceptBreakdownToolUI(DatabaseManager dbManager) {
        this.dbManager = dbManager;  // Assign the passed dbManager to the field
        this.mainWindow = new MainApplicationWindow(this.dbManager, this); // Use the assigned dbManager
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
        // Create a new DatabaseManager instance
        DatabaseManager dbManager = new DatabaseManager();

        // Prompt the user for the database path
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the path to the database file:");
        String dbPath = scanner.nextLine();

        // Check if the input path is not empty
        if (dbPath.isEmpty()) {
            System.out.println("No path entered. Exiting the application.");
            return;
        }

        // Set the database file path in DatabaseManager
        dbManager.setDataFilePath(dbPath);

        // Attempt to establish a connection to the database
        if (dbManager.connect() == null) {
            System.out.println("Failed to connect to the database using the provided path. Exiting the application.");
            return;
        }

        // Continue with the rest of your application logic
        // For example, creating the main application window
        SwingUtilities.invokeLater(() -> {
            // Initialize the main frame of your application
            ConceptBreakdownToolUI frame = new ConceptBreakdownToolUI(dbManager);
            frame.setVisible(true); // Make the frame visible
        });
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
                "Category: 0;Tutorial\n" +
                        "Concept: 1;Adding Categories;Tutorial;To add a new Category, select 'Add' and choose 'Category'. Enter a unique ID and the category name.\n" +
                        "Component: Step-by-Step Process;Choose 'Add' and select 'Category'. Provide an ID and name.\n" +
                        "Component: Example Entry;Category: 1, UX Design Skills\n" +
                        "Concept: 2;Adding Concepts;Tutorial;Concepts are ideas or topics within a Category. To add, select 'Add', then 'Concept'. Specify if the Concept belongs to an existing Category by entering the Category Name. Provide an ID, Topic, and Details.\n" +
                        "Component: Detailed Instructions;Select 'Add', choose 'Concept', and fill in the required information.\n" +
                        "Component: Example Entry;Concept: 101, Wireframing, UX Design Skills, Basics of wireframing\n" +
                        "Concept: 3;Adding Components;Tutorial;Components are elements that make up a Concept. To add a Component, choose 'Add' and select 'Component'. You will need to specify the Concept ID it belongs to and provide details.\n" +
                        "Component: Step-by-Step Guide;Choose 'Add', then 'Component'. Link to a Concept ID and provide the details.\n" +
                        "Component: Example Entry;Component: Organizing Systems, How to organize content effectively.\n";
    }


    public void setMainWindow(MainApplicationWindow mainWindow) {
        this.mainWindow = mainWindow;
    }
}
