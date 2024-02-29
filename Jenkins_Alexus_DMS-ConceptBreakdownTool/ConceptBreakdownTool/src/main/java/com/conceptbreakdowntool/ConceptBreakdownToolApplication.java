package com.conceptbreakdowntool;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;


/*
Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Feb 12th, 2024
 ClassName: ConceptBreakdownToolApplication

 Purpose: Comprehensive User interview and Command-Line interactions for managing concepts, components, and categories.

 Attributes:
 String[] DIAGRAM_TYPES: A static array containing different types of diagrams that can be recommended to the user for visualizing concepts.

 Methods:
 main(): Main method that initiates the application, handling user input for managing concept, components, and categories, and providing options such as adding, updating, and removing. Printing the database, recommending diagrams, and displaying concepts in a table format.
 recommendDiagram(): Randomly selects and recommends a diagram type from the 'DIAGRAM_TYPES' array to the user for visualizing concepts.
 displayAsTable(): Displays the concepts organized by categories in a tabular format, including the details of components associated with each concept.
 addObjects(): User can add a new concept, component, or category to the database.
 updateObjects(): User can update a concept, component, or category in the database.
 removeObjects(): User can remove a concept, component, or category in the database.
 loadFromFile(): Load concepts, components, and categories from a file into the database.
 printDatabase(): Prints the entire database contents, including categories, concepts, and components, into the console.

 //Getters and Setters: Provides access and allows modification of the concepts and attributes.
 getId(), setID()
 getTopic(), setTopic()
 getCategory(), setCategory()
 getComponents(), setComponents()
 getDetails(), setDetails()

 //addComponent(): Adds a 'Component' to the concept's list of components.
 */


public class ConceptBreakdownToolApplication {
    public static final String[] DIAGRAM_TYPES = {
            "Flowchart",
            "Venn Diagram",
            "UML Diagram",
            "Mind Map",
            "Fishbone Diagram",
            "Concept Map",
            "Affinity Diagram",
            "Relationship Diagram"
    };
    private DatabaseManager dbManager;

    //main(): Main method that initiates the application, handling user input for managing concept, components, and categories, and providing options such as adding, updating, and removing. Printing the database, recommending diagrams, and displaying concepts in a table format.
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        Scanner scanner = new Scanner(System.in);

        // Loads data from the file
        System.out.println("Welcome to the Concept Breakdown Tool.");
        System.out.print("Please enter the filename with its path to load data: ");
        String filename = scanner.nextLine().trim();
        File file = new File(filename);

        if (file.exists() && !file.isDirectory()) {
            System.out.println("Starting file load.");
            loadFromFile(dbManager, filename);
            System.out.println("File load completed.");
            displayAsTable(dbManager); // Display data as a table after loading from file
            System.out.println("Type 'help' to see available commands or start entering commands.");
        } else {
            System.err.println("File not found or it is a directory: " + file.getAbsolutePath());
            scanner.close();
            return; // Exit if file not found to prevent further execution
        }

        // User command loop
        // Just before the while loop starts
        System.out.println("\nEntering command loop. Please enter a command:");


