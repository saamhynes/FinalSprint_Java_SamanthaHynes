
// import com.DataBaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HealthMonitoringApp {
    public static void main(String[] args) {
        boolean isConnected = DatabaseConnection.isConnectedToHealthApp();
        if (isConnected) {
            System.out.println();
            System.out.println("Connected to the Health App database.");
        } else {
            System.out.println();
            System.out.println("Not connected to the Health App database.");
        }

        loginMenu();
    }

    public static void loginMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Are you a doctor or patient?");
        System.out.println("1. Doctor");
        System.out.println("2. Patient");
        System.out.println("3. Exit");
        System.out.println("Please enter choice(1-3): ");

        int loginChoice = scanner.nextInt();
        scanner.nextLine();

        switch (loginChoice) {
            case 1: // doctor portal
                DoctorPortalDao doctorPortalDao = new DoctorPortalDao();
                doctorPortalDao.doctorMenu(scanner);
                break;
            case 2: // patient portal
                HealthDataCrud.patientPortal();
                break;
            case 3:
                System.out.println("Exiting the system...");
                scanner.close();
                System.exit(0);
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }

    public static void displayMenu() {
        Scanner scanner = new Scanner(System.in);
        HealthDataCrud healthDataCrud = new HealthDataCrud();
        // Show main menu

        while (true) {
            System.out.println("------------------------------");
            System.out.println("\nPatient Portal");
            System.out.println("------------------------------");
            System.out.println("1. User Information");
            System.out.println("2. Health Data");
            System.out.println("3. Set Medication Reminder.");
            System.out.println("4. Doctor Recommendations");
            System.out.println("5. Logout");
            System.out.println("Please enter your choice(1-4): ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    crudMenu(scanner);
                    break;
                case 2:
                    healthDataCrudMenu(scanner);
                    break;
                case 3:
                    // added all of this stuff here because i couldnt get it working elsewhere
                    System.out.println("Setting Medication Reminder");
                    System.out.println("-------------------------------");
                    System.out.println("Enter the user ID:");
                    int userId = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("Enter the time of day(HH:MM): ");
                    String timeOfDay = scanner.nextLine();

                    healthDataCrud.setMedReminder(userId, timeOfDay);
                    break;
                case 4:
                    RecommendationSystem.drRecommendMenu();
                    break;
                case 5:
                    System.out.println("Logging out...");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void medReminder(Scanner scanner, int userId) {
        HealthDataCrud healthDataCrud = new HealthDataCrud();

        String medications = healthDataCrud.getMeds(userId);

        if (medications != null && !medications.isEmpty()) {
            if (!medications.equals("No medications found")) {

                System.out.println("Medications: " + medications);

                System.out.println("Do you want to set a reminder for your medications?(y/n)");
                String response = scanner.nextLine();

                if (response.equalsIgnoreCase("y")) {
                    System.out.println("Enter time of day for reminder(HH:MM): ");
                    String timeOfDay = scanner.nextLine();

                    boolean success = healthDataCrud.setMedReminder(userId, timeOfDay);
                    if (success) {
                        System.out.println("Reminder set for " + timeOfDay);
                    } else {
                        System.out.println("Reminder could not be set.");
                    }
                } else {
                    System.out.println("Reminder not set.");
                }
            } else {
                System.out.println("No medications found.");
            }
        } else {
            System.out.println("No medications found.");
        }
    }

    public static void crudMenu(Scanner scanner) {
        UserCrud userCrud = new UserCrud();

        while (true) {
            System.out.println("------------------------------");
            System.out.println("\nPatient Portal - User Information");
            System.out.println("------------------------------");
            System.out.println("1. Update your user information");
            System.out.println("2. Delete your user account");
            System.out.println("3. Back to main menu");
            System.out.println("Please enter your choice(1-5): ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    userCrud.updateUserMenu(scanner);
                    break;
                case 2:
                    userCrud.deleteUserMenu(scanner);
                    break;
                case 3:
                    return; // back to main menu
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    public static void healthDataCrudMenu(Scanner scanner) {
        HealthDataCrud healthDataCrud = new HealthDataCrud();

        while (true) {
            System.out.println("\nHealth Data Menu");
            System.out.println("1. Add health data");
            System.out.println("2. Get health data by user ID");
            System.out.println("3. Delete health data");
            System.out.println("4. Back to main menu");
            System.out.println("Please enter your choice(1-5): ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    healthDataCrud.createHealthData(scanner);
                    break;
                case 2:
                    healthDataCrud.getHealthDataByUserId(scanner);
                    break;
                case 3:
                    healthDataCrud.deleteHealthData(scanner);
                    break;
                case 4:
                    return; // back to main menu
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }

    }

}
