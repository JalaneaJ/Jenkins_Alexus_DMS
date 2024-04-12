package com.conceptbreakdowntool;
/**
Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Apr 11th, 2024
 ClassName: Component

 Purpose: Represents, Organizes, and Manages individual components.

 @author Alexus Jenkins
 @version 5.0
 **/

public class Component {
    private String topic;
    private String details;
    private int conceptId;

    /**Constructor(Component): Creates a new component instance with specific details.
     * @param topic: the main topic or title of the concept
     * @param details: Additional descriptive details about the concept.
     * @param conceptId: The ID of the concept this component is associated with.
     **/
    public Component(String topic, String details, int conceptId) {
        this.topic = topic;
        this.details = details;
        this.conceptId = conceptId;
    }


    /**GETTERS AND SETTERS **/
    /**getTopic(): Gets the topic for the component.
     * @return the component's topic **/
    public String getTopic() { return topic; }
    /**
     * setTopic(): sets the topic for the component.
     * @param topic the new topic for the component.
     * **/
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**getDetails(): Gets the topic for the component.
     * @return the component's details **/
    public String getDetails() { return details; }

    /**setDetails(): Set the details for the component.
     * @param details the detailed info about the component
     **/
    public void setDetails(String details) {
        this.details = details;
    }

    /**getConceptId(): Gets the concept id for the component.
     * @return the concept's id **/
    public int getConceptId() {
        return conceptId;
    }

    /**toString(): Returns a string representation of the component's topic.
     * @return the component's topic **/
    @Override
    public String toString() {
        return this.getTopic();
    }
}
