package admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/secure/adminDashboard")
public class AdminDashboard extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setHeader("WWW-Authenticate", "Bearer realm=\"Access to admin dashboard\""); // Changed student→admin
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtil.validateToken(token);
            String userEmail = claims.getSubject();

            // Changed to use admin method and class
            AdminUpdate userUpdate = UserStorage.getAdminUpdate(userEmail); // StudentUpdate→AdminUpdate
            if (userUpdate == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Admin not found"); // User→Admin
                return;
            }

            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setContentType("application/json");

            JsonObject responseJson = getJsonObject(userUpdate);
            resp.getWriter().println(new Gson().toJson(responseJson));

        } catch (ExpiredJwtException e) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
        } catch (MalformedJwtException e) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is malformed");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }

    // Updated parameter type to AdminUpdate
    private static JsonObject getJsonObject(AdminUpdate userUpdate) { // StudentUpdate→AdminUpdate
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("userId", userUpdate.getUserId());
        responseJson.addProperty("userName", userUpdate.getUserName());
        responseJson.addProperty("userEmail", userUpdate.getUserEmail());
        responseJson.addProperty("userAddress", userUpdate.getUserAddress());
        responseJson.addProperty("userAge", userUpdate.getUserAge());
        responseJson.addProperty("userPhoneNumber", userUpdate.getUserPhoneNumber());
        return responseJson;
    }

}
