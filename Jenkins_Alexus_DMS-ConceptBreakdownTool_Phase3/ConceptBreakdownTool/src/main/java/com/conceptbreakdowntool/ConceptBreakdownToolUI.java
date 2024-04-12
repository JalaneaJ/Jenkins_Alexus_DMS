/**
 Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Apr 11th 2024
 ClassName: ConceptBreakdownToolUI

 Purpose: A GUI for the Concept Breakdown Tool Application that is designed to manage and organize categories, concepts, and components in a structured database.

- Initialization Methods: Responsible for setting up the initial stage of the application.
    *Constructor(ConceptBreakdownToolUI): Initializes the main UI frame; Sets up the database manager and application window; Configures the window properties.
    *initComponents(): Sets up the components of the UI (Buttons, Labels, Panels).
- UI Action Methods: Used to handle user interactions through UI, responding to button clicks and other actions.
    * showInstructions(): Displays the instructions to the user.
    * createFile(): Handles the creation of a new database file (.db).
- Main Method: Main entry point of the application, handles initial setup before the UI is displayed.
    * main(): Starts the application; Sets up the database connection; Launches the main UI window; Prompts the user for the database file path; Handles database connection initialization.
- UI and File Handling: Handles direct interactions with the filesystem and user interface controls.
    * initComponents(): Sets up the components of the UI (Buttons, Labels, Panels).
            --instructionsButton(): Displays the application's instructions.
            --createFileButton(): Initiate the database file creation process.
            --loadFileButton(): Opens a file chooser to load an existing database.

 @author Alexus Jenkins
 @version 5.0
 **/

package com.conceptbreakdowntool;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 Initialization Methods: Responsible for setting up the initial stage of the application.
 **/
public class ConceptBreakdownToolUI extends JFrame {
    private DatabaseManager dbManager;
    private MainApplicationWindow mainWindow;
    /**
    Constructor(ConceptBreakdownToolUI): Initializes the main UI frame; Sets up the database manager and application window; Configures the window properties.
     * @param dbManager The database manager instance for database operations.
    **/
    public ConceptBreakdownToolUI(DatabaseManager dbManager) {
        this.dbManager = dbManager;  // Assign the passed dbManager to the field
        this.mainWindow = new MainApplicationWindow(this.dbManager, this); // Use the assigned dbManager
        setTitle("Concept Breakdown Tool");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        initComponents();
    }
    /**
     initComponents(): Sets up the components of the UI (Buttons, Labels, Panels).
     **/
    private void initComponents() {
        int gap = 25;
        int startY = (getHeight() - 3 * gap - 100) / 2;

        JLabel heading = new JLabel("Concept Breakdown Tool", SwingConstants.CENTER);
        heading.setFont(new Font("Serif", Font.BOLD, 24));
        heading.setBounds((getWidth() - 300) / 2, startY, 300, 30);

        JTextField inputField = new JTextField();
        inputField.setBounds((getWidth() - 300) / 2, startY + 30 + gap, 300, 15);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setBounds(0, startY + 60 + 2 * gap, getWidth(), 35);
        buttonsPanel.setOpaque(false);

        /**
         createFileButton(): Initiate the database file creation process.
         **/
        JButton createFileButton = new JButton("Create File");
        createFileButton.addActionListener(e -> createFile());

        /**
         instructionsButton(): Displays the application's instructions.
         **/
        JButton instructionsButton = new JButton("Instructions");
        instructionsButton.addActionListener(e -> showInstructions());

        /**
         loadFileButton(): Opens a file chooser to load an existing database.
         **/
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

    /**
     Main Method: Main entry point of the application, handles initial setup before the UI is displayed.
     **/
    /**
     * main(): Starts the application; Sets up the database connection; Launches the main UI window; Prompts the user for the database file path; Handles database connection initialization.
     * @param args The command line arguments.
     * **/
    public static void main(String[] args) {
        // Create a new DatabaseManager instance
        DatabaseManager dbManager = new DatabaseManager();

        // Prompt the user for the database path
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the path to the database file (please make sure it is within the file directory):");
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



    /**
     UI Action Methods: Used to handle user interactions through UI, responding to button clicks and other actions.
     **/

    /**
     showInstructions(): Displays the instructions to the user.
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
     createFile(): Handles the creation of a new database file (.db).
     **/
    private void createFile() {
        String fileName = JOptionPane.showInputDialog(this, "Enter a name for the .db file:", "Create Database File", JOptionPane.PLAIN_MESSAGE);
        if (fileName == null || fileName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No file name entered. Operation canceled.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // User cancelled or entered an empty name
        }
        if (!fileName.endsWith(".db")) {
            fileName += ".db"; // Ensure the file has a .db extension
        }

        File databaseFile = new File(fileName);
        if (databaseFile.exists()) {
            JOptionPane.showMessageDialog(this, "Database file already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (databaseFile.createNewFile()) {
                JOptionPane.showMessageDialog(this, "Database file created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
                     Statement stmt = conn.createStatement()) {
                    // SQL for creating tables
                    String sqlCreateCategory = "CREATE TABLE Category (Category_ID INTEGER PRIMARY KEY, Category_Topic TEXT NOT NULL);";
                    String sqlCreateConcept = "CREATE TABLE Concept (Concept_ID INTEGER PRIMARY KEY, Concept_Topic TEXT NOT NULL, Category_ID INTEGER NOT NULL, Concept_Details TEXT NOT NULL, FOREIGN KEY(Category_ID) REFERENCES Category(Category_ID));";
                    String sqlCreateComponent = "CREATE TABLE Component (Component_Topic TEXT NOT NULL UNIQUE, Component_Description TEXT NOT NULL, Concept_ID INTEGER NOT NULL, FOREIGN KEY(Concept_ID) REFERENCES Concept(Concept_ID));";

                    stmt.execute(sqlCreateCategory);
                    stmt.execute(sqlCreateConcept);
                    stmt.execute(sqlCreateComponent);
                    JOptionPane.showMessageDialog(this, "Tables created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to create the database file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** setMainWindow(): Sets the transition to the Main Application Window class.
     * @param mainWindow: transitions to 'MainApplicationWindow' frame
     * **/
    public void setMainWindow(MainApplicationWindow mainWindow) {
        this.mainWindow = mainWindow;
    }
}
