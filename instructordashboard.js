const instructorToken = localStorage.getItem('instructorJwtToken');
if (!instructorToken) {
    window.location.href = 'instructorlogin.html';
} else {
    displayInstructorInfo();
    loadInstructorDashboard();
}

function displayInstructorInfo() {
    const instructorName = localStorage.getItem('instructorUserName');
    const instructorEmail = localStorage.getItem('instructorUserEmail');
    const instructorId = localStorage.getItem('instructorUserId');
    const instructorAddress = localStorage.getItem('instructorAddress');
    const instructorAge = localStorage.getItem('instructorAge');
    const instructorPhone = localStorage.getItem('instructorPhoneNumber');

    const welcomeDiv = document.getElementById('instructor-welcome-message');
    const infoDiv = document.getElementById('instructor-user-info');

    if (welcomeDiv) {
        welcomeDiv.innerHTML = `<h1>Welcome ${instructorName || 'Instructor'}!</h1>`;
    }

    let infoHTML = `
        <p>Email: <strong>${instructorEmail || 'N/A'}</strong></p>
        <p>Instructor ID: <strong>${instructorId || 'N/A'}</strong></p>
    `;

    if (instructorAddress) infoHTML += `<p>Address: <strong>${instructorAddress}</strong></p>`;
    if (instructorAge) infoHTML += `<p>Age: <strong>${instructorAge}</strong></p>`;
    if (instructorPhone) infoHTML += `<p>Phone: <strong>${instructorPhone}</strong></p>`;

    if (infoDiv) {
        infoDiv.innerHTML = infoHTML;
    }
}

async function loadInstructorDashboard() {
    try {
        const response = await fetch('http://localhost:8080/Web/secure/instructorDashboard', {
            headers: { 
                'Authorization': `Bearer ${instructorToken}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 401) {
            clearInstructorData();
            window.location.href = 'instructorlogin.html';
            return;
        }

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log("Instructor dashboard data:", data);
        
        displayCourses(data.courses.courses); // Now data.courses is an array
        
    } catch (error) {
        console.error('Error loading instructor dashboard:', error);
        const errorElement = document.getElementById('instructor-error');
        if (errorElement) {
            errorElement.textContent = 'Error loading data. Please try again.';
        }
        if (error.message.includes('401')) {
            clearInstructorData();
            window.location.href = 'instructorlogin.html';
        }
    }
}

function displayCourses(coursesArray) {
    const coursesList = document.getElementById('courses-list');
    if (!coursesList || !Array.isArray(coursesArray)) return;

    coursesList.innerHTML = `
        <h2>My Courses</h2>
        <div class="courses-container"></div>
    `;
    
    const container = coursesList.querySelector('.courses-container');
    const fragment = document.createDocumentFragment();

    coursesArray.forEach(course => {
        const status = course.status?.toLowerCase() || 'pending';
        const card = document.createElement('div');
        card.className = `course-card ${status}`;
        card.innerHTML = `
            <h3>${course.title}</h3>
            <p>ID: ${course.id} | Status: ${status.toUpperCase()}</p>
            <p>Price: $${(course.price || 0).toFixed(2)}</p>
            <div class="course-actions">
                <button onclick="editCourse(${course.id})">Edit</button>
                <button onclick="deleteCourse(${course.id})">Delete</button>
            </div>
            <div class="feedback-section">
                <h4 onclick="toggleFeedback(this)">Course Feedback</h4>
                <ul class="feedback-list">
                    ${course.feedback && course.feedback.length > 0 ?
                course.feedback.map(fb => `
                        <li class="feedback-item">
                            <div class="feedback-header">
                                <span>${fb.studentName}</span>
                                <span>Rating: ${fb.rating}/5</span>
                            </div>
                            <div class="feedback-comment">${fb.comment}</div>
                        </li>
                    `).join('')
                : '<li>No feedback yet</li>'}
                </ul>
            </div>
        `;
        fragment.appendChild(card);
    });

    container.appendChild(fragment);
}

function toggleFeedback(header) {
    const feedbackList = header.nextElementSibling;
    feedbackList.classList.toggle('active');
}

function instructorEdit() {
    window.location.href = 'instructorupdate.html';
}

function createCourse() {
    window.location.href = 'courseCreate.html';
}

function instructorLogout() {
    fetch('http://localhost:8080/Web/instructorLogout', {
        method: 'POST',
        headers: {
           'Authorization': `Bearer ${localStorage.getItem('instructorJwtToken')}`
        }
    }).finally(() => {
        clearInstructorData();
        window.location.href = 'instructorlogin.html';
    });
}

async function instructorDeleteProfile() {
    const confirmation = confirm("Are you sure you want to delete your instructor profile? This action cannot be undone!");
    if (!confirmation) return;

    const token = localStorage.getItem('instructorJwtToken');
    if (!instructorToken) {
        window.location.href = 'instructorlogin.html';
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/Web/secure/instructorDelete', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${instructorToken}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        
        if (response.ok) {
            clearInstructorData();
            alert('Instructor profile deleted successfully');
            window.location.href = 'instructorregister.html';
        } else {
            throw new Error(result.error || 'Instructor deletion failed');
        }
    } catch (error) {
        console.error('Instructor delete error:', error);
        alert(error.message);
    }
}

function editCourse(courseId) {
    window.location.href = `courseEdit.html?courseId=${courseId}`;
}

async function deleteCourse(courseId) {
    if (!confirm('Delete this course permanently?')) return;
    
    try {
        const response = await fetch(`http://localhost:8080/Web/secure/courseDelete?courseId=${courseId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${instructorToken}` }
        });
        
        if (response.ok) {
            loadInstructorDashboard(); // Refresh list
        }
    } catch (error) {
        alert('Delete failed');
    }
}

function clearInstructorData() {
    localStorage.removeItem('instructorJwtToken');
    localStorage.removeItem('instructorUserName');
    localStorage.removeItem('instructorUserEmail');
    localStorage.removeItem('instructorUserId');
    localStorage.removeItem('instructorAddress');
    localStorage.removeItem('instructorAge');
    localStorage.removeItem('instructorPhoneNumber');
}