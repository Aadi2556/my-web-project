package student;

import common.security.JwtUtil;
import common.storage.CourseStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/secure/forceDeleteEnrollment")
public class ForceDeleteEnrollmentServlet extends HttpServlet {
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int studentId = JwtUtil.getUserIdFromRequest(request);
            int courseId = Integer.parseInt(request.getParameter("courseId"));

            // Directly delete enrollment without payment check
            CourseStorage.deleteEnrollment(studentId, courseId);
            out.write("{\"status\":\"success\"}");
        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\":\"Force delete failed\"}");
        }
    }
}