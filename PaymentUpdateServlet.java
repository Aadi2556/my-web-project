package student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import common.payment.Payment;
import common.security.JwtUtil;
import common.storage.PaymentStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/secure/updatePayment")
public class PaymentUpdateServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        try {
            int studentId = JwtUtil.getUserIdFromRequest(request);
            JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

            if (!jsonObject.has("paymentId")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing payment ID");
                return;
            }

            int paymentId;
            try {
                paymentId = jsonObject.get("paymentId").getAsInt();
            } catch (NumberFormatException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid payment ID format");
                return;
            }

            Payment existing = PaymentStorage.getPaymentById(paymentId);
            if (existing == null || existing.getStudentId() != studentId) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Payment not found");
                return;
            }

            String cardNumber = jsonObject.get("cardNumber").getAsString();
            String cvv = jsonObject.get("cvv").getAsString();

            // Update existing payment instead of creating a new one
            existing.setBankDetails(cardNumber + "|" + cvv);
            existing.setStatus("PENDING");
            existing.setPaymentDate(new Date()); // Optionally update payment date

            PaymentStorage.updatePayment(existing);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("message", "Payment updated successfully");
            response.getWriter().write(gson.toJson(responseBody));

        } catch (Exception e) {
            handleGenericError(response, e);
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);
        response.getWriter().write(gson.toJson(error));
    }

    private void handleGenericError(HttpServletResponse response, Exception e) throws IOException {
        e.printStackTrace();
        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Payment update failed: " + e.getMessage());
    }
}