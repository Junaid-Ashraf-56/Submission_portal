document.addEventListener('DOMContentLoaded', () => {
    const card = document.querySelector('.setup-card');
    card.style.opacity = '0';
    card.style.transform = 'translateY(30px)';

    setTimeout(() => {
        card.style.transition = 'all 0.6s ease-out';
        card.style.opacity = '1';
        card.style.transform = 'translateY(0)';
    }, 100);

    const toggleNew = document.getElementById('toggleNew');
    const toggleConfirm = document.getElementById('toggleConfirm');
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');

    toggleNew.addEventListener('click', () => {
        const type = newPassword.type === 'password' ? 'text' : 'password';
        newPassword.type = type;
        toggleNew.classList.toggle('bi-eye');
        toggleNew.classList.toggle('bi-eye-slash');
    });

    toggleConfirm.addEventListener('click', () => {
        const type = confirmPassword.type === 'password' ? 'text' : 'password';
        confirmPassword.type = type;
        toggleConfirm.classList.toggle('bi-eye');
        toggleConfirm.classList.toggle('bi-eye-slash');
    });

    newPassword.addEventListener('input', () => {
        const value = newPassword.value;

        const reqLength = document.getElementById('req-length');
        if (value.length >= 8) {
            reqLength.classList.add('valid');
            reqLength.classList.remove('invalid');
        } else {
            reqLength.classList.remove('valid');
            reqLength.classList.add('invalid');
        }

        const reqUppercase = document.getElementById('req-uppercase');
        if (/[A-Z]/.test(value)) {
            reqUppercase.classList.add('valid');
            reqUppercase.classList.remove('invalid');
        } else {
            reqUppercase.classList.remove('valid');
            reqUppercase.classList.add('invalid');
        }

        const reqLowercase = document.getElementById('req-lowercase');
        if (/[a-z]/.test(value)) {
            reqLowercase.classList.add('valid');
            reqLowercase.classList.remove('invalid');
        } else {
            reqLowercase.classList.remove('valid');
            reqLowercase.classList.add('invalid');
        }

        const reqNumber = document.getElementById('req-number');
        if (/[0-9]/.test(value)) {
            reqNumber.classList.add('valid');
            reqNumber.classList.remove('invalid');
        } else {
            reqNumber.classList.remove('valid');
            reqNumber.classList.add('invalid');
        }
    });

    const form = document.getElementById('setupForm');
    form.addEventListener('submit', (e) => {
        if (newPassword.value !== confirmPassword.value) {
            e.preventDefault();
            alert('Passwords do not match!');
            confirmPassword.focus();
        }
    });
});
