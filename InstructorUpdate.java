package instructor;

import common.models.UserUpdate;

public class InstructorUpdate extends UserUpdate {
    public InstructorUpdate() {
    }

    public InstructorUpdate(int userId, String userName, String userEmail, String userPassword, String userAddress, int userAge, int userPhoneNumber) {
        super(userId, userName, userEmail, userPassword, userAddress, userAge, userPhoneNumber);
    }
}
