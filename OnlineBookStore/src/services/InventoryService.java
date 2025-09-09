// InventoryService.java
package services;

import models.Book;
import exceptions.BookNotFoundException;
import exceptions.InsufficientStockException;
import utils.FileHandler;
import utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {
    private static final String BOOKS_FILE = "data/books.csv";
    private List<Book> books;

    public InventoryService() {
        loadBooks();
    }

    private void loadBooks() {
        books = new ArrayList<>();
        List<String> lines = FileHandler.readFile(BOOKS_FILE);

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 6) {
                Book book = new Book(
                        parts[0], parts[1], parts[2], parts[3],
                        Double.parseDouble(parts[4]), Integer.parseInt(parts[5])
                );
                books.add(book);
            }
        }
    }

    private void saveBooks() {
        List<String> lines = books.stream()
                .map(Book::toString)
                .collect(Collectors.toList());
        FileHandler.writeFile(BOOKS_FILE, lines);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public List<Book> searchBooks(String query) {
        return books.stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                book.getAuthor().toLowerCase().contains(query.toLowerCase()) ||
                                book.getGenre().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Book getBookById(String id) throws BookNotFoundException {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));
    }

    public void addBook(Book book) {
        book.setId(IDGenerator.generateID());
        books.add(book);
        saveBooks();
    }

    public void updateBook(Book updatedBook) throws BookNotFoundException {
        Book book = getBookById(updatedBook.getId());
        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setGenre(updatedBook.getGenre());
        book.setPrice(updatedBook.getPrice());
        book.setStockQuantity(updatedBook.getStockQuantity());
        saveBooks();
    }

    public void removeBook(String id) throws BookNotFoundException {
        Book book = getBookById(id);
        books.remove(book);
        saveBooks();
    }

    public void updateStock(String bookId, int quantity) throws BookNotFoundException, InsufficientStockException {
        Book book = getBookById(bookId);
        if (book.getStockQuantity() + quantity < 0) {
            throw new InsufficientStockException("Insufficient stock for book: " + book.getTitle());
        }
        book.setStockQuantity(book.getStockQuantity() + quantity);
        saveBooks();
    }
}