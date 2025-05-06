package instructor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

@WebServlet("/secure/courseCreate")
@MultipartConfig(
        maxFileSize = 52428800L,     // 50MB
        maxRequestSize = 52428800L,  // 50MB
        fileSizeThreshold = 0
)
public class CourseCreateServlet extends HttpServlet {
    private static final String[] ALLOWED_FILE_TYPES = {
            "pdf", "doc", "docx", "ppt", "pptx", "txt", "jpg", "png", "mp4"
    };
    private static final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        List<String> files = new ArrayList<>();
        Course course = new Course();

        try {
            // Validate JWT token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing authorization token");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int instructorId = Integer.parseInt(claims.get("userId").toString());

            // Validate title
            String title = request.getParameter("title");
            if (title == null || title.isEmpty()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Course title is required");
                return;
            }

            // Validate price
            double price;
            try {
                price = Double.parseDouble(request.getParameter("price"));
                if (price <= 0) {
                    throw new NumberFormatException("Price must be positive");
                }
            } catch (NumberFormatException | NullPointerException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid price format");
                return;
            }

            // Get description
            String description = request.getParameter("description");

            // Handle file uploads
            String uploadDir = getServletContext().getRealPath("/uploads");
            File saveDir = new File(uploadDir);
            if (!saveDir.exists()) saveDir.mkdirs();

            for (Part part : request.getParts()) {
                String fileName = part.getSubmittedFileName();
                if (fileName != null && !fileName.isEmpty()) {
                    String safeFileName = sanitizeFilename(fileName);
                    File filePath = new File(saveDir, safeFileName);

                    try (InputStream input = part.getInputStream()) {
                        Files.copy(input, filePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        files.add(safeFileName);
                    }
                }
            }

            // Configure course
            course.setTitle(title);
            course.setDescription(description != null ? description : "");
            course.setFiles(files);
            course.setCreatedAt(new Date());
            course.setInstructorId(instructorId);
            course.setPrice(price);

            // Save to storage
            CourseStorage.createCourse(course);

            // Build success response
            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("status", "success");
            successResponse.addProperty("message", "Course created");
            successResponse.addProperty("courseId", course.getCourseId());
            response.getWriter().write(gson.toJson(successResponse));

        } catch (ExpiredJwtException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Internal server error";
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + errorMessage);
            e.printStackTrace();
        }
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("status", "error");
        errorResponse.addProperty("message", message != null ? message : "Unknown error occurred");

        response.getWriter().write(gson.toJson(errorResponse));
    }
}