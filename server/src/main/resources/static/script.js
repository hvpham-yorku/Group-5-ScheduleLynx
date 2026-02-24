// ============================
// SCHEDULE LYNX - MAIN SCRIPT
// ============================
// noinspection UnnecessaryLocalVariableJS

// Global Variables
let tasks = [];
let currentWeekStart = getMonday(new Date());
let selectedTaskId = null;
let currentUser = null;


// ============================
// AUTH FUNCTIONS
// ============================

// Check if user is logged in
function isLoggedIn() {
    const storedUser = localStorage.getItem('schedulelynxUser');
    return storedUser !== null;
}

// Get current logged in user
function getCurrentUser() {
    const storedUser = localStorage.getItem('schedulelynxUser');
    return storedUser ? JSON.parse(storedUser) : null;
}

// Handle login
function handleLogin(event) {
    if (event) event.preventDefault();
    
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;

    if (!username || !password) {
        alert('Please enter username and password');
        return;
    }

    // Simple authentication (in real app, validate against backend)
    const user = {
        username: username,
        email: username.includes('@') ? username : username + '@schedulelynx.app',
        loginTime: new Date().toISOString(),
        rememberMe: rememberMe
    };

    // Store user in localStorage
    localStorage.setItem('schedulelynxUser', JSON.stringify(user));
    
    // Load tasks for this user
    loadUserTasks(username);
    
    // Redirect to dashboard
    window.location.href = 'index.html';
}

// Demo login
function loginDemo() {
    document.getElementById('username').value = 'demo';
    document.getElementById('password').value = 'demo123';
    handleLogin();
}

// Handle signup
function handleSignup(event) {
    if (event) event.preventDefault();

    const name = document.getElementById('signupName').value.trim();
    const email = document.getElementById('signupEmail').value.trim();
    const username = document.getElementById('signupUsername').value.trim();
    const password = document.getElementById('signupPassword').value;
    const confirmPassword = document.getElementById('signupConfirmPassword').value;

    if (!name || !email || !username || !password || !confirmPassword) {
        alert('Please fill in all fields');
        return;
    }

    if (password !== confirmPassword) {
        alert('Passwords do not match');
        return;
    }

    if (password.length < 6) {
        alert('Password must be at least 6 characters');
        return;
    }

    // Check if username already exists (in real app, check against backend)
    const existingUsers = localStorage.getItem('allUsers') ? JSON.parse(localStorage.getItem('allUsers')) : {};
    if (existingUsers[username]) {
        alert('Username already exists');
        return;
    }

    // Create new user
    const newUser = {
        username: username,
        email: email,
        name: name,
        password: password, // In real app, hash the password!
        createdAt: new Date().toISOString()
    };

    // Store user credentials
    existingUsers[username] = newUser;
    localStorage.setItem('allUsers', JSON.stringify(existingUsers));
    
    // Auto-login
    const user = {
        username: username,
        email: email,
        name: name,
        loginTime: new Date().toISOString(),
        rememberMe: true
    };
    
    localStorage.setItem('schedulelynxUser', JSON.stringify(user));
    
    // Create empty tasks array for new user
    localStorage.setItem(`tasks_${username}`, JSON.stringify([]));
    
    alert('Account created successfully! Logging in...');
    window.location.href = 'index.html';
}

// Logout
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.removeItem('schedulelynxUser');
        window.location.href = 'login.html';
    }
}

// Toggle signup form
function toggleSignup(event) {
    event.preventDefault();
    const loginBox = document.querySelector('.login-box');
    const signupBox = document.getElementById('signupForm');
    
    if (loginBox.style.display === 'none') {
        loginBox.style.display = 'block';
        signupBox.style.display = 'none';
    } else {
        loginBox.style.display = 'none';
        signupBox.style.display = 'block';
    }
}

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    
    // If on login page and already logged in, redirect to dashboard
    if (currentPage === 'login.html' && isLoggedIn()) {
        window.location.href = 'index.html';
        return;
    }
    
    // Allow public pages (home, features, login). Redirect to login for protected pages when not authenticated
    const publicPages = ['home.html', 'features.html', 'login.html'];
    if (!isLoggedIn() && !publicPages.includes(currentPage)) {
        window.location.href = 'login.html';
        return;
    }
    
    // Load user data if logged in
    if (isLoggedIn()) {
        currentUser = getCurrentUser();
        if (document.getElementById('userName')) {
            document.getElementById('userName').textContent = currentUser.username;
        }
    }
    
    // Initialize page-specific handlers
    if (currentPage === 'login.html') {
        initializeLoginHandlers();
    } else if (currentPage === 'index.html') {
        initializeDashboard();
    } else if (currentPage === 'timetable.html') {
        initializeFormHandlers();
        initializeScheduleDisplay();
        loadTasksFromStorage();
    }
});

