package org.example;

//Alexus Jenkins | Software Development(CEN-3024C) | Jan 18th, 2024
//Purpose: Provide a demonstration of how manage and display book data using the 'Book', 'LibraryManager', and 'FileManager' classes.
//Program Objective: The Management of Book Data within a library collection.
//Inputs: filename (String), book1, book2
//Methods: main(), Book(), getUniqueID(), getTitle(), getAuthor(), addBook(), listBooks(), removeBook(), FileManager(), loadBookData(), saveBookData()
//Instructions: To disable commands use "/* */" around the command.
    // For example:
        /*
        //Add the sample book to the library collection
            libraryManager.addBook(book1);
        */


public class LibraryManagementSystemApplication{
    public static void main(String[] args){
        //Create a sample book
        Book book1 = new Book(1001, "The Last Great American Dynasty", "Taylor Swift");

        //Display Book Details
        displayBookDetails(book1);

        //Create LibraryManager Instance: Manages and Organizes the library's collection of books
        LibraryManager libraryManager = createLibraryManager();

        //Add the sample book to the library collection
        libraryManager.addBook(book1);

        //Display the list of books in the collection
        System.out.println("\n ##Library Collection ");
        libraryManager.listBooks();

        //Remove the book from the library
        libraryManager.removeBook(book1.getUniqueID());

        //Display the updated list of books in the library
        /* System.out.println("\nUpdated Library Collection: ");
        libraryManager.listBooks();*/

        //Specify the filename for storing book data
        String filename = "libraryData.txt";

        //Create a FileManager Instance: Handles input and output operations related to book data files.
        FileManager fileManager = new FileManager(filename);

        //Load book data from the file and display it
        System.out.println("\nLoaded Book Data from the File: ");
        fileManager.loadBookData();
    }

    //displayBookDetails(): Displaying the details of a specific book
    public static void displayBookDetails(Book book){
        System.out.println("##Book Details ");
        System.out.println("Unique ID: " + book.getUniqueID());
        System.out.println("Title: " + book.getTitle());
        System.out.println("Author: " + book.getAuthor());
    }

    //createLibraryManager(): Creates and returns a new instance of the 'LibraryManager' class
    public static LibraryManager createLibraryManager() {
        return new LibraryManager();
    }

    //manageLibrary(): Adds books to the Library, Lists the books in the Library collection, Removes books from the Library Colllection, and Updates the Library Collection
    public static void manageLibrary(LibraryManager libraryManager, Book book) {
        libraryManager.addBook(book);

        /* System.out.println("\nLibrary Collection: ");
        libraryManager.listBooks(); */

        libraryManager.removeBook(book.getUniqueID());

        /* System.out.println("\nUpdated Library Collection: ");
        libraryManager.listBooks(); */
    }

    // displayLibraryData(): Displays loaded book data from the file
    public static void displayLibraryData(FileManager fileManager) {
        System.out.println("\nLoaded Book Data from the File: ");
        fileManager.loadBookData();
    }

}

