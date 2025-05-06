package instructor;

public class InstructorRecord {
    private int userId;
    private String userName;
    private String userEmail;
    private String userPassword;
    private String userAddress;
    private int userAge;
    private int userPhoneNumber;

    public InstructorRecord(InstructorRegister register){
        this.userId = register.getUserId();
        this.userName = register.getUserName();
        this.userEmail = register.getUserEmail();
        this.userPassword = register.getUserPassword();
        this.userAddress = "";  // Default value
        this.userAge = 0;       // Default value
        this.userPhoneNumber = 0;
    }
    public InstructorRecord(InstructorUpdate update){
        this.userId = update.getUserId();
        this.userName = update.getUserName();
        this.userEmail = update.getUserEmail();
        this.userPassword = update.getUserPassword();
        this.userAddress = update.getUserAddress();
        this.userAge = update.getUserAge();
        this.userPhoneNumber = update.getUserPhoneNumber();
    }

    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserPassword() { return userPassword; }
    public String getUserAddress() { return userAddress; }
    public int getUserAge() { return userAge; }
    public int getUserPhoneNumber() { return userPhoneNumber; }
}
