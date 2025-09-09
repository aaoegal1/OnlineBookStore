// User.java
package models;

public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private String address;
    private boolean isAdmin;

    public User(String id, String username, String password, String email, String address, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.address = address;
        this.isAdmin = isAdmin;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%b",
                id, username, password, email, address, isAdmin);
    }
}