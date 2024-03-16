package com.conceptbreakdowntool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {
    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        dbManager = new DatabaseManager();
    }

    @Test
    void testAddObject() {
        // Test adding a Concept
        dbManager.addObject("concept", 1, "Test Concept", "Test Category", "Test Details");
        Concept addedConcept = dbManager.getConcept(1);
        assertNotNull(addedConcept);
        assertEquals("Test Concept", addedConcept.getTopic());

        // Test adding a Component
        dbManager.addObject("component", "Test Component", "Test Component Details");
        Component addedComponent = dbManager.getComponent("Test Component");
        assertNotNull(addedComponent);
        assertEquals("Test Component Details", addedComponent.getDetails());

        // Test adding a Category
        dbManager.addObject("category", 1, "Test Category");
        Category addedCategory = dbManager.getCategory(1);
        assertNotNull(addedCategory);
        assertEquals("Test Category", addedCategory.getTopic());
    }

    @Test
    void testRemoveObject() {
        // Prepopulate with test data
        dbManager.addObject("concept", 1, "Test Concept", "Test Category", "Test Details");
        dbManager.addObject("component", "Test Component", "Test Component Details");
        dbManager.addObject("category", 1, "Test Category");

        // Test removing a Concept
        dbManager.removeObject("concept", 1);
        assertNull(dbManager.getConcept(1));

        // Test removing a Component
        dbManager.removeObject("component", "Test Component");
        assertNull(dbManager.getComponent("Test Component"));

        // Test removing a Category
        dbManager.removeObject("category", 1);
        assertNull(dbManager.getCategory(1));
    }

    @Test
    void testUpdateObject() {
        // Prepopulate with test data
        dbManager.addObject("concept", 1, "Test Concept", "Test Category", "Test Details");

        // Test updating a Concept
        dbManager.updateObject("concept", 1, "Updated Concept", "Updated Category", "Updated Details");
        Concept updatedConcept = dbManager.getConcept(1);
        assertNotNull(updatedConcept);
        assertEquals("Updated Concept", updatedConcept.getTopic());
        assertEquals("Updated Category", updatedConcept.getCategory());
        assertEquals("Updated Details", updatedConcept.getDetails());
    }
}
