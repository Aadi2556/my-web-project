package instructor;

import com.google.gson.Gson;
import common.models.Course;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/secure/courseGet")
public class CourseGetServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int courseId = Integer.parseInt(request.getParameter("courseId"));
            int instructorId = JwtUtil.getUserIdFromRequest(request);

            Course course = CourseStorage.getCourseById(courseId);
            if (course == null || course.getInstructorId() != instructorId) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType("application/json");
            new Gson().toJson(course, response.getWriter());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}