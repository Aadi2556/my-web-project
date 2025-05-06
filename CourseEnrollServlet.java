package student;

import common.models.Course;
import common.models.StudentEnrollment;
import common.payment.Payment;
import common.storage.CourseStorage;
import common.storage.PaymentStorage;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/secure/courseEnroll")
public class CourseEnrollServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Get course ID and student ID
            int courseId = Integer.parseInt(request.getParameter("courseId"));
            int studentId = (Integer) request.getAttribute("userId");

            List<StudentEnrollment> existing = CourseStorage.getAllEnrollments()
                    .stream()
                    .filter(e -> e.getStudentId() == studentId && e.getCourseId() == courseId)
                    .collect(Collectors.toList());

            if (!existing.isEmpty()) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Already enrolled");
                return;
            }

            // 2. Check course status
            Course course = CourseStorage.getCourseById(courseId);
            if (course == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
                return;
            }
            if (!"approved".equalsIgnoreCase(course.getStatus())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Course not approved");
                return;
            }

            // 3. Check approved payment
            Payment payment = PaymentStorage.getApprovedPayment(studentId, courseId);
            if (payment == null) {
                response.sendError(HttpServletResponse.SC_PAYMENT_REQUIRED, "Payment not approved");
                return;
            }

            // 4. Proceed with enrollment
            StudentEnrollment enrollment = new StudentEnrollment();
            enrollment.setCourseId(courseId);
            enrollment.setStudentId(studentId);
            enrollment.setEnrolledAt(new Date());

            CourseStorage.enrollStudent(enrollment);

            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"success\"}");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Enrollment failed");
        }
    }
}