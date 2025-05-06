

function showSuccess(message) {
    const statusDiv = document.getElementById('updateStatus');
    statusDiv.innerHTML = `
        <div class="success-alert">
            ✅ ${message}
        </div>
    `;
    statusDiv.style.color = '#28a745';
    statusDiv.style.display = 'block';
}

// Then modify the existing code to use these functions
document.addEventListener('DOMContentLoaded', async () => {
    try {
        // ... [existing code] ...
    } catch (error) {
        showError(`Failed to initialize: ${error.message}`);
    }
});

document.addEventListener('DOMContentLoaded', async () => {
    try {
        const token = localStorage.getItem('jwtToken');
        if (!token) throw new Error('No authentication token');

        const params = new URLSearchParams(window.location.search);
        const paymentId = validatePaymentId(params.get('paymentId'));

        await verifyPaymentOwnership(paymentId, token);
        setupFormSubmission(paymentId, token);
        
    } catch (error) {
        showError(error.message);
    }
});

function validatePaymentId(paymentId) {
    const id = Number(paymentId);
    if (!paymentId || isNaN(id) || id <= 0) {
        throw new Error(`Invalid payment ID: ${paymentId}`);
    }
    return id;
}

async function verifyPaymentOwnership(paymentId, token) {
    const response = await fetch(`/secure/payments/${paymentId}/verify`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Payment verification failed');
    }
}

function setupFormSubmission(paymentId, token) {
    document.getElementById('paymentForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const formData = {
            paymentId: paymentId,
            cardNumber: document.getElementById('newCardNumber').value.trim(),
            cvv: document.getElementById('newCvv').value.trim()
        };

        try {
            const response = await fetch('/secure/updatePayment', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();
            
            if (!response.ok) throw new Error(result.message || 'Update failed');
            
            showSuccess('Payment updated! Redirecting...');
            setTimeout(() => window.location.href = 'stddash.html', 1500);
            
        } catch (error) {
            showError(error.message);
        }
    });
}