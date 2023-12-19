import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

public class HealthDataCrud {
    private UserCrud userCrud;

    //
    public HealthDataCrud() {
        this.userCrud = new UserCrud();
    }

    public static void patientPortal() {
        Scanner scanner = new Scanner(System.in);
        HealthDataCrud healthDataCrud = new HealthDataCrud();

        while (true) {
            System.out.println("------------------------------");
            System.out.println("Patient Portal");
            System.out.println("------------------------------");

            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Logout");
            System.out.println("Please enter your choice(1-3): ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    // Login
                    patientLogin(scanner);
                    break;
                case 2:
                    // Register
                    healthDataCrud.userCrud.createUser(scanner);
                    break;
                case 3:
                    // Logout
                    System.out.println("Logging out...");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid. Please try again.");
                    break;
            }
        }
    }

    public static User patientLogin(Scanner scanner) {
        HealthDataCrud healthDataCrud = new HealthDataCrud();
        System.out.println("------------------------------");
        System.out.println("Patient Login");
        System.out.println("------------------------------");

        System.out.println("Please enter your email: ");
        String email = scanner.nextLine();

        System.out.println("Enter your password:");
        String password = scanner.nextLine();

        User user = healthDataCrud.fetchUserPatient(email, password);
        if (user != null) {
            System.out.println("Login successful.");
            System.out.println("------------------------------");
            HealthMonitoringApp.displayMenu();

            return user;
        } else {
            System.out.println("Incorrect email or password.");

            return null;
        }
    }

    private User fetchUserPatient(String email, String password) {
        try (Connection connection = DatabaseConnection.getCon()) {
            String query = "SELECT * FROM users WHERE email = ? ";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedHashedPassword = resultSet.getString("password");
                        if (BCrypt.checkpw(password, storedHashedPassword)) {
                            int id = resultSet.getInt("id");
                            String firstName = resultSet.getString("firstName");
                            String lastName = resultSet.getString("lastName");
                            boolean isDoctor = resultSet.getBoolean("isDoctor");

                            return new User(id, firstName, lastName, email, password, isDoctor);
                        }

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return null;
    }

    public static boolean createHealthData(HealthData healthData) {
        String insertHealthDataQuery = "INSERT INTO healthdata (user_id, weight, height, bmi, date) VALUES (?, ?, ?, ?, ?)";
        String insertDailyActivitiesQuery = "INSERT INTO dailyactivities (user_id, steps, waterdrankoz, caloriesburned) VALUES (?, ?, ?, ?)";
        String insertVitalSignsQuery = "INSERT INTO vitalsigns (user_id, heartRate, bodyTemp, bloodPressure) VALUES (?, ?, ?, ?)";
        String insertMedicalRecordsQuery = "INSERT INTO medicalrecords (user_id, medical_condition, medications) VALUES (?, ?, ?)";

        boolean success = false;

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement healthDataStatement = con.prepareStatement(insertHealthDataQuery,
                        Statement.RETURN_GENERATED_KEYS);
                PreparedStatement dailyActivitiesStatement = con.prepareStatement(insertDailyActivitiesQuery);
                PreparedStatement vitalSignsStatement = con.prepareStatement(insertVitalSignsQuery);
                PreparedStatement medicalRecordsStatement = con.prepareStatement(insertMedicalRecordsQuery)) {

            con.setAutoCommit(false);

            healthDataStatement.setInt(1, healthData.getUserId());
            healthDataStatement.setDouble(2, healthData.getWeight());
            healthDataStatement.setDouble(3, healthData.getHeight());
            healthDataStatement.setDouble(4, healthData.calculateBMI());
            healthDataStatement.setDate(5, new java.sql.Date(healthData.getDate().getTime()));

            int affectedRows = healthDataStatement.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = healthDataStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int healthDataId = generatedKeys.getInt(1);

                    dailyActivitiesStatement.setInt(1, healthData.getUserId());
                    dailyActivitiesStatement.setInt(2, healthData.getSteps());
                    dailyActivitiesStatement.setDouble(3, healthData.getWaterDrankOz());
                    dailyActivitiesStatement.setInt(4, healthData.getCaloriesBurned());

                    int dailyActivitiesAffectedRows = dailyActivitiesStatement.executeUpdate();

                    vitalSignsStatement.setInt(1, healthData.getUserId());
                    vitalSignsStatement.setInt(2, healthData.getHeartRate());
                    vitalSignsStatement.setDouble(3, healthData.getBodyTemp());
                    vitalSignsStatement.setString(4, healthData.getBloodPressure());

                    int vitalSignsAffectedRows = vitalSignsStatement.executeUpdate();

                    medicalRecordsStatement.setInt(1, healthData.getUserId());
                    medicalRecordsStatement.setString(2, healthData.getMedicalCondition());
                    medicalRecordsStatement.setString(3, healthData.getMedications());

                    int medicalRecordsAffectedRows = medicalRecordsStatement.executeUpdate();

                    if (medicalRecordsAffectedRows > 0) {
                        success = true;
                    }
                }
            }

