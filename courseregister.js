document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('adminJwtToken');
    if (!token) {
        window.location.href = 'adminlogin.html';
        return;
    }

    loadCourseList();
});

async function loadCourseList() {
    const errorMessage = document.getElementById('error-message');
    const courseList = document.getElementById('course-list');

    try {
        const response = await fetch('http://localhost:8080/Web/secure/courseRegister', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('adminJwtToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const text = await response.text();
            console.error('Server response:', text);
            throw new Error(`HTTP error! status: ${response.status}, message: ${text}`);
        }

        const courses = await response.json();
        courseList.innerHTML = await Promise.all(courses.map(async course => {
            const instructorName = await getInstructorName(course.instructorId);
            return `
                <tr>
                    <td>${course.title || 'Untitled'}</td>
                    <td>${course.instructorId}</td>
                    <td>${instructorName}</td>
                    <td>$${course.price.toFixed(2)}</td>
                    <td>${new Date(course.createdAt).toLocaleString()}</td>
                </tr>
            `;
        })).then(rows => rows.join(''));
    } catch (error) {
        console.error('Error loading courses:', error);
        if (errorMessage) {
            errorMessage.textContent = `Failed to load course list: ${error.message}`;
        }
    }
}

async function getInstructorName(instructorId) {
    try {
        const response = await fetch(`http://localhost:8080/Web/secure/instructorName?id=${instructorId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('adminJwtToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const text = await response.text();
            console.error('Instructor name response:', text);
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return data.name || 'Unknown';
    } catch (error) {
        console.error('Error fetching instructor name:', error);
        return 'Unknown';
    }
}

function goBack() {
    window.location.href = 'admindashboard.html';
}