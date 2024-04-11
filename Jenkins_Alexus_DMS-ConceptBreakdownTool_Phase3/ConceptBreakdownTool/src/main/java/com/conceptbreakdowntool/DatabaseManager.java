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
    private String dataFilePath; // This file will be created in the current working directory of the application
    private Connection dbConnection;
    private Scanner scanner;
    private DatabaseManager dbManager;
    private int conceptId;
    private MainApplicationWindow.UIUpdateListener uiUpdateListener;

    public void setUIUpdateListener(MainApplicationWindow.UIUpdateListener listener) {
        this.uiUpdateListener = listener;
    }

    public Connection connect() {
        if (dataFilePath == null || dataFilePath.isEmpty()) {
            throw new IllegalStateException("Database file path is not set.");
        }
        String url = "jdbc:sqlite:" + this.dataFilePath;
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found: " + e.getMessage());
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
        this.dataFilePath = "./conceptBreakdownTool.db";
        if (this.dataFilePath != null) {
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
        }

        initializeDatabase(); // Ensure this is called to setup database tables
        loadDataFromDatabase(); // Load existing data from database
    }

    public void setDataFilePath(String filePath) {
        this.dataFilePath = filePath;
    }

    //CRUD OPERATIONS
    /* ): Adds a new Concept to the database.
        Arguments:
            - concept: The Concept object to be added.
      addConcept(  Return value: None
     */
    public void addConcept(Concept concept) {
        // SQL command to insert a new row into the Concept table
        String sql = "INSERT INTO Concept(Concept_ID, Concept_Topic, Category_ID, Concept_Details) VALUES(?,?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set the parameters for the prepared statement based on the concept object
            pstmt.setInt(1, concept.getId());
            pstmt.setString(2, concept.getTopic());
            pstmt.setString(3, concept.getCategory());
            pstmt.setString(4, concept.getDetails());

            // Execute the insert operation
            int affectedRows = pstmt.executeUpdate();

            // If the insert was successful, retrieve the generated key for the new concept
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Set the ID of the concept object to the generated key
                        concept.setId(generatedKeys.getInt(1));
                        // Add the new concept to the in-memory list
                        this.concepts.add(concept);
                        System.out.println("Concept added successfully to both the database and in-memory list.");
                        if (uiUpdateListener != null) {
                            // Trigger UI update if the listener is set
                            uiUpdateListener.updateUI();
                        }
                    } else {
                        throw new SQLException("Creating concept failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // Handle exceptions appropriately here
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

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0 && uiUpdateListener != null) {
                components.add(component);
                System.out.println("Component added successfully to both the database and in-memory list.");
                uiUpdateListener.updateUI();
            }
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
        boolean insertSuccess = false;

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, category.getId());
            pstmt.setString(2, category.getTopic());
            int affectedRows = pstmt.executeUpdate();

            // Check if insert was successful by the number of affected rows
            if (affectedRows > 0) {
                insertSuccess = true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // After successfully adding the category to the database, update the UI.
        if (insertSuccess && uiUpdateListener != null) {
            System.out.println("Updating UI after adding category");
            uiUpdateListener.updateUI();
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
    public boolean deleteComponent(String componentTopic) {
        String sql = "DELETE FROM Component WHERE Component_Topic = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, componentTopic);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                components.removeIf(component -> component.getTopic().equals(componentTopic));
                System.out.println("Component deleted successfully from the database and in-memory list.");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error deleting component: " + e.getMessage());
        }
        return false;
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
        // Clear current lists to avoid duplicating items if this method is called multiple times
        categories.clear();
        concepts.clear();
        components.clear();

        // Load categories
        String sqlCategories = "SELECT * FROM Category";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sqlCategories)) {

            while (rs.next()) {
                Category category = new Category(rs.getInt("Category_ID"), rs.getString("Category_Topic"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.out.println("Error loading categories: " + e.getMessage());
        }

        // Load concepts
        String sqlConcepts = "SELECT * FROM Concept";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sqlConcepts)) {

            while (rs.next()) {
                int categoryId = rs.getInt("Category_ID");
                Category category = getCategoryById(categoryId);
                if(category != null){
                    String categoryTopic = category.getTopic(); // Get the topic from the Category object.
                    Concept concept = new Concept(rs.getInt("Concept_ID"), rs.getString("Concept_Topic"), categoryTopic, rs.getString("Concept_Details"));
                    concepts.add(concept);
                } else {
                    System.err.println("Category with ID " + categoryId + " not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading concepts: " + e.getMessage());
        }



        // Load components
        String sqlComponents = "SELECT * FROM Component";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlComponents)) {

            while (rs.next()) {
                int fetchedComponentId = rs.getInt("Component_ID"); // Get componentId from ResultSet
                String topic = rs.getString("Component_Topic");
                String description = rs.getString("Component_Description");
                int fetchedConceptId = rs.getInt("Concept_ID"); // Get conceptId from ResultSet
                Component component = new Component(topic, description, fetchedConceptId);
                components.add(component);
            }
        } catch (SQLException e) {
            System.out.println("Error loading components: " + e.getMessage());
        }

    }


    //loadDataFromFile()
    // Revised component loading to associate components with concepts indirectly
    public boolean loadDataFromFile(String filepath) {
        Connection conn = null;
        Statement stmt = null;
        boolean isSuccess = true;

        try {
            // Establish the connection to the database
            String url = "jdbc:sqlite:" + filepath;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();

            // Clear the current lists to avoid duplication
            categories.clear();
            concepts.clear();
            components.clear();

            // Load categories from the database file
            ResultSet rsCategories = stmt.executeQuery("SELECT * FROM Category");
            while (rsCategories.next()) {
                categories.add(new Category(rsCategories.getInt("Category_ID"), rsCategories.getString("Category_Topic")));
            }
            rsCategories.close();

            // Load concepts from the database file
            ResultSet rsConcepts = stmt.executeQuery("SELECT * FROM Concept");
            while (rsConcepts.next()) {
                concepts.add(new Concept(rsConcepts.getInt("Concept_ID"), rsConcepts.getString("Concept_Topic"),
                        rsConcepts.getString("Category_ID"), rsConcepts.getString("Concept_Details")));
            }
            rsConcepts.close();

            // Load components from the database file
            ResultSet rsComponents = stmt.executeQuery("SELECT * FROM Component");
            while (rsComponents.next()) {
                components.add(new Component(rsComponents.getString("Component_Topic"),
                        rsComponents.getString("Component_Description"), rsComponents.getInt("Concept_ID")));
            }
            rsComponents.close();

            // Notify UI update listener if available
            if (uiUpdateListener != null) {
                uiUpdateListener.updateUI();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            isSuccess = false; // Set isSuccess to false if any exception is caught
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return isSuccess;
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
                int newComponentId = generateNewComponentId(); // Generate a new ID here, not in the constructor
                addComponent(new Component((String) params[0], (String) params[1], (Integer) params[2]));
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
                deleteComponent(String.valueOf((Integer) params[0]));
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
        List<Category> updatedCategories = new ArrayList<>();
        String sql = "SELECT * FROM Category";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                // Assuming you have a constructor that accepts id and topic
                updatedCategories.add(new Category(rs.getInt("Category_ID"), rs.getString("Category_Topic")));
            }
            this.categories = updatedCategories; // Update the in-memory list
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
            System.err.println("Error loading components by concept ID: " + e.getMessage());
        }

        return componentsList;
    }


    // Method to check and create tables if they don't exist
    private void initializeDatabase() {
        String sqlCategory = "CREATE TABLE IF NOT EXISTS Category (Category_ID INTEGER PRIMARY KEY, Category_Topic TEXT NOT NULL);";
        String sqlConcept = "CREATE TABLE IF NOT EXISTS Concept (Concept_ID INTEGER PRIMARY KEY, Concept_Topic TEXT NOT NULL, Category_ID INTEGER NOT NULL, Concept_Details TEXT NOT NULL, FOREIGN KEY(Category_ID) REFERENCES Category(Category_ID));";
        String sqlComponent = "CREATE TABLE IF NOT EXISTS Component (Component_ID INTEGER PRIMARY KEY, Component_Topic TEXT NOT NULL, Component_Description TEXT NOT NULL, Concept_ID INTEGER NOT NULL, FOREIGN KEY(Concept_ID) REFERENCES Concept(Concept_ID));";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            // Create Category table if it doesn't exist
            stmt.execute(sqlCategory);
            // Create Concept table if it doesn't exist
            stmt.execute(sqlConcept);
            // Create Component table if it doesn't exist
            stmt.execute(sqlComponent);
            System.out.println("Database tables verified/created successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    // Method to load data from the database
    private void loadDataFromDatabase() {
        loadCategoriesFromDb();
        loadConceptsFromDb();
        loadComponentsFromDb();
    }

    private void loadCategoriesFromDb() {
        String sql = "SELECT * FROM Category";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                this.categories.add(new Category(rs.getInt("Category_ID"), rs.getString("Category_Topic")));
            }
        } catch (SQLException e) {
            System.err.println("Error loading categories from the database: " + e.getMessage());
        }
    }

    private void loadConceptsFromDb() {
        String sql = "SELECT * FROM Concept";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                this.concepts.add(new Concept(rs.getInt("Concept_ID"), rs.getString("Concept_Topic"), rs.getString("Category_ID"), rs.getString("Concept_Details")));
            }
        } catch (SQLException e) {
            System.err.println("Error loading concepts from the database: " + e.getMessage());
        }
    }

    private void loadComponentsFromDb() {
        String sql = "SELECT * FROM Component";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                this.components.add(new Component(rs.getString("Component_Topic"), rs.getString("Component_Description"), rs.getInt("Concept_ID")));
            }
        } catch (SQLException e) {
            System.err.println("Error loading components from the database: " + e.getMessage());
        }
    }

    public int generateNewComponentId() {
        String sql = "SELECT MAX(Component_ID) as maxId FROM Component";
        int id = 0;

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Get the highest ID and increment it
            if (rs.next()) {
                id = rs.getInt("maxId") + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error generating new component ID: " + e.getMessage());
        }

        return id;
    }


}
