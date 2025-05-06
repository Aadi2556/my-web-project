package instructor;

import common.security.JwtUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@WebServlet("/secure/fileDelete")
public class FileDeleteServlet extends HttpServlet {

    @Override  // Changed to public access
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String filename = request.getParameter("filename");
            int instructorId = JwtUtil.getUserIdFromRequest(request);

            String uploadDir = getServletContext().getRealPath("/uploads");
            File file = new File(uploadDir, filename);

            response.setContentType("application/json");

            if (file.exists() && file.delete()) {
                response.getWriter().write("{\"status\":\"success\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"File not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Server error\"}");
        }
    }
}