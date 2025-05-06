package common.payment;

import java.util.Date;

// common/models/Payment.java
public class Payment {
    private int paymentId;
    private int studentId;
    private int courseId;
    private double amount;
    private Date paymentDate;
    private String status; // PENDING, APPROVED, REJECTED
    private String bankDetails;
    private String adminApprovalDate;

    // Getters and setters

    public Payment() {
    }

    public Payment(int paymentId, int studentId, int courseId, double amount, Date paymentDate, String status, String bankDetails, String adminApprovalDate) {
        this.paymentId = paymentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
        this.bankDetails = bankDetails;
        this.adminApprovalDate = adminApprovalDate;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getAdminApprovalDate() {
        return adminApprovalDate;
    }

    public void setAdminApprovalDate(String adminApprovalDate) {
        this.adminApprovalDate = adminApprovalDate;
    }
}