package student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.models.Course;
import common.security.JwtUtil;
import common.storage.EnrollmentStorage;
import common.storage.CourseStorage;
import common.storage.FeedbackStorage;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/secure/studentDashboard")
public class StudentDashboard extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String token = req.getHeader("Authorization").substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int studentId = claims.get("userId", Integer.class);

            List<Map<String, Object>> enrollments = EnrollmentStorage.getEnrollmentsWithPaymentStatus(studentId);

            // Add course files and feedback for approved enrollments
            for (Map<String, Object> enrollment : enrollments) {
                int courseId = ((Number) enrollment.get("courseId")).intValue();
                if ("APPROVED".equals(enrollment.get("paymentStatus"))) {
                    Course course = CourseStorage.getCourseById(courseId);
                    if (course != null) {
                        enrollment.put("files", course.getFiles());
                    }
                }
                // Add feedback for the course
                enrollment.put("feedback", FeedbackStorage.getFeedbackByCourse(courseId));
            }

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("studentId", studentId);
            responseJson.add("enrollments", new Gson().toJsonTree(enrollments));

            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(responseJson));

        } catch (Exception e) {
            System.err.println("Dashboard error: ");
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}