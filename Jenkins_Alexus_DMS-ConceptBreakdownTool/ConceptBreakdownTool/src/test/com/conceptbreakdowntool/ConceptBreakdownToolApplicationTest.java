package com.conceptbreakdowntool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ConceptBreakdownToolApplicationTest {
    private final ConceptBreakdownToolApplication app = new ConceptBreakdownToolApplication();
    private DatabaseManager mockDbManager;
    private Scanner mockScanner;

    @BeforeEach
    void setUp() {
        mockDbManager = mock(DatabaseManager.class);
        mockScanner = mock(Scanner.class);
        app.setDbManager(mockDbManager); // Assuming a setter or some way to inject the mock
    }

    @Test
    void testAddObjects() {
        // Before each input sequence where a number is expected, add the following lines:
        // Assuming '123' is the expected valid integer input
        when(mockScanner.hasNextInt()).thenReturn(true);
        when(mockScanner.nextInt()).thenReturn(123);
        when(mockScanner.nextLine()).thenReturn("category", "Test Category"); // Adjust based on context

        // Continue with the rest of your test scenario...
        app.addObjects(mockScanner, mockDbManager);
        verify(mockDbManager, times(1)).addCategory(any(Category.class));

        // Reset mocks and set up for the next scenario...
        reset(mockScanner, mockDbManager);
    }

    @Test
    void testRemoveObjects() {
        // Simulate removing a category with valid integer input
        when(mockScanner.hasNextInt()).thenReturn(true); // Simulate that an integer will be entered
        when(mockScanner.nextInt()).thenReturn(123); // Simulate the actual integer entered by the user
        when(mockScanner.nextLine()).thenReturn("category", "123"); // Continue with user input simulation as needed
        app.removeObjects(mockScanner, mockDbManager);
        verify(mockDbManager, times(1)).deleteCategory(123); // Verify that deleteCategory was called with the correct ID

        // Reset mocks for the next scenario
        reset(mockScanner, mockDbManager);
    }

    @Test
    void testUpdateObjects() {
        // Simulate updating a concept
        when(mockScanner.nextLine())
                .thenReturn("concept") // For "What would you like to update?"
                .thenReturn("123") // For "Enter Concept ID for update:"
                .thenReturn("New Topic") // For "Enter new Topic:"
                .thenReturn("New Category") // For "Enter new Category:"
                .thenReturn("New Details"); // For "Enter new Details:"
        when(mockScanner.hasNextInt()).thenReturn(true); // Simulate scanner has an integer
        when(mockScanner.nextInt()).thenReturn(123); // Simulate the integer input for Concept ID

        when(mockDbManager.updateConcept(123, "New Topic", "New Details")).thenReturn(true);

        app.updateObjects(mockScanner, mockDbManager);

        // Verify the correct method call with expected arguments
        verify(mockDbManager).updateConcept(123, "New Topic", "New Details");
    }

    @Test
    void testRecommendDiagram() {
        // Assuming recommendDiagram now returns the name of the diagram
        String selectedDiagram = ConceptBreakdownToolApplication.recommendDiagram();

        // Verify the selected diagram is from the expected set
        Set<String> validDiagrams = new HashSet<>(Arrays.asList(ConceptBreakdownToolApplication.DIAGRAM_TYPES));
        boolean isValidDiagram = validDiagrams.contains(selectedDiagram);

        // Adding assertion message for clarity in case of test failure
        assertTrue(isValidDiagram, "The selected diagram should be one of the predefined types but was " + selectedDiagram);

        // Additionally, if you want to see the selected diagram during the test:
        System.out.println("Randomly selected diagram during the test: " + selectedDiagram);
    }
}
