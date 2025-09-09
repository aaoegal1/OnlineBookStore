// OrderService.java
package services;

import models.Order;
import models.CartItem;
import exceptions.BookNotFoundException;
import exceptions.InsufficientStockException;
import exceptions.UserNotFoundException;
import utils.FileHandler;
import utils.IDGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class OrderService {
    private static final String ORDERS_FILE = "data/orders.csv";
    private List<Order> orders;
    private InventoryService inventoryService;
    private UserService userService;

    public OrderService(InventoryService inventoryService, UserService userService) {
        this.inventoryService = inventoryService;
        this.userService = userService;
        loadOrders();
    }

    private void loadOrders() {
        orders = new ArrayList<>();
        List<String> lines = FileHandler.readFile(ORDERS_FILE);

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                String id = parts[0];
                String userId = parts[1];
                double totalAmount = Double.parseDouble(parts[2]);
                Date orderDate = new Date(Long.parseLong(parts[3]));
                String status = parts[4];

                List<CartItem> items = new ArrayList<>();
                if (parts.length > 5) {
                    String[] itemParts = parts[5].split(";");
                    for (String itemStr : itemParts) {
                        String[] itemDetails = itemStr.split(":");
                        if (itemDetails.length == 2) {
                            try {
                                String bookId = itemDetails[0];
                                int quantity = Integer.parseInt(itemDetails[1]);
                                double price = inventoryService.getBookById(bookId).getPrice();
                                items.add(new CartItem(bookId, quantity, price));
                            } catch (BookNotFoundException e) {
                                System.out.println("Warning: Book not found for order item: " + e.getMessage());
                            }
                        }
                    }
                }

                Order order = new Order(id, userId, items, totalAmount, orderDate, status);
                orders.add(order);
            }
        }
    }

    private void saveOrders() {
        List<String> lines = orders.stream()
                .map(Order::toString)
                .collect(Collectors.toList());
        FileHandler.writeFile(ORDERS_FILE, lines);
    }

    public Order createOrder(String userId, List<CartItem> items)
            throws UserNotFoundException, BookNotFoundException, InsufficientStockException {

        // Verify user exists
        userService.getUserById(userId);

        // Check stock and calculate total
        double totalAmount = 0;
        for (CartItem item : items) {
            inventoryService.updateStock(item.getBookId(), -item.getQuantity());
            totalAmount += item.getTotalPrice();
        }

        // Create order
        Order order = new Order(
                IDGenerator.generateOrderID(),
                userId,
                items,
                totalAmount,
                new Date(),
                "PENDING"
        );

        orders.add(order);
        saveOrders();
        return order;
    }

    public List<Order> getUserOrders(String userId) {
        return orders.stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public void updateOrderStatus(String orderId, String status) {
        orders.stream()
                .filter(order -> order.getId().equals(orderId))
                .findFirst()
                .ifPresent(order -> {
                    order.setStatus(status);
                    saveOrders();
                });
    }

    public void cancelOrder(String orderId) throws BookNotFoundException, InsufficientStockException {
        Order order = orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElse(null);

        if (order != null && !order.getStatus().equals("CANCELLED")) {
            // Restore stock
            for (CartItem item : order.getItems()) {
                inventoryService.updateStock(item.getBookId(), item.getQuantity());
            }

            order.setStatus("CANCELLED");
            saveOrders();
        }
    }
}