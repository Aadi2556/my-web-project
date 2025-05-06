package instructor;

import com.google.gson.Gson;
import common.models.Course;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/secure/courseUpdate")
@MultipartConfig(
        maxFileSize = 52428800L,     // 50MB
        maxRequestSize = 52428800L,  // 50MB
        fileSizeThreshold = 0
)
public class CourseUpdateServlet extends HttpServlet {

    private static final String[] ALLOWED_FILE_TYPES = {
            "pdf", "doc", "docx", "ppt", "pptx", "txt", "jpg", "png", "mp4"
    };

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        List<String> newFiles = new ArrayList<>();

        try {
            // Authentication and authorization
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing authorization token");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int instructorId = Integer.parseInt(claims.get("userId").toString());

            // Get course ID
            int courseId = Integer.parseInt(request.getParameter("courseId"));

            // Validate course ownership
            Course existingCourse = CourseStorage.getCourseById(courseId);
            if (existingCourse == null || existingCourse.getInstructorId() != instructorId) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Not authorized to update this course");
                return;
            }

            // Create updated course object
            Course updatedCourse = new Course();
            updatedCourse.setCourseId(courseId);
            updatedCourse.setTitle(request.getParameter("title"));
            updatedCourse.setDescription(request.getParameter("description"));
            updatedCourse.setInstructorId(instructorId);
            updatedCourse.setCreatedAt(existingCourse.getCreatedAt());
            updatedCourse.setStatus(existingCourse.getStatus());

            // Copy existing files and messages
            updatedCourse.setFiles(new ArrayList<>(existingCourse.getFiles()));
            updatedCourse.setMessages(new ArrayList<>(existingCourse.getMessages()));

            // Handle file uploads
            String uploadDir = getServletContext().getRealPath("/uploads");
            File saveDir = new File(uploadDir);
            if (!saveDir.exists()) saveDir.mkdirs();

            for (Part part : request.getParts()) {
                String fileName = part.getSubmittedFileName();
                if (fileName != null && !fileName.isEmpty()) {
                    if (!isValidFileType(fileName)) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                                "Invalid file type: " + getFileExtension(fileName));
                        return;
                    }

                    String safeFileName = sanitizeFilename(fileName);
                    File filePath = new File(saveDir, safeFileName);

                    try (InputStream input = part.getInputStream()) {
                        Files.copy(input, filePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        newFiles.add(safeFileName);
                    }
                }
            }

            // Add new files to course
            updatedCourse.getFiles().addAll(newFiles);

            // Update course in storage
            CourseStorage.updateCourse(updatedCourse);

            // Build response
            response.getWriter().write(String.format(
                    "{\"status\":\"success\",\"message\":\"Course updated\",\"newFiles\":%s}",
                    new Gson().toJson(newFiles)
            ));

        } catch (ExpiredJwtException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Update failed: " + e.getMessage());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Methods", "PUT, OPTIONS");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Helper methods remain unchanged
    private boolean isValidFileType(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();
        for (String allowed : ALLOWED_FILE_TYPES) {
            if (allowed.equalsIgnoreCase(ext)) return true;
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private String sanitizeFilename(String filename) {
        String cleanName = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return System.currentTimeMillis() + "_" + cleanName;
    }

    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write(String.format(
                "{\"status\":\"error\",\"message\":\"%s\"}", message
        ));
    }
}