// ── Tab switching ──
const tabTitles = {
    'dashboard':      { title: 'Dashboard',                   sub: 'Welcome back, Admin' },
    'cr-list':        { title: 'All CRs',                     sub: 'Browse and manage Class Representatives' },
    'students-list':  { title: 'All Students',                sub: 'Browse all registered students' },
    'cr-requests':    { title: 'CR Registration Requests',    sub: 'Review and approve incoming CR accounts' }
};

function showTab(id) {
    // Hide all tab sections
    document.querySelectorAll('.tab-section').forEach(s => s.classList.remove('active'));

    // Show the target tab
    const target = document.getElementById('tab-' + id);
    if (target) target.classList.add('active');

    // Update sidebar active state
    document.querySelectorAll('.sidebar-link').forEach(l => l.classList.remove('active'));
    const clicked = Array.from(document.querySelectorAll('.sidebar-link'))
        .find(l => l.getAttribute('onclick') && l.getAttribute('onclick').includes(id));
    if (clicked) clicked.classList.add('active');

    // Update topbar title
    if (tabTitles[id]) {
        document.getElementById('page-title').textContent = tabTitles[id].title;
        document.getElementById('page-sub').textContent   = tabTitles[id].sub;
    }

    // Close sidebar on mobile after navigation
    if (window.innerWidth < 992) {
        document.getElementById('sidebar').classList.remove('open');
    }
}

// ── Mobile sidebar toggle ──
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
}

// ── Per-table search ──
// Filters rows inside a tbody by matching typed text
function searchTable(inputId, tbodyId) {
    const q = document.getElementById(inputId).value.toLowerCase();
    const rows = document.querySelectorAll('#' + tbodyId + ' > tr');
    rows.forEach(row => {
        const txt = row.textContent.toLowerCase();
        row.style.display = txt.includes(q) ? '' : 'none';
    });
}

// ── Global topbar search ──
// Searches whichever tab is currently active
function globalSearchFn(val) {
    const q = val.toLowerCase();
    const activeTab = document.querySelector('.tab-section.active');
    if (!activeTab) return;

    // Skip expandable CR-student rows so they don't flicker
    const rows = activeTab.querySelectorAll('tbody > tr');
    rows.forEach(row => {
        if (row.classList.contains('cr-students-row')) return;
        const txt = row.textContent.toLowerCase();
        row.style.display = (!q || txt.includes(q)) ? '' : 'none';
    });
}

// ── CR expand / collapse students ──
function toggleCRStudents(btn, studentsDivId) {
    const div = document.getElementById(studentsDivId);
    if (!div) return;
    btn.classList.toggle('open');
    div.classList.toggle('open');
}

// ── On page load ──
window.addEventListener('DOMContentLoaded', () => {
    // If there are pending requests, pulse the badge in the sidebar
    const pendingBadge = document.querySelector('#requests-sidebar-btn .badge-count');
    if (pendingBadge && parseInt(pendingBadge.textContent) > 0) {
        pendingBadge.style.animation = 'none';
        setInterval(() => {
            pendingBadge.style.opacity = pendingBadge.style.opacity === '0.4' ? '1' : '0.4';
        }, 900);
    }

    // Auto-dismiss flash messages after 4 seconds
    document.querySelectorAll('.flash-alert').forEach(el => {
        setTimeout(() => {
            el.style.transition = 'opacity 0.5s';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 500);
        }, 4000);
    });
});