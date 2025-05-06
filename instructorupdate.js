async function instructorSubmitUpdate() {
    const token = localStorage.getItem('instructorJwtToken');
    if (!token) {
        window.location.href = 'instructorlogin.html';
        return;
    }

    const updateData = {
        userId: parseInt(localStorage.getItem('instructorUserId')),
        userName: document.getElementById('instructorUserName').value,
        userEmail: document.getElementById('instructorUserEmail').value,
        userPassword: document.getElementById('instructorUserPassword').value || undefined,
        userAddress: document.getElementById('instructorUserAddress').value,
        userAge: parseInt(document.getElementById('instructorUserAge').value) || 0,
        userPhoneNumber: parseInt(document.getElementById('instructorUserPhoneNumber').value) || 0
    };

    try {
        const response = await fetch('http://localhost:8080/Web/secure/instructorUpdate', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(updateData)
        });

        if (response.status === 401) {
            localStorage.clear();
            window.location.href = 'instructorlogin.html';
            return;
        }

        const result = await response.json();
        
        if (response.ok) {
            // Update localStorage after successful update
            localStorage.setItem('instructorUserName', updateData.userName);
            localStorage.setItem('instructorUserEmail', updateData.userEmail);
            localStorage.setItem('instructorUserAddress', updateData.userAddress);
            localStorage.setItem('instructorUserAge', updateData.userAge);
            localStorage.setItem('instructorUserPhoneNumber', updateData.userPhoneNumber);
            
            // Handle email change scenario
            if (updateData.userEmail !== localStorage.getItem('originalInstructorEmail')) {
                alert('Email updated - please login again');
                localStorage.clear();
                window.location.href = 'instructorlogin.html';
            }
        } else {
            throw new Error(result.message || 'Instructor update failed');
        }
    } catch (error) {
        console.error('Instructor update error:', error);
        document.getElementById('instructorUpdateResult').innerText = error.message;
    }
}