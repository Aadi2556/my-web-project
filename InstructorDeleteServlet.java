package instructor;

import com.google.gson.Gson;
import common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/secure/instructorDelete")
public class InstructorDeleteServlet extends HttpServlet {
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();
        PrintWriter writer = resp.getWriter();

        try {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writer.println(gson.toJson(Map.of("error", "Missing authorization token")));
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            String userEmail = claims.getSubject();

            boolean deleted = UserStorage.deleteUser(userEmail);

            if (deleted) {
                writer.println(gson.toJson(Map.of("message", "Instructor deleted successfully")));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writer.println(gson.toJson(Map.of("error", "Instructor not found")));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println(gson.toJson(Map.of("error", "Delete Failed: " + e.getMessage())));
        }
    }
}
