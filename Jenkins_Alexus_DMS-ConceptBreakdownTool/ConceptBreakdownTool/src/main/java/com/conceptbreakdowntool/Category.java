package com.conceptbreakdowntool;

/*
Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Feb 12th, 2024
 ClassName: Category

 Purpose: Represents, Organizes, and Manages individual components.

 Attributes:
 String topic: Main subject or theme of the 'Component'.
 String details: An extended description or information about the component.

 Methods:
 //Constructor
 Category(): Initializes a new instance of the 'Category' class with the provided topic and details.

 //Getters and Setters: Provides access and allows modification of the categories and attributes.
 getId(), setId()
 getTopic(), setTopic()
 */

//Constructor
//Category(): Initializes a new instance of the 'Category' class with the provided topic and details.
public class Category {
    private int id;
    private String topic;

    public Category(int id, String topic) {
        this.id = id;
        this.topic = topic;
    }

    //Getters and Setters: Provides access and allows modification of the categories and attributes.
    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) {
        this.topic = topic;
    }
}
