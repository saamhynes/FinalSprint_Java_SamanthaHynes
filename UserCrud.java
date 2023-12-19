// Task 1: User registration and login: Users can create accounts and securely log in to the system.

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.Scanner;

public class UserCrud {

    public boolean createUser(User user, int doctorId) {
        boolean success = false;
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        System.out.println("Length of hashed password: " + hashedPassword.length());

        String query = "INSERT INTO users (firstName, lastName, email, password, isDoctor, doctor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, hashedPassword);
            statement.setBoolean(5, user.isDoctor());
            statement.setInt(6, doctorId);

            int updatedRows = statement.executeUpdate();
            if (updatedRows != 0) {
                success = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    public User getUserById(int id) {
        User user = null;
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String email = rs.getString("email");
                String password = rs.getString("password");
                boolean isDoctor = rs.getBoolean("isDoctor");

                user = new User(id, firstName, lastName, email, password, isDoctor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User getUserByEmail(String email) {
        User user = null;
        String query = "SELECT * FROM users where email = ?";

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {

            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String password = rs.getString("password");
                boolean isDoctor = rs.getBoolean("isDoctor");

                user = new User(id, firstName, lastName, email, password, isDoctor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean updateUser(User user) {
        boolean success = false;
        String query = "UPDATE users SET firstName = ?, lastName = ?, email = ?, password = ?, isDoctor = ? WHERE id = ?";

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setBoolean(5, user.isDoctor());
            statement.setInt(6, user.getId());

            int updatedRows = statement.executeUpdate();
            if (updatedRows != 0) {
                success = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean deleteUser(String email) {
        boolean success = false;
        String query = "DELETE FROM users WHERE email = ?";

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {

            statement.setString(1, email);

            int updatedRows = statement.executeUpdate();
            if (updatedRows != 0) {
                success = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean verifyPassword(String email, String password) {
        boolean verified = false;
        String hashedPasswordFromDB = "";

        String query = "SELECT password FROM users WHERE email = ?";

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {

            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                hashedPasswordFromDB = rs.getString("password");
            }

            if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                verified = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return verified;
    }

    // Instead of having a huge health monitoring app I thought it would be easier
    // to create all methods here and call them in the main file instead

    // Create the User Menu
    public void createUser(Scanner scanner) {
        System.out.println("Enter your first name:");
        String firstName = scanner.nextLine();

        System.out.println("Enter your last name:");
        String lastName = scanner.nextLine();

        System.out.println("Enter your email:");
        String email = scanner.nextLine();

        System.out.println("Enter your password:");
        String password = scanner.nextLine();

        System.out.println("Are you a doctor? (true/false)");
        boolean isDoctor = scanner.nextBoolean();

        System.out.println("Enter your doctor's ID:");
        int doctorId = scanner.nextInt();
        scanner.nextLine();

        User newUser = new User(-1, firstName, lastName, email, password, isDoctor);
        boolean success = createUser(newUser, doctorId);

        if (success) {
            System.out.println("User created successfully. Please login.");
        } else {
            System.out.println("Failed to create user, please try again.");
        }
    }

    // Get the user info by email Menu
    public void getUserByEmailMenu(Scanner scanner) {
        System.out.println("Enter the email of the user:");
        String email = scanner.nextLine();

        User user = getUserByEmail(email);

        if (user != null) {
            System.out.println("User details:");
            System.out.println("ID: " + user.getId());
            System.out.println("First name: " + user.getFirstName());
            System.out.println("Last name: " + user.getLastName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Is a Doctor: " + user.isDoctor());
        } else {
            System.out.println("No user found with email " + email);
        }
    }

    // Update the user info menu
    public void updateUserMenu(Scanner scanner) {
        System.out.println("Enter the email of the user you want to update: ");
        String email = scanner.nextLine();

        // get user email
        User existingUser = getUserByEmail(email);
        if (existingUser == null) {
            System.out.println("No user found with email " + email);
            return;
        }

        // if user is found, update the user
        System.out.println("Please enter the new first name: ");
        String newFirstName = scanner.nextLine();

        System.out.println("Please enter the new last name: ");
        String newLastName = scanner.nextLine();

        System.out.println("Please enter the new email: ");
        String newEmail = scanner.nextLine();

        System.out.println("Please enter the new password: ");
        String newPassword = scanner.nextLine();

        // hash the new password
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        System.out.println("Is the user a doctor? (true/false)");
        boolean newIsDoctor = scanner.nextBoolean();

        // create the new user with new info
        User updatedUser = new User(existingUser.getId(), newFirstName, newLastName, newEmail, hashedPassword,
                newIsDoctor);

        boolean success = updateUser(updatedUser);
        if (success) {
            System.out.println("User information updated.");
        } else {
            System.out.println("Failed to update user information.");
        }
    }

    // delete user menu
    public void deleteUserMenu(Scanner scanner) {
        System.out.println("Enter the user email you would like to delete: ");
        String email = scanner.nextLine();

        boolean success = deleteUser(email);

        if (success) {
            System.out.println("User deleted.");

        } else {
            System.out.println("Could not delete user at this time.");
        }
    }

}
