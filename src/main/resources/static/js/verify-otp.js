document.addEventListener('DOMContentLoaded', () => {
    const card = document.querySelector('.verify-card');
    card.style.opacity = '0';
    card.style.transform = 'translateY(30px)';

    setTimeout(() => {
        card.style.transition = 'all 0.6s ease-out';
        card.style.opacity = '1';
        card.style.transform = 'translateY(0)';
    }, 100);

    document.querySelector('.otp-input').focus();
});

// OTP Inputs
const inputs = document.querySelectorAll('.otp-input');
const otpHidden = document.getElementById('otp-hidden');
const form = document.getElementById('otp-form');

inputs.forEach((input, index) => {

    input.addEventListener('input', (e) => {
        if (e.target.value) {
            if (index < inputs.length - 1) inputs[index + 1].focus();
        }
        updateHiddenField();
    });

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !e.target.value && index > 0) {
            inputs[index - 1].focus();
        }
    });

    input.addEventListener('keypress', (e) => {
        if (!/[0-9]/.test(e.key)) e.preventDefault();
    });

    input.addEventListener('paste', (e) => {
        e.preventDefault();
        const pasted = e.clipboardData.getData('text').slice(0, 6);
        pasted.split('').forEach((char, i) => {
            if (inputs[i]) inputs[i].value = char;
        });
        updateHiddenField();
    });
});

function updateHiddenField() {
    otpHidden.value = Array.from(inputs).map(inp => inp.value).join('');
}

// Submit Validation
form.addEventListener('submit', (e) => {
    updateHiddenField();
    if (otpHidden.value.length !== 6) {
        e.preventDefault();
        alert('Please enter all 6 digits');
    }
});

// Timer
let timeLeft = 300;
const timerElement = document.getElementById('timer');

function updateTimer() {
    const min = Math.floor(timeLeft / 60);
    const sec = timeLeft % 60;

    timerElement.textContent = `${min}:${String(sec).padStart(2, '0')}`;

    if (timeLeft > 0) {
        timeLeft--;
        setTimeout(updateTimer, 1000);
    } else {
        timerElement.textContent = 'Expired';
        timerElement.style.color = '#dc3545';
    }
}

updateTimer();

// Resend
document.getElementById('resend-btn').addEventListener('click', (e) => {
    e.preventDefault();
    alert("A new code has been sent to your email.");
    timeLeft = 300;
    updateTimer();
});
