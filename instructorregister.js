function instructorSubmit() {
    const instructorData = {
        userEmail: document.getElementById("instructor-input-1").value,
        userPassword: document.getElementById("instructor-input-2").value,
        userId: document.getElementById("instructor-input-3").value,
        userName: document.getElementById("instructor-input-4").value
    };

    const requestOptions = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(instructorData),
        redirect: "follow"
    };

    fetch("http://localhost:8080/Web/instructorRegister", requestOptions)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text();
        })
        .then(result => {
            console.log("Instructor registration successful:", result);
            document.getElementById("instructor-result").innerText = "Instructor registration successful!";
            // Redirect to instructor login after successful registration
            setTimeout(() => {
                window.location.href = "instructorlogin.html";
            }, 1500);
        })
        .catch(error => {
            console.error("Instructor registration failed:", error);
            document.getElementById("instructor-result").innerText = "Instructor registration failed: " + error.message;
        });
}