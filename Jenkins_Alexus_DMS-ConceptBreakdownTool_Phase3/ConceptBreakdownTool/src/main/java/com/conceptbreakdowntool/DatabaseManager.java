/**
 Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Apr 11th 2024
 ClassName: DatabaseManager

 Purpose: Manage the operations of adding, updating, deleting, and retrieving categories, concepts, and components by maintaining a connection to the SQLite Database.

 - CONSTRUCTOR AND INITIALIZATION: Responsible for storing categories, concepts, and components. Sets up the database connection. Loads data from the database.
     * Constructor(DatabaseManager): Initializes the main window; Sets up the database manager connection; Prepare the UI components.
     * initializeDatabase(): Ensures the necessary database tables are created if they don't already exist.
     * loadDataFromDatabase(), loadCategoriesFromDb(), loadConceptsFromDb(), loadComponentsFromDb(): Load existing data from the database into memory.
 - CRUD OPERATIONS: Handles the creation, reading, updating, and deletion of data in the database.
     * addCategory(), addConcept(), addComponent(): Adds new entries to the database tables.
     * updateCategory(), updateConcept(), updateComponent(): Updates existing entries to the database tables.
     * deleteCategory(), deleteConcept(), deleteComponent(): Removes entries from the database.
 - DATA RETRIEVAL METHODS: Used to retrieve data from the database.
     * getCategories(), getConcepts(), getComponents(): Gathers lists from categories, concepts, and components stored in the database.
     * getConcept(), getComponent(), getCategory(), getCategoryById(): Gathers single entries based on IDs.
 - UTILITY METHODS
     * setUIUpdateListener(): Sets a listener to update the UI when data changes have occurred.
     * connect(): Establishes a connection to the database.
     * generateNewComponentId(): Create a new ID for a component.
 - DATA PERSISTENCE METHODS
    * saveAllData(): Saves the current state of the database to a file.
    * loadDataFromFile(): Loads data from a file path into the application.
 - OBJECT MANIPULATION METHODS: Adds, Updates, and Removes objects from the database in a dynamic way.
    * addObject(), updateObject(), removeObject(): Handles addition, update, or removal of concepts, components, or categories.
 - SPECIALIZED RETRIEVAL AND UPDATE METHODS
    * getConceptsByCategoryId(), getComponentsByConceptId(): Gathers concepts or components by an ID.
    * addComponentToConcept(): Adds a component to a concept.

 @author Alexus Jenkins
 @version 5.0
 **/

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

public class DatabaseManager {
    //Attributes
    private List<Concept> concepts;
    private List<Component> components;
    private List<Category> categories;
    private String dataFilePath; // This file will be created in the current working directory of the application
    private Connection dbConnection;
    private Scanner scanner;
    private DatabaseManager dbManager;
    private MainApplicationWindow.UIUpdateListener uiUpdateListener;



    public void setDataFilePath(String filePath) {
        this.dataFilePath = filePath;
    }

    /**
     CONSTRUCTOR AND INITIALIZATION: Responsible for storing categories, concepts, and components. Sets up the database connection. Loads data from the database.
     **/
    /**
     Constructor(DatabaseManager): Initializes the main window; Sets up the database manager connection; Prepare the UI components.
     **/
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