            if (success) {
                con.commit();
            } else {
                con.rollback();
            }

            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    public List<HealthData> getHealthDataByUserId(int userId) {
        String query = "SELECT * FROM healthData " +
                "JOIN dailyactivities ON healthData.user_id = dailyactivities.user_id " +
                "JOIN vitalSigns ON healthData.user_id = vitalSigns.user_id " +
                "JOIN medicalRecords ON healthData.user_id = medicalRecords.user_id " +
                "WHERE healthData.user_id = ?";

        List<HealthData> healthDataList = new ArrayList<>();

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    HealthData healthData = new HealthData();
                    healthData.setId(resultSet.getInt("id"));

                    healthData.setWeight(resultSet.getDouble("weight"));
                    healthData.setHeight(resultSet.getDouble("height"));
                    healthData.setBmi(resultSet.getDouble("bmi"));

                    healthData.extractDailyActivitesFromResultSet(resultSet, healthData);
                    healthData.extractVitalSignsFromResultSet(resultSet, healthData);
                    healthData.extractMedicalRecordsFromResultSet(resultSet, healthData);

                    healthDataList.add(healthData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return healthDataList;
    }

    public boolean deleteHealthData(int id) {
        boolean success = false;
        String queryHealthData = "DELETE FROM healthData WHERE user_id = ?";
        String queryDailyActivities = "DELETE FROM dailyactivities WHERE user_id = ?";
        String queryVitalSigns = "DELETE FROM vitalSigns WHERE user_id = ?";
        String queryMedicalRecords = "DELETE FROM medicalRecords WHERE user_id = ?";

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statementHealthData = con.prepareStatement(queryHealthData);
                PreparedStatement statementDailyActivities = con.prepareStatement(queryDailyActivities);
                PreparedStatement statementVitalSigns = con.prepareStatement(queryVitalSigns);
                PreparedStatement statementMedicalRecords = con.prepareStatement(queryMedicalRecords)) {

            con.setAutoCommit(false);

            statementHealthData.setInt(1, id);
            int rowsDeletedHealthData = statementHealthData.executeUpdate();

            statementDailyActivities.setInt(1, id);
            int rowsDeletedDailyActivities = statementDailyActivities.executeUpdate();

            statementVitalSigns.setInt(1, id);
            int rowsDeletedVitalSigns = statementVitalSigns.executeUpdate();

            statementMedicalRecords.setInt(1, id);
            int rowsDeletedMedicalRecords = statementMedicalRecords.executeUpdate();

            if (rowsDeletedHealthData > 0 || rowsDeletedDailyActivities > 0 || rowsDeletedVitalSigns > 0
                    || rowsDeletedMedicalRecords > 0) {
                success = true;
                con.commit();
            } else {
                con.rollback();
            }
            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;

    }

    // menus
    public void createHealthData(Scanner scanner) {
        HealthData healthData = new HealthData();

        System.out.println();
        System.out.println("Enter user id:");
        healthData.setUserId(scanner.nextInt());
        scanner.nextLine();

        System.out.println("Enter date (YYYY-MM-DD):");
        String dateString = scanner.nextLine();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            java.util.Date parsedDate = dateFormat.parse(dateString);
            Date sqlDate = new Date(parsedDate.getTime());
            healthData.setDate(sqlDate);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Using current date instead.");
            healthData.setDate(new Date(System.currentTimeMillis()));
        }

        System.out.println("----------------------------");
        System.out.println("Entering health data...");
        System.out.println("----------------------------");

        System.out.println("Enter weight in lbs:");
        healthData.setWeight(scanner.nextDouble());
        scanner.nextLine();

        System.out.println("Enter height in centimeters:");
        healthData.setHeight(scanner.nextDouble());
        scanner.nextLine();

        System.out.println("----------------------------");
        System.out.println("Entering vital signs...");
        System.out.println("----------------------------");

        System.out.println("Enter heart rate:");
        healthData.setHeartRate(scanner.nextInt());
        scanner.nextLine();

        System.out.println("Enter body temperature in degrees C:");
        healthData.setBodyTemp(scanner.nextDouble());
        scanner.nextLine();

        System.out.println("Enter blood pressure(low, average, high):");
        healthData.setBloodPressure(scanner.nextLine());

        System.out.println("----------------------------");
        System.out.println("Entering daily activites...");
        System.out.println("----------------------------");

        System.out.println("Enter steps:");
        healthData.setSteps(scanner.nextInt());
        scanner.nextLine();

        System.out.println("Enter water drank in oz:");
        healthData.setWaterDrankOz(scanner.nextDouble());
        scanner.nextLine();

        System.out.println("Enter calories burned:");
        healthData.setCaloriesBurned(scanner.nextInt());
        scanner.nextLine();

        System.out.println("----------------------------");
        System.out.println("Entering medical info...");
        System.out.println("----------------------------");

        System.out.println("Enter any medical conditions(if none, enter N/A):");
        healthData.setMedicalCondition(scanner.nextLine());

        System.out.println("Enter medications(if none, enter N/A):");
        healthData.setMedications(scanner.nextLine());

        boolean success = createHealthData(healthData);
        if (success) {
            System.out.println("Health data created successfully.");
        } else {
            System.out.println("Failed to create health data.");
        }

    }

