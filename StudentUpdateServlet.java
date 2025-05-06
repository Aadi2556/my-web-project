package student;

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

@WebServlet("/secure/studentUpdate")
public class StudentUpdateServlet extends HttpServlet {
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json"); // Force JSON for all responses
        PrintWriter writer = resp.getWriter();
        Gson gson = new Gson();
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writer.println("Missing or invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token); // Could throw an exception
            String originalEmail = claims.getSubject();

            StudentUpdate studentUpdate = gson.fromJson(req.getReader(), StudentUpdate.class);
            boolean success = UserStorage.updateUser(originalEmail, studentUpdate);

            if (success) {
                writer.println(gson.toJson(Map.of("message", "User details updated successfully")));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writer.println(gson.toJson(Map.of("error", "User not found or update failed")));
            }
        } catch (Exception e) {
            // Log the exception to diagnose the 500 error
            e.printStackTrace(); // Check server logs for this!
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println(gson.toJson(Map.of("error", "Server error: " + e.getMessage()))); // Optional: include details for debugging

        }
    }
}
