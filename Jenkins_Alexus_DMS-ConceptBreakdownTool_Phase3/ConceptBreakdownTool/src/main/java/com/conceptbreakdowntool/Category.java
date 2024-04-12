package com.conceptbreakdowntool;

/**
Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Apr 11th, 2024
 ClassName: Category

 Purpose: Represents, Organizes, and Manages individual categories.

 @author Alexus Jenkins
 @version 5.0
 **/

public class Category {
    private int id;
    private String topic;

    /**Constructor(Component): Creates a new component instance with specific details.
     * @param id: the id of the category
     * @param topic: the main topic of the category
     **/
    public Category(int id, String topic) {
        this.id = id;
        this.topic = topic;
    }

    /**GETTERS AND SETTERS **/
    /**getId(): Gets the id for the category.
     * @return the category's id **/

    public int getId() { return id; }

    /**getTopic(): Gets the topic for the category.
     * @return the category's topic **/
    public String getTopic() { return topic; }

    /**setTopic(): Sets the topic for the component.
     * @param topic: the topic of the component
     **/
    public void setTopic(String topic) {
        this.topic = topic;
    }
}
