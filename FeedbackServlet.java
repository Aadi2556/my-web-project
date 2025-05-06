package student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.models.Feedback;
import common.security.JwtUtil;
import common.storage.FeedbackStorage;
import common.storage.PaymentStorage;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@WebServlet("/secure/feedback")
public class FeedbackServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int studentId = claims.get("userId", Integer.class);
            String studentName = claims.get("userName", String.class);

            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);
            int courseId = jsonObject.get("courseId").getAsInt();
            String comment = jsonObject.get("comment").getAsString();
            int rating = jsonObject.get("rating").getAsInt();

            // Validate rating
            if (rating < 1 || rating > 5) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Rating must be between 1 and 5");
                return;
            }

            // Verify approved payment
            if (PaymentStorage.getApprovedPayment(studentId, courseId) == null) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "No approved payment for this course");
                return;
            }

            Feedback feedback = new Feedback();
            feedback.setCourseId(courseId);
            feedback.setStudentId(studentId);
            feedback.setStudentName(studentName);
            feedback.setComment(comment);
            feedback.setRating(rating);
            feedback.setSubmittedAt(new Date());

            FeedbackStorage.createFeedback(feedback);

            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("message", "Feedback submitted successfully");
            response.addProperty("feedbackId", feedback.getFeedbackId());
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Feedback submission failed: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int studentId = claims.get("userId", Integer.class);

            String feedbackIdParam = req.getParameter("feedbackId");
            String courseIdParam = req.getParameter("courseId");

            if (feedbackIdParam != null) {
                // Get specific feedback by ID
                int feedbackId = Integer.parseInt(feedbackIdParam);
                Feedback feedback = FeedbackStorage.getFeedbackByIdAndStudent(feedbackId, studentId);
                if (feedback == null) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Feedback not found or unauthorized");
                    return;
                }
                resp.getWriter().write(gson.toJson(feedback));
            } else if (courseIdParam != null) {
                // Get all feedback for a course
                int courseId = Integer.parseInt(courseIdParam);
                List<Feedback> feedbackList = FeedbackStorage.getFeedbackByCourse(courseId);
                resp.getWriter().write(gson.toJson(feedbackList));
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing feedbackId or courseId parameter");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid feedback ID or course ID");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve feedback: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int studentId = claims.get("userId", Integer.class);
            String studentName = claims.get("userName", String.class);

            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);
            int feedbackId = jsonObject.get("feedbackId").getAsInt();
            String comment = jsonObject.get("comment").getAsString();
            int rating = jsonObject.get("rating").getAsInt();

            // Validate rating
            if (rating < 1 || rating > 5) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Rating must be between 1 and 5");
                return;
            }

            // Retrieve existing feedback to verify ownership
            Feedback existingFeedback = FeedbackStorage.getFeedbackByIdAndStudent(feedbackId, studentId);
            if (existingFeedback == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Feedback not found or unauthorized");
                return;
            }

            // Update feedback
            existingFeedback.setComment(comment);
            existingFeedback.setRating(rating);
            existingFeedback.setSubmittedAt(new Date()); // Update timestamp
            existingFeedback.setStudentName(studentName); // Update name in case it changed

            FeedbackStorage.updateFeedback(existingFeedback);

            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("message", "Feedback updated successfully");
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Feedback update failed: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int studentId = claims.get("userId", Integer.class);

            int feedbackId = Integer.parseInt(req.getParameter("feedbackId"));

            FeedbackStorage.deleteFeedback(feedbackId, studentId);

            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("message", "Feedback deleted successfully");
            resp.getWriter().write(gson.toJson(response));

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid feedback ID");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Feedback deletion failed: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);
        resp.getWriter().write(gson.toJson(error));
    }
}