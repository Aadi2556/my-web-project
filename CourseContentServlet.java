package student;

import common.models.Course;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/secure/file")
public class CourseContentServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String token = request.getHeader("Authorization").substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int studentId = claims.get("userId", Integer.class);

            int courseId = Integer.parseInt(request.getParameter("courseId"));
            String fileName = request.getParameter("file");

            Course course = CourseStorage.getCourseById(courseId);
            if (course == null || !course.getFiles().contains(fileName)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
                return;
            }

            // Verify enrollment and payment
            Object payment = common.storage.PaymentStorage.getApprovedPayment(studentId, courseId);
            if (payment == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to access this file");
                return;
            }

            File file = new File(request.getServletContext().getRealPath("/Uploads/" + fileName));
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found on server");
                return;
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving file");
            e.printStackTrace();
        }
    }
}