        while (true) {
            System.out.println("Available Commands: [add, update, remove, print, recommend, help, exit]");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "add":
                        addObjects(scanner, dbManager);
                        break;
                    case "update":
                        updateObjects(scanner, dbManager);
                        break;
                    case "remove":
                        removeObjects(scanner, dbManager);
                        break;
                    case "print":
                        displayAsTable(dbManager);
                        break;
                    case "recommend":
                        recommendDiagram();
                        break;
                    case "help":
                        printInstructions();
                        break;
                    case "exit":
                        System.out.println("Exiting application.");
                        return; // Exit the application
                    default:
                        System.out.println("Unknown command. Please try again.");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter valid numeric values.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    //recommendDiagram(): Randomly selects and recommends a diagram type from the 'DIAGRAM_TYPES' array to the user for visualizing concepts.
    public static String recommendDiagram() {
        Random random = new Random();
        int index = random.nextInt(DIAGRAM_TYPES.length);
        return DIAGRAM_TYPES[index]; // Instead of printing, return the type for testing
    }



    //displayAsTable(): Displays the concepts organized by categories in a tabular format, including the details of components associated with each concept.
    private static void displayAsTable(DatabaseManager dbManager) {
        System.out.println("\nDatabase Contents:");
        for (Category category : dbManager.getCategories()) {
            System.out.println("\nCategory Name: " + category.getTopic());
            System.out.println("Category ID: " + category.getId());
            for (Concept concept : dbManager.getConcepts()) {
                if (concept.getCategory().equals(category.getTopic())) {
                    System.out.println("Concept ID: " + concept.getId());
                    System.out.println("Concept Name and Details: " + concept.getTopic() + " | " + concept.getDetails());
                    if (!concept.getComponents().isEmpty()) {
                        System.out.println("+------------------------+--------------------------------------------+");
                        System.out.println("| COMPONENTS             | DESCRIPTION                                |");
                        System.out.println("+------------------------+--------------------------------------------+");
                        for (Component component : concept.getComponents()) {
                            printFormattedComponentRow(component.getTopic(), component.getDetails());
                        }
                        // Prints the bottom border after listing all components
                        System.out.println("+------------------------+--------------------------------------------+");
                    } else {
                        System.out.println("No components available for this concept.");
                    }
                }
            }
        }
    }


    private static void printFormattedComponentRow(String topic, String details) {
        final int topicWidth = 22; // Adjusted for COMPONENTS column width
        final int detailWidth = 42; // Adjusted for DESCRIPTION column width
        List<String> detailLines = splitIntoLines(details, detailWidth);

        // Print component rows without the bottom border
        for (int i = 0; i < detailLines.size(); i++) {
            if (i == 0) {
                // Print the component topic and the first line of details
                System.out.printf("| %-"+topicWidth+"s | %-"+detailWidth+"s |\n", topic, detailLines.get(i));
            } else {
                // Print subsequent lines of details with blank topic space
                System.out.printf("| %"+topicWidth+"s | %-"+detailWidth+"s |\n", "", detailLines.get(i));
            }
        }
    }

    private static List<String> splitIntoLines(String text, int width) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() + 1 > width) {
                lines.add(line.toString());
                line = new StringBuilder();
            }
            if (line.length() > 0) {
                line.append(" ");
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    //addObjects(): User can add a new concept, component, or category to the database.
    public static void addObjects(Scanner scanner, DatabaseManager dbManager) {
        try {
            System.out.println("What would you like to add? (Concept/Component/Category): ");
            String type = scanner.nextLine().trim().toLowerCase();

            switch (type) {
                case "concept":
                    // Ask if the concept has a category
                    System.out.println("Does this concept have a category? (yes/no): ");
                    String hasCategory = scanner.nextLine().trim().toLowerCase();
                    if ("yes".equals(hasCategory)) {
                        // Ask for the Category Name
                        System.out.println("\nEnter the Category Name: ");
                        String categoryName = scanner.nextLine().trim();
                        if (dbManager.findCategoryByName(categoryName) == null) {
                            System.out.println("Category does not exist. Please create the category first.");
                            //Redirects to create a category or handle it differently
                        } else {
                            int conceptId = safelyParseInt(scanner, "\nEnter Concept ID: ");
                            System.out.println("\nEnter Concept Topic: ");
                            String conceptTopic = scanner.nextLine().trim();
                            System.out.println("\nEnter Concept Details: ");
                            String conceptDetails = scanner.nextLine().trim();
                            // Adding the concept with the verified category name
                            dbManager.addConcept(new Concept(conceptId, conceptTopic, categoryName, conceptDetails));
                            System.out.println("Concept added.");
                        }
                    } else if ("no".equals(hasCategory)) {
                        System.out.println("Please create a category first.");
                        // Redirects to create a category or handle it differently
                    } else {
                        System.out.println("Invalid response. Please answer with 'yes' or 'no'.");
                    }
                    break;
                case "component":
                    System.out.println("Enter Concept ID to which this component belongs:");
                    int conceptId = safelyParseInt(scanner, "Enter Concept ID: ");
                    Concept concept = dbManager.getConcept(conceptId);
                    if (concept != null) {
                        System.out.println("\nEnter Component Topic: ");
                        String componentTopic = scanner.nextLine().trim();
                        System.out.println("\nEnter Component Details: ");
                        String componentDetails = scanner.nextLine().trim();
                        dbManager.addComponentToConcept(conceptId, new Component(componentTopic, componentDetails));
                        System.out.println("Component '" + componentTopic + "' added successfully to Concept ID: " + conceptId);
                    } else {
                        System.out.println("Concept ID does not exist. Please ensure the concept ID is correct.");
                    }
                    break;
                case "category":
                    // Removed the extra println here for Category ID
                    int categoryId = safelyParseInt(scanner, "\nEnter Category ID: ");
                    System.out.println("\nEnter Category Topic: "); // Keep this line as is
                    String categoryTopic = scanner.nextLine().trim();
                    // Check if category already exists
                    if (dbManager.findCategoryByName(categoryTopic) != null) {
                        System.out.println("Category already exists. Please enter a different name.");
                    } else {
                        dbManager.addCategory(new Category(categoryId, categoryTopic));
                        System.out.println("Category '" + categoryTopic + "' added successfully.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid type provided. Please choose 'concept', 'component', or 'category'.");
            }
            dbManager.saveAllData(); //Saves changes to the database
            System.out.println("Data saved successfully.");

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }


    //updateObjects(): User can update a concept, component, or category in the database.
    public static void updateObjects(Scanner scanner, DatabaseManager dbManager) {
        System.out.println("What would you like to update? (Concept/Component/Category): ");
        String type = scanner.nextLine().trim().toLowerCase();
        try {
            switch (type) {
                case "concept":
                    int conceptId = safelyParseInt(scanner, "Enter Concept ID for update: ");

                    // Check if the concept exists first before asking for new topic and details
                    Concept existingConcept = dbManager.getConcept(conceptId); // Assuming getConcept(int id) method exists and returns null if not found
                    if (existingConcept == null) {
                        System.out.println("Concept with ID " + conceptId + " not found.");
                        break; // Exit the case if the concept doesn't exist
                    }

                    System.out.println("\nEnter new Topic: ");
                    String newConceptTopic = scanner.nextLine().trim();
                    System.out.println("\nEnter new Details: ");
                    String newConceptDetails = scanner.nextLine().trim();

                    // Now you're sure the concept exists, proceed with the update
                    if (dbManager.updateConcept(conceptId, newConceptTopic, newConceptDetails)) {
                        System.out.println("Concept updated.");
                    } else {
                        // This else part might not be necessary anymore if getConcept ensures existence before this point
                        System.out.println("Unexpected error: Concept update failed.");
                    }
                    break;

                case "component":
                    conceptId = safelyParseInt(scanner, "Enter Concept ID to which this component belongs:");
                    System.out.println("\nEnter Component Topic to update:");
                    String oldComponentTopic = scanner.nextLine().trim();
                    System.out.println("\nEnter new Component Topic:"); // Assuming you need this based on your method signature.
                    String newComponentTopic = scanner.nextLine().trim();
                    System.out.println("\nEnter new Component Details:");
                    String newComponentDetails = scanner.nextLine().trim();
                    if (dbManager.updateComponent(oldComponentTopic, newComponentTopic, newComponentDetails)) {
                        System.out.println("Component details updated successfully.");
                    } else {
                        System.out.println("Concept or Component not found or update failed.");
                    }
                    break;
                case "category":
                    int categoryId = safelyParseInt(scanner, "Enter Category ID for update: ");
                    System.out.println("Enter New Category Topic: ");
                    String newCategoryTopic = scanner.nextLine().trim();
                    if (dbManager.updateCategory(categoryId, newCategoryTopic)) {
                        System.out.println("Category updated.");
                    } else {
                        System.out.println("Category not found or update failed.");
                    }
                    break;
                default:
                    System.out.println("Invalid type. Please choose 'concept', 'component', or 'category'.");
                    break;
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        dbManager.saveAllData();
        System.out.println("Data saved successfully.");
    }


    //removeObjects(): User can remove a concept, component, or category in the database.
    public static void removeObjects(Scanner scanner, DatabaseManager dbManager) {
        System.out.println("What would you like to remove? (Concept/Component/Category): ");
        String type = scanner.nextLine().trim().toLowerCase();

        try {
            switch (type) {
                case "concept":
                    System.out.println("Enter Concept ID to remove: ");
                    int conceptId = safelyParseInt(scanner, "Enter a valid Concept ID: "); // Use safelyParseInt for input validation
                    if (dbManager.deleteConcept(conceptId)) {
                        System.out.println("Concept removed.");
                    } else {
                        System.out.println("Concept could not be found or removed.");
                    }
                    break;
                case "component":
                    System.out.println("Enter Component Topic to remove: ");
                    String componentTopic = scanner.nextLine().trim();
                    if (dbManager.deleteComponent(componentTopic)) {
                        System.out.println("Component removed.");
                    } else {
                        System.out.println("Component could not be found or removed.");
                    }
                    break;
                case "category":
                    System.out.println("Enter Category ID to remove: ");
                    int categoryId = safelyParseInt(scanner, "Enter a valid Category ID: "); // Correct prompt for ID input
                    if (dbManager.deleteCategory(categoryId)) {
                        System.out.println("Category removed.");
                    } else {
                        System.out.println("Category could not be found or removed.");
                    }
                    break;
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        dbManager.saveAllData();
        System.out.println("Data saved successfully.");
    }


    //loadFromFile(): Load concepts, components, and categories from a file into the database.
    private static void loadFromFile(DatabaseManager dbManager, String filename) {
        try {
            dbManager.loadDataFromFile(filename); // Assuming you moved the logic here
            System.out.println("Data loaded successfully from " + filename);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    //printDatabase(): Prints the entire database contents, including categories, concepts, and components, into the console.
    private static void printDatabase (DatabaseManager dbManager){
        System.out.println("##Database Contents:");

        for (Category category : dbManager.getCategories()) {
            System.out.println(category.getTopic());

            // Filter concepts by category
            List<Concept> conceptsInCategory = dbManager.getConcepts().stream()
                    .filter(concept -> concept.getCategory().equals(category.getTopic()))
                    .collect(Collectors.toList());

            for (Concept concept : conceptsInCategory) {
                System.out.println("- " + concept.getTopic());

                // Directly use the getComponents method of Concept
                for (Component component : concept.getComponents()) {
                    System.out.println("   - " + component.getTopic());
                }
            }
        }
    }

    private static void printInstructions() {
        System.out.println("\nUsage Instructions:");
        System.out.println("  add - Start the process to add a category, concept, or component.");
        System.out.println("  list - Display all concepts and their components in a table format.");
        System.out.println("  recommend - Get a random recommendation for a diagram type.");
        System.out.println("  load - Load data from a specified file. (Note: This should be done at the start of the application)");
        System.out.println("  print - Display the entire database of categories, concepts, and components in a table format.");
        System.out.println("  update - Update existing categories, concepts, and components.");
        System.out.println("  remove - Remove existing categories, concepts, and components.");
        System.out.println("  help - Show this list of commands.");
        System.out.println("  exit - Exit the application.");
        System.out.println("\nFollow the prompts after each command for further instructions.");
    }

    private static int safelyParseInt(Scanner scanner, String prompt) {
        int number;
        do {
            System.out.print(prompt);
            while (!scanner.hasNextInt()) {
                System.out.println("That's not a number. Please enter a number.");
                scanner.next(); // Move scanner past the current input
            }
            number = scanner.nextInt();
        } while (number <= 0);
        scanner.nextLine(); // Consume newline left-over
        return number;
    }

    public void setDbManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public DatabaseManager getDbManager() {
        return dbManager;
    }
}