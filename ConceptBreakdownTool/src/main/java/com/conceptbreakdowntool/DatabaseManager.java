package com.conceptbreakdowntool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

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
        this.concepts.add(concept);
    }

    /*
     * addComponent(): Adds a new Component to the database.
        Arguments:
            - component: The Component object to be added.
        Return value: None
     */
    public void addComponent(Component component) {
        this.components.add(component);
    }

    /* addCategory(): Adds a new Category to the database.
        Arguments:
            - category: The Category object to be added.
        Return value: None
     */
    public void addCategory(Category category) {
        this.categories.add(category);
    }

    /* updateConcept(): Updates an existing Concept in the database.
     Arguments:
        - conceptId
        - newTopic
        - newCategory
        - newDetails
     Return value: None
     */
    public void updateConcept(int conceptId, String newTopic, String newCategory, String newDetails) {
        for (Concept concept : concepts) {
            if (concept.getId() == conceptId) {
                concept.setTopic(newTopic);
                concept.setCategory(newCategory);
                concept.setDetails(newDetails);
                return;
            }
        }
        System.out.println("Concept with ID " + conceptId + " not found.");
    }

    /* updateComponent(): Updates an existing Component in the database.
     Arguments:
        - oldTopic
        - newTopic
        - newDetails
     Return value: None
     */
    public void updateComponent(String oldTopic, String newTopic, String newDetails) {
        for (Component component : components) {
            if (component.getTopic().equals(oldTopic)) {
                component.setTopic(newTopic);
                component.setDetails(newDetails);
                return;
            }
        }
        System.out.println("Component with topic '" + oldTopic + "' not found.");
    }

    /* updateCategory(): Updates an existing Category in the database.
     Arguments:
        - categoryId
        - newTopic
     Return value: None
     */
    public void updateCategory(int categoryId, String newTopic) {
        for (Category category : categories) {
            if (category.getId() == categoryId) {
                category.setTopic(newTopic);
                return;
            }
        }
        System.out.println("Category with ID " + categoryId + " not found.");
    }

    /* deleteConcept(): Deletes an existing Concept from the database based on its ID.
     Arguments:
        - conceptId: The ID of the Concept to be deleted.
     Return value: None
     */
    public void deleteConcept(int conceptId) {
        concepts.removeIf(concept -> concept.getId() == conceptId);
    }

    /* deleteComponent(): Deletes an existing Component from the database based on its topic.
     Arguments:
        - componentTopic: The topic of the Component to be deleted.
     Return value: None
     */
    public void deleteComponent(String componentTopic) {
        components.removeIf(component -> component.getTopic().equals(componentTopic));
    }

    /* deleteCategory(): Deletes an existing Category from the database based on its ID.
     Arguments:
        - categoryId: The ID of the Category to be deleted.
     Return value: None
     */
    public void deleteCategory(int categoryId) {
        categories.removeIf(category -> category.getId() == categoryId);
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
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter("data.json")) {
            Map<String, Object> allData = new HashMap<>();
            allData.put("concepts", this.concepts);
            allData.put("components", this.components);
            allData.put("categories", this.categories);
            gson.toJson(allData, writer);
        } catch (IOException e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }

    public void loadAllData() {
        Gson gson = new Gson();
        File file = new File("data.json");
        if (file.exists() && file.length() != 0) {
            try (FileReader reader = new FileReader(file)) {
                Type dataType = new TypeToken<Map<String, List<?>>>(){}
                        .getType();
                Map<String, List<?>> allData = gson.fromJson(reader, dataType);

                this.concepts = (List<Concept>) allData.get("concepts");
                this.components = (List<Component>) allData.get("components");
                this.categories = (List<Category>) allData.get("categories");
            } catch (IOException e) {
                System.err.println("Failed to load data: " + e.getMessage());
            } catch (ClassCastException e) {
                System.err.println("Data format error: " + e.getMessage());
            }
        } else {
            System.out.println("Data file does not exist or is empty. Starting with a clean slate.");
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
         */    public void updateObject(String objectType, Object... params) {
        switch (objectType.toLowerCase()) {
            case "concept":
                // Assuming params order: int conceptId, String newTopic, String newCategory, String newDetails
                updateConcept((Integer) params[0], (String) params[1], (String) params[2], (String) params[3]);
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
    public void addComponentToConcept(int conceptId, Component component) {
        Concept concept = getConcept(conceptId);
        if (concept != null) {
            concept.addComponent(component);
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


}

