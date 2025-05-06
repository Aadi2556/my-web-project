package student;

import common.models.UserLogin;

public class StudentLogin extends UserLogin {
    public StudentLogin() {
    }

    public StudentLogin(String userEmail, String userPassword) {
        super(userEmail, userPassword);
    }
}
