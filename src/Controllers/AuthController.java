package Controllers;

import Models.User;
import Utils.JsonUtils;
import Utils.PasswordUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * AuthController.java
 * 
 * Handles user authentication and registration.
 */
public class AuthController {

    /**
     * Registers a new user.
     * 
     * @param username The desired username.
     * @param password The desired password.
     * @return True if registration is successful, false if username already exists.
     */
    public static boolean register(String username, String password) {
        try {
            // Check if user already exists
            if (JsonUtils.findUser(username) != null) {
                return false; // User exists
            }

            // Hash the password
            String hashedPassword = PasswordUtils.hashPassword(password);

            // Create new user with default categories and priorities
            User newUser = new User(username, hashedPassword);
            newUser.setCategories(new ArrayList<>(Arrays.asList("Default Category")));
            newUser.setPriorities(new ArrayList<>(Arrays.asList("Default")));

            // Add user to JSON
            return JsonUtils.addUser(newUser);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticates a user.
     * 
     * @param username The username.
     * @param password The password.
     * @return User object if authentication is successful, null otherwise.
     */
    public static User login(String username, String password) {
        try {
            User user = JsonUtils.findUser(username);
            if (user != null) {
                if (PasswordUtils.checkPassword(password, user.getPasswordHash())) {
                    return user; // Successful login
                }
            }
            return null; // Login failed
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
