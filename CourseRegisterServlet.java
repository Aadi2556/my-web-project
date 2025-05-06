package admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.models.Course;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import common.utils.Queue;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/secure/courseRegister")
public class CourseRegisterServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = JwtUtil.validateToken(token); // Validate token without role check
                String userEmail = claims.getSubject();
                if (userEmail == null) {
                    sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: no subject");
                    return;
                }

                List<Course> courses = CourseStorage.getAllCourses();
                Queue queue = new Queue();
                for (Course course : courses) {
                    queue.enqueue(course);
                }

                Course[] sortedCourses = queue.toArray();
                resp.getWriter().write(gson.toJson(sortedCourses));
            } catch (ExpiredJwtException e) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            } catch (MalformedJwtException e) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Token is malformed");
            } catch (Exception e) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            }
        } catch (Exception e) {
            System.err.println("Course register error: " + e.getMessage());
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve courses: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);
        resp.getWriter().write(gson.toJson(error));
    }
}