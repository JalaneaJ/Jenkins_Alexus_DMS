/**
 Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Apr 11th 2024
 ClassName: Concept

 Purpose: Represents, Organizes, and Manages individual concepts.

 @author Alexus Jenkins
 @version 5.0
 **/

package com.conceptbreakdowntool;

import java.util.*;

public class Concept {
    private int id;
    private String topic;
    private String category;
    private List<Component> components;
    private String details;

    /**Constructor(Concept): Creates a new concept instance with specific details.
     * @param id: the unique id for the concept
     * @param topic: the main topic or title of the concept
     * @param category: the category to which the concept belongs to
     * @param details: Additional descriptive details about the concept.
     **/
    public Concept(int id, String topic, String category, String details) {
        this.id = id;
        this.topic = topic;
        this.category = category;
        this.details = details; // Assign the details parameter to the details field
        this.components = new ArrayList<>();
    }


    /**GETTERS AND SETTERS **/
    /**getId(): Gets the id for the concept.
     * @return the concept's id **/
    public int getId() { return id; }
    /**setId(): Sets the id for the concept.
     * @param id
     **/
    public void setId(int id) {
        this.id = id;
    }
    /**
     getTopic(): Gets the topic of the concept.
     @return the topic of the concept
     **/
    public String getTopic() { return topic; }
    /**
     getCategory(): Gets the category to which the concept belongs.
     @return the category name
     **/
    public String getCategory() { return category; }
    /**
     getComponents(): Gets the list of components related to the concept.
     **/
    public List<Component> getComponents() { return components; }
    /**
     getDetails(): Gets the detailed information about this concept.
     **/
    public String getDetails() {
        return details;
    }

}