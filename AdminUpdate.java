package admin;

import common.models.UserUpdate;

public class AdminUpdate extends UserUpdate {
    public AdminUpdate() {
    }

    public AdminUpdate(int userId, String userName, String userEmail, String userPassword, String userAddress, int userAge, int userPhoneNumber) {
        super(userId, userName, userEmail, userPassword, userAddress, userAge, userPhoneNumber);
    }
}
