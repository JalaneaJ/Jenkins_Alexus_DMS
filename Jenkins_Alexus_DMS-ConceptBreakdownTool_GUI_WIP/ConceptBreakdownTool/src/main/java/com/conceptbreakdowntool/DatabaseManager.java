package com.conceptbreakdowntool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


/*
 Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Feb 12th, 2024
 ClassName: DatabaseManager

 Purpose: This class simulates a database manager for the Concept Breakdown Tool application.
 It handles CRUD operations for concepts, components, and categories using in-memory storage,
 with capabilities to load and save data to and from a JSON file, ensuring persistence across sessions.

 Attributes:
 List<Concept> concepts: Stores all the 'Concept' objects.
 List<Component> components: Stores all the 'Component' objects.
 List<Category> categories: Stores all the 'Category' objects.

 Methods:
 //Constructor
 DatabaseManager(): Initializes the lists for storing concepts, components, and categories.

 //CRUD Operations
 addConcept(): Adds a new Concept.
 addComponent(): Adds a new Component.
 addCategory(): Adds a new Category.
 updateConcept(): Updates an existing Concept.
 updateComponent(): Updates an existing Component.
 updateCategory(): Updates an existing Category.
 deleteConcept(): Deletes a Concept by ID.
 deleteComponent(): Deletes a Component by topic.
 deleteCategory(): Deletes a Category by ID.

 //Data Persistence
 saveAllData(): Saves the current state to a JSON file.
 loadAllData(): Loads data from a JSON file.

 //Retrieval Methods
 getConcepts(): Retrieves all Concepts.
 getComponents(): Retrieves all Components.
 getCategories(): Retrieves all Categories.

 //Utility Methods
 addComponentToConcept(): Adds a Component to a specific Concept.
 addComponentToLastConcept(): Adds a Component to the last added Concept.
 addObject(): Adding an object.
 updateObject(): Updating an object.
 removeObject(): Removing an object.

 */

public class DatabaseManager {
    //Attributes
    private List<Concept> concepts;
    private List<Component> components;
    private List<Category> categories;
    private String dataFilePath; // New variable to store the path of the data file
    private Connection dbConnection;


