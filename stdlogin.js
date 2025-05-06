function submit() {
    const email = document.getElementById("input-1").value;
    const password = document.getElementById("input-2").value;

    const requestOptions = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            "userEmail": email,
            "userPassword": password
        }),
        redirect: "follow"
    };

    fetch("http://localhost:8080/Web/studentLogin", requestOptions)
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw err; });
            }
            return response.json();
        })
        .then(result => {
            console.log("Login successful:", result);

            if (result.token) {
                // Store token and user data
                localStorage.setItem('jwtToken', result.token);

                // Decode and store user info from token
                const payload = JSON.parse(atob(result.token.split('.')[1]));
                localStorage.setItem('userName', payload.userName);
                localStorage.setItem('userEmail', payload.userEmail);
                localStorage.setItem('userId', payload.userId);

                // After storing user data in localStorage
                localStorage.setItem('originalEmail', payload.userEmail); // Add this line

                alert("Login successful!");
                window.location.href = "stddash.html";
            } else {
                throw new Error("No token received");
            }
        })
        .catch(error => {
            console.error("Login failed:", error);
            document.getElementById("result").innerText =
                error.message || "Login failed. Please check your credentials.";
        });
}