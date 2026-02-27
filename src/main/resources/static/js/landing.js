// Landing Page JavaScript

// Navbar scroll effect
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar-landing');
    if (window.scrollY > 50) {
        navbar.classList.add('scrolled');
    } else {
        navbar.classList.remove('scrolled');
    }
});

// Smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            const navbarHeight = document.querySelector('.navbar-landing').offsetHeight;
            const targetPosition = target.offsetTop - navbarHeight;

            window.scrollTo({
                top: targetPosition,
                behavior: 'smooth'
            });
        }
    });
});

// Form submission handling
const contactForm = document.querySelector('.contact-form form');
if (contactForm) {
    contactForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // Get form data
        const formData = new FormData(this);

        // Show loading state
        const submitBtn = this.querySelector('.btn-submit');
        const originalText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="bi bi-hourglass-split me-2"></i>Sending...';
        submitBtn.disabled = true;

        // Simulate form submission (replace with actual AJAX call)
        setTimeout(() => {
            // Reset form
            this.reset();

            // Show success message
            submitBtn.innerHTML = '<i class="bi bi-check-circle me-2"></i>Message Sent!';
            submitBtn.classList.remove('btn-submit');
            submitBtn.classList.add('btn-success');

            // Reset button after 3 seconds
            setTimeout(() => {
                submitBtn.innerHTML = originalText;
                submitBtn.classList.remove('btn-success');
                submitBtn.classList.add('btn-submit');
                submitBtn.disabled = false;
            }, 3000);
        }, 1500);
    });
}

// Video placeholder click handler
const videoPlaceholder = document.querySelector('.video-placeholder');
if (videoPlaceholder) {
    videoPlaceholder.addEventListener('click', function() {
        alert('Add your video URL in the HTML file to enable video playback!');
    });
}

// Animate elements on scroll
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver(function(entries) {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe all feature cards
document.querySelectorAll('.feature-card').forEach(card => {
    card.style.opacity = '0';
    card.style.transform = 'translateY(30px)';
    card.style.transition = 'all 0.6s ease';
    observer.observe(card);
});

// Console welcome message
console.log('%cAssignment Portal', 'color: #667eea; font-size: 24px; font-weight: bold;');
console.log('%cWelcome to the Assignment Portal!', 'color: #718096; font-size: 14px;');