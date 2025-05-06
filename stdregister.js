function registerStudent() {
  const userData = {
      userEmail: document.getElementById("input-1").value,
      userPassword: document.getElementById("input-2").value,
      userId: document.getElementById("input-3").value,
      userName: document.getElementById("input-4").value
  };

  const requestOptions = {
      method: "POST",
      headers: {
          "Content-Type": "application/json",
          // Add Authorization header if needed (unlikely for registration)
      },
      body: JSON.stringify(userData),
      // Remove credentials unless you're using cookies
      redirect: "follow"
  };

  fetch("http://localhost:8080/Web/studentRegister", requestOptions)
      .then(response => {
          if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
          }
          return response.text();
      })
      .then(result => {
          console.log("Registration successful:", result);
          document.getElementById("result").innerText = "Registration successful!";
          
          // If your backend returns a JWT on registration (uncommon):
          // const { token } = JSON.parse(result);
          // localStorage.setItem('jwtToken', token);
          setTimeout(() => {
            window.location.href = "stdlogin.html";
        }, 1500);
          // Redirect to login or handle next steps
      })
      .catch(error => {
          console.error("Registration failed:", error);
          document.getElementById("result").innerText = "Registration failed: " + error.message;
      });
}