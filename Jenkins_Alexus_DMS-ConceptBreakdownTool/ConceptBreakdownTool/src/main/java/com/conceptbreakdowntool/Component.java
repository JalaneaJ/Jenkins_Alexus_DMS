package com.conceptbreakdowntool;
/*
Name: Alexus Jenkins
 Course: CEN 3042C
 Date: Feb 12th, 2024
 ClassName: Component

 Purpose: Represents, Organizes, and Manages individual components.

 Attributes:
 String topic: Main subject or theme of the 'Component'.
 String details: An extended description or information about the component.

 Methods:
 //Constructor
 Component(): Initializes a new instance of the 'Component' class with the provided topic and details.

 //Getters and Setters: Provides access and allows modification of the components and attributes.
 getTopic(), setTopic()
 getDetails(), setDetails()
 */

//Constructor
//Component(): Initializes a new instance of the 'Component' class with the provided topic and details.
public class Component {
    private String topic;
    private String details;

    public Component(String topic, String details) {
        this.topic = topic;
        this.details = details;
    }


    //Getters and Setters: Provides access and allows modification of the concepts and attributes.
    public String getTopic() { return topic; }
    public void setTopic(String topic) {
        this.topic = topic;
    }


    public String getDetails() { return details; }
    public void setDetails(String details) {
        this.details = details;
    }

}
