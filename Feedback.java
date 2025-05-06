package common.models;

import java.util.Date;

public class Feedback {
    private int feedbackId;
    private int courseId;
    private int studentId;
    private String studentName;
    private String comment;
    private int rating; // 1-5
    private Date submittedAt;

    public Feedback() {}

    public Feedback(int courseId, int studentId, String studentName, String comment, int rating, Date submittedAt) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.comment = comment;
        this.rating = rating;
        this.submittedAt = submittedAt;
    }

    // Getters and Setters
    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
}