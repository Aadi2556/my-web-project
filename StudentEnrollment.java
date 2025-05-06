package common.models;

import java.util.Date;

public class StudentEnrollment {
    public int courseId;
    public int studentId;
    public Date enrolledAt;

    public StudentEnrollment() {
    }

    public StudentEnrollment(int courseId, int studentId, Date enrolledAt) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.enrolledAt = enrolledAt;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Date getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(Date enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}