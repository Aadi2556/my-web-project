package student;

import common.models.UserUpdate;

public class StudentUpdate extends UserUpdate {
    public StudentUpdate() {
    }

    public StudentUpdate(int userId, String userName, String userEmail, String userPassword, String userAddress, int userAge, int userPhoneNumber) {
        super(userId, userName, userEmail, userPassword, userAddress, userAge, userPhoneNumber);
    }
}
