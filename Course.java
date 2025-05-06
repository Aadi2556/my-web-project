package common.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Course {
    public int courseId;
    public String title;
    public String description;
    public int instructorId;
    public String status;
    public Date createdAt;
    public double price;
    public List<String> files;
    public List<String> messages;

    public Course() {
        this.files = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.status = "pending";
    }

    public Course(int courseId, String title, String description, int instructorId, String status, Date createdAt, double price, List<String> files, List<String> messages) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.status = status;
        this.createdAt = createdAt;
        this.price = price;
        this.files = files;
        this.messages = messages;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(int instructorId) {
        this.instructorId = instructorId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