// Initialize login form handlers
function initializeLoginHandlers() {
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupFormElement');
    
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    if (signupForm) {
        signupForm.addEventListener('submit', handleSignup);
    }
}

// Load user-specific tasks
function loadUserTasks(username) {
    const userTasks = localStorage.getItem(`tasks_${username}`);

    if (userTasks) tasks = JSON.parse(userTasks);
    else tasks = [];
}

// Save user-specific tasks
function saveUserTasks(username) {
    localStorage.setItem(`tasks_${username}`, JSON.stringify(tasks));
}


// ============================
// UTILITY FUNCTIONS
// ============================

// Get the Monday of the current week
function getMonday(d) {
    d = new Date(d);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    return new Date(d.setDate(diff));
}

// Format date as YYYY-MM-DD
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Format date for display
function formatDateDisplay(date) {
    const options = { month: 'short', day: 'numeric', year: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

// Get day name
function getDayName(date) {
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    return days[date.getDay()];
}

// Add days to a date
function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

// Get unique ID
function generateId() {
    return Date.now() + Math.random().toString(36).substr(2, 9);
}


// ============================
// DASHBOARD FUNCTIONS
// ============================

function initializeDashboard() {
    if (!isLoggedIn()) return;
    
    currentUser = getCurrentUser();
    loadUserTasks(currentUser.username);
    updateDashboardStats();
    updateUpcomingTasks();
    updateWeekScheduleMini();
    updateTaskBreakdown();
}

// Refresh helper: update dashboard widgets when visible
function refreshDashboardIfVisible() {
    // Only run if dashboard elements exist on the page
    if (document.getElementById('totalTasksCount')) {
        updateDashboardStats();
        updateUpcomingTasks();
        updateWeekScheduleMini();
        updateTaskBreakdown();
    }
}

function updateDashboardStats() {
    const today = new Date();
    const weekFromNow = addDays(today, 7);
    
    // Total tasks
    document.getElementById('totalTasksCount').textContent = tasks.length;
    
    // Upcoming tasks (next 7 days)
    const upcoming = tasks.filter(task => {
        const dueDate = new Date(task.dueDate);
        return dueDate > today && dueDate <= weekFromNow;
    });
    document.getElementById('upcomingCount').textContent = upcoming.length;
    
    // Overdue tasks
    const overdue = tasks.filter(task => {
        const dueDate = new Date(task.dueDate);
        return dueDate < today && !task.completed;
    });
    document.getElementById('overdueCount').textContent = overdue.length;
    
    // This week's hours
    let totalHours = 0;
    tasks.forEach(task => {
        const dueDate = new Date(task.dueDate);
        if (dueDate >= getMonday(today) && dueDate <= addDays(getMonday(today), 6)) {
            totalHours += task.estimatedHours;
        }
    });
    document.getElementById('weekHoursCount').textContent = totalHours + 'h';
}

function updateUpcomingTasks() {
    const upcomingTasksList = document.getElementById('upcomingTasksList');
    const today = new Date();
    const weekFromNow = addDays(today, 7);
    
    // Get upcoming tasks, sorted by dueDate
    const upcoming = tasks
        .filter(task => {
            const dueDate = new Date(task.dueDate);
            return dueDate > today && dueDate <= weekFromNow && !task.completed;
        })
        .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
        .slice(0, 5); // Show top 5
    
    if (upcoming.length === 0) {
        upcomingTasksList.innerHTML = '<p class="empty-state">No upcoming tasks. <a href="timetable.html">Add a task</a>!</p>';
        return;
    }
    
    upcomingTasksList.innerHTML = upcoming.map(task => `
        <div class="task-item-dashboard ${task.type}" onclick="viewTaskDetails('${task.id}')">
            <div class="task-item-badge ${task.type}">${task.type}</div>
            <div class="task-item-title">${task.title}</div>
            <div class="task-item-dueDate">Due: ${formatDateDisplay(new Date(task.dueDate))}</div>
        </div>
    `).join('');
}

function updateWeekScheduleMini() {
    const weekScheduleMini = document.getElementById('weekScheduleMini');
    const monday = getMonday(new Date());
    
    let hasEvents = false;
    const weekDays = [];
    
    for (let i = 0; i < 7; i++) {
        const dayDate = addDays(monday, i);
        const dayName = getDayName(dayDate).substring(0, 1);
        const dateStr = formatDate(dayDate);
        
        const dayEvents = getEventsForDay(dateStr);
        const eventCount = dayEvents.length;
        hasEvents = hasEvents || eventCount > 0;
        
        weekDays.push(`
            <div class="day-mini">
                <div class="day-mini-label">${dayName}</div>
                <div class="day-mini-box ${eventCount > 0 ? 'has-events' : 'empty'}">
                    ${eventCount > 0 ? eventCount + ' tasks' : '—'}
                </div>
            </div>
        `);
    }

    if (!hasEvents) weekScheduleMini.innerHTML =
        '<p class="empty-state">No events scheduled. ' +
        '<a href="timetable.html">Create your schedule</a>!</p>';
    else weekScheduleMini.innerHTML =
        '<div style="display: grid; ' +
        'grid-template-columns: repeat(7, 1fr); ' +
        'gap: 0.75rem;">' + weekDays.join('') + '</div>';
}

function updateTaskBreakdown() {
    const taskBreakdown = document.getElementById('taskBreakdown');
    const types = ['class', 'assignment', 'exam', 'shift', 'personal'];
    
    const breakdown = {};
    types.forEach(type => {
        breakdown[type] = tasks.filter(t => t.type === type).length;
    });
    
    const hasAnyTasks = Object.values(breakdown).some(count => count > 0);
    
    if (!hasAnyTasks) {
        taskBreakdown.innerHTML = '<p class="empty-state">Add tasks to see breakdown</p>';
        return;
    }
    
    const typeLabels = {
        class: 'Classes',
        assignment: 'Assignments',
        exam: 'Exams',
        shift: 'Shifts',
        personal: 'Personal'
    };
    
    taskBreakdown.innerHTML = types
        .filter(type => breakdown[type] > 0)
        .map(type => `
            <div class="breakdown-item ${type}">
                <div class="breakdown-label">${typeLabels[type]}</div>
                <div class="breakdown-count">${breakdown[type]}</div>
            </div>
        `).join('');
}


// ============================
// FORM HANDLING
// ============================

function initializeFormHandlers() {
    const taskForm = document.getElementById('taskForm');
    const taskTypeSelect = document.getElementById('taskType');
    const isRecurringCheckbox = document.getElementById('isRecurring');
    const recurrenceTypeSelect = document.getElementById('recurrenceType');

    // Show/hide time fields based on task type
    taskTypeSelect.addEventListener('change', function() {
        const startTimeGroup = document.getElementById('startTimeGroup');
        const endTimeGroup = document.getElementById('endTimeGroup');
        const estimatedHoursGroup = document.getElementById('estimatedHoursGroup');
        const recurringGroup = document.getElementById('recurringGroup');
        const recurrenceOptions = document.getElementById('recurenceOptions');
        const daysOfWeekGroup = document.getElementById('daysOfWeekGroup');
        const isRecurringCheckbox = document.getElementById('isRecurring');
        
        if (this.value === 'task') {
            startTimeGroup.style.display = 'none';
            endTimeGroup.style.display = 'none';
            estimatedHoursGroup.style.display = 'block';
            recurringGroup.style.display = 'none';
            isRecurringCheckbox.checked = false;
            recurrenceOptions.style.display = 'none';
            daysOfWeekGroup.style.display = 'none';
        } else {
            startTimeGroup.style.display = 'flex';
            endTimeGroup.style.display = 'flex';

            estimatedHoursGroup.style.display = 'none';
            recurringGroup.style.display = 'block';
            recurrenceOptions.style.display = isRecurringCheckbox.checked ? 'block' : 'none';

        }
    });

    // Show/hide recurrence options
    isRecurringCheckbox.addEventListener('change', function() {
        if (taskTypeSelect.value !== 'event') return;

        const recurrenceOptions = document.getElementById('recurrenceOptions');
        const daysOfWeekGroup = document.getElementById('daysOfWeekGroup');

        recurrenceOptions.style.display = this.checked ? 'block' : 'none';

        if (!this.checked)
        {
            daysOfWeekGroup.style.display = 'none';
        }
    });

    // Show/hide days of week based on recurrence type
    recurrenceTypeSelect.addEventListener('change', function() {
        const daysOfWeekGroup = document.getElementById('daysOfWeekGroup');
        daysOfWeekGroup.style.display = this.value === 'weekly' || this.value === 'biweekly' ? 'flex' : 'none';
    });

    // Form submission
    taskForm.addEventListener('submit', function(e) {
        e.preventDefault();
        addTask();
        taskForm.reset();
        document.getElementById('recurrenceOptions').style.display = 'none';
        document.getElementById('startTimeGroup').style.display = 'none';
        document.getElementById('endTimeGroup').style.display = 'none';
    });

    taskTypeSelect.dispatchEvent(new Event('change'));

}

async function addTask() {

    if (!currentUser) {
        alert("You are not logged in.");
        return;
    }

    // TODO: These could be made into simple getters to reduce boilerplate code
    const title = document.getElementById('taskTitle').value.trim();
    const type = document.getElementById('taskType').value;
    const dueDate = document.getElementById('dueDate').value;

    const estimatedHoursInput = document.getElementById('estimatedHours');
    const estimatedHours = estimatedHoursInput ? parseFloat(estimatedHoursInput.value) : 0;

    const startTime = document.getElementById('startTime').value;
    const endTime = document.getElementById('endTime').value;
    const description = document.getElementById('description').value.trim();
    const isRecurring = document.getElementById('isRecurring').checked;
    const recurrenceType = document.getElementById('recurrenceType').value;
    const recurrenceEnd = document.getElementById('recurrenceEnd').value;

    // Basic validation (keeps backend errors from being your first feedback)
    if (!title || !type || !dueDate) {
        alert("Please fill in Title, Type, and Date.");
        return;
    }
    if (type === "event") {
        if (!startTime || !endTime) {
            alert("Please enter start and end time for an Event.");
            return;
        }
    } else if (type === "task") {
        if (!estimatedHours || estimatedHours < 1) {
            alert("Estimated hours must be at least 1 for a Task.");
            return;
        }
    }

    // Helper: convert YYYY-MM-DD -> backend Weekday enum string
    function toBackendWeekdayEnum(dateStr) {
        // Use noon to avoid any weird timezone edge cases around midnight
        const d = new Date(`${dateStr}T12:00:00`);
        const jsDay = d.getDay(); // 0=Sun ... 6=Sat
        const map = {
            0: "SUNDAY",
            1: "MONDAY",
            2: "TUESDAY",
            3: "WEDNESDAY",
            4: "THURSDAY",
            5: "FRIDAY",
            6: "SATURDAY"
        };
        return map[jsDay];
    }

    const item = {
        id: generateId(),              // will be replaced by backend id on success
        title: title,
        type: type,
        dueDate: dueDate,              // kept for your UI calendar/grid
        description: description,
        completed: false,
        createdAt: new Date().toISOString()
    };

    // Enable generate schedule button
    document.getElementById('generateSchedule').disabled = false;

    let apiURL;
    let payload;

    if (type === "event") {
        apiURL = "http://localhost:8080/api/events";

        payload = {
            title: title,
            day: toBackendWeekdayEnum(dueDate),
            start: startTime, // LocalTime expects "HH:mm" (your input gives this)
            end: endTime
        };
    } else {
        apiURL = "http://localhost:8080/api/tasks";

        payload = {
            title: title,
            dueDate: dueDate, // LocalDate expects "YYYY-MM-DD"
            estimatedHours: Math.round(estimatedHours), // backend expects int
            difficulty: "MEDIUM" // required by backend; update later if you add a UI field
        };
    }

    let response = await fetch(apiURL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    console.log(response);
    if (!response.ok) return;

    const created = await response.json().catch(() => null);
    if (created && created.id != null) {
        item.id = String(created.id);
    }

    if (type === "event") {
        item.startTime = startTime;
        item.endTime = endTime;
        item.estimatedHours = 0;
        item.isRecurring = false;
        item.recurrenceType = null;
        item.recurrenceEnd = null;
        item.recurrenceDays = [];
    } else {
        item.estimatedHours = Math.round(estimatedHours);
        item.startTime = null;
        item.endTime = null;
        item.isRecurring = false;
        item.recurrenceType = null;
        item.recurrenceEnd = null;
        item.recurrenceDays = [];
    }

    tasks.push(item);
    saveUserTasks(currentUser.username);
    updateTasksDisplay();
    renderScheduleGrid();
    refreshDashboardIfVisible();
}


// ============================
// TASK DISPLAY
// ============================

function updateTasksDisplay() {
    const tasksList = document.getElementById('tasksList');
    
    if (tasks.length === 0) {
        tasksList.innerHTML = '<p class="empty-state">No tasks added yet. Add a task to get started!</p>';
        return;
    }

    tasksList.innerHTML = tasks.map(task => `
        <div class="task-card ${task.type}" onclick="viewTaskDetails('${task.id}')">
            <div class="task-card-type">${task.type.charAt(0).toUpperCase() + task.type.slice(1)}</div>
            <div class="task-card-title">${task.title}</div>
            <div class="task-card-dueDate">Due: ${formatDateDisplay(new Date(task.dueDate))}</div>
            ${task.type === 'task'
            ? `<div class="task-card-time">${task.estimatedHours} hours</div>`
            : (task.startTime ? `<div class="task-card-time">${task.startTime} - ${task.endTime}</div>` : '')
            }
            ${task.isRecurring ? '<div class="task-card-time">Recurring: ' + task.recurrenceType + '</div>' : ''}
        </div>
    `).join('');
}

function viewTaskDetails(taskId) {
    const task = tasks.find(t => t.id === taskId);
    if (!task) return;

    selectedTaskId = taskId;
    const modal = document.getElementById('taskModal');
    const modalBody = document.getElementById('modalBody');

    const recurrenceText = task.isRecurring 
        ? `${task.recurrenceType.charAt(0).toUpperCase() + task.recurrenceType.slice(1)} (${task.recurrenceDays.join(', ')})`
        : 'No';

    modalBody.innerHTML = `
        <div class="modal-detail">
            <span class="modal-detail-label">Title:</span>
            <span class="modal-detail-value">${task.title}</span>
        </div>
        <div class="modal-detail">
            <span class="modal-detail-label">Type:</span>
            <span class="modal-detail-value">${task.type}</span>
        </div>
        <div class="modal-detail">
            <span class="modal-detail-label">dueDate:</span>
            <span class="modal-detail-value">${formatDateDisplay(new Date(task.dueDate))}</span>
        </div>
        <div class="modal-detail">
            <span class="modal-detail-label">Estimated Time:</span>
            <span class="modal-detail-value">${task.estimatedHours} hours</span>
        </div>
        ${task.startTime ? `
        <div class="modal-detail">
            <span class="modal-detail-label">Start Time:</span>
            <span class="modal-detail-value">${task.startTime}</span>
        </div>
        ` : ''}
        ${task.endTime ? `
        <div class="modal-detail">
            <span class="modal-detail-label">End Time:</span>
            <span class="modal-detail-value">${task.endTime}</span>
        </div>
        ` : ''}
        <div class="modal-detail">
            <span class="modal-detail-label">Recurring:</span>
            <span class="modal-detail-value">${recurrenceText}</span>
        </div>
        ${task.description ? `
        <div class="modal-detail">
            <span class="modal-detail-label">Description:</span>
            <span class="modal-detail-value">${task.description}</span>
        </div>
        ` : ''}
    `;

    modal.classList.add('active');
}


// ============================
// MODAL HANDLING
// ============================

document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('taskModal');
    
    if (modal) {
        const closeBtn = document.querySelector('.close-modal');
        const closeModalBtn = document.getElementById('closeModalBtn');
        const deleteTaskBtn = document.getElementById('deleteTaskBtn');
        const editTaskBtn = document.getElementById('editTaskBtn');

        closeBtn.addEventListener('click', () => modal.classList.remove('active'));
        closeModalBtn.addEventListener('click', () => modal.classList.remove('active'));

        window.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });

        deleteTaskBtn.addEventListener('click', deleteTaskListener);
        editTaskBtn.addEventListener('click', editSelectedTask);
    }
});

