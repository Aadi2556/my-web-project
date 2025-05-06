document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('courseForm');
    const submitBtn = document.getElementById('submitBtn');
    const statusMessage = document.getElementById('statusMessage');
    const loading = document.getElementById('loading');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        statusMessage.textContent = '';
        loading.style.display = 'block';
        submitBtn.disabled = true;

        const token = localStorage.getItem('instructorJwtToken');
        if (!token) {
            showError('Not authenticated. Please login first.');
            window.location.href = 'instructorlogin.html';
            return;
        }

        const formData = new FormData(form);
        
        try {
            const response = await fetch('http://localhost:8080/Web/secure/courseCreate', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            
            const result = await response.json();
    
    if (result.status === 'success') {
        showSuccess(result.message);
        setTimeout(() => {
            window.location.href = 'lobby.html';
        }, 1500);
    } else {
        showError(result.message);
    }
} catch (error) {
    showError('Failed to process response from server');

        } finally {
            loading.style.display = 'none';
            submitBtn.disabled = false;
        }
    });

    // Rest of the code remains the same

    function showError(message) {
        statusMessage.className = 'error';
        statusMessage.textContent = message;
    }

    function showSuccess(message) {
        statusMessage.className = 'success';
        statusMessage.textContent = message;
    }
});