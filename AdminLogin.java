package admin;

import common.models.UserLogin;

public class AdminLogin extends UserLogin {
    public AdminLogin() {
    }

    public AdminLogin(String userEmail, String userPassword) {
        super(userEmail, userPassword);
    }
}
