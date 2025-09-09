// Main.java
import models.*;
import services.*;
import exceptions.*;
import utils.*;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static InventoryService inventoryService;
    private static UserService userService;
    private static OrderService orderService;
    private static PaymentService paymentService;
    private static User currentUser;

    public static void main(String[] args) {
        initializeServices();
        showWelcomeScreen();
    }

    private static void initializeServices() {
        inventoryService = new InventoryService();
        userService = new UserService();
        orderService = new OrderService(inventoryService, userService);
        paymentService = new PaymentService(orderService);
    }

    private static void showWelcomeScreen() {
        while (true) {
            System.out.println("\n=== Welcome to Online Book Store ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Browse Books");
            System.out.println("4. Exit");
            System.out.print("Please choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    browseBooks();
                    break;
                case 4:
                    System.out.println("Thank you for visiting. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            currentUser = userService.authenticate(username, password);
            System.out.println("Login successful! Welcome, " + currentUser.getUsername());

            if (currentUser.isAdmin()) {
                showAdminMenu();
            } else {
                showCustomerMenu();
            }
        } catch (UserNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void register() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();

        try {
            User newUser = new User(null, username, password, email, address, false);
            userService.registerUser(newUser);
            System.out.println("Registration successful! You can now login.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void browseBooks() {
        List<Book> books = inventoryService.getAllBooks();
        displayBooks(books);

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void showCustomerMenu() {
        List<CartItem> cart = new ArrayList<>();

        while (true) {
            System.out.println("\n=== Customer Menu ===");
            System.out.println("1. Browse Books");
            System.out.println("2. Search Books");
            System.out.println("3. View Cart");
            System.out.println("4. Checkout");
            System.out.println("5. View Orders");
            System.out.println("6. Logout");
            System.out.print("Please choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    browseBooksAndAddToCart(cart);
                    break;
                case 2:
                    searchBooksAndAddToCart(cart);
                    break;
                case 3:
                    viewCart(cart);
                    break;
                case 4:
                    checkout(cart);
                    break;
                case 5:
                    viewOrders();
                    break;
                case 6:
                    currentUser = null;
                    cart.clear();
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void browseBooksAndAddToCart(List<CartItem> cart) {
        List<Book> books = inventoryService.getAllBooks();
        displayBooks(books);

        System.out.print("Enter book ID to add to cart (or 0 to go back): ");
        String bookId = scanner.nextLine();

        if (!bookId.equals("0")) {
            try {
                Book book = inventoryService.getBookById(bookId);
                System.out.print("Enter quantity: ");
                int quantity = getIntInput();

                if (quantity > 0 && quantity <= book.getStockQuantity()) {
                    // Check if book already in cart
                    Optional<CartItem> existingItem = cart.stream()
                            .filter(item -> item.getBookId().equals(bookId))
                            .findFirst();

                    if (existingItem.isPresent()) {
                        existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
                    } else {
                        cart.add(new CartItem(bookId, quantity, book.getPrice()));
                    }

                    System.out.println("Added to cart: " + book.getTitle());
                } else {
                    System.out.println("Invalid quantity or insufficient stock.");
                }
            } catch (BookNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void searchBooksAndAddToCart(List<CartItem> cart) {
        System.out.print("Enter search query: ");
        String query = scanner.nextLine();

        List<Book> results = inventoryService.searchBooks(query);
        displayBooks(results);

        if (!results.isEmpty()) {
            System.out.print("Enter book ID to add to cart (or 0 to go back): ");
            String bookId = scanner.nextLine();

            if (!bookId.equals("0")) {
                try {
                    Book book = inventoryService.getBookById(bookId);
                    System.out.print("Enter quantity: ");
                    int quantity = getIntInput();

                    if (quantity > 0 && quantity <= book.getStockQuantity()) {
                        // Check if book already in cart
                        Optional<CartItem> existingItem = cart.stream()
                                .filter(item -> item.getBookId().equals(bookId))
                                .findFirst();

                        if (existingItem.isPresent()) {
                            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
                        } else {
                            cart.add(new CartItem(bookId, quantity, book.getPrice()));
                        }

                        System.out.println("Added to cart: " + book.getTitle());
                    } else {
                        System.out.println("Invalid quantity or insufficient stock.");
                    }
                } catch (BookNotFoundException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private static void viewCart(List<CartItem> cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        System.out.println("\n=== Your Cart ===");
        double total = 0;

        for (CartItem item : cart) {
            try {
                Book book = inventoryService.getBookById(item.getBookId());
                double itemTotal = item.getTotalPrice();
                total += itemTotal;

                System.out.printf("%s by %s - $%.2f x %d = $%.2f%n",
                        book.getTitle(), book.getAuthor(),
                        book.getPrice(), item.getQuantity(), itemTotal);
            } catch (BookNotFoundException e) {
                System.out.println("Error: Book not found for cart item");
            }
        }

        System.out.printf("Total: $%.2f%n", total);

        System.out.println("\n1. Update quantities");
        System.out.println("2. Remove items");
        System.out.println("3. Back to menu");
        System.out.print("Choose an option: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                updateCartQuantities(cart);
                break;
            case 2:
                removeFromCart(cart);
                break;
            case 3:
                // Just go back
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void updateCartQuantities(List<CartItem> cart) {
        System.out.print("Enter book ID to update: ");
        String bookId = scanner.nextLine();

        Optional<CartItem> item = cart.stream()
                .filter(i -> i.getBookId().equals(bookId))
                .findFirst();

        if (item.isPresent()) {
            try {
                Book book = inventoryService.getBookById(bookId);
                System.out.print("Enter new quantity: ");
                int newQuantity = getIntInput();

                if (newQuantity > 0 && newQuantity <= book.getStockQuantity() + item.get().getQuantity()) {
                    item.get().setQuantity(newQuantity);
                    System.out.println("Quantity updated.");
                } else {
                    System.out.println("Invalid quantity or insufficient stock.");
                }
            } catch (BookNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Book not found in cart.");
        }
    }

    private static void removeFromCart(List<CartItem> cart) {
        System.out.print("Enter book ID to remove: ");
        String bookId = scanner.nextLine();

        boolean removed = cart.removeIf(item -> item.getBookId().equals(bookId));

        if (removed) {
            System.out.println("Item removed from cart.");
        } else {
            System.out.println("Book not found in cart.");
        }
    }

    private static void checkout(List<CartItem> cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty. Nothing to checkout.");
            return;
        }

        // Display cart summary
        viewCart(cart);

        System.out.print("Proceed to checkout? (y/n): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("y")) {
            try {
                Order order = orderService.createOrder(currentUser.getId(), cart);

                System.out.println("\nOrder created successfully!");
                System.out.println("Order ID: " + order.getId());
                System.out.printf("Total: $%.2f%n", order.getTotalAmount());

                // Process payment
                System.out.println("\n=== Payment ===");
                System.out.println("1. Credit Card");
                System.out.println("2. Debit Card");
                System.out.println("3. PayPal");
                System.out.print("Choose payment method: ");

                int methodChoice = getIntInput();
                String paymentMethod = "UNKNOWN";

                switch (methodChoice) {
                    case 1: paymentMethod = "CREDIT_CARD"; break;
                    case 2: paymentMethod = "DEBIT_CARD"; break;
                    case 3: paymentMethod = "PAYPAL"; break;
                }

                Payment payment = paymentService.processPayment(
                        order.getId(), order.getTotalAmount(), paymentMethod);

                System.out.println("Payment processed successfully!");
                System.out.println("Payment ID: " + payment.getId());

                // Clear cart
                cart.clear();

            } catch (UserNotFoundException | BookNotFoundException | InsufficientStockException e) {
                System.out.println("Error during checkout: " + e.getMessage());
            }
        }
    }

    private static void viewOrders() {
        List<Order> orders = orderService.getUserOrders(currentUser.getId());

        if (orders.isEmpty()) {
            System.out.println("You have no orders yet.");
            return;
        }

        System.out.println("\n=== Your Orders ===");
        for (Order order : orders) {
            System.out.println("\nOrder ID: " + order.getId());
            System.out.println("Date: " + order.getOrderDate());
            System.out.println("Status: " + order.getStatus());
            System.out.printf("Total: $%.2f%n", order.getTotalAmount());

            System.out.println("Items:");
            for (CartItem item : order.getItems()) {
                try {
                    Book book = inventoryService.getBookById(item.getBookId());
                    System.out.printf("  %s by %s - $%.2f x %d%n",
                            book.getTitle(), book.getAuthor(), item.getPrice(), item.getQuantity());
                } catch (BookNotFoundException e) {
                    System.out.println("  [Book details unavailable]");
                }
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void showAdminMenu() {
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Manage Books");
            System.out.println("2. View All Orders");
            System.out.println("3. View All Payments");
            System.out.println("4. Logout");
            System.out.print("Please choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    manageBooks();
                    break;
                case 2:
                    viewAllOrders();
                    break;
                case 3:
                    viewAllPayments();
                    break;
                case 4:
                    currentUser = null;
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void manageBooks() {
        while (true) {
            System.out.println("\n=== Book Management ===");
            System.out.println("1. View All Books");
            System.out.println("2. Add New Book");
            System.out.println("3. Update Book");
            System.out.println("4. Delete Book");
            System.out.println("5. Back to Admin Menu");
            System.out.print("Please choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    displayBooks(inventoryService.getAllBooks());
                    break;
                case 2:
                    addNewBook();
                    break;
                case 3:
                    updateBook();
                    break;
                case 4:
                    deleteBook();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addNewBook() {
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Author: ");
        String author = scanner.nextLine();
        System.out.print("Genre: ");
        String genre = scanner.nextLine();
        System.out.print("Price: ");
        double price = getDoubleInput();
        System.out.print("Stock Quantity: ");
        int stock = getIntInput();

        Book newBook = new Book(null, title, author, genre, price, stock);
        inventoryService.addBook(newBook);

        System.out.println("Book added successfully!");
    }

    private static void updateBook() {
        displayBooks(inventoryService.getAllBooks());

        System.out.print("Enter book ID to update: ");
        String bookId = scanner.nextLine();

        try {
            Book book = inventoryService.getBookById(bookId);

            System.out.print("Title (" + book.getTitle() + "): ");
            String title = scanner.nextLine();
            if (!title.isEmpty()) book.setTitle(title);

            System.out.print("Author (" + book.getAuthor() + "): ");
            String author = scanner.nextLine();
            if (!author.isEmpty()) book.setAuthor(author);

            System.out.print("Genre (" + book.getGenre() + "): ");
            String genre = scanner.nextLine();
            if (!genre.isEmpty()) book.setGenre(genre);

            System.out.print("Price (" + book.getPrice() + "): ");
            String priceStr = scanner.nextLine();
            if (!priceStr.isEmpty()) book.setPrice(Double.parseDouble(priceStr));

            System.out.print("Stock Quantity (" + book.getStockQuantity() + "): ");
            String stockStr = scanner.nextLine();
            if (!stockStr.isEmpty()) book.setStockQuantity(Integer.parseInt(stockStr));

            inventoryService.updateBook(book);
            System.out.println("Book updated successfully!");

        } catch (BookNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteBook() {
        displayBooks(inventoryService.getAllBooks());

        System.out.print("Enter book ID to delete: ");
        String bookId = scanner.nextLine();

        try {
            inventoryService.removeBook(bookId);
            System.out.println("Book deleted successfully!");
        } catch (BookNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();

        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        System.out.println("\n=== All Orders ===");
        for (Order order : orders) {
            System.out.println("\nOrder ID: " + order.getId());
            try {
                User user = userService.getUserById(order.getUserId());
                System.out.println("Customer: " + user.getUsername());
            } catch (UserNotFoundException e) {
                System.out.println("Customer: [Unknown]");
            }
            System.out.println("Date: " + order.getOrderDate());
            System.out.println("Status: " + order.getStatus());
            System.out.printf("Total: $%.2f%n", order.getTotalAmount());

            System.out.println("Items:");
            for (CartItem item : order.getItems()) {
                try {
                    Book book = inventoryService.getBookById(item.getBookId());
                    System.out.printf("  %s by %s - $%.2f x %d%n",
                            book.getTitle(), book.getAuthor(), item.getPrice(), item.getQuantity());
                } catch (BookNotFoundException e) {
                    System.out.println("  [Book details unavailable]");
                }
            }

            // Show update option for admins
            if (!order.getStatus().equals("CANCELLED") && !order.getStatus().equals("COMPLETED")) {
                System.out.println("1. Update status");
                System.out.println("2. Cancel order");
                System.out.println("3. Next order");
                System.out.print("Choose an option: ");

                int option = getIntInput();

                if (option == 1) {
                    System.out.println("1. PROCESSING");
                    System.out.println("2. COMPLETED");
                    System.out.print("Choose new status: ");

                    int statusChoice = getIntInput();
                    String newStatus = "PROCESSING";

                    if (statusChoice == 2) newStatus = "COMPLETED";

                    orderService.updateOrderStatus(order.getId(), newStatus);
                    System.out.println("Order status updated.");

                } else if (option == 2) {
                    try {
                        orderService.cancelOrder(order.getId());
                        System.out.println("Order cancelled.");
                    } catch (BookNotFoundException | InsufficientStockException e) {
                        System.out.println("Error cancelling order: " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void viewAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();

        if (payments.isEmpty()) {
            System.out.println("No payments found.");
            return;
        }

        System.out.println("\n=== All Payments ===");
        for (Payment payment : payments) {
            System.out.println("\nPayment ID: " + payment.getId());
            System.out.println("Order ID: " + payment.getOrderId());
            System.out.printf("Amount: $%.2f%n", payment.getAmount());
            System.out.println("Date: " + payment.getPaymentDate());
            System.out.println("Method: " + payment.getPaymentMethod());
            System.out.println("Status: " + payment.getStatus());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void displayBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        System.out.println("\n=== Books ===");
        System.out.printf("%-8s %-30s %-20s %-15s %-8s %-5s%n",
                "ID", "Title", "Author", "Genre", "Price", "Stock");
        System.out.println("-----------------------------------------------------------------------------");

        for (Book book : books) {
            System.out.printf("%-8s %-30s %-20s %-15s $%-7.2f %-5d%n",
                    book.getId(),
                    truncate(book.getTitle(), 28),
                    truncate(book.getAuthor(), 18),
                    truncate(book.getGenre(), 13),
                    book.getPrice(),
                    book.getStockQuantity());
        }
    }

    private static String truncate(String str, int length) {
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }

    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}