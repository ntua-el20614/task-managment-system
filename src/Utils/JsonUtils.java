package Utils;

import Models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JsonUtils.java
 * 
 * Utility class for handling JSON serialization and deserialization.
 */
public class JsonUtils {
    private static final String FILE_PATH = "medialab/users.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Reads all users from the users.json file.
     * 
     * @return List of users.
     */
    public static List<User> readUsers() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
            List<User> users = gson.fromJson(reader, userListType);
            if (users == null) {
                users = new ArrayList<>();
            }
            return users;
        } catch (FileNotFoundException e) {
            // If file doesn't exist, return empty list
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Writes all users to the users.json file.
     * 
     * @param users List of users to write.
     */
    public static void writeUsers(List<User> users) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds a user by username.
     * 
     * @param username Username to search for.
     * @return User object if found, null otherwise.
     */
    public static User findUser(String username) {
        List<User> users = readUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Adds a new user to the users.json file.
     * 
     * @param newUser The new user to add.
     * @return True if added successfully, false if username already exists.
     */
    public static boolean addUser(User newUser) {
        List<User> users = readUsers();
        // Check if username already exists
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(newUser.getUsername())) {
                return false; // Username already exists
            }
        }
        users.add(newUser);
        writeUsers(users);
        return true;
    }

    /**
     * Updates an existing user in the users.json file.
     * 
     * @param updatedUser The user with updated information.
     */
    public static void updateUser(User updatedUser) {
        List<User> users = readUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                break;
            }
        }
        writeUsers(users);
    }
}
