// IDGenerator.java
package utils;

import java.util.UUID;

public class IDGenerator {
    public static String generateID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generateOrderID() {
        return "ORD-" + generateID();
    }

    public static String generatePaymentID() {
        return "PAY-" + generateID();
    }
}