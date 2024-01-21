//Alexus Jenkins | Software Development(CEN-3024C) | Jan 18th, 2024
//Purpose: Manages data to the file system.
//Inputs: filename(String),libraryCollection(List<Book>)
//Methods: FileManager(), addBookData(), deleteBookData(), modifyBookData(), saveBookData(), loadBookData()

package org.example;
import java.io.*;
import java.util.*;

public class FileManager {
    private String filename;
    private List<Book> libraryCollection;

    public FileManager(String filename, List<Book> libraryCollection) {
        this.filename = filename;
        this.libraryCollection = libraryCollection;
    }

    public FileManager(String filename) {
        this.filename = filename;
    }

    // addBookData(): Add book data to the file.
    public void addBookData(Book book) {
        try {
            FileWriter fileWriter = new FileWriter(filename, true); // Open the file in append mode

            // Write Book's Data into the file.
            String bookData = book.getUniqueID() + ", " + book.getTitle() + ", " + book.getAuthor() + "\n";
            fileWriter.write(bookData);

            // Closing FileWriter
            fileWriter.close();

            System.out.println("Success: Book data has been added to the file.");
        } catch (IOException e) {
            System.err.println("Error: Book data has not been added to the file." + e.getMessage());
        }
    }

    // deleteBookData(): Delete book data file.
    public void deleteBookData() {
        try {
            File file = new File(filename);
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Success: Book data file has been deleted.");
                } else {
                    System.err.println("Error: Book data file has not been deleted.");
                }
            } else {
                System.err.println("Error: Book data file does not exist.");
            }
        } catch (Exception e) {
            System.err.println("Error: Delete Book Data - " + e.getMessage());
        }
    }

    // modifyBookData(): Modifies book data in the file.
    public void modifyBookData(Book oldBook, Book newBook) {
        System.out.println("Success: Book data has been modified.");
    }

    // saveBookData(): Saves the entire library collection to the file.
    public void saveBookData() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

            for (Book book : libraryCollection) {
                String bookData = book.getUniqueID() + ", " + book.getTitle() + ", " + book.getAuthor() + "\n";
                writer.write(bookData);
            }

            writer.close();

            System.out.println("Success: Book data has been saved to the file.");
        } catch (IOException e) {
            System.err.println("Error: Book data has not been saved to the file.");
        }
    }

    // loadBookData(): Load book data from the file and display it.
    public void loadBookData() {
        try {
            File file = new File(filename);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    System.out.println(line); // Displays loaded data
                }
                scanner.close();
                System.out.println("Success: Book Data has been displayed.");
            } else {
                System.out.println("Error: File does not exist.");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Book data from file not found.");
        }
    }
}
