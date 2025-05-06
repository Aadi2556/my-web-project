package common.storage;

import common.models.Feedback;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FeedbackStorage {
    private static final String DATA_DIR = System.getProperty("catalina.base") + "/webapps/Web/WEB-INF/data";
    private static final String FEEDBACK_FILE = DATA_DIR + "/feedback.txt";
    private static final String FEEDBACK_ID_COUNTER_FILE = DATA_DIR + "/feedback_id_counter.txt";
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

            File file = new File(FEEDBACK_FILE);
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Failed to create file: " + FEEDBACK_FILE);
            }
        } catch (IOException e) {
            System.err.println("Feedback storage initialization error:");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize feedback storage", e);
        }
    }

    private static void initializeCounterFile() {
        try {
            File counterFile = new File(FEEDBACK_ID_COUNTER_FILE);
            int maxFeedbackId = getAllFeedback().stream()
                    .mapToInt(Feedback::getFeedbackId)
                    .max()
                    .orElse(0);

            int counterValue = maxFeedbackId + 1;

            if (counterFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(counterFile))) {
                    String line = reader.readLine();
                    if (line != null) {
                        int fileCounterValue = Integer.parseInt(line.trim());
                        counterValue = Math.max(fileCounterValue, counterValue);
                    }
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(counterFile))) {
                writer.write(String.valueOf(counterValue));
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize feedback ID counter file: " + e.getMessage());
            throw new RuntimeException("Feedback ID counter initialization failed", e);
        }
    }

    public static synchronized void createFeedback(Feedback feedback) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FEEDBACK_FILE, true))) {
            feedback.setFeedbackId(generateNextFeedbackId());
            writer.write(formatFeedback(feedback));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error creating feedback: " + e.getMessage());
            throw new RuntimeException("Feedback creation failed", e);
        }
    }

    public static synchronized Feedback getFeedbackByIdAndStudent(int feedbackId, int studentId) {
        return getAllFeedback().stream()
                .filter(f -> f.getFeedbackId() == feedbackId && f.getStudentId() == studentId)
                .findFirst()
                .orElse(null);
    }

    public static synchronized void updateFeedback(Feedback feedback) {
        List<Feedback> feedbackList = getAllFeedback();
        boolean found = false;

        for (int i = 0; i < feedbackList.size(); i++) {
            if (feedbackList.get(i).getFeedbackId() == feedback.getFeedbackId() &&
                    feedbackList.get(i).getStudentId() == feedback.getStudentId()) {
                feedbackList.set(i, feedback);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("Feedback not found or unauthorized");
        }

        rewriteFeedbackFile(feedbackList);
    }

    public static synchronized void deleteFeedback(int feedbackId, int studentId) {
        List<Feedback> feedbackList = getAllFeedback();
        int initialSize = feedbackList.size();

        feedbackList = feedbackList.stream()
                .filter(f -> !(f.getFeedbackId() == feedbackId && f.getStudentId() == studentId))
                .collect(Collectors.toList());

        if (feedbackList.size() == initialSize) {
            throw new RuntimeException("Feedback not found or unauthorized");
        }

        rewriteFeedbackFile(feedbackList);
    }

    private static void rewriteFeedbackFile(List<Feedback> feedbackList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FEEDBACK_FILE))) {
            for (Feedback feedback : feedbackList) {
                writer.write(formatFeedback(feedback));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error rewriting feedback file: " + e.getMessage());
            throw new RuntimeException("Failed to update feedback file", e);
        }
    }

    private static String formatFeedback(Feedback feedback) {
        return String.join(DELIMITER,
                String.valueOf(feedback.getFeedbackId()),
                String.valueOf(feedback.getCourseId()),
                String.valueOf(feedback.getStudentId()),
                feedback.getStudentName().replace("|", "~"),
                feedback.getComment().replace("|", "~"),
                String.valueOf(feedback.getRating()),
                DATE_FORMAT.format(feedback.getSubmittedAt())
        );
    }

    private static synchronized int generateNextFeedbackId() {
        File counterFile = new File(FEEDBACK_ID_COUNTER_FILE);
        int currentId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(counterFile))) {
            String line = reader.readLine();
            currentId = line != null ? Integer.parseInt(line.trim()) : 0;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading feedback ID counter: " + e.getMessage());
            throw new RuntimeException("Failed to generate feedback ID", e);
        }

        int nextId = currentId + 1;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(counterFile))) {
            writer.write(String.valueOf(nextId));
        } catch (IOException e) {
            System.err.println("Error updating feedback ID counter: " + e.getMessage());
            throw new RuntimeException("Failed to update feedback ID counter", e);
        }

        return nextId;
    }

    public static synchronized List<Feedback> getFeedbackByCourse(int courseId) {
        return getAllFeedback().stream()
                .filter(f -> f.getCourseId() == courseId)
                .collect(Collectors.toList());
    }

    private static List<Feedback> getAllFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FEEDBACK_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Feedback feedback = parseFeedback(line);
                if (feedback != null) {
                    feedbackList.add(feedback);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading feedback: " + e.getMessage());
        }
        return feedbackList;
    }

    private static Feedback parseFeedback(String line) {
        String[] parts = line.split("\\" + DELIMITER, 7);
        if (parts.length != 7) {
            System.err.println("Invalid feedback format: " + line);
            return null;
        }

        try {
            Feedback feedback = new Feedback();
            feedback.setFeedbackId(Integer.parseInt(parts[0]));
            feedback.setCourseId(Integer.parseInt(parts[1]));
            feedback.setStudentId(Integer.parseInt(parts[2]));
            feedback.setStudentName(parts[3].replace("~", "|"));
            feedback.setComment(parts[4].replace("~", "|"));
            feedback.setRating(Integer.parseInt(parts[5]));
            feedback.setSubmittedAt(DATE_FORMAT.parse(parts[6]));
            return feedback;
        } catch (Exception e) {
            System.err.println("Error parsing feedback: " + line);
            e.printStackTrace();
            return null;
        }
    }
}