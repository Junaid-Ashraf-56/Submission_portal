
document.addEventListener('DOMContentLoaded', function () {

    const passwordInput = document.getElementById('passwordInput');
    const confirmInput  = document.getElementById('confirmInput');
    const strengthBar   = document.getElementById('strengthBar');
    const strengthText  = document.getElementById('strengthText');
    const matchText     = document.getElementById('matchText');

    const STRENGTH_COLORS = ['', '#e74c3c', '#e67e22', '#f1c40f', '#27ae60'];
    const STRENGTH_LABELS = ['', 'Weak',    'Fair',    'Good',    'Strong' ];

    /* ── Password Strength ── */
    passwordInput.addEventListener('input', function () {
        const val = passwordInput.value;
        let score = 0;

        if (val.length >= 8)            score++;   // length
        if (/[A-Z]/.test(val))          score++;   // uppercase
        if (/[0-9]/.test(val))          score++;   // digit
        if (/[^A-Za-z0-9]/.test(val))   score++;   // special char

        strengthBar.style.width      = (score * 25) + '%';
        strengthBar.style.background = STRENGTH_COLORS[score] || '#e9ecef';
        strengthText.textContent     = score ? STRENGTH_LABELS[score] : '';
        strengthText.style.color     = STRENGTH_COLORS[score] || '#adb5bd';

        // Re-check match whenever password changes
        checkMatch();
    });

    /* ── Confirm Password Match ── */
    confirmInput.addEventListener('input', checkMatch);

    function checkMatch() {
        if (!confirmInput.value) {
            matchText.textContent = '';
            return;
        }

        if (confirmInput.value === passwordInput.value) {
            matchText.textContent  = '✓ Passwords match';
            matchText.style.color  = '#27ae60';
        } else {
            matchText.textContent  = '✗ Passwords do not match';
            matchText.style.color  = '#e74c3c';
        }
    }

});