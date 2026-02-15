// ============================
// SCHEDULE LYNX - MAIN SCRIPT
// ============================

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
    if (userTasks) {
        tasks = JSON.parse(userTasks);
    } else {
        tasks = [];
    }
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
        const deadline = new Date(task.deadline);
        return deadline > today && deadline <= weekFromNow;
    });
    document.getElementById('upcomingCount').textContent = upcoming.length;
    
    // Overdue tasks
    const overdue = tasks.filter(task => {
        const deadline = new Date(task.deadline);
        return deadline < today && !task.completed;
    });
    document.getElementById('overdueCount').textContent = overdue.length;
    
    // This week's hours
    let totalHours = 0;
    tasks.forEach(task => {
        const deadline = new Date(task.deadline);
        if (deadline >= getMonday(today) && deadline <= addDays(getMonday(today), 6)) {
            totalHours += task.estimatedHours;
        }
    });
    document.getElementById('weekHoursCount').textContent = totalHours + 'h';
}

function updateUpcomingTasks() {
    const upcomingTasksList = document.getElementById('upcomingTasksList');
    const today = new Date();
    const weekFromNow = addDays(today, 7);
    
    // Get upcoming tasks, sorted by deadline
    const upcoming = tasks
        .filter(task => {
            const deadline = new Date(task.deadline);
            return deadline > today && deadline <= weekFromNow && !task.completed;
        })
        .sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
        .slice(0, 5); // Show top 5
    
    if (upcoming.length === 0) {
        upcomingTasksList.innerHTML = '<p class="empty-state">No upcoming tasks. <a href="timetable.html">Add a task</a>!</p>';
        return;
    }
    
    upcomingTasksList.innerHTML = upcoming.map(task => `
        <div class="task-item-dashboard ${task.type}" onclick="viewTaskDetails('${task.id}')">
            <div class="task-item-badge ${task.type}">${task.type}</div>
            <div class="task-item-title">${task.title}</div>
            <div class="task-item-deadline">Due: ${formatDateDisplay(new Date(task.deadline))}</div>
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
    
    if (!hasEvents) {
        weekScheduleMini.innerHTML = '<p class="empty-state">No events scheduled. <a href="timetable.html">Create your schedule</a>!</p>';
    } else {
        weekScheduleMini.innerHTML = '<div style="display: grid; grid-template-columns: repeat(7, 1fr); gap: 0.75rem;">' + weekDays.join('') + '</div>';
    }
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
        
        if (this.value === 'class' || this.value === 'shift') {
            startTimeGroup.style.display = 'flex';
            endTimeGroup.style.display = 'flex';
        } else {
            startTimeGroup.style.display = 'none';
            endTimeGroup.style.display = 'none';
        }
    });

    // Show/hide recurrence options
    isRecurringCheckbox.addEventListener('change', function() {
        const recurrenceOptions = document.getElementById('recurrenceOptions');
        recurrenceOptions.style.display = this.checked ? 'block' : 'none';
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
}

function addTask() {
    const taskTitle = document.getElementById('taskTitle').value.trim();
    const taskType = document.getElementById('taskType').value;
    const deadline = document.getElementById('deadline').value;
    const estimatedHours = parseFloat(document.getElementById('estimatedHours').value);
    const startTime = document.getElementById('startTime').value;
    const endTime = document.getElementById('endTime').value;
    const description = document.getElementById('description').value.trim();
    const isRecurring = document.getElementById('isRecurring').checked;
    const recurrenceType = document.getElementById('recurrenceType').value;
    const recurrenceEnd = document.getElementById('recurrenceEnd').value;

    if (!taskTitle || !taskType || !deadline || !estimatedHours) {
        alert('Please fill in all required fields');
        return;
    }

    // Get selected days of week if recurring
    let selectedDays = [];
    if (isRecurring && (recurrenceType === 'weekly' || recurrenceType === 'biweekly')) {
        const checkboxes = document.querySelectorAll('.days-checkbox input[type="checkbox"]:checked');
        selectedDays = Array.from(checkboxes).map(cb => cb.value);
        if (selectedDays.length === 0) {
            alert('Please select at least one day for recurring tasks');
            return;
        }
    }

    const task = {
        id: generateId(),
        title: taskTitle,
        type: taskType,
        deadline: deadline,
        estimatedHours: estimatedHours,
        startTime: startTime || null,
        endTime: endTime || null,
        description: description,
        isRecurring: isRecurring,
        recurrenceType: recurrenceType,
        recurrenceEnd: recurrenceEnd,
        recurrenceDays: selectedDays,
        completed: false,
        createdAt: new Date().toISOString()
    };

    tasks.push(task);
    saveUserTasks(currentUser.username);
    updateTasksDisplay();
    refreshDashboardIfVisible();
    
    // Enable generate schedule button
    document.getElementById('generateSchedule').disabled = false;
    
    alert(`Task "${taskTitle}" added successfully!`);
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
            <div class="task-card-deadline">Due: ${formatDateDisplay(new Date(task.deadline))}</div>
            <div class="task-card-time">${task.estimatedHours} hours</div>
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
            <span class="modal-detail-label">Deadline:</span>
            <span class="modal-detail-value">${formatDateDisplay(new Date(task.deadline))}</span>
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

        deleteTaskBtn.addEventListener('click', deleteSelectedTask);
        editTaskBtn.addEventListener('click', editSelectedTask);
    }
});

function deleteSelectedTask() {
    if (selectedTaskId && confirm('Are you sure you want to delete this task?')) {
        tasks = tasks.filter(t => t.id !== selectedTaskId);
        saveTasksToStorage();
        updateTasksDisplay();
        refreshDashboardIfVisible();
        document.getElementById('taskModal').classList.remove('active');
        alert('Task deleted successfully!');
    }
}

function editSelectedTask() {
    const task = tasks.find(t => t.id === selectedTaskId);
    if (task) {
        // Populate form with task data
        document.getElementById('taskTitle').value = task.title;
        document.getElementById('taskType').value = task.type;
        document.getElementById('deadline').value = task.deadline;
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
    document.getElementById('clearAll').addEventListener('click', clearAllTasks);
}

function updateWeekDisplay() {
    const weekEnd = addDays(currentWeekStart, 6);
    const displayText = `Week of ${formatDateDisplay(currentWeekStart)} - ${formatDateDisplay(weekEnd)}`;
    document.getElementById('weekDisplay').textContent = displayText;
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
                            ${event.startTime ? `<div class="schedule-event-time">${event.startTime} - ${event.endTime}</div>` : ''}
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
    const events = [];

    tasks.forEach(task => {
        // Check if task falls on this day
        if (task.isRecurring) {
            const taskDate = new Date(dateStr);
            const dayName = getDayName(taskDate);
            
            if (task.recurrenceDays.includes(dayName)) {
                const recurrenceEnd = new Date(task.recurrenceEnd);
                if (taskDate <= recurrenceEnd) {
                    events.push({
                        id: task.id,
                        title: task.title,
                        type: task.type,
                        startTime: task.startTime,
                        endTime: task.endTime
                    });
                }
            }
        } else {
            if (task.deadline === dateStr || (task.type === 'class' || task.type === 'shift')) {
                events.push({
                    id: task.id,
                    title: task.title,
                    type: task.type,
                    startTime: task.startTime,
                    endTime: task.endTime
                });
            }
        }
    });

    // Sort by start time
    events.sort((a, b) => {
        if (a.startTime && b.startTime) {
            return a.startTime.localeCompare(b.startTime);
        }
        return 0;
    });

    return events;
}

function generateSchedule() {
    if (tasks.length === 0) {
        alert('Please add at least one task before generating a schedule');
        return;
    }

    // Get non-recurring tasks that need scheduling
    const tasksToSchedule = tasks.filter(t => !t.isRecurring && (t.type === 'assignment' || t.type === 'exam' || t.type === 'personal'));

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
    // Sort tasks by deadline
    const sortedTasks = tasksToSchedule.sort((a, b) => {
        return new Date(a.deadline) - new Date(b.deadline);
    });

    const scheduledItems = [];

    sortedTasks.forEach(task => {
        const deadline = new Date(task.deadline);
        const hoursNeeded = task.estimatedHours;
        const daysAvailable = Math.ceil((deadline - new Date()) / (1000 * 60 * 60 * 24));

        if (daysAvailable > 0) {
            const hoursPerDay = Math.min(3, hoursNeeded / daysAvailable);
            
            let currentDate = new Date();
            let remainingHours = hoursNeeded;

            while (remainingHours > 0 && currentDate < deadline) {
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
                            Due: ${formatDateDisplay(new Date(item.task.deadline))}
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
        if (tasks.length > 0) {
            document.getElementById('generateSchedule').disabled = false;
        }
        // refresh dashboard after loading
        refreshDashboardIfVisible();
    }
}

function clearAllTasks() {
    if (confirm('Are you sure you want to clear all tasks? This cannot be undone.')) {
        tasks = [];
        saveTasksToStorage();
        updateTasksDisplay();
        document.getElementById('timeline').innerHTML = '<p class="empty-state">Tasks will appear here once you add them and generate the schedule.</p>';
        renderScheduleGrid();
        document.getElementById('generateSchedule').disabled = true;
        refreshDashboardIfVisible();
        alert('All tasks cleared!');
    }
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