async function deleteTaskListener() {

    if (!selectedTaskId) return;

    const item = tasks.find(t => t.id === selectedTaskId);
    if (!item) return;

    if (!confirm("Are you sure you want to delete this task?")) return;

    let response;
    if (item.type === "event") {

        const eventID = selectedTaskId;
        const request = "http://localhost:8080/api/events/" + eventID;
        response = await fetch(request, {method: 'DELETE',});

    } else if (item.type === "task") {

        const taskID = selectedTaskId;
        const request = "http://localhost:8080/api/tasks/" + taskID;
        response = await fetch(request, {method: 'DELETE',});

    }

    console.log(response);
    if(!response.ok) return;

    tasks = tasks.filter(t => t.id !== selectedTaskId);
    saveTasksToStorage();
    updateTasksDisplay();
    renderScheduleGrid();
    refreshDashboardIfVisible();
    document.getElementById("taskModal").classList.remove("active");
}

function editSelectedTask() {
    const task = tasks.find(t => t.id === selectedTaskId);
    if (task) {
        // Populate form with task data
        document.getElementById('taskTitle').value = task.title;
        document.getElementById('taskType').value = task.type;
        document.getElementById('dueDate').value = task.dueDate;
        document.getElementById('estimatedHours').value = task.estimatedHours;
        document.getElementById('startTime').value = task.startTime || '';
        document.getElementById('endTime').value = task.endTime || '';
        document.getElementById('description').value = task.description;
        document.getElementById('isRecurring').checked = task.isRecurring;
        document.getElementById('recurrenceType').value = task.recurrenceType;
        document.getElementById('recurrenceEnd').value = task.recurrenceEnd || '';

        // Show/hide time fields
        if (task.type === 'class' || task.type === 'shift') {
            document.getElementById('startTimeGroup').style.display = 'flex';
            document.getElementById('endTimeGroup').style.display = 'flex';
        }

        // Show recurrence options
        if (task.isRecurring) {
            document.getElementById('recurrenceOptions').style.display = 'block';
            if (task.recurrenceType === 'weekly' || task.recurrenceType === 'biweekly') {
                document.getElementById('daysOfWeekGroup').style.display = 'flex';
                document.querySelectorAll('.days-checkbox input').forEach(cb => {
                    cb.checked = task.recurrenceDays.includes(cb.value);
                });
            }
        }

        // Delete old task and scroll to form
        tasks = tasks.filter(t => t.id !== selectedTaskId);
        document.getElementById('taskModal').classList.remove('active');
        document.getElementById('taskTitle').focus();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}


// ============================
// SCHEDULE GENERATION & DISPLAY
// ============================

function initializeScheduleDisplay() {
    updateWeekDisplay();
    renderScheduleGrid();
    
    document.getElementById('prevWeek').addEventListener('click', () => {
        currentWeekStart = addDays(currentWeekStart, -7);
        updateWeekDisplay();
        renderScheduleGrid();
    });

    document.getElementById('nextWeek').addEventListener('click', () => {
        currentWeekStart = addDays(currentWeekStart, 7);
        updateWeekDisplay();
        renderScheduleGrid();
    });

    document.getElementById('generateSchedule').addEventListener('click', generateSchedule);
    document.getElementById('clearAll').addEventListener('click', clearAllItems);
}

function updateWeekDisplay() {
    const weekEnd = addDays(currentWeekStart, 6);
    document.getElementById('weekDisplay').textContent =
        `Week of ${formatDateDisplay(currentWeekStart)} - ${formatDateDisplay(weekEnd)}`;
}

function renderScheduleGrid() {
    const scheduleGrid = document.getElementById('scheduleGrid');
    scheduleGrid.innerHTML = '';

    for (let i = 0; i < 7; i++) {
        const dayDate = addDays(currentWeekStart, i);
        const dayName = getDayName(dayDate);
        const formattedDate = formatDate(dayDate);

        // Get events for this day
        const dayEvents = getEventsForDay(formattedDate);

        const dayColumn = document.createElement('div');
        dayColumn.className = 'day-column';
        dayColumn.innerHTML = `
            <div class="day-header">${dayName}<br>${dayDate.getDate()}</div>
            <div class="day-content">
                ${dayEvents.length > 0 
                    ? dayEvents.map(event => `
                        <div class="schedule-event ${event.type}" onclick="viewTaskDetails('${event.id}')">
                            <div class="schedule-event-title">${event.title}</div>
                            ${event.startTime
                                ? `<div class="schedule-event-time">${event.startTime} - ${event.endTime}</div>`
                                : `<div class="schedule-event-time">${event.label || 'Due'}</div>`
                            }
                        </div>
                    `).join('')
                    : '<p class="empty-state">No events</p>'
                }
            </div>
        `;

        scheduleGrid.appendChild(dayColumn);
    }
}

function getEventsForDay(dateStr) {
    const items = [];

    tasks.forEach(item => {
        // Check if task falls on this day
        if(item.type ==='event'){
            if(item.isRecurring){
                const dayName = getDayName(new Date(dateStr)); 
                const end = item.recurrenceEnd ? new Date(item.recurrenceEnd) : null;

                const allowed = 
                    !item.recurrenceDays || item.recurrenceDays.length === 0
                        ? true
                        : item.recurrenceDays.includes(dayName);


                const start = item.dueDate ? new Date(item.dueDate) : null;
                const cur = new Date(dateStr);

                if (
                    allowed &&
                    (!start || cur >= start) &&   
                    (!end || cur <= end)          
                ) {
                    items.push({
                    id: item.id,
                    title: item.title,
                    type: 'event',
                    startTime: item.startTime,
                    endTime: item.endTime,
                    label: null
                    });
                }
            }
            else {
                 if (item.dueDate === dateStr) {
                    items.push({
                        id: item.id,
                        title: item.title,
                        type: 'event',
                        startTime: item.startTime,
                        endTime: item.endTime,
                        label: null
                    });
                }
            }
        }

        if(item.type === 'task'){
            if(item.dueDate === dateStr){
                items.push({
                    id: item.id,
                    title: item.title,
                    type: 'task',     
                    startTime: null,
                    endTime: null,
                    label: 'Due'  
                })
            }
        }
    });

    // Sort by start time
    items.sort((a, b) => {
        const aTimed = !!a.startTime;
        const bTimed = !!b.startTime;

        if (aTimed && bTimed) return a.startTime.localeCompare(b.startTime);
        if (aTimed) return -1;
        if (bTimed) return 1;
        return a.title.localeCompare(b.title);
    });

    return items;
}

function generateSchedule() {
    if (tasks.length === 0) {
        alert('Please add at least one task before generating a schedule');
        return;
    }

    // Get non-recurring tasks that need scheduling
    const tasksToSchedule = tasks.filter(t => t.type === 'task');
    if (tasksToSchedule.length === 0) {
        alert('Add assignment, exam, or personal tasks to generate a schedule');
        return;
    }

    // Simple scheduling algorithm: distribute tasks across available days
    const scheduledTasks = distributeTasksAcrossTime(tasksToSchedule);
    
    renderTimeline(scheduledTasks);
    alert('Schedule generated successfully! Check the timeline below.');
}

function distributeTasksAcrossTime(tasksToSchedule) {

    const sortedTasks = tasksToSchedule.sort((a, b) => {
        return new Date(a.dueDate) - new Date(b.dueDate);
    });

    const scheduledItems = [];

    sortedTasks.forEach(task => {
        const dueDate = new Date(task.dueDate);
        const hoursNeeded = task.estimatedHours;
        const daysAvailable = Math.ceil((dueDate - new Date()) / (1000 * 60 * 60 * 24));

        if (daysAvailable > 0) {
            const hoursPerDay = Math.min(3, hoursNeeded / daysAvailable);
            
            let currentDate = new Date();
            let remainingHours = hoursNeeded;

            while (remainingHours > 0 && currentDate < dueDate) {
                const scheduledHours = Math.min(hoursPerDay, remainingHours);
                
                scheduledItems.push({
                    date: formatDateDisplay(currentDate),
                    dateStr: formatDate(currentDate),
                    task: task,
                    hours: scheduledHours
                });

                remainingHours -= scheduledHours;
                currentDate = addDays(currentDate, 1);
            }
        }
    });

    return scheduledItems;
}

function renderTimeline(scheduledItems) {
    const timeline = document.getElementById('timeline');

    if (scheduledItems.length === 0) {
        timeline.innerHTML = '<p class="empty-state">No tasks to schedule.</p>';
        return;
    }

    // Group by date
    const groupedByDate = {};
    scheduledItems.forEach(item => {
        if (!groupedByDate[item.date]) {
            groupedByDate[item.date] = [];
        }
        groupedByDate[item.date].push(item);
    });

    // Render timeline
    timeline.innerHTML = Object.keys(groupedByDate).sort((a, b) => {
        return new Date(a) - new Date(b);
    }).map(date => {
        const dateItems = groupedByDate[date];
        return `
            <div class="timeline-item">
                <div class="timeline-date">${date}</div>
                ${dateItems.map(item => `
                    <div class="timeline-task ${item.task.type}" onclick="viewTaskDetails('${item.task.id}')">
                        <div class="timeline-task-title">${item.task.title}</div>
                        <div class="timeline-task-info">
                            Scheduled: ${item.hours} hours | 
                            Due: ${formatDateDisplay(new Date(item.task.dueDate))}
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }).join('');
}


// ============================
// STORAGE MANAGEMENT
// ============================

function saveTasksToStorage() {
    if (currentUser) {
        saveUserTasks(currentUser.username);
        // update dashboard widgets if they're present
        refreshDashboardIfVisible();
    }
}

function loadTasksFromStorage() {
    if (currentUser) {
        loadUserTasks(currentUser.username);
        updateTasksDisplay();
        renderScheduleGrid();
        if (tasks.length > 0) {
            document.getElementById('generateSchedule').disabled = false;
        }
        // refresh dashboard after loading
        refreshDashboardIfVisible();
    }
}

/** Deletes all items (tasks and events) from the server. Use with caution. */
async function clearAllItems() {

    if (!(confirm("Are you sure you want to clear all tasks?\nThis cannot be undone."))) return;

    const taskRequest = "http://localhost:8080/api/tasks"
    let restResponse = await fetch(taskRequest, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    });
    console.log(restResponse);

    const eventRequest = "http://localhost:8080/api/events"
    let eventResponse = await fetch(eventRequest, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    });
    console.log(eventRequest);

    // TODO: separate concerns of clearing the frontend so if one fails the other is still cleared
    if (!restResponse.ok || !eventRequest) return;

    tasks = [];
    saveTasksToStorage();
    updateTasksDisplay();
    document.getElementById('timeline').innerHTML = '<p class="empty-state">Tasks will appear here once you add them and generate the schedule.</p>';
    renderScheduleGrid();
    document.getElementById('generateSchedule').disabled = true;
    refreshDashboardIfVisible();
    alert('All tasks cleared!');
}


// ============================
// PAGE NAVIGATION
// ============================

document.addEventListener('DOMContentLoaded', function() {
    // Highlight current page in navigation
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPage || (currentPage === '' && href === 'index.html')) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
});
