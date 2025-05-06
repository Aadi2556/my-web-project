document.addEventListener('DOMContentLoaded', () => {
    loadStudentList();
});

async function loadStudentList() {
    const token = localStorage.getItem('adminJwtToken');
    const errorMessage = document.getElementById('error-message');

    if (!token) {
        console.error('No adminJwtToken found in localStorage');
        if (errorMessage) {
            errorMessage.textContent = 'Please log in as an admin';
        }
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/Web/secure/studentList', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const text = await response.text();
            console.error('Server response:', text);
            throw new Error(`HTTP error! status: ${response.status}, message: ${text}`);
        }

        const data = await response.json();
        displayStudents(data);
    } catch (error) {
        console.error('Error loading students:', error);
        if (errorMessage) {
            errorMessage.textContent = `Failed to load students: ${error.message}`;
        }
    }
}

function displayStudents(students) {
    const studentList = document.getElementById('student-list');
    if (!studentList) {
        console.error('No element with id "student-list" found');
        return;
    }

    studentList.innerHTML = '';
    students.forEach(student => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${student.studentId}</td>
            <td>${student.name}</td>
            <td>${student.email}</td>
            <td>${new Date(student.registrationTime).toLocaleString()}</td>
        `;
        studentList.appendChild(row);
    });
}

function goBack() {
    window.location.href = 'admindashboard.html';
}