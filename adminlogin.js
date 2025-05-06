function adminSubmit() {
    const email = document.getElementById("admin-input-1").value;
    const password = document.getElementById("admin-input-2").value;

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

    fetch("http://localhost:8080/Web/adminLogin", requestOptions)
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw err; });
            }
            return response.json();
        })
        .then(result => {
            console.log("Admin login successful:", result);

            if (result.token) {
                // Store admin-specific token and data
                localStorage.setItem('adminJwtToken', result.token);
                
                // Decode and store admin info from token
                const payload = JSON.parse(atob(result.token.split('.')[1]));
                localStorage.setItem('adminUserName', payload.userName);
                localStorage.setItem('adminUserEmail', payload.userEmail);
                localStorage.setItem('adminUserId', payload.userId);
                localStorage.setItem('originalAdminEmail', payload.userEmail);

                alert("Admin login successful!");
                window.location.href = "admindashboard.html";
            } else {
                throw new Error("No admin token received");
            }
        })
        .catch(error => {
            console.error("Admin login failed:", error);
            document.getElementById("admin-result").innerText =
                error.message || "Admin login failed. Please check your credentials.";
        });
}