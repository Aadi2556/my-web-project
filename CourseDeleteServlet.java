package instructor;

import common.security.JwtUtil;
import common.storage.CourseStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/secure/courseDelete")
public class CourseDeleteServlet extends HttpServlet {
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        try {
            int courseId = Integer.parseInt(request.getParameter("courseId"));
            int instructorId = JwtUtil.getUserIdFromRequest(request);

            if (!CourseStorage.isCourseOwner(courseId, instructorId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not course owner");
                return;
            }

            CourseStorage.deleteCourse(courseId);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"success\"}");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
