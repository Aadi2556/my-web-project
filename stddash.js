const token = localStorage.getItem('jwtToken');
if (!token) {
    window.location.href = 'stdlogin.html';
}

// DOM Elements
const welcomeMessage = document.getElementById('welcomeMessage');
const userInfoDiv = document.getElementById('user-info');
const enrolledCoursesDiv = document.getElementById('enrolled-courses');
const errorMessageDiv = document.getElementById('errorMessage');

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        displayUserInfo();
        loadDashboard();
        setupEventListeners();
    }
});

function setupEventListeners() {
    document.getElementById('deleteProfileBtn').addEventListener('click', deleteProfile);
    document.getElementById('logoutBtn').addEventListener('click', logout);
    document.getElementById('editProfileBtn').addEventListener('click', editProfile);
    document.getElementById('enrollNewBtn').addEventListener('click', coursedash);
}

// Core Functions
async function loadDashboard() {
    try {
        console.log("Starting dashboard load...");
        const response = await fetch('http://localhost:8080/Web/secure/studentDashboard', {
            headers: getAuthHeaders()
        });

        console.log("Response status:", response.status);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log("Received data:", data);

        displayEnrolledCourses(data.enrollments || []);

    } catch (error) {
        console.error('Load error:', error);
        errorMessageDiv.textContent = error.message;
    }
}

function displayEnrolledCourses(enrollments) {
    const container = document.getElementById('enrolled-courses');
    const studentId = parseInt(localStorage.getItem('userId'));

    container.innerHTML = enrollments.map(enrollment => `
        <div class="course-card ${(enrollment.paymentStatus || 'pending').toLowerCase()}">
            <h3>${enrollment.courseTitle || 'Untitled Course'}</h3>
            <div class="course-meta">
                <span class="course-id">ID: ${enrollment.courseId}</span>
                <span class="enrollment-date">${new Date(enrollment.enrolledAt).toLocaleDateString()}</span>
            </div>
            <div class="payment-status">
                Status: <strong>${enrollment.paymentStatus || 'PENDING'}</strong>
                ${enrollment.paymentStatus === 'PENDING' ?
            `<div class="pending-actions">
                        <a href="paymentupdate.html?paymentId=${enrollment.paymentId}" class="update-button">
                            Update Payment
                        </a>
                        <button class="delete-enrollment" data-course-id="${enrollment.courseId}">
                            Delete Enrollment
                        </button>
                    </div>`
            : ''}
            </div>
            ${enrollment.paymentStatus === 'APPROVED' && enrollment.files ?
            `<div class="materials-section">
                    <h4 onclick="toggleMaterials(this)">Course Materials</h4>
                    <ul class="materials-list">
                        ${enrollment.files.map(file => `
                            <li>
                                <button class="download-btn" 
                                        data-course-id="${enrollment.courseId}" 
                                        data-file="${file}">
                                    Download ${file}
                                </button>
                            </li>
                        `).join('')}
                    </ul>
                </div>`
            : ''}
            ${enrollment.paymentStatus === 'APPROVED' ?
            `<div class="feedback-section">
                    <h4 onclick="toggleFeedback(this)">Course Feedback</h4>
                    <div class="feedback-form">
                        <textarea placeholder="Write your feedback..." data-course-id="${enrollment.courseId}"></textarea>
                        <select data-course-id="${enrollment.courseId}">
                            <option value="5">5 Stars</option>
                            <option value="4">4 Stars</option>
                            <option value="3">3 Stars</option>
                            <option value="2">2 Stars</option>
                            <option value="1">1 Star</option>
                        </select>
                        <button class="submit-feedback-btn" data-course-id="${enrollment.courseId}">Submit Feedback</button>
                    </div>
                    <ul class="feedback-list">
                        ${enrollment.feedback && enrollment.feedback.length > 0 ?
                enrollment.feedback.map(fb => `
                                <li class="feedback-item">
                                    <div class="feedback-header">
                                        <span>${fb.studentName}</span>
                                        <span>Rating: ${fb.rating}/5</span>
                                    </div>
                                    <div class="feedback-comment">${fb.comment}</div>
                                    ${fb.studentId === studentId ?
                        `<div class="feedback-actions">
                                            <button class="edit-feedback-btn" data-feedback-id="${fb.feedbackId}" data-course-id="${enrollment.courseId}">Edit</button>
                                            <button class="delete-feedback-btn" data-feedback-id="${fb.feedbackId}" data-course-id="${enrollment.courseId}">Delete</button>
                                        </div>
                                        <div class="edit-feedback-form" style="display: none;" data-feedback-id="${fb.feedbackId}">
                                            <textarea data-course-id="${enrollment.courseId}">${fb.comment}</textarea>
                                            <select data-course-id="${enrollment.courseId}">
                                                <option value="5" ${fb.rating === 5 ? 'selected' : ''}>5 Stars</option>
                                                <option value="4" ${fb.rating === 4 ? 'selected' : ''}>4 Stars</option>
                                                <option value="3" ${fb.rating === 3 ? 'selected' : ''}>3 Stars</option>
                                                <option value="2" ${fb.rating === 2 ? 'selected' : ''}>2 Stars</option>
                                                <option value="1" ${fb.rating === 1 ? 'selected' : ''}>1 Star</option>
                                            </select>
                                            <button class="update-feedback-btn" data-feedback-id="${fb.feedbackId}" data-course-id="${enrollment.courseId}">Update Feedback</button>
                                            <button class="cancel-edit-btn" data-feedback-id="${fb.feedbackId}">Cancel</button>
                                        </div>`
                        : ''}
                                </li>
                            `).join('')
                : '<li>No feedback yet</li>'}
                    </ul>
                </div>`
            : ''}
        </div>
    `).join('');

    // Add delete handlers
    const deleteButtons = container.querySelectorAll('.delete-enrollment');
    deleteButtons.forEach(button => {
        button.addEventListener('click', async () => {
            const courseId = button.dataset.courseId;
            if (confirm('Are you sure you want to delete this enrollment?')) {
                try {
                    const verifyResponse = await fetch(
                        `http://localhost:8080/Web/secure/payment/course/${courseId}`,
                        {
                            headers: getAuthHeaders()
                        }
                    );

                    if (verifyResponse.status === 401) {
                        clearUserData();
                        window.location.href = 'stdlogin.html';
                        return;
                    }

                    const response = await fetch(
                        `http://localhost:8080/Web/secure/deleteEnrollment?courseId=${courseId}`,
                        {
                            method: 'DELETE',
                            headers: getAuthHeaders()
                        }
                    );

                    const result = await response.json();

                    if (!response.ok) {
                        throw new Error(result.error || 'Deletion failed');
                    }

                    alert('Enrollment deleted successfully');
                    loadDashboard();
                } catch (error) {
                    errorMessageDiv.textContent = error.message;
                    console.error('Deletion error:', error);
                }
            }
        });
    });

    // Setup file download handlers
    setupFileDownloads();

    // Setup feedback submission handlers
    setupFeedbackSubmission();

    // Setup feedback edit and delete handlers
    setupFeedbackActions();
}

