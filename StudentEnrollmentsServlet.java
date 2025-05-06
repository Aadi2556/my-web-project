package student;

import common.security.JwtUtil;
import common.storage.EnrollmentStorage;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/secure/myEnrollments")
public class StudentEnrollmentsServlet extends HttpServlet {

    private final Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Validate JWT token and get student ID
            int studentId = authenticateAndGetStudentId(request);

            // Get enrollments with payment status
            List<Map<String, Object>> enrollments = EnrollmentStorage.getEnrollmentsWithPaymentStatus(studentId);

            // Return successful response
            response.getWriter().write(gson.toJson(enrollments));

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token has expired. Please login again.");
        } catch (MalformedJwtException | IllegalArgumentException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid token. Please login again.");
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve enrollments. Please try again later.");
            e.printStackTrace();
        }
    }

    private int authenticateAndGetStudentId(HttpServletRequest request) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid authorization header");
        }

        String token = authHeader.substring(7);
        Claims claims = JwtUtil.validateToken(token);
        return claims.get("userId", Integer.class);
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(gson.toJson(Map.of(
                "status", "error",
                "message", message
        )));
    }
}