package admin;

import common.models.Course;
import common.payment.Payment;
import common.storage.CourseStorage;
import common.storage.PaymentStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/secure/pendingPayments")
public class PendingPaymentsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        try {
            List<Payment> pendingPayments = PaymentStorage.getPendingPayments();
            String jsonResponse = convertPaymentsToJson(pendingPayments);
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve pending payments");
            e.printStackTrace();
        }
    }

    private String convertPaymentsToJson(List<Payment> payments) {
        StringBuilder json = new StringBuilder("[");
        for (Payment p : payments) {
            try {
                Course course = CourseStorage.getCourseById(p.getCourseId());
                String courseTitle = course != null ? course.getTitle() : "Deleted Course";

                json.append(String.format(
                        "{\"id\":%d,\"studentId\":%d,\"courseId\":%d,\"courseTitle\":\"%s\",\"amount\":%.2f,\"date\":\"%s\"},",
                        p.getPaymentId(), p.getStudentId(), p.getCourseId(), courseTitle,
                        p.getAmount(), p.getPaymentDate()
                ));
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
        if (!payments.isEmpty()) json.deleteCharAt(json.length()-1);
        json.append("]");
        return json.toString();
    }
}