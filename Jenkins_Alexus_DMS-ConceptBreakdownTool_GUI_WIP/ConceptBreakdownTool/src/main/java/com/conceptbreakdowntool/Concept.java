package com.conceptbreakdowntool;

import java.util.*;

public class Concept {
    private int id;
    private String topic;
    private String category;
    private List<Component> components;
    private String details;

    public Concept(int id, String topic, String category, String details) {
        this.id = id;
        this.topic = topic;
        this.category = category;
        this.details = details; // Assign the details parameter to the details field
        this.components = new ArrayList<>();
    }


    // Getters and setters
    public int getId() { return id; }
    public String getTopic() { return topic; }
    public String getCategory() { return category; }
    public List<Component> getComponents() { return components; }
    public String getDetails() {
        return details;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Method to add components to this concept
    public void addComponent(Component component) {
        components.add(component);
    }

}