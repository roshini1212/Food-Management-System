const STORAGE_KEY = "fmsRestaurantAccount";

const loginForm = document.getElementById("loginForm");
const signupForm = document.getElementById("signupForm");
const feedback = document.getElementById("authFeedback");

if (loginForm) {
    if (localStorage.getItem(STORAGE_KEY)) {
        window.location.replace("/");
    }

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = Object.fromEntries(new FormData(loginForm).entries());
        await submitAuth("/api/auth/login", payload, "Login successful. Redirecting...");
    });
}

if (signupForm) {
    if (localStorage.getItem(STORAGE_KEY)) {
        window.location.replace("/");
    }

    signupForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = Object.fromEntries(new FormData(signupForm).entries());
        await submitAuth("/api/auth/signup", payload, "Account created. Redirecting...");
    });
}

async function submitAuth(url, payload, successMessage) {
    setFeedback("Processing...", "");

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Request failed");
        }

        const data = await response.json();
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
        setFeedback(successMessage, "feedback-success");
        setTimeout(() => {
            window.location.replace("/");
        }, 500);
    } catch (error) {
        setFeedback(cleanError(error.message), "feedback-error");
    }
}

function setFeedback(text, className) {
    feedback.textContent = text;
    feedback.className = `auth-feedback ${className}`.trim();
}

function cleanError(message) {
    return message.replace(/^Error:\s*/, "");
}
