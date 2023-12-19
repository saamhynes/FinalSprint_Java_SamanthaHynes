
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Scanner;

// working but not working properly.  not putting recommendation in database
// and not displaying it in the running program. 
// only displays that user/patient is within recommended range (when they arent)
public class RecommendationSystem {

        public static void drRecommendMenu() {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter the user id:");
                int userId = scanner.nextInt();
                scanner.nextLine();

                try (Connection connection = DatabaseConnection.getCon()) {
                        String query = "SELECT recommendation FROM recommendation WHERE user_id = ?";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                                preparedStatement.setInt(1, userId);

                                boolean hasRecommendations = false;
                                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                        while (resultSet.next()) {
                                                hasRecommendations = true;

                                                HealthDataCrud healthDataCrud = new HealthDataCrud();
                                                List<String> recommendations = healthDataCrud.showDrRecommend(userId);
                                                for (String recommenadtion : recommendations) {
                                                        System.out.println("Recommendations: " + recommenadtion);
                                                }
                                        }

                                        if (!hasRecommendations) {
                                                System.out.println(
                                                                "No recommendations at this time. Keep maintaining your healthy lifestyle.");
                                        }
                                }
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                }

        }

        public boolean saveRecommend(int userId, String recommend) {
                String query = "INSERT INTO recommendation (user_id, recommendation) VALUES (?, ?)";

                try (Connection connection = DatabaseConnection.getCon();
                                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setString(2, recommend);

                        int rowsInserted = preparedStatement.executeUpdate();
                        return rowsInserted > 0;
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }

        }

        public void drRecommend(int userId) {
                try (Connection connection = DatabaseConnection.getCon()) {
                        String query = "SELECT recommendation FROM recommendation WHERE user_id = ?";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                                preparedStatement.setInt(1, userId);

                                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                        while (resultSet.next()) {
                                                String recommendation = resultSet.getString("recommendation");

                                                System.out.println("Recommendation: " + recommendation);
                                        }
                                }
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                }
        }

}
