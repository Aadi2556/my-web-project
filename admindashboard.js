// Check authentication
const adminToken = localStorage.getItem('adminJwtToken');
if (!adminToken) {
    console.error('No admin token found, redirecting to login');
    window.location.href = 'adminlogin.html';
} else {
    displayAdminInfo();
    loadAdminDashboard();
}

function displayAdminInfo() {
    const adminName = localStorage.getItem('adminUserName');
    const adminEmail = localStorage.getItem('adminUserEmail');
    const adminId = localStorage.getItem('adminUserId');
    const adminAddress = localStorage.getItem('adminAddress');
    const adminAge = localStorage.getItem('adminAge');
    const adminPhone = localStorage.getItem('adminPhoneNumber');

    const welcomeDiv = document.getElementById('admin-welcome-message');
    const infoDiv = document.getElementById('admin-user-info');

    if (welcomeDiv) {
        welcomeDiv.innerHTML = `<h1>Welcome ${adminName || 'Administrator'}!</h1>`;
    }

    let infoHTML = `
        <p>Email: <strong>${adminEmail || 'N/A'}</strong></p>
        <p>Admin ID: <strong>${adminId || 'N/A'}</strong></p>
    `;

    if (adminAddress) infoHTML += `<p>Address: <strong>${adminAddress}</strong></p>`;
    if (adminAge) infoHTML += `<p>Age: <strong>${adminAge}</strong></p>`;
    if (adminPhone) infoHTML += `<p>Phone: <strong>${adminPhone}</strong></p>`;

    if (infoDiv) {
        infoDiv.innerHTML = infoHTML;
    }
}

async function loadAdminDashboard() {
    try {
        const [dashboardResponse, pendingCoursesResponse, pendingPaymentsResponse] = await Promise.all([
            fetch('http://localhost:8080/Web/secure/adminDashboard', {
                headers: { 
                    'Authorization': `Bearer ${adminToken}`,
                    'Content-Type': 'application/json'
                }
            }),
            fetch('http://localhost:8080/Web/secure/pendingCourses', {
                headers: { 'Authorization': `Bearer ${adminToken}` }
            }),
            fetch('http://localhost:8080/Web/secure/pendingPayments', {
                headers: { 'Authorization': `Bearer ${adminToken}` }
            })
        ]);

        const pendingPayments = await pendingPaymentsResponse.json();
        displayPendingPayments(pendingPayments);
        
        const pendingCourses = await pendingCoursesResponse.json();
        displayPendingCourses(pendingCourses);

    } catch (error) {
        console.error('Error loading dashboard:', error);
        document.getElementById('admin-result').textContent = 'Error loading dashboard';
    }
}

function displayPendingPayments(payments) {
    const container = document.getElementById('pending-payments-list');
    if (!container) return;

    container.innerHTML = payments.length > 0 
        ? payments.map(payment => `
            <div class="payment-card pending">
                <h3>${payment.courseTitle}</h3>
                <p>Payment ID: ${payment.id}</p>
                <p>Student ID: ${payment.studentId}</p>
                <p>Amount: $${payment.amount.toFixed(2)}</p>
                <p>Date: ${new Date(payment.date).toLocaleDateString()}</p>
                <button onclick="approvePayment(${payment.id})">Approve Payment</button>
            </div>
        `).join('')
        : '<p>No pending payments requiring approval</p>';
}

window.approvePayment = async function(paymentId) {
    if (!confirm('Are you sure you want to approve this payment?')) return;

    try {
        const response = await fetch(
            `http://localhost:8080/Web/secure/approvePayment?paymentId=${paymentId}`, 
            {
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${adminToken}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        if (response.ok) {
            alert('Payment approved successfully');
            await loadAdminDashboard();
        } else {
            const error = await response.text();
            throw new Error(error);
        }
    } catch (error) {
        console.error('Payment approval error:', error);
        alert(`Approval failed: ${error.message}`);
    }
};

function displayPendingCourses(courses) {
    const container = document.getElementById('pending-courses-list');
    if (!container) return;

    container.innerHTML = courses.length > 0 
        ? courses.map(course => `
            <div class="course-card pending">
                <h3>${course.title}</h3>
                <p>ID: ${course.courseId} | Instructor: ${course.instructorId}</p>
                <p>Price: $${(course.price || 0).toFixed(2)}</p>
                <p>${course.description}</p>
                <button onclick="approveCourse(${course.courseId})">Approve</button>
            </div>
        `).join('')
        : '<p>No pending courses for approval</p>';
}

window.approveCourse = async function(courseId) {
    if (!confirm('Are you sure you want to approve this course?')) return;

    try {
        const response = await fetch(
            `http://localhost:8080/Web/secure/approveCourse?courseId=${courseId}`, 
            {
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${adminToken}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        if (response.ok) {
            alert('Course approved successfully');
            await loadAdminDashboard();
        } else {
            const error = await response.json();
            throw new Error(error.message || 'Approval failed');
        }
    } catch (error) {
        console.error('Approval error:', error);
        alert(`Approval failed: ${error.message}`);
    }
};

// Navigation functions
function viewStudentList() {
    if (!localStorage.getItem('adminJwtToken')) {
        console.error('No admin token found for student list navigation');
        alert('Please log in again');
        window.location.href = 'adminlogin.html';
        return;
    }
    window.location.href = 'studentlist.html';
}

function viewCourseRegister() {
    if (!localStorage.getItem('adminJwtToken')) {
        console.error('No admin token found for course register navigation');
        alert('Please log in again');
        window.location.href = 'adminlogin.html';
        return;
    }
    window.location.href = 'courseregister.html';
}

// Admin profile management functions
function adminEdit() {
    window.location.href = 'adminupdate.html';
}

function adminLogout() {
    fetch('http://localhost:8080/Web/adminLogout', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${adminToken}` }
    }).finally(() => {
        clearAdminData();
        window.location.href = 'adminlogin.html';
    });
}

async function adminDeleteProfile() {
    const confirmation = confirm("Are you sure you want to delete your admin profile? This action cannot be undone!");
    if (!confirmation) return;

    try {
        const response = await fetch('http://localhost:8080/Web/secure/adminDelete', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            clearAdminData();
            alert('Admin profile deleted successfully');
            window.location.href = 'adminregister.html';
        } else {
            const error = await response.json();
            throw new Error(error.message || 'Deletion failed');
        }
    } catch (error) {
        console.error('Delete error:', error);
        alert(`Deletion failed: ${error.message}`);
    }
}

function clearAdminData() {
    const keysToRemove = [
        'adminJwtToken', 'adminUserName', 'adminUserEmail',
        'adminUserId', 'adminAddress', 'adminAge', 'adminPhoneNumber'
    ];
    keysToRemove.forEach(key => localStorage.removeItem(key));
}