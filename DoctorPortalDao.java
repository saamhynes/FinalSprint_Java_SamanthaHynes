
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;
import java.util.Scanner;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import java.util.InputMismatchException;

public class DoctorPortalDao {

    public void doctorMenu(Scanner scanner) {
        while (true) {
            System.out.println("----------------------------");
            System.out.println("Doctor Portal");
            System.out.println("----------------------------");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Logout");
            System.out.println("Please enter your choice(1-3)");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    doctorLogin(scanner);
                    break;
                case 2:
                    registerDoctor(scanner);
                    break;
                case 3:
                    System.out.println("Logout");
                    return;

                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
                    break;
            }
        }
    }

    public void registerDoctor(Scanner scanner) {
        System.out.println("Doctor Registration");
        System.out.println("----------------------------");

        System.out.println("Enter your first name: ");
        String firstName = scanner.nextLine();

        System.out.println("Enter your last name: ");
        String lastName = scanner.nextLine();

        System.out.println("Enter your email: ");
        String email = scanner.nextLine();

        System.out.println("Enter your password: ");
        String password = scanner.nextLine();

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Doctor newDoctor = new Doctor(firstName, lastName, email, hashedPassword, -1);
        int generatedId = saveDoctor(newDoctor);

        if (generatedId != -1) {
            System.out.println("Registration successful. Please login.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }

    private int saveDoctor(Doctor doctor) {
        try (Connection connection = DatabaseConnection.getCon()) {
            String query = "INSERT INTO doctor (firstName, lastName, email, password) VALUES (?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, doctor.getFirstName());
                preparedStatement.setString(2, doctor.getLastName());
                preparedStatement.setString(3, doctor.getEmail());
                preparedStatement.setString(4, doctor.getPassword());

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void doctorLogin(Scanner scanner) {
        System.out.println("----------------------------");
        System.out.println("Login");
        System.out.println("----------------------------");

        System.out.println("Enter your email: ");
        String email = scanner.nextLine();
        System.out.println();

        System.out.println("Enter your password: ");
        String password = scanner.nextLine();

        Doctor doctor = fetchDoctor(email, password);
        if (doctor != null) {
            System.out.println();
            System.out.println("Login successful.");
            accessDoctorFunctionalities(scanner, doctor.getId());
        } else {
            System.out.println("Invalid. Please try again.");
        }
    }

    private Doctor fetchDoctor(String email, String password) {
        try (Connection connection = DatabaseConnection.getCon()) {
            String query = "SELECT * FROM doctor WHERE email = ? ";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedHashedPassword = resultSet.getString("password");
                        if (BCrypt.checkpw(password, storedHashedPassword)) {
                            String firstName = resultSet.getString("firstName");
                            String lastName = resultSet.getString("lastName");
                            int id = resultSet.getInt("id");
                            return new Doctor(firstName, lastName, email, password, id);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void accessDoctorFunctionalities(Scanner scanner, int doctorId) {
        while (true) {
            System.out.println("----------------------------");
            System.out.println("Doctor Portal");
            System.out.println("----------------------------");
            System.out.println("1. View Patients");
            System.out.println("2. View Patient's Health Data");
            System.out.println("3. Logout");
            System.out.println("Please enter your choice(1-3)");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    List<User> users = getUsersByDoctorId(doctorId);
                    displayUsers(users);
                    break;
                case 2:
                    getHealthDataForDoctor(scanner);
                    break;
                case 3:
                    System.out.println("Logging out...");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid. Please try again.");
            }
        }
    }

    // working
    public void displayUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No patients found.");
            return;
        }

        System.out.println("List of Patients:");
        for (User user : users) {
            System.out.println("----------------------------");
            System.out.println("ID: " + user.getId());
            System.out.println("Name: " + user.getFirstName() + " " + user.getLastName());
            System.out.println("Email: " + user.getEmail());

        }
    }

    // working
    public List<User> getUsersByDoctorId(int doctorId) {
        List<User> users = new ArrayList<>();

        System.out.println("Doctor ID: " + doctorId);

        String query = "SELECT * FROM users WHERE doctor_id = ? AND isDoctor = false";

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, doctorId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String email = rs.getString("email");
                String password = rs.getString("password");
                boolean isDoctor = rs.getBoolean("isDoctor");
                users.add(new User(id, firstName, lastName, email, password, isDoctor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void getHealthDataForDoctor(Scanner scanner) {
        System.out.println("Enter the user id:");
        int userId;
        try {
            userId = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid user id.");
            scanner.nextLine();
            return;
        }

        // imported from healtdatacrud yaay
        HealthDataCrud healthDataCrud = new HealthDataCrud();

        List<HealthData> healthDataList = healthDataCrud.getHealthDataByUserId(userId);

        if (healthDataList.isEmpty()) {
            System.out.println("No health data found for user id: " + userId);
        } else {
            System.out.println();
            System.out.println("Health data for user id: " + userId);
            System.out.println("----------------------------");

            for (HealthData healthData : healthDataList) {

                System.out.println("ID: " + healthData.getId());
                System.out.println("Weight: " + healthData.getWeight() + "lbs");
                System.out.println("Height: " + healthData.getHeight() + "cm");
                System.out.println("BMI: " + healthData.getBmi());
                System.out.println("----------------------------");

                // Extracted data from daily activities
                System.out.println("Daily Activity");
                System.out.println("Steps: " + healthData.getSteps());
                System.out.println("Water Drank: " + healthData.getWaterDrankOz() + "oz");
                System.out.println("Calories Burned: " + healthData.getCaloriesBurned());
                System.out.println("----------------------------");

                // Extracted data from vital signs
                System.out.println("Vital Signs");
                System.out.println("Body Temperature: " + healthData.getBodyTemp() + "°C");
                System.out.println("Blood Pressure: " + healthData.getBloodPressure());
                System.out.println("Heart Rate: " + healthData.getHeartRate() + "bpm");
                System.out.println("----------------------------");

                // Extracted data from medical records
                System.out.println("Medical Info");
                System.out.println("Medical Condition: " + healthData.getMedicalCondition());
                System.out.println("Medications: " + healthData.getMedications());

            }
        }
    }

    // private UserCrud userDao;
    // private HealthDataDao healthDataDao;

    // Complete all these methods and add more as needed

    /*
     * public DoctorPortalDao() {
     * userDao = new UserDao();
     * healthDataDao = new HealthDataDao();
     * }
     * 
     * public Doctor getDoctorById(int doctorId) {
     * // Implement this method
     * }
     * 
     * public List<User> getPatientsByDoctorId(int doctorId) {
     * // Implement this method
     * }
     * 
     * public List<HealthData> getHealthDataByPatientId(int patientId) {
     * // Implement this method
     * }
     * 
     * // Add more methods for other doctor-specific tasks
     */
}
