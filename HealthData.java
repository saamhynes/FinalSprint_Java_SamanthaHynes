import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

public class HealthData {
    private int id;
    private int userId;

    private double weight;
    private double height;
    private int steps;
    private int heartRate;
    private Date date;

    private double waterDrankOz;
    private int caloriesBurned;

    private double bmi;

    private int dailyActivitiesId;
    private int vitalSignsId;
    private int medicalRecordsId;

    private double bodyTemp;
    private String bloodPressure;

    private String medicalCondition;
    private String medications;

    private UserCrud userCrud;

    // Constructor, getters, and setters
    public HealthData() {
        // Default constructor
        this.userCrud = new UserCrud();
    }

    public HealthData(int id, int userId, double weight, double height, int steps, int heartRate, Date date) {
        this.id = id;
        this.userId = userId;
        this.weight = weight;
        this.height = height;
        this.steps = steps;
        this.heartRate = heartRate;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = new Date(date.getTime());
    }

    public String toString() {
        return "User ID: " + getUserId() + "\n" +
                "Weight: " + getWeight() + "lbs" + "\n" +
                "Height: " + getHeight() + "cm" + "\n" +
                "Steps: " + getSteps() + "\n" +
                "Heart Rate: " + getHeartRate() + "bpm" + "\n" +
                "Date: " + getDate() + "\n" +
                "Water Drank: " + getWaterDrankOz() + "oz" + "\n" +
                "Calories Burned: " + getCaloriesBurned() + "calories" +
                "\nBMI: " + getBmi();

    }

    public double getWaterDrankOz() {
        return waterDrankOz;
    }

    public void setWaterDrankOz(double waterDrankOz) {
        this.waterDrankOz = waterDrankOz;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double calculateBMI() {
        double heightInMeter = getHeight() / 100;
        return getWeight() / (heightInMeter * heightInMeter);
    }

    public double getBmi() {
        return bmi;
    }

    public int getDailyActivitiesId() {
        return dailyActivitiesId;
    }

    public void setDailyActivities(int dailyActivitiesId) {
        this.dailyActivitiesId = dailyActivitiesId;
    }

    public int getVitalSignsId() {
        return vitalSignsId;
    }

    public void setVitalSignsId(int vitalSignsId) {
        this.vitalSignsId = vitalSignsId;
    }

    public int getMedicalRecordsId() {
        return medicalRecordsId;
    }

    public void setMedicalRecordsId(int medicalRecordsId) {
        this.medicalRecordsId = medicalRecordsId;
    }

    public double getBodyTemp() {
        return this.bodyTemp;
    }

    public void setBodyTemp(double bodyTemp) {
        this.bodyTemp = bodyTemp;
    }

    public String getBloodPressure() {
        return this.bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getMedicalCondition() {
        return medicalCondition;
    }

    public void setMedicalCondition(String medicalCondition) {
        this.medicalCondition = medicalCondition;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public void extractDailyActivitesFromResultSet(ResultSet resultSet, HealthData healthData) throws SQLException {
        healthData.setSteps(resultSet.getInt("steps"));
        healthData.setWaterDrankOz(resultSet.getDouble("waterdrankoz"));
        healthData.setCaloriesBurned(resultSet.getInt("caloriesburned"));
    }

    public void extractVitalSignsFromResultSet(ResultSet resultSet, HealthData healthData) throws SQLException {
        healthData.setHeartRate(resultSet.getInt("heartrate"));
        healthData.setBodyTemp(resultSet.getDouble("bodytemp"));
        healthData.setBloodPressure(resultSet.getString("bloodpressure"));
    }

    public void extractMedicalRecordsFromResultSet(ResultSet resultSet, HealthData healthData) throws SQLException {
        healthData.setMedicalCondition(resultSet.getString("medical_condition"));
        healthData.setMedications(resultSet.getString("medications"));
    }
}
