package student;

import common.payment.Payment;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import common.storage.PaymentStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/secure/deleteEnrollment")
public class EnrollmentDeleteServlet extends HttpServlet {
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int studentId = JwtUtil.getUserIdFromRequest(request);
            int courseId = Integer.parseInt(request.getParameter("courseId"));

            System.out.println("Delete request - Student: " + studentId + ", Course: " + courseId);

            List<Payment> payments = PaymentStorage.getPaymentsByStudent(studentId);
            System.out.println("All payments for student:");
            payments.forEach(p -> System.out.println(
                    "Payment ID: " + p.getPaymentId() +
                            " | Course: " + p.getCourseId() +
                            " | Status: " + p.getStatus()
            ));

            Payment payment = payments.stream()
                    .filter(p -> p.getCourseId() == courseId)
                    .filter(p -> "PENDING".equals(p.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (payment == null) {
                System.out.println("No pending payment found for course: " + courseId);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"No pending payment found for this course\"}");
                return;
            }

            if (!PaymentStorage.deletePayment(payment.getPaymentId())) {
                throw new IOException("Failed to delete payment");
            }

            CourseStorage.deleteEnrollment(studentId, courseId);
            out.write("{\"status\":\"success\"}");

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid course ID format\"}");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}