    public void getHealthDataByUserId(Scanner scanner) {
        System.out.println("Enter the user id:");
        int userId = scanner.nextInt();
        scanner.nextLine();

        List<HealthData> healthDataList = getHealthDataByUserId(userId);

        if (healthDataList.isEmpty()) {
            System.out.println("No health data found for user id: " + userId);
        } else {
            System.out.println();
            System.out.println("Health data for user id: " + userId);
            for (int i = 0; i < healthDataList.size(); i++) {
                HealthData healthData = healthDataList.get(i);

                System.out.println("----------------------------");
                System.out.println("Weight: " + healthData.getWeight() + "lbs");
                System.out.println("Height: " + healthData.getHeight() + "cm");
                System.out.println("BMI: " + healthData.getBmi());
                System.out.println("----------------------------");

                System.out.println("Daily Activity");
                System.out.println("Steps: " + healthData.getSteps());
                System.out.println("Water Drank: " + healthData.getWaterDrankOz() + "oz");
                System.out.println("Calories Burned: " + healthData.getCaloriesBurned());
                System.out.println("----------------------------");

                System.out.println("Vital Signs");
                System.out.println("Body Temperature: " + healthData.getBodyTemp() + "°C");
                System.out.println("Blood Pressure: " + healthData.getBloodPressure());
                System.out.println("Heart Rate: " + healthData.getHeartRate() + "bpm");
                System.out.println("----------------------------");

                System.out.println("Medical Info");
                System.out.println("Medical Condition: " + healthData.getMedicalCondition());
                System.out.println("Medications: " + healthData.getMedications());
                System.out.println("----------------------------");

            }
        }
    }

    public void deleteHealthData(Scanner scanner) {
        System.out.println("Please enter your ID of the health data you would like to delete:");
        int idToDelete = scanner.nextInt();
        scanner.nextLine();

        boolean deleteSuccess = deleteHealthData(idToDelete);
        if (deleteSuccess) {
            System.out.println("All health data records deleted successfully.");
        } else {
            System.out.println("Could not delete health data at this time. Please try again later.");
        }
    }

