package admin;

import com.google.gson.Gson;
import common.models.Course;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/secure/pendingCourses")
public class AdminCourseApprovalServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

            List<Course> pendingCourses = CourseStorage.getAllCourses().stream()
                    .filter(c -> "pending".equals(c.getStatus()))
                    .collect(Collectors.toList());
            resp.getWriter().write(new Gson().toJson(pendingCourses));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}