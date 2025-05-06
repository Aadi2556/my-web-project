package admin;

import common.models.Course;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/secure/approveCourse")
public class ApproveCourseServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }
        try {
            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            String userEmail = claims.getSubject();

            // Verify admin
            if (UserStorage.getAdminRegister(userEmail) == null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Not an admin");
                return;
            }

            int courseId = Integer.parseInt(req.getParameter("courseId"));
            Course course = CourseStorage.getCourseById(courseId);
            if (course == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
                return;
            }

            course.setStatus("approved");
            CourseStorage.updateCourse(course);
            resp.getWriter().write("{\"status\":\"success\"}");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}