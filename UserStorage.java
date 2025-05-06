package student;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class UserStorage {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String USERS_FILE = "users.txt";
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

    public static List<Student> getAllStudents() throws IOException {
        List<Student> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Reading line: [" + line + "]"); // Debug
                Student student = parseStudent(line);
                if (student != null) students.add(student);
            }
        }
        return students;
    }

    private static Student parseStudent(String line) {
        String[] parts = line.split(":", -1); // Handle empty fields
        System.out.println("Split parts: " + Arrays.toString(parts)); // Debug
        if (parts.length < 7) {
            System.err.println("Invalid student record: " + line);
            return null;
        }

        try {
            Student student = new Student();
            student.setStudentId(Integer.parseInt(parts[0]));
            student.setName(parts[1]);
            student.setEmail(parts[2]);

            // Handle registration time (index 7, if present)
            Date registrationTime = new Date(); // Default to current time
            if (parts.length > 7 && !parts[7].isEmpty()) {
                String dateStr = parts[7].trim().replaceAll("[^\\d\\-: ]", ""); // Sanitize
                System.out.println("Sanitized date string: [" + dateStr + "]"); // Debug
                if (DATE_PATTERN.matcher(dateStr).matches()) {
                    try {
                        registrationTime = DATE_FORMAT.parse(dateStr);
                    } catch (Exception e) {
                        System.err.println("Error parsing date: [" + dateStr + "] in line: " + line);
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Invalid date format: [" + dateStr + "] in line: " + line);
                }
            }
            student.setRegistrationTime(registrationTime);
            return student;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing student: " + line);
            e.printStackTrace();
            return null;
        }
    }

    public static void addUser(StudentRegister user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(String.format("%d:%s:%s:%s:::0:%s",
                    user.getUserId(),
                    user.getUserName(),
                    user.getUserEmail(),
                    user.getUserPassword(),
                    DATE_FORMAT.format(new Date())));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean validateUser(String email, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", -1);
                if (parts.length >= 7 && parts[2].equals(email) && parts[3].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static StudentRegister getStudentRegister(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", -1);
                if (parts.length >= 7 && parts[2].equals(email)) {
                    try {
                        return new StudentRegister(
                                Integer.parseInt(parts[0]),
                                parts[1],
                                parts[3],
                                parts[2]
                        );
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing student register: " + line);
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static StudentUpdate getStudentUpdate(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", -1);
                if (parts.length >= 7 && parts[2].equals(email)) {
                    try {
                        return new StudentUpdate(
                                Integer.parseInt(parts[0]),
                                parts[1],
                                parts[2],
                                parts[3],
                                parts.length > 4 ? parts[4] : "",
                                parts.length > 5 && !parts[5].isEmpty() ? Integer.parseInt(parts[5]) : 0,
                                parts.length > 6 && !parts[6].isEmpty() ? Integer.parseInt(parts[6]) : 0
                        );
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing student update: " + line);
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateUser(String email, StudentUpdate update) {
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", -1);
                if (parts.length >= 7 && parts[2].equals(email)) {
                    line = String.format("%d:%s:%s:%s:%s:%d:%d:%s",
                            update.getUserId(),
                            update.getUserName(),
                            update.getUserEmail(),
                            update.getUserPassword(),
                            update.getUserAddress(),
                            update.getUserAge(),
                            update.getUserPhoneNumber(),
                            parts.length > 7 ? parts[7] : DATE_FORMAT.format(new Date()));
                    found = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean deleteUser(String email) {
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", -1);
                if (parts.length >= 7 && parts[2].equals(email)) {
                    found = true;
                    continue;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}