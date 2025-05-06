package student;

import java.util.Date;

public class StudentRecord {
    private int userId;
    private String userName;
    private String userEmail;
    private String userPassword;
    private String userAddress;
    private int userAge;
    private int userPhoneNumber;
    private Date registrationTime;

    // Constructor using StudentRegister + defaults
    public StudentRecord(StudentRegister register) {
        this.userId = register.getUserId();
        this.userName = register.getUserName();
        this.userEmail = register.getUserEmail();
        this.userPassword = register.getUserPassword();
        this.userAddress = "";  // Default value
        this.userAge = 0;       // Default value
        this.userPhoneNumber = 0; // Default value
        this.registrationTime = new Date(); // Set to current time
    }

    // Constructor using StudentUpdate
    public StudentRecord(StudentUpdate update) {
        this.userId = update.getUserId();
        this.userName = update.getUserName();
        this.userEmail = update.getUserEmail();
        this.userPassword = update.getUserPassword();
        this.userAddress = update.getUserAddress();
        this.userAge = update.getUserAge();
        this.userPhoneNumber = update.getUserPhoneNumber();
        this.registrationTime = new Date(); // Default to current time (update if stored)
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserPassword() { return userPassword; }
    public String getUserAddress() { return userAddress; }
    public int getUserAge() { return userAge; }
    public int getUserPhoneNumber() { return userPhoneNumber; }
    public Date getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(Date registrationTime) { this.registrationTime = registrationTime; }
}