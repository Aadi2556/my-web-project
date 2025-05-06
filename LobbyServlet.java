package instructor;

import com.google.gson.Gson;
import common.models.Course;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/secure/lobby")
public class LobbyServlet extends HttpServlet {
    private final Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int instructorId = JwtUtil.getUserIdFromRequest(request);
            List<Course> allCourses = CourseStorage.getCoursesByInstructor(instructorId);

            List<Course> pendingCourses = allCourses.stream()
                    .filter(c -> "pending".equals(c.getStatus()))
                    .collect(Collectors.toList());

            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(pendingCourses));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
