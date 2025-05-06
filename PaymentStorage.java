package common.storage;

import common.models.Course;
import common.payment.Payment;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentStorage {
    private static final String DATA_DIR = System.getProperty("catalina.base") + "/webapps/Web/WEB-INF/data";
    private static final String PAYMENTS_FILE = DATA_DIR + "/payments.txt";
    private static final String PAYMENT_ID_COUNTER_FILE = DATA_DIR + "/payment_id_counter.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String DELIMITER = "|";

    static {
        initializeStorage();
        initializeCounterFile();
    }

    private static void initializeStorage() {
        try {
            File directory = new File(DATA_DIR);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + DATA_DIR);
            }

            File file = new File(PAYMENTS_FILE);
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Failed to create file: " + PAYMENTS_FILE);
            }
        } catch (IOException e) {
            System.err.println("Payment storage initialization error:");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize payment storage", e);
        }
    }

    private static void initializeCounterFile() {
        try {
            File counterFile = new File(PAYMENT_ID_COUNTER_FILE);
            int maxPaymentId = getAllPayments().stream()
                    .mapToInt(Payment::getPaymentId)
                    .max()
                    .orElse(0);

            int counterValue = maxPaymentId + 1;

            if (counterFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(counterFile))) {
                    String line = reader.readLine();
                    if (line != null) {
                        int fileCounterValue = Integer.parseInt(line.trim());
                        counterValue = Math.max(fileCounterValue, counterValue);
                    }
                } catch (IOException | NumberFormatException e) {
                    System.err.println("Error reading counter file: " + e.getMessage());
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(counterFile))) {
                writer.write(String.valueOf(counterValue));
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize payment ID counter file: " + e.getMessage());
            throw new RuntimeException("Payment ID counter initialization failed", e);
        }
    }

    public static synchronized void createPayment(Payment payment) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENTS_FILE, true))) {
            payment.setPaymentId(generateNextPaymentId());
            String line = formatPayment(payment);
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error creating payment: " + e.getMessage());
            throw new RuntimeException("Payment creation failed", e);
        }
    }
    private static String formatPayment(Payment payment) {
        return String.join(DELIMITER,
                String.valueOf(payment.getPaymentId()),
                String.valueOf(payment.getStudentId()),
                String.valueOf(payment.getCourseId()),
                String.valueOf(payment.getAmount()),
                DATE_FORMAT.format(payment.getPaymentDate()),
                payment.getStatus(),
                payment.getBankDetails().replace("|", "~"), // Sanitize input
                payment.getAdminApprovalDate() != null ?
                        payment.getAdminApprovalDate() : "null"
        );
    }
    private static synchronized int generateNextPaymentId() {
        File counterFile = new File(PAYMENT_ID_COUNTER_FILE);
        int currentId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(counterFile))) {
            String line = reader.readLine();
            currentId = line != null ? Integer.parseInt(line.trim()) : 0;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading payment ID counter: " + e.getMessage());
            throw new RuntimeException("Failed to generate payment ID", e);
        }

        int nextId = currentId + 1;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(counterFile))) {
            writer.write(String.valueOf(nextId));
        } catch (IOException e) {
            System.err.println("Error updating payment ID counter: " + e.getMessage());
            throw new RuntimeException("Failed to generate payment ID", e);
        }

        return nextId;
    }

    public static synchronized List<Payment> getPendingPayments() {
        return getAllPayments().stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    public static synchronized boolean approvePayment(int paymentId) {
        List<Payment> payments = getAllPayments();
        boolean found = false;

        for (Payment payment : payments) {
            if (payment.getPaymentId() == paymentId && "PENDING".equals(payment.getStatus())) {
                payment.setStatus("APPROVED");
                payment.setAdminApprovalDate(DATE_FORMAT.format(new Date()));
                found = true;
                break;
            }
        }

        if (found) {
            saveAllPayments(payments);
            return true;
        }
        return false;
    }

    public static synchronized Payment getApprovedPayment(int studentId, int courseId) {
        return getAllPayments().stream()
                .filter(p -> p.getStudentId() == studentId
                        && p.getCourseId() == courseId
                        && "APPROVED".equals(p.getStatus()))
                .findFirst()
                .orElse(null);
    }

    private static List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Processing payment line: " + line);
                Payment payment = parsePayment(line);
                if (payment != null) {
                    System.out.println("Valid payment: " + payment.getPaymentId());
                    payments.add(payment);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading payments: " + e.getMessage());
        }
        System.out.println("Total valid payments loaded: " + payments.size());
        return payments;
    }

    public static synchronized void updatePayment(Payment updatedPayment) {
        try {
            List<Payment> payments = getAllPayments();

            if (payments.stream().noneMatch(p -> p.getPaymentId() == updatedPayment.getPaymentId())) {
                throw new RuntimeException("Payment not found for ID: " + updatedPayment.getPaymentId());
            }

            payments.replaceAll(p -> p.getPaymentId() == updatedPayment.getPaymentId() ? updatedPayment : p);
            saveAllPayments(payments);
        } catch (Exception e) {
            System.err.println("Failed to update payment: " + e.getMessage());
            throw new RuntimeException("Payment update failed", e);
        }
    }

    private static void saveAllPayments(List<Payment> payments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENTS_FILE))) {
            for (Payment payment : payments) {
                writer.write(formatPayment(payment));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving payments: " + e.getMessage());
            throw new RuntimeException("Payment save failed", e);
        }
    }


    private static Payment parsePayment(String line) {
        String[] parts = line.split("\\" + DELIMITER, 8);
        if (parts.length != 8) {
            System.err.println("Invalid payment format: " + line);
            return null;
        }

        try {
            Payment payment = new Payment();
            payment.setPaymentId(Integer.parseInt(parts[0]));
            payment.setStudentId(Integer.parseInt(parts[1]));
            payment.setCourseId(Integer.parseInt(parts[2]));
            payment.setAmount(Double.parseDouble(parts[3]));
            payment.setPaymentDate(DATE_FORMAT.parse(parts[4]));
            payment.setStatus(parts[5]);
            payment.setBankDetails(parts[6].replace("~", "|"));
            payment.setAdminApprovalDate(parts[7].equals("null") ? null : parts[7]);
            return payment;
        } catch (Exception e) {
            System.err.println("Error parsing payment: " + line);
            e.printStackTrace();
            return null;
        }
    }
    public static synchronized Payment getPaymentById(int paymentId) {
        return getAllPayments().stream()
                .filter(p -> p.getPaymentId() == paymentId)
                .findFirst()
                .orElse(null);

    }

    public static synchronized List<Payment> getPaymentsByStudent(int studentId) {
        try {
            List<Payment> all = getAllPayments();
            System.out.println("Total payments in system: " + all.size());
            return all.stream()
                    .filter(p -> p.getStudentId() == studentId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting payments for student " + studentId);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static synchronized double getEarningsByInstructor(int instructorId) {
        return getAllPayments().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .mapToDouble(p -> {
                    try {
                        Course course = CourseStorage.getCourseById(p.getCourseId());
                        return (course != null && course.getInstructorId() == instructorId)
                                ? p.getAmount() : 0.0;
                    } catch (IOException e) {
                        System.err.println("Error fetching course: " + e.getMessage());
                        return 0.0;
                    }
                })
                .sum();
    }
    public static synchronized boolean deletePayment(int paymentId) {
        List<Payment> payments = getAllPayments();
        boolean removed = payments.removeIf(p -> p.getPaymentId() == paymentId);
        if (removed) {
            saveAllPayments(payments);
            return true;
        }
        return false;
    }
    public static synchronized List<Payment> getPaymentsByCourse(int courseId) {
        return getAllPayments().stream()
                .filter(p -> p.getCourseId() == courseId)
                .collect(Collectors.toList());
    }
}