    // CONSTRUCTOR
    // DatabaseManager(): Initializes the lists for storing concepts, components, and categories.
    public DatabaseManager() {
        this.concepts = new ArrayList<>();
        this.components = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    //CRUD OPERATIONS
    /* addConcept(): Adds a new Concept to the database.
        Arguments:
            - concept: The Concept object to be added.
        Return value: None
     */
    public void addConcept(Concept concept) {
        if (findCategoryByName(concept.getCategory()) != null) { // Check if the category of the concept exists
            concepts.add(concept); // Add the concept if its category exists
        } else {
            System.err.println("Category not found for concept: " + concept.getTopic());
        }
    }

    /*
     * addComponent(): Adds a new Component to the database.
        Arguments:
            - component: The Component object to be added.
        Return value: None
     */
    public void addComponent(Component component) {
        this.components.add(component);
        System.out.println("Component '" + component.getTopic() + "' added successfully.");
    }


    /* addCategory(): Adds a new Category to the database.
        Arguments:
            - category: The Category object to be added.
        Return value: None
     */
    public void addCategory(Category category) {
        if (findCategoryByName(category.getTopic()) == null) { // Check if the category already exists
            categories.add(category); // Add the category if it doesn't exist
        } else {
            System.err.println("Category '" + category.getTopic() + "' already exists.");
        }
    }

    /* updateConcept(): Updates an existing Concept in the database.
     Arguments:
        - conceptId
        - newTopic
        - newCategory
        - newDetails
     Return value: None
     */
    public boolean updateConcept(int conceptId, String newTopic, String newDetails) {
        System.out.println("Attempting to update Concept ID: " + conceptId); // Debug statement
        for (Concept concept : concepts) {
            System.out.println("Checking Concept ID: " + concept.getId()); // Debug statement
            if (concept.getId() == conceptId) {
                concept.setTopic(newTopic);
                concept.setDetails(newDetails);
                System.out.println("Update successful for Concept ID: " + conceptId); // Debug statement
                return true;
            }
        }
        System.out.println("Concept with ID " + conceptId + " not found."); // Debug statement
        return false;
    }



    /* updateComponent(): Updates an existing Component in the database.
     Arguments:
        - oldTopic
        - newTopic
        - newDetails
     Return value: None
     */
    public boolean updateComponent(String oldTopic, String newTopic, String newDetails) {
        for (Component component : components) {
            if (component.getTopic().equals(oldTopic)) {
                component.setTopic(newTopic);
                component.setDetails(newDetails);
                return false;
            }
        }
        System.out.println("Component with topic '" + oldTopic + "' not found.");
        return false;
    }

    /* updateCategory(): Updates an existing Category in the database.
     Arguments:
        - categoryId
        - newTopic
     Return value: None
     */
    // When updating a category that does not exist
    public boolean updateCategory(int categoryId, String newTopic) {
        for (Category category : categories) {
            if (category.getId() == categoryId) {
                category.setTopic(newTopic);
                return true;
            }
        }
        return false;
    }

    //findCategoryById()
    // Method to find a category by its ID
    private Category findCategoryById(int categoryId) {
        for (Category category : categories) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null; // Return null if no matching category is found
    }


    /* deleteConcept(): Deletes an existing Concept from the database based on its ID.
     Arguments:
        - conceptId: The ID of the Concept to be deleted.
     Return value: None
     */
    public boolean deleteConcept(int conceptId) {
        concepts.removeIf(concept -> concept.getId() == conceptId);
        return false;
    }

    /* deleteComponent(): Deletes an existing Component from the database based on its topic.
     Arguments:
        - componentTopic: The topic of the Component to be deleted.
     Return value: None
     */
    public boolean deleteComponent(String componentTopic) {
        components.removeIf(component -> component.getTopic().equals(componentTopic));
        return false;
    }

    /* deleteCategory(): Deletes an existing Category from the database based on its ID.
     Arguments:
        - categoryId: The ID of the Category to be deleted.
     Return value: None
     */
    public boolean deleteCategory(int categoryId) {
        Iterator<Category> iterator = categories.iterator();
        while (iterator.hasNext()) {
            Category category = iterator.next();
            if (category.getId() == categoryId) {
                iterator.remove(); // Remove the category if the ID matches
                // Additionally, remove all concepts associated with this category
                concepts.removeIf(concept -> concept.getCategory().equals(category.getTopic()));
                return true; // Indicate success
            }
        }
        return false; // Indicate failure if category not found
    }


    /* deleteComponentByTopic(): Deletes a concept from the database based on its topic.
        Arguments:
          - topic: The Topic of the Concept
        Return value: "All concepts with the topic [topic name] have been removed."
     */
    public void deleteConceptByTopic(String topic) {
        concepts.removeIf(concept -> concept.getTopic().equalsIgnoreCase(topic));
        System.out.println("All concepts with topic '" + topic + "' have been removed.");
    }

    /* deleteComponentByDetails(): Deleted a Component from the database based on its details.
        Arguments:
         - details: The details of the Component
        Return Value: "All components with details [detail information] has been removed."
     */
    public void deleteComponentByDetails(String details) {
        components.removeIf(component -> component.getDetails().equalsIgnoreCase(details));
        System.out.println("All components with details '" + details + "' have been removed.");
    }

    /* deleteCategoryByTopic(): Deletes a Category from the database based on its topic
        Arguments:
         - topic: Topic of the Category
        Return Value: "All categories with topic [topic name] have been removed."
     */
    public void deleteCategoryByTopic(String topic) {
        categories.removeIf(category -> category.getTopic().equalsIgnoreCase(topic));
        System.out.println("All categories with topic '" + topic + "' have been removed.");
    }

    //DATA PERSISTENCE
    /*  saveAllData(): Saves the current state of concepts, components, and categories to a JSON file.
        Arguments: None
        Return value: None
     */
    public void saveAllData() {
        if (this.dataFilePath == null) {
            System.err.println("No file path specified for saving data.");
            return;
        }

        try (PrintWriter out = new PrintWriter(this.dataFilePath)) {
            // Write categories
            for (Category category : categories) {
                out.println("Category: " + category.getId() + "," + category.getTopic());
            }
            // Write concepts
            for (Concept concept : concepts) {
                out.println("Concept: " + concept.getId() + "," + concept.getTopic() + "," + concept.getCategory() + "," + concept.getDetails());
                // Write components of each concept
                for (Component component : concept.getComponents()) {
                    out.println("Component: " + component.getTopic() + "," + component.getDetails());
                }
            }
            out.flush();
        } catch (FileNotFoundException e) {
            System.err.println("Error saving data to file: " + e.getMessage());
        }
    }


    /*  loadAllData(): Loads concepts, components, and categories from a JSON file into the application.
        Arguments: None
        Return value: None
     */
    public void loadAllData() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data.json")) {
            Type dataType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> allData = gson.fromJson(reader, dataType);

            this.concepts = gson.fromJson(gson.toJson(allData.get("concepts")), new TypeToken<List<Concept>>(){}.getType());
            this.components = gson.fromJson(gson.toJson(allData.get("components")), new TypeToken<List<Component>>(){}.getType());
            this.categories = gson.fromJson(gson.toJson(allData.get("categories")), new TypeToken<List<Category>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //loadDataFromFile()
    public void loadDataFromFile(String filename) {
        File file = new File(filename);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("Category:")) {
                    String[] parts = line.substring("Category:".length()).split(",", 2);
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    addCategory(new Category(id, name));
                } else if (line.startsWith("Concept:")) {
                    String[] parts = line.substring("Concept:".length()).split(",", 4);
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String category = parts[2].trim();
                    String details = parts[3].trim();
                    addConcept(new Concept(id, name, category, details));
                } else if (line.startsWith("Component:")) {
                    String[] parts = line.substring("Component:".length()).split(",", 2);
                    String topic = parts[0].trim();
                    String details = parts[1].trim();
                    // Here, you need to decide how to associate components with concepts, as the current file format doesn't specify this relationship directly
                    // You might add them to the last added concept, or adjust your file format to specify this relationship
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        }
    }



    /* addObject(): Adds an Object to a Concept, Component, or Category.
Arguments:
    - objectType: Determines if the objective will be for a concept, component, or category
Return value: Either an object will be added or an error message will appear.
 */
    public void addObject(String objectType, Object... params) {
        switch (objectType.toLowerCase()) {
            case "concept":
                // Assuming params order: int id, String topic, String category, String details
                addConcept(new Concept((Integer) params[0], (String) params[1], (String) params[2], (String) params[3]));
                break;
            case "component":
                // Assuming params order: String topic, String details
                addComponent(new Component((String) params[0], (String) params[1]));
                break;
            case "category":
                // Assuming params order: int id, String topic
                addCategory(new Category((Integer) params[0], (String) params[1]));
                break;
            default:
                System.out.println("Invalid object type for addition.");
        }
    }

    /* updateObject(): Updates an Object to a Concept, Component, or Category.
        Arguments:
            - objectType: Determines if the objective will be for a concept, component, or category
        Return value: Either an object will be added or an error message will appear.
         */
    public void updateObject(String objectType, Object... params) {
        switch (objectType.toLowerCase()) {
            case "concept":
                // Assuming params order: int conceptId, String newTopic, String newCategory, String newDetails
                updateConcept((Integer) params[0], (String) params[1], (String) params[3]);
                break;
            case "component":
                // Assuming params order: String oldTopic, String newTopic, String newDetails
                updateComponent((String) params[0], (String) params[1], (String) params[2]);
                break;
            case "category":
                // Assuming params order: int categoryId, String newTopic
                updateCategory((Integer) params[0], (String) params[1]);
                break;
            default:
                System.out.println("Invalid object type for update.");
        }
    }

    /* removeObject(): Removes an Object to a Concept, Component, or Category.
        Arguments:
            - objectType: Determines if the objective will be for a concept, component, or category
        Return value: Either an object will be added or an error message will appear.
         */    public void removeObject(String objectType, Object... params) {
        switch (objectType.toLowerCase()) {
            case "concept":
                // Assuming params is the conceptId
                deleteConcept((Integer) params[0]);
                break;
            case "component":
                // Assuming params is the component's topic
                deleteComponent((String) params[0]);
                break;
            case "category":
                // Assuming params is the categoryId
                deleteCategory((Integer) params[0]);
                break;
            default:
                System.out.println("Invalid object type for removal.");
        }
    }

    //RETRIEVAL METHODS
    // getConcepts(): Retrieves all Concepts from the database.
    public List<Concept> getConcepts() {
        return this.concepts;
    }

    // getComponents(): Retrieves all Components from the database.
    public List<Component> getComponents() {
        return this.components;
    }

    // getCategories(): Retrieves all Categories from the database.
    public List<Category> getCategories() {
        return this.categories;
    }
    /* getConcept(): Retrieves a Concept from the database based on its ID.
 Arguments:
    - conceptId: The ID of the Concept to retrieve.
 Return value: The found Concept object or null if not found.
 */
    public Concept getConcept(int conceptId) {
        return concepts.stream()
                .filter(concept -> concept.getId() == conceptId)
                .findFirst()
                .orElse(null);
    }

    /* getComponent(): Retrieves a Component from the database based on its topic.
     Arguments:
        - componentTopic: The topic of the Component to retrieve.
     Return value: The found Component object or null if not found.
     */
    public Component getComponent(String componentTopic) {
        return components.stream()
                .filter(component -> component.getTopic().equals(componentTopic))
                .findFirst()
                .orElse(null);
    }

    /* getCategory(): Retrieves a Category from the database based on its ID.
     Arguments:
        - categoryId: The ID of the Category to retrieve.
     Return value: The found Category object or null if not found.
     */
    public Category getCategory(int categoryId) {
        return categories.stream()
                .filter(category -> category.getId() == categoryId)
                .findFirst()
                .orElse(null);
    }


    //UTILITY METHODS
    /* addComponentToConcept(): Adds a Component to a Concept based on the Concept's ID.
     Arguments:
        - conceptId: The ID of the Concept to which the Component should be added.
        - component: The Component object to be added to the Concept.
     Return value: None
     */
    // Inside the addComponentToConcept method in DatabaseManager
    public void addComponentToConcept(int conceptId, Component component) {
        Concept concept = getConcept(conceptId);
        if (concept == null) {
            System.out.println("Concept with ID " + conceptId + " does not exist.");
            return; // Stop execution for this method
        }
        concept.getComponents().add(component);
        System.out.println("Component added successfully.");
    }



    /* addComponentToLastConcept(): Adds a Component to the last added Concept in the database.
    Arguments:
        - component: Component of the Component class
    Return value: Either the component or an error message.
     */
    public void addComponentToLastConcept(Component component) {
        if (!concepts.isEmpty()) {
            Concept lastConcept = concepts.get(concepts.size() - 1);
            lastConcept.addComponent(component);
        } else {
            System.err.println("No concepts available to add a component.");
        }
    }
    public Category findCategoryByName(String name) {
        for (Category category : categories) {
            if (category.getTopic().equalsIgnoreCase(name)) {
                return category; // Return the category if found
            }
        }
        return null; // Return null if the category is not found
    }


    public boolean updateConceptTopic(int conceptId, String newTopic, String newConceptDetails) {
        for (Concept concept : concepts) {
            if (concept.getId() == conceptId) {
                concept.setTopic(newTopic);
                return true; // Successfully updated the topic
            }
        }
        return false; // Concept not found
    }

    public void updateCategoryName(int categoryId, String newName) {
        for (Category category : categories) {
            if (category.getId() == categoryId) {
                category.setTopic(newName);
                break; // Exit the loop once the matching category is found and updated
            }
        }
    }


    public Category getCategoryById(int id) {
        for (Category category : categories) {
            if (category.getId() == id) {
                return category;
            }
        }
        return null; // Category not found
    }


    public boolean deleteCategoryById(int id) {
        String query = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Similarly, handle exceptions properly
            return false;
        }
    }

    public boolean removeCategoryById(int categoryId) {
        // Your existing code for deleting a category by ID
        return deleteCategory(categoryId);
    }

    public List<Concept> getConceptsByCategory(String categoryName) {
        return concepts.stream()
                .filter(concept -> concept.getCategory().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList());
    }


    public List<Concept> getConceptsByCategoryId(int categoryId) {
        // First, find the category name associated with the given categoryId
        Category category = categories.stream()
                .filter(c -> c.getId() == categoryId)
                .findFirst()
                .orElse(null);

        if (category == null) {
            return Collections.emptyList(); // No category found with the given ID, return an empty list
        }

        final String categoryName = category.getTopic();

        // Now, filter concepts that are associated with this category name
        return concepts.stream()
                .filter(concept -> concept.getCategory().equals(categoryName))
                .collect(Collectors.toList());
    }


}
