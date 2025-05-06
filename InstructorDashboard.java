package instructor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.models.Course;
import common.models.StudentEnrollment;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import common.storage.FeedbackStorage;
import common.storage.PaymentStorage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/secure/instructorDashboard")
public class InstructorDashboard extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setContentType("application/json");

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
            return;
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            String userEmail = claims.getSubject();

            // Get instructor details
            InstructorUpdate instructor = UserStorage.getInstructorUpdate(userEmail);
            if (instructor == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Instructor not found");
                return;
            }

            // Get instructor's courses and earnings
            int instructorId = instructor.getUserId();
            List<Course> courses = CourseStorage.getCoursesByInstructor(instructorId);
            Map<Integer, List<StudentEnrollment>> courseEnrollments = new HashMap<>();

            for (Course course : courses) {
                List<StudentEnrollment> enrollments = CourseStorage.getEnrollmentsForCourse(course.getCourseId());
                courseEnrollments.put(course.getCourseId(), enrollments);
            }

            // Build complete response
            JsonObject response = new JsonObject();
            response.add("instructor", buildInstructorJson(instructor));
            response.add("courses", buildCoursesJson(courses, courseEnrollments));

            resp.getWriter().write(gson.toJson(response));

        } catch (ExpiredJwtException e) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private JsonObject buildInstructorJson(InstructorUpdate instructor) {
        JsonObject json = new JsonObject();
        json.addProperty("id", instructor.getUserId());
        json.addProperty("name", instructor.getUserName());
        json.addProperty("email", instructor.getUserEmail());
        json.addProperty("address", instructor.getUserAddress());
        json.addProperty("age", instructor.getUserAge());
        json.addProperty("phone", instructor.getUserPhoneNumber());

        // Add earnings calculation
        double totalEarnings = PaymentStorage.getEarningsByInstructor(instructor.getUserId());
        json.addProperty("earnings", totalEarnings);

        return json;
    }

    private JsonObject buildCoursesJson(List<Course> courses, Map<Integer, List<StudentEnrollment>> enrollments) {
        JsonObject json = new JsonObject();
        JsonArray coursesArray = new JsonArray();

        for (Course course : courses) {
            JsonObject courseJson = new JsonObject();
            courseJson.addProperty("id", course.getCourseId());
            courseJson.addProperty("title", course.getTitle());
            courseJson.addProperty("description", course.getDescription());
            courseJson.addProperty("createdAt", course.getCreatedAt().toString());
            courseJson.addProperty("status", course.getStatus());
            courseJson.addProperty("price", course.getPrice());

            // Add feedback
            courseJson.add("feedback", gson.toJsonTree(FeedbackStorage.getFeedbackByCourse(course.getCourseId())));

            if (enrollments.containsKey(course.getCourseId())) {
                courseJson.add("enrollments", gson.toJsonTree(enrollments.get(course.getCourseId())));
            }

            coursesArray.add(courseJson);
        }

        json.add("courses", coursesArray);
        return json;
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        resp.getWriter().write(gson.toJson(error));
    }
}