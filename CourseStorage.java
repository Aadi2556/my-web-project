package common.storage;

import common.models.Course;
import common.models.StudentEnrollment;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CourseStorage {
    private static final String DATA_DIR = System.getProperty("catalina.base") + "/webapps/Web/WEB-INF/data";
    private static final String COURSES_FILE = DATA_DIR + "/courses.txt";
    private static final String ENROLLMENTS_FILE = DATA_DIR + "/enrollments.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void createCourse(Course course) throws IOException {
        course.setCourseId(getNextCourseId());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COURSES_FILE, true))) {
            writer.write(formatCourse(course));
            writer.newLine();
        }
    }

    private static String formatCourse(Course course) {
        return String.join("|",
                String.valueOf(course.getCourseId()),
                course.getTitle() != null ? course.getTitle() : "",
                course.getDescription() != null ? course.getDescription() : "",
                String.valueOf(course.getInstructorId()),
                DATE_FORMAT.format(course.getCreatedAt() != null ? course.getCreatedAt() : new Date()),
                String.format("%.2f", course.getPrice()),
                String.join(",", course.getFiles() != null ? course.getFiles() : Collections.emptyList()),
                String.join(",", course.getMessages() != null ? course.getMessages() : Collections.emptyList()),
                course.getStatus() != null ? course.getStatus() : ""
        );
    }

    public static List<Course> getAllCourses() throws IOException {
        List<Course> courses = new ArrayList<>();
        File file = new File(COURSES_FILE);
        if (!file.exists()) return courses;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Course course = parseCourse(line);
                if (course != null) courses.add(course);
            }
        }
        return courses;
    }

    private static Course parseCourse(String line) {
        String[] parts = line.split("\\|", -1); // Handle empty fields
        if (parts.length < 9) {
            System.err.println("Invalid course record: " + line);
            return null;
        }

        try {
            Course course = new Course();
            course.setCourseId(Integer.parseInt(parts[0]));
            course.setTitle(parts[1]);
            course.setDescription(parts[2]);
            course.setInstructorId(Integer.parseInt(parts[3]));
            course.setCreatedAt(DATE_FORMAT.parse(parts[4]));
            course.setPrice(parts[5].isEmpty() ? 0.0 : Double.parseDouble(parts[5]));
            course.setFiles(parts[6].isEmpty() ? Collections.emptyList() : Arrays.asList(parts[6].split(",", -1)));
            course.setMessages(parts[7].isEmpty() ? Collections.emptyList() : Arrays.asList(parts[7].split(",", -1)));
            course.setStatus(parts[8]);
            return course;
        } catch (ParseException | NumberFormatException e) {
            System.err.println("Error parsing course: " + line);
            e.printStackTrace();
            return null;
        }
    }

    public static void enrollStudent(StudentEnrollment enrollment) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ENROLLMENTS_FILE, true))) {
            writer.write(formatEnrollment(enrollment));
            writer.newLine();
        }
    }

    public static void updateCourse(Course updated) throws IOException {
        List<Course> courses = getAllCourses();
        courses.replaceAll(c -> c.getCourseId() == updated.getCourseId() ? updated : c);
        writeAllCourses(courses);
    }

    private static String formatEnrollment(StudentEnrollment enrollment) {
        return String.join("|",
                String.valueOf(enrollment.getCourseId()),
                String.valueOf(enrollment.getStudentId()),
                DATE_FORMAT.format(enrollment.getEnrolledAt() != null ? enrollment.getEnrolledAt() : new Date())
        );
    }

    public static List<Course> getCoursesByInstructor(int instructorId) {
        List<Course> courses = new ArrayList<>();
        try {
            List<Course> allCourses = getAllCourses();
            for (Course course : allCourses) {
                if (course.getInstructorId() == instructorId) {
                    courses.add(course);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public static List<StudentEnrollment> getEnrollmentsForStudent(int studentId) throws IOException {
        return getAllEnrollments().stream()
                .filter(e -> e.getStudentId() == studentId)
                .collect(Collectors.toList());
    }

    public static List<StudentEnrollment> getEnrollmentsForCourse(int courseId) throws IOException {
        return getAllEnrollments().stream()
                .filter(e -> e.getCourseId() == courseId)
                .sorted(Comparator.comparing(StudentEnrollment::getEnrolledAt).reversed())
                .collect(Collectors.toList());
    }

    public static List<StudentEnrollment> getAllEnrollments() throws IOException {
        List<StudentEnrollment> enrollments = new ArrayList<>();
        File file = new File(ENROLLMENTS_FILE);
        if (!file.exists()) return enrollments;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                StudentEnrollment enrollment = parseEnrollment(line);
                if (enrollment != null) enrollments.add(enrollment);
            }
        }
        return enrollments;
    }

    private static StudentEnrollment parseEnrollment(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 3) {
            System.err.println("Invalid enrollment record: " + line);
            return null;
        }

        try {
            StudentEnrollment enrollment = new StudentEnrollment();
            enrollment.setCourseId(Integer.parseInt(parts[0]));
            enrollment.setStudentId(Integer.parseInt(parts[1]));
            enrollment.setEnrolledAt(parts[2].isEmpty() ? new Date() : DATE_FORMAT.parse(parts[2]));
            return enrollment;
        } catch (ParseException | NumberFormatException e) {
            System.err.println("Error parsing enrollment: " + line);
            e.printStackTrace();
            return null;
        }
    }

    private static int getNextCourseId() throws IOException {
        return getAllCourses().stream()
                .mapToInt(Course::getCourseId)
                .max()
                .orElse(0) + 1;
    }

    public static void deleteCourse(int courseId) throws IOException {
        List<Course> courses = getAllCourses();
        courses.removeIf(c -> c.getCourseId() == courseId);
        writeAllCourses(courses);

        List<StudentEnrollment> enrollments = getAllEnrollments();
        enrollments.removeIf(e -> e.getCourseId() == courseId);
        writeAllEnrollments(enrollments);
    }

    public static synchronized void deleteEnrollment(int studentId, int courseId) throws IOException {
        List<StudentEnrollment> enrollments = getAllEnrollments();
        boolean removed = enrollments.removeIf(e ->
                e.getStudentId() == studentId && e.getCourseId() == courseId
        );

        if (removed) {
            writeAllEnrollments(enrollments);
        } else {
            throw new IOException("Enrollment not found for deletion");
        }
    }

    private static void writeAllCourses(List<Course> courses) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COURSES_FILE))) {
            for (Course course : courses) {
                writer.write(formatCourse(course));
                writer.newLine();
            }
        }
    }

    private static void writeAllEnrollments(List<StudentEnrollment> enrollments) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ENROLLMENTS_FILE))) {
            for (StudentEnrollment enrollment : enrollments) {
                writer.write(formatEnrollment(enrollment));
                writer.newLine();
            }
        }
    }

    public static Course getCourseById(int courseId) throws IOException {
        return getAllCourses().stream()
                .filter(c -> c.getCourseId() == courseId)
                .findFirst()
                .orElse(null);
    }

    public static boolean isCourseOwner(int courseId, int instructorId) throws IOException {
        Course course = getCourseById(courseId);
        return course != null && course.getInstructorId() == instructorId;
    }
}