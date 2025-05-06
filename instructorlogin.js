function instructorSubmit() {
    const email = document.getElementById("instructor-input-1").value;
    const password = document.getElementById("instructor-input-2").value;

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

    fetch("http://localhost:8080/Web/instructorLogin", requestOptions)
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw err; });
            }
            return response.json();
        })
        .then(result => {
            console.log("Instructor login successful:", result);

            if (result.token) {
                // Store instructor-specific token and data
                localStorage.setItem('instructorJwtToken', result.token);
                
                // Decode and store instructor info from token
                const payload = JSON.parse(atob(result.token.split('.')[1]));
                localStorage.setItem('instructorUserName', payload.userName);
                localStorage.setItem('instructorUserEmail', payload.userEmail);
                localStorage.setItem('instructorUserId', payload.userId);
                localStorage.setItem('originalInstructorEmail', payload.userEmail);

                alert("Instructor login successful!");
                window.location.href = "instructordashboard.html";
            } else {
                throw new Error("No instructor token received");
            }
        })
        .catch(error => {
            console.error("Instructor login failed:", error);
            document.getElementById("instructor-result").innerText =
                error.message || "Instructor login failed. Please check your credentials.";
        });
}