document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = 'stdlogin.html';
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const courseId = params.get('courseId');

    if (!courseId) {
        alert('Course ID is missing');
        window.history.back();
        return;
    }

    try {
        // Load course details
        const response = await fetch(`http://localhost:8080/Web/course/${courseId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('jwtToken');
                window.location.href = 'stdlogin.html';
                return;
            }
            throw new Error('Failed to load course details');
        }

        const course = await response.json();
        document.getElementById('courseTitle').textContent = course.title;
        document.getElementById('paymentAmount').textContent = course.price.toFixed(2);
    } catch (error) {
        alert('Error: ' + error.message);
        window.history.back();
    }

    // Payment form submission
    document.getElementById('paymentForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const paymentData = {
            courseId: parseInt(courseId),
            cardNumber: document.getElementById('cardNumber').value,
            cvv: document.getElementById('cvv').value
        };

        try {
            const response = await fetch('http://localhost:8080/Web/secure/makePayment', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(paymentData)
            });

            const result = await response.json();

            if (response.ok) {
                localStorage.setItem('lastPaymentId', result.paymentId);
                
                document.getElementById('paymentStatus').textContent =
                    'Payment submitted for admin approval';
                document.getElementById('paymentStatus').style.color = 'green';
                
                setTimeout(() => {
                    window.location.href = 'stddash.html'; // Reload dashboard to fetch latest data
                }, 2000);
            } else {
                if (response.status === 401) {
                    localStorage.removeItem('jwtToken');
                    window.location.href = 'stdlogin.html';
                    return;
                }
                throw new Error(result.message || 'Payment failed');
            }
        } catch (error) {
            document.getElementById('paymentStatus').textContent = error.message;
            document.getElementById('paymentStatus').style.color = 'red';
        }
    });
});