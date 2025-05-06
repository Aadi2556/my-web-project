document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('instructorJwtToken');
    if (!token) window.location.href = 'instructorlogin.html';

    try {
        const response = await fetch('http://localhost:8080/Web/secure/lobby', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const courses = await response.json();
        const container = document.getElementById('lobby-courses');
        container.innerHTML = '';

        courses.forEach(course => {
            const card = document.createElement('div');
            card.className = `course-card ${course.status}`;
            card.innerHTML = `
                <h3>${course.title}</h3>
                <p>Course ID: ${course.courseId}</p>
                <p>Price: $${(course.price || 0).toFixed(2)}</p>
                <p>Instructor ID: ${course.instructorId}</p>
                <p>Status: <strong>${course.status?.toUpperCase() || 'PENDING'}</strong></p>
                <p>${course.description}</p>
            `;
            container.appendChild(card);
        });

        // Redirect to dashboard after 2 seconds
        setTimeout(() => {
            window.location.href = 'instructordashboard.html';
        }, 2000);

    } catch (error) {
        console.error('Error loading lobby:', error);
        // Redirect even if there's an error
        setTimeout(() => {
            window.location.href = 'instructordashboard.html';
        }, 2000);
    }
});