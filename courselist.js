document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('http://localhost:8080/Web/courses');
        const courses = await response.json();
        const container = document.getElementById('courses-list');

        courses.forEach(course => {
            // Only show approved courses
            if (course.status === 'approved') {
                container.innerHTML += `
                    <div class="course-card">
                        <h3>${course.title}</h3>
                        <p>${course.description}</p>
                        <p>Price: $${course.price?.toFixed(2) || '0.00'}</p>
                        <p>Instructor ID: ${course.instructorId}</p>
                        <button class="enroll-btn" data-course-id="${course.courseId}">Enroll Now</button>
                    </div>
                `;
            }
        });

        // Add event listener for enrollment buttons
        container.addEventListener('click', async (e) => {
            if (e.target.classList.contains('enroll-btn')) {
                const courseId = e.target.dataset.courseId;
                await handleEnrollment(courseId);
            }
        });
    } catch (error) {
        console.error('Error loading courses:', error);
    }
});

async function handleEnrollment(courseId) {
    try {
        const response = await fetch(`http://localhost:8080/Web/course/${courseId}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const course = await response.json();

        if (course.status !== 'approved') {
            alert('Course is no longer available for enrollment');
            return;
        }

        window.location.href = `payment.html?courseId=${courseId}&price=${course.price}`;
    } catch (error) {
        console.error('Enrollment error:', error);
        alert(error.message || 'Error initiating enrollment process');
    }
}