package admin;

import common.storage.PaymentStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/secure/approvePayment")
public class ApprovePaymentServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        try {
            int paymentId = Integer.parseInt(request.getParameter("paymentId"));

            if (PaymentStorage.approvePayment(paymentId)) {
                response.getWriter().write(
                        "{\"status\":\"success\",\"message\":\"Payment approved\"}"
                );
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Payment not found or already processed");
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid payment ID format");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Payment approval failed");
            e.printStackTrace();
        }
    }
}