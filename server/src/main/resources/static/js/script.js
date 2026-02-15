/* ===== Authentication Functions ===== */

// Check if user is logged in
function isLoggedIn() {
	return localStorage.getItem('schedulelynxUser') !== null;
}

// Get current user from localStorage
function getCurrentUser() {
	const user = localStorage.getItem('schedulelynxUser');
	return user ? JSON.parse(user) : null;
}

// Handle login form submission
function handleLogin(event) {
	event.preventDefault();
	
	const username = jQuery('#loginUsername').val();
	const password = jQuery('#loginPassword').val();
	const rememberMe = jQuery('#customCheck1').is(':checked');
	
	if (!username || !password) {
		alert('Please enter both username and password');
		return;
	}
	
	// Store user to localStorage
	const user = {
		username: username,
		email: username,
		loginTime: new Date().toISOString(),
		rememberMe: rememberMe
	};
	
	localStorage.setItem('schedulelynxUser', JSON.stringify(user));
	
	// Load user tasks
	loadUserTasks(username);
	
	// Redirect to home
	window.location.href = 'index.html';
}

// Handle signup form submission
function handleSignup(event) {
	event.preventDefault();
	
	const name = jQuery('#signupName').val();
	const email = jQuery('#signupEmail').val();
	const password = jQuery('#signupPassword').val();
	const confirmPassword = jQuery('#signupConfirmPassword').val();
	
	if (!name || !email || !password || !confirmPassword) {
		alert('Please fill in all fields');
		return;
	}
	
	if (password !== confirmPassword) {
		alert('Passwords do not match');
		return;
	}
	
	// For now, treat signup as successful login with new user
	const user = {
		username: email,
		email: email,
		loginTime: new Date().toISOString(),
		rememberMe: false
	};
	
	localStorage.setItem('schedulelynxUser', JSON.stringify(user));
	loadUserTasks(email);
	window.location.href = 'index.html';
}

// Load user tasks (placeholder)
function loadUserTasks(username) {
	// Initialize user data structure if not exists
	const userDataKey = 'schedulelynx_' + username;
	if (!localStorage.getItem(userDataKey)) {
		localStorage.setItem(userDataKey, JSON.stringify({
			tasks: [],
			events: [],
			availability: []
		}));
	}
}

/* ===== Form Slider Animation ===== */

jQuery(document).ready(function() {
	
	// Form sliding animation for signup
	jQuery('.sign-up-click').on('click', function(e) {
		e.preventDefault();
		
		// Animate login slide out
		jQuery('.login-slide').velocity({
			translateX: ['-100%', '0%']
		}, {
			duration: 500,
			easing: 'easeInOutQuad'
		});
		
		// Animate signup slide in
		jQuery('.signup-slide').velocity({
			translateX: ['0%', '100%']
		}, {
			duration: 500,
			easing: 'easeInOutQuad'
		});
		
		// Hide forgot password slide if visible
		jQuery('.forgot-password-slide').velocity({
			translateX: ['100%', '0%']
		}, {
			duration: 0
		});
	});
	
	// Return to login from signup
	jQuery('.login-click').on('click', function(e) {
		e.preventDefault();
		
		// Animate signup slide out
		jQuery('.signup-slide').velocity({
			translateX: ['100%', '0%']
		}, {
			duration: 500,
			easing: 'easeInOutQuad'
		});
		
		// Animate login slide in
		jQuery('.login-slide').velocity({
			translateX: ['0%', '-100%']
		}, {
			duration: 500,
			easing: 'easeInOutQuad'
		});
		
		// Hide forgot password slide
		jQuery('.forgot-password-slide').velocity({
			translateX: ['100%', '0%']
		}, {
			duration: 0
		});
	});
	
	// Show forgot password slide
	jQuery('.forgot-password-click').on('click', function(e) {
		e.preventDefault();
		
		// Animate login slide out
		jQuery('.login-slide').velocity({
			translateX: ['-100%', '0%']
		}, {
			duration: 500,
			easing: 'easeInOutQuad'
		});
		
		// Animate forgot password slide in
		jQuery('.forgot-password-slide').velocity({
			translateX: ['0%', '100%']
		}, {
			duration: 500,
			easing: 'easeInOutQuad'
		});
	});
	
	/* ===== Form Submission Handlers ===== */
	
	// Login form submit
	jQuery('#loginFormElement').on('submit', function(e) {
		handleLogin(e);
	});
	
	// Signup form submit
	jQuery('#signupFormElement').on('submit', function(e) {
		handleSignup(e);
	});
	
	// Forgot password form submit
	jQuery('#forgotFormElement').on('submit', function(e) {
		e.preventDefault();
		alert('Password reset link has been sent to your email');
		// Return to login slide
		jQuery('.forgot-password-slide').velocity({
			translateX: ['0%', '-100%']
		}, {
			duration: 500,
			easing: 'easeInOutQuad'
		});
		
		jQuery('.login-slide').velocity({
			translateX: ['-100%', '0%']
		}, {
			duration: 500,
			easing: 'easeInOutQuad'
		});
	});
	
	/* ===== Form Input Floating State ===== */
	
	// Add classes for floating form enhancement
	jQuery('.form-control').on('focus', function() {
		jQuery(this).addClass('is-focus');
		if (jQuery(this).val() !== '') {
			jQuery(this).addClass('has-value');
		}
	});
	
	jQuery('.form-control').on('blur', function() {
		jQuery(this).removeClass('is-focus');
		if (jQuery(this).val() === '') {
			jQuery(this).removeClass('has-value');
		}
	});
	
	jQuery('.form-control').on('keyup', function() {
		if (jQuery(this).val() !== '') {
			jQuery(this).addClass('has-value');
		} else {
			jQuery(this).removeClass('has-value');
		}
	});
	
	// Check login status and redirect if already logged in
	if (isLoggedIn()) {
		// Optionally redirect to home if accessing login page while logged in
		// window.location.href = 'index.html';
	}
	
});