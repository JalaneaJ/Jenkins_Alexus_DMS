package com.conceptbreakdowntool;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String[] DIAGRAM_TYPES = {
            "Flowchart",
            "Venn Diagram",
            "UML Diagram",
            "Mind Map",
            "Venn Diagram",
            "Fishbone Diagram",
            "Concept Map",
            "Affinity Diagram",
            "Relationship Diagram"
    };

    //main(): Main method that initiates the application, handling user input for managing concept, components, and categories, and providing options such as adding, updating, and removing. Printing the database, recommending diagrams, and displaying concepts in a table format.
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        Scanner scanner = new Scanner(System.in);

        // To load data and print database
        System.out.println("Please enter the filename with its path:");
        String filename = scanner.nextLine();
        File file = new File(filename); // Correctly initialize 'file' here

        // Load from file and print database if file exists
        if (file.exists() && !file.isDirectory()) {
            loadFromFile(dbManager, filename);
            printDatabase(dbManager);
        } else {
            System.err.println("File not found or it is a directory: " + file.getAbsolutePath());
            return; // Exit if file not found to avoid repeating the load and print operations
        }

        // Menu for add, update, or remove options
        boolean running = true;
        while (running) {
            System.out.println("\n##Choose an action:");
            System.out.println("1: Add Concept/Component/Category");
            System.out.println("2: Update Concept/Component/Category");
            System.out.println("3: Remove Concept/Component/Category");
            System.out.println("4: Print Database");
            System.out.println("5: Recommend a Diagram");
            System.out.println("6: Display Concepts as Table");
            System.out.println("7: Exit \n");
            System.out.print("##Enter option number: ");
            int option = Integer.parseInt(scanner.nextLine().trim());

            switch (option) {
                case 1:
                    addObjects(scanner, dbManager);
                    break;
                case 2:
                    updateObjects(scanner, dbManager);
                    break;
                case 3:
                    removeObjects(scanner, dbManager);
                    break;
                case 4:
                    printDatabase(dbManager);
                    break;
                case 5:
                    recommendDiagram();
                    break;
                case 6:
                    displayAsTable(dbManager);
                    break;
                case 7:
                    running = false;
                    System.out.println("Exiting program.");
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 7.");
            }
        }
        scanner.close();
    }

    //recommendDiagram(): Randomly selects and recommends a diagram type from the 'DIAGRAM_TYPES' array to the user for visualizing concepts.
    private static void recommendDiagram() {
        Random random = new Random();
        int index = random.nextInt(DIAGRAM_TYPES.length);
        System.out.println("Recommended Diagram for Visualization: " + DIAGRAM_TYPES[index]);
    }

    //displayAsTable(): Displays the concepts organized by categories in a tabular format, including the details of components associated with each concept.
    private static void displayAsTable(DatabaseManager dbManager) {
        // Iterate through each category
        for (Category category : dbManager.getCategories()) {
            System.out.println("\nCategory Name: " + category.getTopic());

            // Filter concepts by category
            List<Concept> conceptsInCategory = dbManager.getConcepts().stream()
                    .filter(concept -> concept.getCategory().equals(category.getTopic()))
                    .collect(Collectors.toList());

            for (Concept concept : conceptsInCategory) {
                System.out.println("Concept ID: " + concept.getId());

                // Check if concept has components to display
                if (!concept.getComponents().isEmpty()) {
                    System.out.println("+------------------------+--------------------------------------------+");
                    System.out.println("| COMPONENTS             | DESCRIPTION                                |");
                    System.out.println("+------------------------+--------------------------------------------+");

                    for (Component component : concept.getComponents()) {
                        System.out.printf("| %-22s | %-42s |\n", component.getTopic(), component.getDetails());
                        System.out.println("+------------------------+--------------------------------------------+");
                    }
                } else {
                    System.out.println("No components available for this concept.");
                }
            }
        }
    }

    //addObjects(): User can add a new concept, component, or category to the database.
    private static void addObjects (Scanner scanner, DatabaseManager dbManager){
        System.out.println("What would you like to add? (Concept/Component/Category): ");
        String type = scanner.nextLine().trim().toLowerCase();

        switch (type) {
            case "concept":
                System.out.println("Enter Concept ID: ");
                int conceptId = Integer.parseInt(scanner.nextLine().trim());
                System.out.println("Enter Concept Topic: ");
                String conceptTopic = scanner.nextLine().trim();
                System.out.println("Enter Concept Category: ");
                String conceptCategory = scanner.nextLine().trim();
                System.out.println("Enter Concept Details: ");
                String conceptDetails = scanner.nextLine().trim();
                dbManager.addConcept(new Concept(conceptId, conceptTopic, conceptCategory, conceptDetails));
                System.out.println("Concept added.");
                break;
            case "component":
                System.out.println("Enter Component Topic: ");
                String componentTopic = scanner.nextLine().trim();
                System.out.println("Enter Component Details: ");
                String componentDetails = scanner.nextLine().trim();
                dbManager.addComponent(new Component(componentTopic, componentDetails));
                System.out.println("Component added.");
                break;
            case "category":
                System.out.println("Enter Category ID: ");
                int categoryId = Integer.parseInt(scanner.nextLine().trim());
                System.out.println("Enter Category Topic: ");
                String categoryTopic = scanner.nextLine().trim();
                dbManager.addCategory(new Category(categoryId, categoryTopic));
                System.out.println("Category added.");
                break;
            default:
                System.out.println("Invalid type.");
                break;
        }
    }

    //updateObjects(): User can update a concept, component, or category in the database.
    private static void updateObjects (Scanner scanner, DatabaseManager dbManager){
        System.out.println("What would you like to update? (Concept/Component/Category): ");
        String type = scanner.nextLine().trim().toLowerCase();

        switch (type) {
            case "concept":
                System.out.println("Enter Concept ID for update: ");
                int conceptId = Integer.parseInt(scanner.nextLine().trim());
                System.out.println("Enter new Topic: ");
                String newConceptTopic = scanner.nextLine().trim();
                System.out.println("Enter new Category: ");
                String newConceptCategory = scanner.nextLine().trim();
                System.out.println("Enter new Details: ");
                String newConceptDetails = scanner.nextLine().trim();
                dbManager.updateConcept(conceptId, newConceptTopic, newConceptCategory, newConceptDetails);
                System.out.println("Concept updated.");
                break;
            case "component":
                System.out.println("Enter old Component Topic for update: ");
                String oldTopic = scanner.nextLine().trim();
                System.out.println("Enter new Topic: ");
                String newComponentTopic = scanner.nextLine().trim();
                System.out.println("Enter new Details: ");
                String newComponentDetails = scanner.nextLine().trim();
                dbManager.updateComponent(oldTopic, newComponentTopic, newComponentDetails);
                System.out.println("Component updated.");
                break;
            case "category":
                System.out.println("Enter Category ID for update: ");
                int categoryId = Integer.parseInt(scanner.nextLine().trim());
                System.out.println("Enter new Topic: ");
                String newCategoryTopic = scanner.nextLine().trim();
                dbManager.updateCategory(categoryId, newCategoryTopic);
                System.out.println("Category updated.");
                break;
            default:
                System.out.println("Invalid type.");
                break;
        }
    }

    //removeObjects(): User can remove a concept, component, or category in the database.
    private static void removeObjects (Scanner scanner, DatabaseManager dbManager){
        System.out.println("What would you like to remove? (Concept/Component/Category): ");
        String type = scanner.nextLine().trim().toLowerCase();

        switch (type) {
            case "concept":
                System.out.println("Enter Concept ID to remove: ");
                int conceptId = Integer.parseInt(scanner.nextLine().trim());
                dbManager.deleteConcept(conceptId);
                System.out.println("Concept removed.");
                break;
            case "component":
                System.out.println("Enter Component Topic to remove: ");
                String componentTopic = scanner.nextLine().trim();
                dbManager.deleteComponent(componentTopic);
                System.out.println("Component removed.");
                break;
            case "category":
                System.out.println("Enter Category ID to remove: ");
                int categoryId = Integer.parseInt(scanner.nextLine().trim());
                dbManager.deleteCategory(categoryId);
                System.out.println("Category removed.");
                break;
            default:
                System.out.println("Invalid type.");
                break;
        }
    }

    //loadFromFile(): Load concepts, components, and categories from a file into the database.
    private static void loadFromFile(DatabaseManager dbManager, String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            Concept lastConcept = null; // Keep track of the last loaded concept

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(":", 2);
                if (parts.length < 2) {
                    System.err.println("Invalid line format: " + line);
                    continue;
                }
                String type = parts[0].trim();
                String[] properties = parts[1].trim().split(",", -1);

                switch (type) {
                    case "Category":
                        if (properties.length < 2) {
                            System.err.println("Invalid category format: " + line);
                            break;
                        }
                        int catId = Integer.parseInt(properties[0].trim());
                        String catName = properties[1].trim();
                        dbManager.addCategory(new Category(catId, catName));
                        break;
                    case "Concept":
                        if (properties.length < 4) {
                            System.err.println("Invalid concept format: " + line);
                            break;
                        }
                        int conId = Integer.parseInt(properties[0].trim());
                        String conName = properties[1].trim();
                        String conCategory = properties[2].trim();
                        String conDetails = properties[3].trim();
                        lastConcept = new Concept(conId, conName, conCategory, conDetails);
                        dbManager.addConcept(lastConcept);
                        break;
                    case "Component":
                        if (properties.length < 2 || lastConcept == null) {
                            System.err.println("Invalid component format or no concept defined: " + line);
                            break;
                        }
                        String compName = properties[0].trim();
                        String compDetails = properties[1].trim();
                        lastConcept.addComponent(new Component(compName, compDetails));
                        break;
                    default:
                        System.err.println("Unknown type in line: " + line);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric value: " + e.getMessage());
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
}
