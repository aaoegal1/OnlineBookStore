package models;

public class CartItem {
    // CartItem.java

        private String bookId;
        private int quantity;
        private double price;

        public CartItem(String bookId, int quantity, double price) {
            this.bookId = bookId;
            this.quantity = quantity;
            this.price = price;
        }

        // Getters and setters
        public String getBookId() { return bookId; }
        public void setBookId(String bookId) { this.bookId = bookId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public double getTotalPrice() {
            return price * quantity;
        }
    }

