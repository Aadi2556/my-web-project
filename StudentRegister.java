package student;

import common.models.UserRegister;

public class StudentRegister extends UserRegister {
    public StudentRegister() {
    }

    public StudentRegister(int userId, String userName, String userPassword, String userEmail) {
        super(userId, userName, userPassword, userEmail);
    }
}
