package com.conceptbreakdowntool;

import com.google.gson.Gson;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.conceptbreakdowntool.ConceptBreakdownToolApplication.safelyParseInt;



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
    private String dataFilePath = "conceptBreakdownTool.db"; // This file will be created in the current working directory of the application
    private Connection dbConnection;
    private Scanner scanner;
    private DatabaseManager dbManager;
    private int conceptId;

    Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:/Users/alexusjenkins/Documents/CEN3042C-Software_Development/Jenkins_Alexus_DMS-ConceptBreakdownTool/conceptBreakdownTool.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }

    public void testConnection() {
        try (Connection conn = this.connect()) {
            if (conn != null) {
                System.out.println("Connection to SQLite has been established.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // CONSTRUCTOR
    // DatabaseManager(): Initializes the lists for storing concepts, components, and categories.
    public DatabaseManager() {
        this.concepts = new ArrayList<>();
        this.components = new ArrayList<>();
        this.categories = new ArrayList<>();

        // Check if the default data file exists; if not, create it
        File defaultDataFile = new File(dataFilePath);
        if (!defaultDataFile.exists()) {
            try {
                // Attempt to create the new file
                boolean fileCreated = defaultDataFile.createNewFile();
                if (fileCreated) {
                    System.out.println("New data file created: " + defaultDataFile.getAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("Failed to create default data file: " + e.getMessage());
            }
        }

        // Optionally, load existing data from the file if needed
        loadAllData(); // This will load data from the default file path
    }

    public void setDataFilePath(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }

    //CRUD OPERATIONS
    /* ): Adds a new Concept to the database.
        Arguments:
            - concept: The Concept object to be added.
      addConcept(  Return value: None
     */
    public void addConcept(Concept concept) {
        String sql = "INSERT INTO Concept(topic, category, details) VALUES(?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, concept.getTopic());
            pstmt.setString(2, concept.getCategory());
            pstmt.setString(3, concept.getDetails());

            int affectedRows = pstmt.executeUpdate();

            // Check the affected rows
            if (affectedRows > 0) {
                // If the insert was successful, add the concept to the in-memory list
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        concept.setId(generatedKeys.getInt(1)); // Assuming `id` is auto-incremented by the database
                        this.concepts.add(concept);
                    } else {
                        throw new SQLException("Creating concept failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /*
     * addComponent(): Adds a new Component to the database.
        Arguments:
            - component: The Component object to be added.
        Return value: None
     */
    public void addComponent(Component component) {
        String sql = "INSERT INTO Component(Component_Topic, Component_Description, Concept_ID) VALUES(?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, component.getTopic());
            pstmt.setString(2, component.getDetails());
            pstmt.setInt(3, component.getConceptId());

            pstmt.executeUpdate();
            components.add(component);

            System.out.println("Component added successfully to both the database and in-memory list.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /* addCategory(): Adds a new Category to the database.
        Arguments:
            - category: The Category object to be added.
        Return value: None
     */
    public void addCategory(Category category) {
        String sql = "INSERT INTO Category (Category_ID, Category_Topic) VALUES (?, ?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, category.getId());
            pstmt.setString(2, category.getTopic());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
        String sql = "UPDATE Concept SET Concept_Topic = ?, Concept_Details = ? WHERE Concept_ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newTopic);
            pstmt.setString(2, newDetails);
            pstmt.setInt(3, conceptId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    /* updateComponent(): Updates an existing Component in the database.
     Arguments:
        - oldTopic
        - newTopic
        - newDetails
     Return value: None
     */
    public boolean updateComponent(int conceptId, String oldTopic, String newTopic, String newDetails) {
        // SQL statement to update a component based on Concept ID and old topic
        String sql = "UPDATE Component SET topic = ?, details = ? WHERE concept_id = ? AND topic = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set parameters for the prepared statement
            pstmt.setString(1, newTopic);
            pstmt.setString(2, newDetails);
            pstmt.setInt(3, conceptId);
            pstmt.setString(4, oldTopic);

            // Execute the update
            int affectedRows = pstmt.executeUpdate();

            // Check if the update was successful
            if (affectedRows > 0) {
                // Update the in-memory representation
                for (Component component : components) {
                    if (component.getConceptId() == conceptId && component.getTopic().equals(oldTopic)) {
                        component.setTopic(newTopic);
                        component.setDetails(newDetails);
                        System.out.println("Component updated successfully in the database and in-memory list.");
                        return true; // Update successful
                    }
                }
                return true; // Database update successful, in-memory update not applicable or failed
            }
        } catch (SQLException e) {
            System.out.println("Error updating component: " + e.getMessage());
        }
        return false; // Update unsuccessful
    }





    /* updateCategory(): Updates an existing Category in the database.
     Arguments:
        - categoryId
        - newTopic
     Return value: None
     */
    // When updating a category that does not exist
    public boolean updateCategory(int categoryId, String newTopic) {
        // SQL statement to update the category name based on its ID
        String sql = "UPDATE Category SET topic = ? WHERE id = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the new topic and the category ID for the update
            pstmt.setString(1, newTopic);
            pstmt.setInt(2, categoryId);

            // Execute the update
            int affectedRows = pstmt.executeUpdate();

            // Check if the update was successful by examining affected rows
            if (affectedRows > 0) {
                // Additionally, update the in-memory list of categories
                for (Category category : categories) {
                    if (category.getId() == categoryId) {
                        category.setTopic(newTopic);
                        System.out.println("Category updated successfully in the database and in-memory list.");
                        return true; // Update successful
                    }
                }
                return true; // Database update successful, in-memory update not applicable or failed to find
            }
        } catch (SQLException e) {
            System.out.println("Error updating category: " + e.getMessage());
        }
        return false; // Update unsuccessful
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
        // SQL statement to delete a concept based on its ID
        String sql = "DELETE FROM Concept WHERE id = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the ID parameter for the delete operation
            pstmt.setInt(1, conceptId);

            // Execute the delete statement
            int affectedRows = pstmt.executeUpdate();

            // Check if the delete operation was successful
            if (affectedRows > 0) {
                // If successful, remove the concept from the in-memory list
                concepts.removeIf(concept -> concept.getId() == conceptId);
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false; // Return false if deletion was unsuccessful
    }


    /* deleteComponent(): Deletes an existing Component from the database based on its topic.
     Arguments:
        - componentTopic: The topic of the Component to be deleted.
     Return value: None
     */
    public boolean deleteComponent(int componentId) {
        // SQL statement to delete a component based on its ID
        String sql = "DELETE FROM Component WHERE id = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the parameters for the SQL query
            pstmt.setInt(1, componentId);

            // Execute the delete operation
            int affectedRows = pstmt.executeUpdate();

            // Check if the delete was successful by examining affected rows
            if (affectedRows > 0) {
                // Additionally, remove the component from the in-memory list
                components.removeIf(component -> component.getConceptId() == componentId);
                System.out.println("Component deleted successfully from the database and in-memory list.");
                return true; // Delete successful
            }
        } catch (SQLException e) {
            System.out.println("Error deleting component: " + e.getMessage());
        }
        return false; // Delete unsuccessful
    }


    /* deleteCategory(): Deletes an existing Category from the database based on its ID.
     Arguments:
        - categoryId: The ID of the Category to be deleted.
     Return value: None
     */
    public boolean deleteCategory(int categoryId) {
        // SQL statement adjusted to match table column names
        String sql = "DELETE FROM Category WHERE Category_ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Logic for handling successful deletion
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }




    //DATA PERSISTENCE
    /*  saveAllData(): Saves the current state of concepts, components, and categories to a JSON file.
        Arguments: None
        Return value: None
     */
    public void saveAllData() {
        if (this.dataFilePath == null || this.dataFilePath.isEmpty()) {
            System.err.println("Data file path is not specified. Data not saved.");
            return;
        }

        // Backup the existing file before overwriting
        File file = new File(this.dataFilePath);
        File backupFile = new File(this.dataFilePath + ".bak");
        if (file.exists()) {
            try {
                Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Failed to create backup file: " + e.getMessage());
                return; // Stop the save operation if backup fails
            }
        }

        // Check that data is not null
        if (concepts == null || components == null || categories == null) {
            System.err.println("Attempted to save null data. Operation aborted.");
            return; // Don't overwrite existing data with null
        }

        // Proceed with saving data
        Gson gson = new Gson();
        Map<String, Object> allData = new HashMap<>();
        allData.put("concepts", concepts);
        allData.put("components", components);
        allData.put("categories", categories);

        try (Writer writer = new FileWriter(this.dataFilePath)) {
            gson.toJson(allData, writer);
        } catch (IOException e) {
            System.err.println("Error saving data to file: " + e.getMessage());
            // Attempt to restore from backup
            try {
                Files.copy(backupFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.err.println("Data restored from backup due to save failure.");
            } catch (IOException ex) {
                System.err.println("Failed to restore data from backup: " + ex.getMessage());
            }
        }
    }


    /*  loadAllData(): Loads concepts, components, and categories from a JSON file into the application.
        Arguments: None
        Return value: None
     */
    public void loadAllData() {
        // Load categories
        String sqlCategories = "SELECT * FROM Category";
        // Execute this SQL query to load categories and iterate over the ResultSet to populate your categories list

        // Load concepts
        String sqlConcepts = "SELECT * FROM Concept";
        // Execute this SQL query to load concepts and iterate over the ResultSet to populate your concepts list

        // Load components
        String sqlComponents = "SELECT * FROM Component";
        // Execute this SQL query to load components and iterate over the ResultSet to populate your components list
    }

    //loadDataFromFile()
    // Revised component loading to associate components with concepts indirectly
    public void loadDataFromFile(String filepath) {
        categories.clear();
        concepts.clear();
        components.clear();

        try (Connection conn = this.connect()) {
            // Load categories
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT Category_ID, Category_Topic FROM Category")) {
                while (rs.next()) {
                    categories.add(new Category(rs.getInt("Category_ID"), rs.getString("Category_Topic")));
                }
            }

            // Load concepts
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT Concept_ID, Concept_Topic, Concept_Details, Category_ID FROM Concept")) {
                while (rs.next()) {
                    concepts.add(new Concept(rs.getInt("Concept_ID"), rs.getString("Concept_Topic"),
                            findCategoryById(rs.getInt("Category_ID")).getTopic(),
                            rs.getString("Concept_Details")));
                }
            }

            // Load components and directly associate them with the concept
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT Component_Topic, Component_Description, Concept_ID FROM Component")) {
                while (rs.next()) {
                    Component component = new Component(rs.getString("Component_Topic"),
                            rs.getString("Component_Description"),
                            rs.getInt("Concept_ID"));
                    // Find the concept and add the component to it
                    for (Concept concept : concepts) {
                        if (concept.getId() == component.getConceptId()) {
                            concept.addComponent(component);
                            break; // Stop searching once the concept is found and the component is added
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading data from database: " + e.getMessage());
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
                addComponent(new Component((String) params[0], (String) params[1], conceptId));
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
                updateConcept((Integer) params[0], (String) params[1], (String) params[2]);
                break;
            // Inside the updateObjects() method
            case "component":
                int componentId = safelyParseInt(scanner, "Enter Component ID to update:");
                System.out.println("Enter old Component Topic:");
                String oldComponentTopic = scanner.nextLine().trim();
                System.out.println("Enter new Component Topic:");
                String newComponentTopic = scanner.nextLine().trim();
                System.out.println("Enter new Component Details:");
                String newComponentDetails = scanner.nextLine().trim();

                // Directly updating the component using its Concept ID and old topic
                if (dbManager.updateComponent(componentId, oldComponentTopic, newComponentTopic, newComponentDetails)) {
                    System.out.println("Component details updated successfully.");
                } else {
                    System.out.println("Component not found or update failed.");
                }
                break;

            case "category":
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
         */
    public void removeObject(String objectType, Object... params) {
        switch (objectType.toLowerCase()) {
            case "concept":
                deleteConcept((Integer) params[0]);
                break;
            case "component":
                // Adjusted to expect the component's ID as the parameter for deletion
                deleteComponent((Integer) params[0]);
                break;
            case "category":
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
    // When adding a component, ensure you set the correct conceptId
    public void addComponentToConcept(int conceptId, Component component) {
        Concept concept = getConcept(conceptId);
        if (concept != null) {
            concept.getComponents().add(component);
            // Add the component to the database as well
            addComponent(component);
        } else {
            System.out.println("Concept with ID " + conceptId + " does not exist.");
        }
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
        String query = "DELETE FROM Category WHERE id = ?";
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

    public List<Component> getComponentsByConceptId(int conceptId) {
        List<Component> componentsList = new ArrayList<>();
        String sql = "SELECT Component_Topic, Component_Description FROM Component WHERE Concept_ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, conceptId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String topic = rs.getString("Component_Topic");
                String description = rs.getString("Component_Description");
                Component component = new Component(topic, description, conceptId);
                componentsList.add(component);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return componentsList;
    }



}
