package instructor;

import common.models.UserRegister;

public class InstructorRegister extends UserRegister {
    public InstructorRegister() {
    }

    public InstructorRegister(int userId, String userName, String userPassword, String userEmail) {
        super(userId, userName, userPassword, userEmail);
    }
}
