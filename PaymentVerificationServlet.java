package student;

import com.google.gson.Gson;
import common.payment.Payment;
import common.security.JwtUtil;
import common.storage.PaymentStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/secure/payment/course/*")
public class PaymentVerificationServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Get authenticated student ID
            int studentId = JwtUtil.getUserIdFromRequest(request);

            // Extract course ID from URL path
            String[] pathParts = request.getPathInfo().split("/");
            if (pathParts.length < 2) {
                response.setStatus(400);
                out.write("{\"error\":\"Missing course ID\"}");
                return;
            }

            int courseId = Integer.parseInt(pathParts[1]);

            // Find pending payment
            List<Payment> payments = PaymentStorage.getPaymentsByStudent(studentId);
            Payment payment = payments.stream()
                    .filter(p -> p.getCourseId() == courseId)
                    .filter(p -> "PENDING".equals(p.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (payment == null) {
                response.setStatus(404);
                out.write("{\"error\":\"No pending payment found\"}");
                return;
            }

            out.write("{\"valid\":true}");
        } catch (NumberFormatException e) {
            response.setStatus(400);
            out.write("{\"error\":\"Invalid course ID format\"}");
        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\":\"Server error\"}");
            e.printStackTrace();
        }
    }
}