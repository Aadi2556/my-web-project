package admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.security.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@WebServlet("/secure/instructorName")
public class InstructorNameServlet extends HttpServlet {
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

                String idParam = req.getParameter("id");
                if (idParam == null) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing instructor ID");
                    return;
                }

                int instructorId = Integer.parseInt(idParam);
                String name = getInstructorNameById(instructorId);

                JsonObject response = new JsonObject();
                response.addProperty("name", name);
                resp.getWriter().write(gson.toJson(response));
            } catch (ExpiredJwtException e) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            } catch (MalformedJwtException e) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Token is malformed");
            } catch (Exception e) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid instructor ID");
        } catch (Exception e) {
            System.err.println("Instructor name error: " + e.getMessage());
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve instructor name: " + e.getMessage());
        }
    }

    private String getInstructorNameById(int instructorId) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", -1);
                if (parts.length >= 7 && Integer.parseInt(parts[0]) == instructorId) {
                    return parts[1]; // Return instructor name
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);
        resp.getWriter().write(gson.toJson(error));
    }
}