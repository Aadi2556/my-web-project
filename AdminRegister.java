package admin;

import common.models.UserRegister;

public class AdminRegister extends UserRegister {
    public AdminRegister() {
    }

    public AdminRegister(int userId, String userName, String userPassword, String userEmail) {
        super(userId, userName, userPassword, userEmail);
    }
}
