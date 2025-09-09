// UserService.java
package services;

import models.User;
import exceptions.UserNotFoundException;
import utils.FileHandler;
import utils.IDGenerator;
import utils.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private static final String USERS_FILE = "data/users.csv";
    private List<User> users;

    public UserService() {
        loadUsers();
    }

    private void loadUsers() {
        users = new ArrayList<>();
        List<String> lines = FileHandler.readFile(USERS_FILE);

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 6) {
                User user = new User(
                        parts[0], parts[1], parts[2], parts[3],
                        parts[4], Boolean.parseBoolean(parts[5])
                );
                users.add(user);
            }
        }
    }

    private void saveUsers() {
        List<String> lines = users.stream()
                .map(User::toString)
                .collect(Collectors.toList());
        FileHandler.writeFile(USERS_FILE, lines);
    }

    public User authenticate(String username, String password) throws UserNotFoundException {
        return users.stream()
                .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password"));
    }

    public User getUserById(String id) throws UserNotFoundException {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    public void registerUser(User user) throws IllegalArgumentException {
        if (!Validator.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!Validator.isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters with uppercase, lowercase, number and special character");
        }

        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (users.stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.setId(IDGenerator.generateID());
        user.setAdmin(false);
        users.add(user);
        saveUsers();
    }

    public void updateUser(User updatedUser) throws UserNotFoundException {
        User user = getUserById(updatedUser.getId());
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setAddress(updatedUser.getAddress());
        saveUsers();
    }
}