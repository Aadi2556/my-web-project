function adminSubmit() {
    const adminData = {
        userEmail: document.getElementById("admin-input-1").value,
        userPassword: document.getElementById("admin-input-2").value,
        userId: document.getElementById("admin-input-3").value,
        userName: document.getElementById("admin-input-4").value
    };
  
    const requestOptions = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(adminData),
        redirect: "follow"
    };
  
    fetch("http://localhost:8080/Web/adminRegister", requestOptions)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text();
        })
        .then(result => {
            console.log("Admin registration successful:", result);
            document.getElementById("admin-result").innerText = "Admin registration successful!";
            // Redirect to admin login after successful registration
            setTimeout(() => {
                window.location.href = "adminlogin.html";
            }, 1500);
        })
        .catch(error => {
            console.error("Admin registration failed:", error);
            document.getElementById("admin-result").innerText = "Admin registration failed: " + error.message;
        });
  }