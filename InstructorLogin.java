package instructor;

import common.models.UserLogin;

public class InstructorLogin extends UserLogin {
    public InstructorLogin() {
    }

    public InstructorLogin(String userEmail, String userPassword) {
        super(userEmail, userPassword);
    }
}
