// Order.java
package models;

import java.util.Date;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private List<CartItem> items;
    private double totalAmount;
    private Date orderDate;
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED

    public Order(String id, String userId, List<CartItem> items, double totalAmount, Date orderDate, String status) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(",").append(userId).append(",").append(totalAmount).append(",")
                .append(orderDate.getTime()).append(",").append(status);

        for (CartItem item : items) {
            sb.append(";").append(item.getBookId()).append(":").append(item.getQuantity());
        }

        return sb.toString();
    }
}