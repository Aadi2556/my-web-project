package instructor;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
@WebServlet(urlPatterns = "/instructorRegister")
public class InstructorRegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reqReader = req.getReader();

        InstructorRegister instructorRegister = new Gson().fromJson(reqReader, InstructorRegister.class);

        UserStorage.addUser(instructorRegister);

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        resp.setContentType("text/plain");

        resp.getWriter().println("Instructor Details: " + instructorRegister.getUserEmail() + instructorRegister.getUserName() + instructorRegister.getUserId() + instructorRegister.getUserPassword());
    }
}
