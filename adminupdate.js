async function adminSubmitUpdate() {
    const token = localStorage.getItem('adminJwtToken');
    if (!token) {
        window.location.href = 'adminlogin.html';
        return;
    }

    const updateData = {
        userId: parseInt(localStorage.getItem('adminUserId')),
        userName: document.getElementById('adminUserName').value,
        userEmail: document.getElementById('adminUserEmail').value,
        userPassword: document.getElementById('adminUserPassword').value || undefined,
        userAddress: document.getElementById('adminUserAddress').value,
        userAge: parseInt(document.getElementById('adminUserAge').value) || 0,
        userPhoneNumber: parseInt(document.getElementById('adminUserPhoneNumber').value) || 0
    };

    try {
        const response = await fetch('http://localhost:8080/Web/secure/adminUpdate', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(updateData)
        });

        if (response.status === 401) {
            localStorage.clear();
            window.location.href = 'adminlogin.html';
            return;
        }

        const result = await response.json();
        
        if (response.ok) {
            // Update localStorage after successful update
            localStorage.setItem('adminUserName', updateData.userName);
            localStorage.setItem('adminUserEmail', updateData.userEmail);
            localStorage.setItem('adminUserAddress', updateData.userAddress);
            localStorage.setItem('adminUserAge', updateData.userAge);
            localStorage.setItem('adminUserPhoneNumber', updateData.userPhoneNumber);
            
            // Handle email change scenario
            if (updateData.userEmail !== localStorage.getItem('originalAdminEmail')) {
                alert('Email updated - please login again');
                localStorage.clear();
                window.location.href = 'adminlogin.html';
            }
        } else {
            throw new Error(result.message || 'Admin update failed');
        }
    } catch (error) {
        console.error('Admin update error:', error);
        document.getElementById('adminUpdateResult').innerText = error.message;
    }
}

