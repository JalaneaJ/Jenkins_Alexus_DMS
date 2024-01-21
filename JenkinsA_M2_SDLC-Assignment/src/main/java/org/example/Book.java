package org.example;

//Alexus Jenkins | Software Development(CEN-3024C) | Jan 18th, 2024
//Purpose: Represents the books within the library collection.
//Inputs: unique_ID(int), title(String), author(String)
//Methods: Book(), String getDetails()

public class Book{
    //Field Attributes
    private int unique_ID; //Book Identification Number
    private String title; //Title of the Book
    private String author; //Author of the Book

    //Constructor
    public Book(int unique_ID, String title, String author){
        this.unique_ID = unique_ID;
        this.title = title;
        this.author = author;
    }
    //Getter - Book Details
    public String getDetails(){
        return "Unique ID: " + unique_ID + "\nTitle: " + title + "\nAuthor: " + author;
    }
    //Getter - Unique ID
    public int getUniqueID() {
        return unique_ID;
    }

    //Getter - getTitle
    public String getTitle() {
        return title;
    }

    //Getter - getAuthor
    public String getAuthor() {
        return author;
    }
    
    //Setter - Author: Update the Author of a Book
    public void setAuthor(String author){
        this.author = author;
    }

    //Setter - Title: Change the title of a Book
    public void setTitle(String title) {
        this.title = title;
    }




}
				