    public String getMeds(int userId) {
        String medications = null;
        try (Connection connection = DatabaseConnection.getCon()) {
            String query = "SELECT medications FROM medicalrecords WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        medications = resultSet.getString("medications");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medications;
    }

    public boolean setMedReminder(int userId, String timeOfDay) {
        try (Connection connection = DatabaseConnection.getCon()) {
            String query = "UPDATE medicalrecords SET reminder = ? WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, timeOfDay);
                preparedStatement.setInt(2, userId);

                preparedStatement.executeUpdate();

                System.out.println("User ID: " + userId);
                System.out.println("Time of Day: " + timeOfDay);
                System.out.println("Reminder set for " + timeOfDay);

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.out.println("Reminder could not be set.");
            e.printStackTrace();
        }
        return false;
    }

    // working, but not working properly for some reason

    public HealthData drRecommendHealthData(int userId) {
        String query = "SELECT * FROM healthdata " +
                "JOIN dailyactivities ON healthdata.user_id = dailyactivities.user_id " +
                "JOIN vitalsigns ON healthdata.user_id = vitalsigns.user_id " +
                "JOIN medicalrecords ON healthData.user_id = medicalrecords.user_id " +
                "WHERE healthdata.user_id = ?";

        HealthData healthData = null;

        try (Connection con = DatabaseConnection.getCon();
                PreparedStatement statement = con.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    healthData = new HealthData();
                    healthData.setId(resultSet.getInt("id"));
                    healthData.setWeight(resultSet.getDouble("weight"));
                    healthData.setHeight(resultSet.getDouble("height"));
                    healthData.setBmi(resultSet.getDouble("bmi"));

                    healthData.setSteps(resultSet.getInt("steps"));
                    healthData.setWaterDrankOz(resultSet.getDouble("waterdrankoz"));
                    healthData.setCaloriesBurned(resultSet.getInt("caloriesburned"));

                    healthData.setBodyTemp(resultSet.getDouble("heartRate"));
                    healthData.setBloodPressure(resultSet.getString("bodyTemp"));
                    healthData.setHeartRate(resultSet.getInt("bloodPressure"));

                    healthData.setMedicalCondition(resultSet.getString("medical_condition"));
                    healthData.setMedications(resultSet.getString("medications"));

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return healthData;
    }

    public List<String> showDrRecommend(int userId) {

        HealthData healthData = drRecommendHealthData(userId);

        List<String> recommendations = new ArrayList<>();

        // heart rate
        int heartRate = healthData.getHeartRate();
        if (heartRate < 60) {
            recommendations.add(
                    "Your heart rate is low. Eat more whole foods like fruit, vegetables, nuts, beans and lean proteins.  Adopt an exercise regime to improve cardiovascular health.");
        } else if (heartRate > 100) {
            recommendations.add(
                    "Your heart rate is high. Eat more whole foods like fruit, vegetables, nuts, beans and lean proteins.  Adopt an exercise regime to improve cardiovascular health.");
        } else if (heartRate > 200) {
            recommendations.add("Head to your nearest emergency department.");
        }

        double bodyTemp = healthData.getBodyTemp();
        if (bodyTemp < 35 || bodyTemp > 39.4) {
            recommendations.add(
                    "Your body temperature is not within normal range. Head to your nearest emergency department.");
        }

        int steps = healthData.getSteps();
        if (steps < 2000) {
            recommendations.add(
                    "Try to take more than 3000 steps a day,and aim for 5000 steps for better physical activity.");
        }

        double waterdrankoz = healthData.getWaterDrankOz();
        if (waterdrankoz < 72) {
            recommendations.add(
                    "Ensure you're drinking enough water. Try to aim for 72oz of water intake per day.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("No recommendations at this time. Keep up your healthy lifestyle.");
        }

        RecommendationSystem recommendationSystem = new RecommendationSystem();
        for (String recommendation : recommendations) {
            boolean saved = recommendationSystem.saveRecommend((userId), recommendation);
            if (!saved) {
                System.out.println("Failed to save recommendations.");
            }
        }
        return recommendations;
    }
}
