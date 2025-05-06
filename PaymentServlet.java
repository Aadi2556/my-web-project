package student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.models.Course;
import common.models.StudentEnrollment;
import common.payment.Payment;
import common.security.JwtUtil;
import common.storage.CourseStorage;
import common.storage.PaymentStorage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/secure/makePayment")
public class PaymentServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            int studentId = claims.get("userId", Integer.class);

            JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);
            int courseId = jsonObject.get("courseId").getAsInt();
            String cardNumber = jsonObject.get("cardNumber").getAsString();
            String cvv = jsonObject.get("cvv").getAsString();

            Course course = CourseStorage.getCourseById(courseId);
            if (course == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Course not found");
                return;
            }
            if (!"approved".equalsIgnoreCase(course.getStatus())) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Course not approved for enrollment");
                return;
            }

            Payment payment = createPaymentRecord(studentId, course, cardNumber, cvv);
            PaymentStorage.createPayment(payment);

            createEnrollment(studentId, courseId);
            payment.setBankDetails(cardNumber.replace("|", "-") + "~" + cvv.replace("|", ""));
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("paymentId", payment.getPaymentId());
            responseBody.put("message", "Payment and enrollment processed successfully");

            response.getWriter().write(gson.toJson(responseBody));

        } catch (ExpiredJwtException e) {
            handleJwtError(response, "Token has expired");
        } catch (MalformedJwtException e) {
            handleJwtError(response, "Invalid token format");
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID format");
        } catch (Exception e) {
            handleGenericError(response, e);
        }
    }
    private Payment createPaymentRecord(int studentId, Course course, String cardNumber, String cvv) {
        Payment payment = new Payment();
        payment.setStudentId(studentId);
        payment.setCourseId(course.getCourseId());
        payment.setAmount(course.getPrice());
        payment.setPaymentDate(new Date());

        // Sanitize bank details
        String sanitizedDetails = cardNumber.replace("|", "") + "~" + cvv.replace("|", "");
        payment.setBankDetails(sanitizedDetails);

        payment.setStatus("PENDING");
        return payment;
    }

    private void createEnrollment(int studentId, int courseId) throws IOException {
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setEnrolledAt(new Date());
        CourseStorage.enrollStudent(enrollment);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);
        response.getWriter().write(gson.toJson(error));
    }

    private void handleJwtError(HttpServletResponse response, String message) throws IOException {
        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    private void handleGenericError(HttpServletResponse response, Exception e) throws IOException {
        e.printStackTrace();
        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payment processing failed: " + e.getMessage());
    }
}