    /**
     initializeDatabase(): Ensures the necessary database tables are created if they don't already exist.
     **/
    private void initializeDatabase() {
        String sqlCategory = "CREATE TABLE IF NOT EXISTS Category (Category_ID INTEGER PRIMARY KEY, Category_Topic TEXT NOT NULL);";
        String sqlConcept = "CREATE TABLE IF NOT EXISTS Concept (Concept_ID INTEGER PRIMARY KEY, Concept_Topic TEXT NOT NULL, Category_ID INTEGER NOT NULL, Concept_Details TEXT NOT NULL, FOREIGN KEY(Category_ID) REFERENCES Category(Category_ID));";
        String sqlComponent = "CREATE TABLE IF NOT EXISTS Component (Component_Topic TEXT NOT NULL, Component_Description TEXT NOT NULL, Concept_ID INTEGER NOT NULL, FOREIGN KEY(Concept_ID) REFERENCES Concept(Concept_ID));";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCategory);
            stmt.execute(sqlConcept);
            stmt.execute(sqlComponent);
            System.out.println("Database tables verified/created successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     loadDataFromDatabase(): Load existing data from the database into memory.
     **/
    private void loadDataFromDatabase() {
        loadCategoriesFromDb();
        loadConceptsFromDb();
        loadComponentsFromDb();
    }

    /**
     * loadCategoriesFromDb(): Load existing data from the database into memory.
     * **/
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

    /**
     * loadConceptsFromDb(): Load existing data from the database into memory.
     * **/
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

    /**
     * loadComponentsFromDb(): Load existing data from the database into memory.
     * **/
    private void loadComponentsFromDb() {
        String sql = "SELECT Component_Topic, Component_Description, Concept_ID FROM Component";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String topic = rs.getString("Component_Topic");
                String description = rs.getString("Component_Description");
                int conceptId = rs.getInt("Concept_ID");
                this.components.add(new Component(topic, description, conceptId));
            }
        } catch (SQLException e) {
            System.err.println("Error loading components from the database: " + e.getMessage());
        }
    }


    /**
     CRUD OPERATIONS: Handles the creation, reading, updating, and deletion of data in the database.
     **/
    /**
     addCategory(): Adds new entries to the database tables.
     @param category The category object to add to the database.
     **/
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
    /**
     * addConcept(): Adds new entries to the database tables.
     * @param concept The concept to add. It must be linked to an existing category.
     * **/
    public void addConcept(Concept concept) {
        // Find the category by name to get the Category_ID
        Category category = findCategoryByName(concept.getCategory());
        if (category == null) {
            System.out.println("Category not found for the concept.");
            return; // Stop if the category does not exist
        }

        // SQL command to insert a new row into the Concept table
        // Concept_ID is auto-generated by SQLite
        String sql = "INSERT INTO Concept(Concept_Topic, Category_ID, Concept_Details) VALUES(?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set the parameters for the prepared statement based on the concept object
            pstmt.setString(1, concept.getTopic());
            pstmt.setInt(2, category.getId()); // Use the Category_ID from the found category
            pstmt.setString(3, concept.getDetails());

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
    /**
     * addComponent(): Adds new entries to the database tables.
     * @param component The component to add.
     * **/
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

    /**
     updateCategory(): Updates existing components to the database tables.
     * @param categoryId The ID of the category to update.
     * @param newTopic The new topic to set for the category.
     * @return true if the update was successful, false otherwise.
     **/
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
    /**
     * updateConcept(): Updates existing categories to the database tables.
     * @param conceptId The ID of the concept to update.
     * @param newTopic The new topic for the concept.
     * @param newDetails The new details to set for the concept.
     * @return true if the update was successful, false otherwise.
     * **/
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
    /**
     * updateComponent(): Updates existing concepts to the database tables.
     * @param conceptId The ID of the concept to which the component belongs.
     * @param oldTopic The current topic of the component.
     * @param newTopic The new topic to set for the component.
     * @param newDetails The new details to set for the component.
     * @return true if the update was successful, false otherwise.
     * **/
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

    /**
     deleteCategory(): Removes category from the database.
     @param categoryId Removes the category from the database by id
     **/
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
    /**
     deleteConcept(): Removes concept from the database.
     * @param conceptId The ID of the concept to delete.
      * @return true if the deletion was successful, false otherwise.
     **/
    public boolean deleteConcept(int conceptId) {
        // SQL statement to delete a concept based on its ID
        String sql = "DELETE FROM Concept WHERE Concept_ID = ?";
       deleteComponentsByConceptId(conceptId); // This method would handle deleting components related to the concept.

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the ID parameter for the delete operation
            pstmt.setInt(1, conceptId);

            // Execute the delete statement
            int affectedRows = pstmt.executeUpdate();

            // Check if the delete operation was successful
            if (affectedRows > 0) {
                // Remove the concept from the in-memory list
                concepts.removeIf(concept -> concept.getId() == conceptId);
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false; // Return false if deletion was unsuccessful
    }
    /**
     deleteComponent(): Removes component from the database.
     * @param componentTopic The topic of the component to delete.
      * @return true if the deletion was successful, false otherwise.
     **/
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
    /**
     deleteComponentByConceptId(): Removes component(s) by concept id from the database.
     * @param conceptId The ID of the concept whose components are to be deleted.
     **/
    private void deleteComponentsByConceptId(int conceptId) {
        String sql = "DELETE FROM Component WHERE Concept_ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the Concept_ID parameter for the delete operation
            pstmt.setInt(1, conceptId);

            // Execute the delete statement for components
            pstmt.executeUpdate();

            // Optionally, update the in-memory list of components
            components.removeIf(component -> component.getConceptId() == conceptId);

        } catch (SQLException e) {
            System.out.println("Error deleting components for concept ID " + conceptId + ": " + e.getMessage());
        }
    }


    /**
     DATA RETRIEVAL METHODS: Used to retrieve data from the database.
     **/
    /**
     getConcepts(): Gathers lists from categories, concepts, and components stored in the database.
     * @return a list of all concepts updated from the database.
     **/
    public List<Concept> getConcepts() {
        return this.concepts;
    }
    /**
     getCategories(): Gathers lists from categories, concepts, and components stored in the database.
     * @return a list of all categories updated from the database.
     **/
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
    /**
     getConcept(): Gathers single entries based on IDs.
     * @param conceptId the ID of the concept to retrieve.
     * @return the concept if found, or null if no concept with the given ID exists.
     **/
    public Concept getConcept(int conceptId) {
        return concepts.stream()
                .filter(concept -> concept.getId() == conceptId)
                .findFirst()
                .orElse(null);
    }

    /**
     getComponent(): Gathers single entries based on IDs.
     * @param componentTopic the topic of the component to retrieve.
     * @return the component if found, or null if no component with the given topic exists.
     **/
    public Component getComponent(String componentTopic) {
        return components.stream()
                .filter(component -> component.getTopic().equals(componentTopic))
                .findFirst()
                .orElse(null);
    }
    /**
     getCategory(): Gathers single entries based on IDs.
     * @param categoryId the ID of the category to retrieve.
     * @return the category if found, or null if no category with the given ID exists.
     **/
    public Category getCategory(int categoryId) {
        return categories.stream()
                .filter(category -> category.getId() == categoryId)
                .findFirst()
                .orElse(null);
    }
    /**
     getCategoryById(): Gathers single entries based on IDs.
     * @param id the ID of the category to find.
     * @return the found category or null if no category with such ID exists.
     **/
    public Category getCategoryById(int id) {
        for (Category category : categories) {
            if (category.getId() == id) {
                return category;
            }
        }
        return null; // Category not found
    }


    /**
     UTILITY METHODS
     **/
    /**
     setUIUpdateListener(): Sets a listener to update the UI when data changes have occurred.
     * @param listener the UI update listener to be notified of changes
     **/
    public void setUIUpdateListener(MainApplicationWindow.UIUpdateListener listener) {
        this.uiUpdateListener = listener;
    }
    /**
     connect(): Establishes a connection to the database.
     * @return a connection to the database
     **/
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
    /**
     generateNewComponentId(): Create a new ID for a component.
     * @return the newly generated unique component ID
     **/
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


    /**
     DATA PERSISTENCE METHODS
     **/
    /**
     saveAllData(): Saves the current state of the database to a file.
     **/
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
    /**
     loadDataFromFile(): Loads data from a file path into the application.
     * @param filepath the path to the file from which data is to be loaded
     * @return true if data loading is successful, false otherwise
     **/
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


    /**
     OBJECT MANIPULATION METHODS: Adds, Updates, and Removes objects from the database in a dynamic way.
     **/
    /**
     addObject(): Handles addition, update, or removal of concepts, components, or categories.
     * @param objectType the type of object to add (category, concept, or component)
     * @param params the parameters needed for creating the object
     **/
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
    /**
     updateObject(): Handles addition, update, or removal of concepts, components, or categories.
     * @param objectType the type of object to update (category, concept, or component)
     * @param params the parameters needed for updating the object
     **/
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
    /**
     removeObject(): Handles addition, update, or removal of concepts, components, or categories.
     * @param objectType the type of object to remove (category, concept, or component)
     * @param params the parameters identifying the object to be removed
     **/
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


    /**
     SPECIALIZED RETRIEVAL AND UPDATE METHODS
     **/
    /**
     getConceptsByCategoryId(): Gathers concepts or components by an ID.
     * @param categoryId the ID of the category
     * @return a list of concepts belonging to the specified category
     **/
    public List<Concept> getConceptsByCategoryId(int categoryId) {
        List<Concept> concepts = new ArrayList<>();
        String sql = "SELECT * FROM Concept WHERE Category_ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Concept concept = new Concept(rs.getInt("Concept_ID"), rs.getString("Concept_Topic"), rs.getString("Category_ID"), rs.getString("Concept_Details"));
                concepts.add(concept);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return concepts;
    }
    /**
     getComponentsByConceptId(): Gathers concepts or components by an ID.
     * @param conceptId the ID of the concept
     * @return a list of components belonging to the specified concept
     **/
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
    /**
     addComponentToConcept(): Adds a component to a concept.
     * @param conceptId the ID of the concept to which the component will be added
     * @param component the component to be added to the concept
     **/
    public void addComponentToConcept(int conceptId, Component component) {
        Concept concept = getConcept(conceptId);
        if (concept != null) {
            addComponent(component);
        } else {
            System.out.println("Concept with ID " + conceptId + " does not exist.");
        }
    }

    /**
     findCategoryByName(): Finds a category by its name.
     * @param name the name of the category to find
     * @return the found category, or null if no such category exists
     **/
    public Category findCategoryByName(String name) {
        for (Category category : categories) {
            if (category.getTopic().equalsIgnoreCase(name)) {
                return category; // Return the category if found
            }
        }
        return null; // Return null if the category is not found
    }

}
