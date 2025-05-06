package student;

import com.google.gson.Gson;
import common.models.Course;
import common.storage.CourseStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/course/*")
public class CourseServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing course ID");
                return;
            }

            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID format");
                return;
            }

            int courseId = Integer.parseInt(splits[1]);
            Course course = CourseStorage.getCourseById(courseId);

            if (course == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
                return;
            }

            response.setContentType("application/json");
            new Gson().toJson(course, response.getWriter());

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID format");
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving course");
        }
    }
}