package common.storage;

import common.models.Course;
import common.models.StudentEnrollment;
import common.payment.Payment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnrollmentStorage {

    public static List<Map<String, Object>> getEnrollmentsWithPaymentStatus(int studentId) throws IOException {
        List<Map<String, Object>> enrollments = new ArrayList<>();

        List<StudentEnrollment> studentEnrollments = CourseStorage.getEnrollmentsForStudent(studentId);
        List<Payment> payments = PaymentStorage.getPaymentsByStudent(studentId);

        for (StudentEnrollment enrollment : studentEnrollments) {
            Map<String, Object> enrollmentData = new HashMap<>();
            Course course = CourseStorage.getCourseById(enrollment.getCourseId());

            System.out.println("Student enrollments: " +
                    CourseStorage.getEnrollmentsForStudent(studentId));

            if (course != null) {
                Payment payment = payments.stream()
                        .filter(p -> p.getCourseId() == enrollment.getCourseId())
                        .findFirst()
                        .orElse(null);

                enrollmentData.put("paymentId",
                        payment != null ? payment.getPaymentId() : null);
                // In getEnrollmentsWithPaymentStatus()
                System.out.println("Checking enrollment: " + enrollment.getCourseId());
                System.out.println("Matching payments: " + payments.stream()
                        .filter(p -> p.getCourseId() == enrollment.getCourseId())
                        .collect(Collectors.toList()));

                enrollmentData.put("courseId", course.getCourseId());
                enrollmentData.put("courseTitle", course.getTitle());
                enrollmentData.put("enrolledAt", enrollment.getEnrolledAt());
                enrollmentData.put("paymentStatus", payment != null ? payment.getStatus() : "PENDING");
                enrollmentData.put("amountPaid", payment != null ? payment.getAmount() : 0);
                enrollmentData.put("paymentId", payment != null ? payment.getPaymentId() : null);
                enrollments.add(enrollmentData);
            }
        }

        return enrollments;
    }
}