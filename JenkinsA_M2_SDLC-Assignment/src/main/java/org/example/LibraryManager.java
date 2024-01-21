//Alexus Jenkins | Software Development(CEN-3024C) | Jan 18th, 2024
//Purpose: Manage the Library Collection.
//Inputs: N/A
//Methods: LibraryManager(), addBook(), removeBook(), listBooks()


package org.example;
import java.util.*;

public class LibraryManager {
    private List<Book> libraryCollection;

    // Constructor
    public LibraryManager() {
        libraryCollection = new ArrayList<>();
    }

    // addBook(): Adds a Book to the Library Collection
    public void addBook(Book book) {
        // Check if unique_ID is not a positive integer
        if (book.getUniqueID() <= 0) {
            throw new IllegalArgumentException("Error: Unique ID must be a positive integer.");
        }

        // Check if unique_ID only contains numbers
        if (!String.valueOf(book.getUniqueID()).matches("\\d+")) {
            throw new IllegalArgumentException("Error: Unique ID must only contain numbers.");
        }

        // Check if title or author contain digits
        if (book.getTitle().matches(".*\\d+.*") || book.getAuthor().matches(".*\\d+.*")) {
            throw new IllegalArgumentException("Error: Title and Author cannot contain digits.");
        }

        libraryCollection.add(book);
    }

    // removeBook(): Removes a Book from the Library Collection
    public void removeBook(int unique_ID) {
        Iterator<Book> iterator = libraryCollection.iterator();
        boolean removed = false;

        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getUniqueID() == unique_ID) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        // Check if the book was removed
        if (!removed) {
            try {
                throw new InvalidUniqueIDException("Error: Unique ID " + unique_ID + " does not exist.");
            } catch (InvalidUniqueIDException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // listBooks(): Lists all the Books in the Library Collection
    public void listBooks() {
        if (libraryCollection.isEmpty()) {
            System.out.println("The library collection is empty.");
        } else {
            System.out.println("Library Collection:");
            for (Book book : libraryCollection) {
                System.out.println("Unique ID: " + book.getUniqueID());
                System.out.println("Title: " + book.getTitle());
                System.out.println("Author: " + book.getAuthor() + "\n");
            }
        }
    }
}
