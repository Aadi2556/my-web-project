package student;

import com.google.gson.Gson;
import common.models.Course;
import common.storage.CourseStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/courses")
public class CourseListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            List<Course> approvedCourses = CourseStorage.getAllCourses().stream()
                    .filter(c -> "approved".equals(c.getStatus()))
                    .collect(Collectors.toList());
            resp.getWriter().write(new Gson().toJson(approvedCourses));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}