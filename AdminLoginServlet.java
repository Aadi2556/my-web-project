package admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(urlPatterns = "/adminLogin")
public class AdminLoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reqReader = req.getReader();
        AdminLogin adminLogin = new Gson().fromJson(reqReader, AdminLogin.class);
        boolean validPass = UserStorage.validateUser(adminLogin.getUserEmail(), adminLogin.getUserPassword());

// Set CORS headers
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        if (validPass) {
            // Changed to use getAdminRegister() instead of getUser()
            AdminRegister user = UserStorage.getAdminRegister(adminLogin.getUserEmail());

            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println(new Gson().toJson("User not found"));
                return;
            }

            String jwtToken = JwtUtil.generateToken(
                    user.getUserEmail(),
                    user.getUserName(),
                    user.getUserId()
            );

            // Return token in response body
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("token", jwtToken);
            resp.getWriter().println(new Gson().toJson(responseJson));
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().println(new Gson().toJson("Invalid credentials"));
        }
    }
}
