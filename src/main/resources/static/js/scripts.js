
/* START LOGIN */
document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const button = document.getElementById('loginBtn');
    const errorDiv = document.getElementById('errorMessage');

    const staffCode = document.getElementById('staffCode').value;
    const password = document.getElementById('password').value;

    // Clear previous error
    errorDiv.innerText = "";

    // Basic validation
    if (!staffCode || !password) {
        errorDiv.innerText = "Please fill in all fields.";
        return;
    }

    // Disable button + loading state
    button.disabled = true;
    button.innerText = "Logging in...";

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                staffCode: parseInt(staffCode),
                password: password
            })
        });

        const data = await response.json();

        if (response.ok) {
            // ✅ Store JWT securely
            localStorage.setItem('jwtToken', data.token);

            // ✅ Redirect
            window.location.href = '/dashboard';

        } else {
            errorDiv.innerText = data.message || 'Invalid credentials';
        }

    } catch (error) {
        errorDiv.innerText = 'Server error. Please try again.';
    } finally {
        // Re-enable button
        button.disabled = false;
        button.innerText = "Login";
    }
});

/* END LOGIN */

