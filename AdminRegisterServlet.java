package admin;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(urlPatterns = "/adminRegister")
public class AdminRegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reqReader = req.getReader();

        AdminRegister adminRegister = new Gson().fromJson(reqReader, AdminRegister.class);

        UserStorage.addUser(adminRegister);

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        resp.setContentType("text/plain");

        resp.getWriter().println("Admin Details: " + adminRegister.getUserEmail() + adminRegister.getUserName() + adminRegister.getUserId() + adminRegister.getUserPassword());
    }
}