function toggleMaterials(header) {
    const materialsList = header.nextElementSibling;
    if (materialsList) {
        materialsList.classList.toggle('active');
    } else {
        console.error('Materials list not found');
    }
}

function toggleFeedback(header) {
    const feedbackList = header.nextElementSibling.nextElementSibling;
    if (feedbackList) {
        feedbackList.classList.toggle('active');
    } else {
        console.error('Feedback list not found for header:', header);
        errorMessageDiv.textContent = 'Error: Feedback section not properly loaded';
    }
}

async function downloadFile(courseId, fileName) {
    try {
        const response = await fetch(`/Web/secure/file?courseId=${courseId}&file=${encodeURIComponent(fileName)}`, {
            headers: getAuthHeaders()
        });
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    } catch (error) {
        errorMessageDiv.textContent = 'File download failed.';
    }
}

function setupFileDownloads() {
    document.querySelectorAll('.download-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const courseId = this.dataset.courseId;
            const fileName = this.dataset.file;
            downloadFile(courseId, fileName);
        });
    });
}

function setupFeedbackSubmission() {
    document.querySelectorAll('.submit-feedback-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const courseId = btn.dataset.courseId;
            const textarea = btn.previousElementSibling.previousElementSibling;
            const select = btn.previousElementSibling;
            const comment = textarea.value.trim();
            const rating = parseInt(select.value);

            if (!comment) {
                errorMessageDiv.textContent = 'Feedback comment cannot be empty';
                return;
            }

            try {
                const response = await fetch('http://localhost:8080/Web/secure/feedback', {
                    method: 'POST',
                    headers: getAuthHeaders(),
                    body: JSON.stringify({
                        courseId: parseInt(courseId),
                        comment,
                        rating
                    })
                });

                const result = await response.json();

                if (response.ok) {
                    alert('Feedback submitted successfully');
                    textarea.value = '';
                    select.value = '5';
                    loadDashboard();
                } else {
                    throw new Error(result.message || 'Feedback submission failed');
                }
            } catch (error) {
                errorMessageDiv.textContent = error.message;
                console.error('Feedback submission error:', error);
            }
        });
    });
}

