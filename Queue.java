package common.utils;

import common.models.Course;

public class Queue {
    private static class Node {
        Course data;
        Node next;

        Node(Course data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node front;
    private Node rear;

    public Queue() {
        this.front = null;
        this.rear = null;
    }

    // Enqueue courses in order of creation date
    public void enqueue(Course course) {
        Node newNode = new Node(course);
        if (isEmpty()) {
            front = rear = newNode;
        } else {
            // Insert in order of creation date (earlier first)
            Node current = front;
            Node prev = null;
            while (current != null && current.data.getCreatedAt().before(course.getCreatedAt())) {
                prev = current;
                current = current.next;
            }
            if (prev == null) {
                newNode.next = front;
                front = newNode;
            } else if (current == null) {
                rear.next = newNode;
                rear = newNode;
            } else {
                prev.next = newNode;
                newNode.next = current;
            }
        }
    }

    // Dequeue to retrieve courses
    public Course dequeue() {
        if (isEmpty()) {
            return null;
        }
        Course data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        return data;
    }

    public boolean isEmpty() {
        return front == null;
    }

    // Convert queue to array for JSON serialization
    public Course[] toArray() {
        java.util.ArrayList<Course> courses = new java.util.ArrayList<>();
        Node current = front;
        while (current != null) {
            courses.add(current.data);
            current = current.next;
        }
        return courses.toArray(new Course[0]);
    }
}