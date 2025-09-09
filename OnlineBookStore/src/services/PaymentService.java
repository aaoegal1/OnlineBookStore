// PaymentService.java
package services;

import models.Payment;
import models.Order;
import utils.FileHandler;
import utils.IDGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentService {
    private static final String PAYMENTS_FILE = "data/payments.csv";
    private List<Payment> payments;
    private OrderService orderService;

    public PaymentService(OrderService orderService) {
        this.orderService = orderService;
        loadPayments();
    }

    private void loadPayments() {
        payments = new ArrayList<>();
        List<String> lines = FileHandler.readFile(PAYMENTS_FILE);

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 6) {
                Payment payment = new Payment(
                        parts[0], parts[1], Double.parseDouble(parts[2]),
                        new Date(Long.parseLong(parts[3])), parts[4], parts[5]
                );
                payments.add(payment);
            }
        }
    }

    private void savePayments() {
        List<String> lines = payments.stream()
                .map(Payment::toString)
                .collect(Collectors.toList());
        FileHandler.writeFile(PAYMENTS_FILE, lines);
    }

    public Payment processPayment(String orderId, double amount, String paymentMethod) {
        Payment payment = new Payment(
                IDGenerator.generatePaymentID(),
                orderId,
                amount,
                new Date(),
                paymentMethod,
                "COMPLETED"
        );

        payments.add(payment);
        savePayments();

        // Update order status
        orderService.updateOrderStatus(orderId, "PROCESSING");

        return payment;
    }

    public List<Payment> getPaymentsByOrder(String orderId) {
        return payments.stream()
                .filter(payment -> payment.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }

    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments);
    }
}