async function submitUpdate() {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = 'stdlogin.html'; // Fixed path
        return;
    }

    // Declare updateData FIRST
    const updateData = {
        userId: parseInt(localStorage.getItem('userId')),
        userName: document.getElementById('userName').value,
        userEmail: document.getElementById('userEmail').value,
        userPassword: document.getElementById('userPassword').value || undefined,
        userAddress: document.getElementById('userAddress').value,
        userAge: parseInt(document.getElementById('userAge').value) || 0,
        userPhoneNumber: parseInt(document.getElementById('userPhoneNumber').value) || 0
    };

    try {
        const response = await fetch('http://localhost:8080/Web/secure/studentUpdate', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(updateData)
        });

        if (response.status === 401) {
            localStorage.clear();
            window.location.href = 'login.html';
            return;
        }

        const result = await response.json();
        
        if (response.ok) {
            // Move localStorage updates HERE after successful response
            localStorage.setItem('userName', updateData.userName);
            localStorage.setItem('userEmail', updateData.userEmail);
            localStorage.setItem('userAddress', updateData.userAddress);
            localStorage.setItem('userAge', updateData.userAge);
            localStorage.setItem('userPhoneNumber', updateData.userPhoneNumber);
            
            // Handle email change scenario
            if (updateData.userEmail !== localStorage.getItem('originalEmail')) {
                alert('Email updated - please login again');
                localStorage.clear();
                window.location.href = 'stdlogin.html'; // Fixed path
            }
        } else {
            throw new Error(result.message || 'Update failed');
        }
    } catch (error) {
        console.error('Update error:', error);
        document.getElementById('updateResult').innerText = error.message;
    }
}