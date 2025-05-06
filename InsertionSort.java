package common.utils;

import student.Student;

public class InsertionSort {
    public static void sortByRegistrationTime(Student[] students) {
        int n = students.length;
        for (int i = 1; i < n; i++) {
            Student key = students[i];
            int j = i - 1;

            // Compare registration times and shift elements
            while (j >= 0 && students[j].getRegistrationTime().after(key.getRegistrationTime())) {
                students[j + 1] = students[j];
                j--;
            }
            students[j + 1] = key;
        }
    }
}