function setupFeedbackActions() {
    // Edit feedback button
    document.querySelectorAll('.edit-feedback-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const feedbackId = btn.dataset.feedbackId;
            const editForm = document.querySelector(`.edit-feedback-form[data-feedback-id="${feedbackId}"]`);
            if (editForm) {
                editForm.style.display = 'block';
                btn.parentElement.style.display = 'none';
            } else {
                console.error('Edit form not found for feedback ID:', feedbackId);
                errorMessageDiv.textContent = 'Error: Edit form not loaded';
            }
        });
    });

    // Update feedback button
    document.querySelectorAll('.update-feedback-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const feedbackId = btn.dataset.feedbackId;
            const courseId = btn.dataset.courseId;
            const editForm = btn.parentElement;
            const textarea = editForm.querySelector('textarea');
            const select = editForm.querySelector('select');
            const comment = textarea.value.trim();
            const rating = parseInt(select.value);

            if (!comment) {
                errorMessageDiv.textContent = 'Feedback comment cannot be empty';
                return;
            }

            try {
                const response = await fetch('http://localhost:8080/Web/secure/feedback', {
                    method: 'PUT',
                    headers: getAuthHeaders(),
                    body: JSON.stringify({
                        feedbackId: parseInt(feedbackId),
                        comment,
                        rating
                    })
                });

                console.log('PUT Response Status:', response.status);
                console.log('PUT Response Headers:', [...response.headers.entries()]);

                let result;
                if (response.headers.get('content-type')?.includes('application/json')) {
                    result = await response.json();
                } else {
                    const text = await response.text();
                    console.error('Non-JSON response:', text.substring(0, 100)); // Log first 100 chars
                    throw new Error('Server returned non-JSON response, possibly an error page');
                }

                if (response.ok) {
                    alert('Feedback updated successfully');
                    editForm.style.display = 'none';
                    editForm.previousElementSibling.style.display = 'block';
                    loadDashboard();
                } else {
                    throw new Error(result.message || `Feedback update failed with status ${response.status}`);
                }
            } catch (error) {
                errorMessageDiv.textContent = error.message;
                console.error('Feedback update error:', error);
            }
        });
    });

    // Cancel edit button
    document.querySelectorAll('.cancel-edit-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const feedbackId = btn.dataset.feedbackId;
            const editForm = document.querySelector(`.edit-feedback-form[data-feedback-id="${feedbackId}"]`);
            if (editForm) {
                editForm.style.display = 'none';
                editForm.previousElementSibling.style.display = 'block';
            } else {
                console.error('Edit form not found for feedback ID:', feedbackId);
            }
        });
    });

    // Delete feedback button
    document.querySelectorAll('.delete-feedback-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const feedbackId = btn.dataset.feedbackId;
            if (confirm('Are you sure you want to delete this feedback?')) {
                try {
                    const response = await fetch(`http://localhost:8080/Web/secure/feedback?feedbackId=${feedbackId}`, {
                        method: 'DELETE',
                        headers: getAuthHeaders()
                    });

                    console.log('DELETE Response Status:', response.status);
                    console.log('DELETE Response Headers:', [...response.headers.entries()]);

                    let result;
                    if (response.headers.get('content-type')?.includes('application/json')) {
                        result = await response.json();
                    } else {
                        const text = await response.text();
                        console.error('Non-JSON response:', text.substring(0, 100)); // Log first 100 chars
                        throw new Error('Server returned non-JSON response, possibly an error page');
                    }

                    if (response.ok) {
                        alert('Feedback deleted successfully');
                        loadDashboard();
                    } else {
                        throw new Error(result.message || `Feedback deletion failed with status ${response.status}`);
                    }
                } catch (error) {
                    errorMessageDiv.textContent = error.message;
                    console.error('Feedback deletion error:', error);
                }
            }
        });
    });
}

function displayUserInfo() {
    const userInfo = {
        name: localStorage.getItem('userName'),
        email: localStorage.getItem('userEmail'),
        id: localStorage.getItem('userId'),
        address: localStorage.getItem('userAddress'),
        age: localStorage.getItem('userAge'),
        phone: localStorage.getItem('userPhoneNumber')
    };

    welcomeMessage.innerHTML = `<h1>Welcome ${userInfo.name}!</h1>`;
    userInfoDiv.innerHTML = createUserInfoHTML(userInfo);
}

// Helper Functions
function getAuthHeaders() {
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
}

function createUserInfoHTML(userInfo) {
    return `
        <p>Email: <strong>${userInfo.email}</strong></p>
        <p>Student ID: <strong>${userInfo.id}</strong></p>
        ${userInfo.address ? `<p>Address: <strong>${userInfo.address}</strong></p>` : ''}
        ${userInfo.age ? `<p>Age: <strong>${userInfo.age}</strong></p>` : ''}
        ${userInfo.phone ? `<p>Phone: <strong>${userInfo.phone}</strong></p>` : ''}
    `;
}

function coursedash() {
    window.location.href = 'courselist.html';
}

function editProfile() {
    window.location.href = 'stdupdate.html';
}

function logout() {
    fetch('http://localhost:8080/Web/logout', {
        method: 'POST',
        headers: getAuthHeaders()
    }).finally(() => {
        clearUserData();
        window.location.href = 'stdlogin.html';
    });
}

async function deleteProfile() {
    if (!confirm("Permanently delete your profile?")) return;

    try {
        const response = await fetch('http://localhost:8080/Web/secure/studentDelete', {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            clearUserData();
            alert('Profile deleted');
            window.location.href = 'stdregister.html';
        }
    } catch (error) {
        alert('Deletion failed: ' + error.message);
    }
}

function clearUserData() {
    const keys = [
        'jwtToken', 'userName', 'userEmail', 'userId',
        'userAddress', 'userAge', 'userPhoneNumber'
    ];
    keys.forEach(key => localStorage.removeItem(key));
}