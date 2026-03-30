// ============================
// SCHEDULE LYNX - MAIN SCRIPT
// ============================

let tasks = [];
let scheduleEntries = [];
let currentWeekStart = getMonday(new Date());
let currentMonthDate = new Date();
let currentView = "week";
let selectedTaskId = null;
let currentUser = null;

// ============================
// API HELPERS
// ============================

async function apiFetch(url, options = {}) {
  const response = await fetch(url, {
    credentials: "same-origin",
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });

  if (response.status === 204) {
    return null;
  }

  let data = null;
  try {
    data = await response.json();
  } catch (e) {
    data = null;
  }

  if (!response.ok) {
    const message = (data && (data.message || data.error)) || "Request failed";
    throw new Error(message);
  }

  return data;
}

// ============================
// AUTH FUNCTIONS
// ============================

function isLoggedIn() {
  return localStorage.getItem("schedulelynxUser") !== null;
}

function getCurrentUser() {
  const storedUser = localStorage.getItem("schedulelynxUser");
  return storedUser ? JSON.parse(storedUser) : null;
}

function handleLogin(event) {
  if (event) event.preventDefault();

  const username = document.getElementById("username")?.value.trim();
  const password = document.getElementById("password")?.value;
  const rememberMe = document.getElementById("rememberMe")?.checked || false;

  if (!username || !password) {
    alert("Please enter username and password");
    return;
  }

  const user = {
    username,
    email: username.includes("@") ? username : `${username}@schedulelynx.app`,
    loginTime: new Date().toISOString(),
    rememberMe,
  };

  localStorage.setItem("schedulelynxUser", JSON.stringify(user));
  window.location.href = "index.html";
}

function loginDemo() {
  const usernameField = document.getElementById("username");
  const passwordField = document.getElementById("password");

  if (usernameField) usernameField.value = "demo";
  if (passwordField) passwordField.value = "demo123";
  handleLogin();
}

function handleSignup(event) {
  if (event) event.preventDefault();

  const name = document.getElementById("signupName")?.value.trim();
  const email = document.getElementById("signupEmail")?.value.trim();
  const username = document.getElementById("signupUsername")?.value.trim();
  const password = document.getElementById("signupPassword")?.value;
  const confirmPassword = document.getElementById(
    "signupConfirmPassword",
  )?.value;

  if (!name || !email || !username || !password || !confirmPassword) {
    alert("Please fill in all fields");
    return;
  }

  if (password !== confirmPassword) {
    alert("Passwords do not match");
    return;
  }

  if (password.length < 6) {
    alert("Password must be at least 6 characters");
    return;
  }

  const user = {
    username,
    email,
    name,
    loginTime: new Date().toISOString(),
    rememberMe: true,
  };

  localStorage.setItem("schedulelynxUser", JSON.stringify(user));
  alert("Account created successfully! Logging in...");
  window.location.href = "index.html";
}

async function logout() {
  if (!confirm("Are you sure you want to logout?")) return;

  try {
    await apiFetch("/api/auth/logout", {
      method: "POST",
    });
  } catch (err) {
    // ignore
  }

  localStorage.removeItem("schedulelynxUser");
  window.location.href = "login.html";
}

function toggleSignup(event) {
  event.preventDefault();
  const loginBox = document.querySelector(".login-box");
  const signupBox = document.getElementById("signupForm");

  if (!loginBox || !signupBox) return;

  if (loginBox.style.display === "none") {
    loginBox.style.display = "block";
    signupBox.style.display = "none";
  } else {
    loginBox.style.display = "none";
    signupBox.style.display = "block";
  }
}

// ============================
// PAGE LOAD HANDLERS
// ============================

document.addEventListener("DOMContentLoaded", function () {
  const currentPage = window.location.pathname.split("/").pop() || "index.html";

  if (currentPage === "login.html" && isLoggedIn()) {
    window.location.href = "index.html";
    return;
  }

  const publicPages = ["home.html", "features.html", "login.html"];
  if (!isLoggedIn() && !publicPages.includes(currentPage)) {
    window.location.href = "login.html";
    return;
  }

  if (isLoggedIn()) {
    currentUser = getCurrentUser();
    if (document.getElementById("userName")) {
      document.getElementById("userName").textContent = currentUser.username;
    }
  }

  const authLink = document.getElementById("authLink");

  if (authLink) {
    if (isLoggedIn()) {
      authLink.textContent = "Logout";
      authLink.href = "#";
      authLink.onclick = function (e) {
        e.preventDefault();
        logout();
      };
    } else {
      authLink.textContent = "Login";
      authLink.href = "login.html";
      authLink.onclick = null;
    }
  }

  if (currentPage === "login.html") {
    initializeLoginHandlers();
  } else if (currentPage === "index.html") {
    initializeDashboard();
  } else if (currentPage === "timetable.html") {
    initializeFormHandlers();
    initializeScheduleDisplay();
    initializePreferenceHandlers();
    loadTasksFromStorage();
    loadSchedulePreferences();
  }
});

document.addEventListener("DOMContentLoaded", function () {
  const currentPage = window.location.pathname.split("/").pop() || "index.html";
  const navLinks = document.querySelectorAll(".nav-link");

  navLinks.forEach((link) => {
    const href = link.getAttribute("href");
    if (href === currentPage || (currentPage === "" && href === "index.html")) {
      link.classList.add("active");
    } else {
      link.classList.remove("active");
    }
  });
});

function initializeLoginHandlers() {
  const loginForm = document.getElementById("loginForm");
  const signupForm = document.getElementById("signupFormElement");

  if (loginForm) loginForm.addEventListener("submit", handleLogin);
  if (signupForm) signupForm.addEventListener("submit", handleSignup);
}

// ============================
// PREFERENCES
// ============================

async function loadSchedulePreferences() {
  try {
    const prefs = await apiFetch("/api/preferences/schedule", {
      method: "GET",
    });

    const allowWeekend = document.getElementById("scheduleAllowWeekend");
    const quietStart = document.getElementById("scheduleQuietHoursStart");
    const quietEnd = document.getElementById("scheduleQuietHoursEnd");

    if (allowWeekend)
      allowWeekend.checked = prefs.allowWeekendScheduling ?? true;
    if (quietStart) quietStart.value = prefs.quietHoursStart || "23:00";
    if (quietEnd) quietEnd.value = prefs.quietHoursEnd || "08:00";
  } catch (err) {
    console.error("Could not load schedule preferences:", err.message);
  }
}

function initializePreferenceHandlers() {
  const saveBtn = document.getElementById("savePreferencesBtn");
  if (saveBtn) {
    saveBtn.addEventListener("click", saveSchedulePreferences);
  }

  const preferenceIds = [
    "scheduleAllowWeekend",
    "scheduleQuietHoursStart",
    "scheduleQuietHoursEnd",
  ];

  preferenceIds.forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    const eventType = el.type === "checkbox" ? "change" : "input";
    el.addEventListener(eventType, () => {
      markScheduleAsStale(
        "Preferences changed. Save preferences, then generate again.",
      );
    });
  });
}

async function saveSchedulePreferences() {
  try {
    const allowWeekendScheduling =
      document.getElementById("scheduleAllowWeekend")?.checked ?? true;
    const quietHoursStart =
      document.getElementById("scheduleQuietHoursStart")?.value || null;
    const quietHoursEnd =
      document.getElementById("scheduleQuietHoursEnd")?.value || null;

    await apiFetch("/api/preferences/schedule", {
      method: "PUT",
      body: JSON.stringify({
        allowWeekendScheduling,
        quietHoursStart,
        quietHoursEnd,
      }),
    });

    markScheduleAsStale("Preferences saved. Please generate again.");
    alert("Schedule preferences saved.");
  } catch (err) {
    alert(err.message);
  }
}

// ============================
// TASK / EVENT / SCHEDULE LOADING
// ============================

async function loadUserTasks(username) {
  const backendTasks = await apiFetch("/api/tasks", { method: "GET" });
  const backendEvents = await apiFetch("/api/events", { method: "GET" });

  const normalizedTasks = backendTasks.map((task) => ({
    id: String(task.id),
    title: task.title,
    type: "task",
    dueDate: task.dueDate,
    description: "",
    completed: false,
    createdAt: null,
    estimatedHours: task.estimatedHours,
    startTime: null,
    endTime: null,
    isRecurring: false,
    recurrenceType: null,
    recurrenceEnd: null,
    recurrenceDays: [],
    difficulty: task.difficulty || "MEDIUM",
    preferredStartTime: task.preferredStartTime || "09:00",
    preferredEndTime: task.preferredEndTime || "21:00",
    maxHoursPerDay: task.maxHoursPerDay ?? 3,
    minBlockHours: task.minBlockHours ?? 1,
    maxBlockHours: task.maxBlockHours ?? 3,
  }));

  const normalizedEvents = backendEvents.map((event) => ({
    id: String(event.id),
    title: event.title,
    type: "event",
    date: event.date,
    description: "",
    completed: false,
    createdAt: null,
    estimatedHours: 0,
    startTime: event.startTime,
    endTime: event.endTime,
    isRecurring: !!event.recurring,
    recurrenceType: event.recurrenceType
      ? event.recurrenceType.toLowerCase()
      : null,
    recurrenceEnd: event.recurrenceEnd || null,
    recurrenceDays: (event.recurrenceDays || []).map(dayOfWeekToShort),
    difficulty: null,
  }));

  tasks = [...normalizedTasks, ...normalizedEvents];
}

async function loadScheduleEntries() {
  const entries = await apiFetch("/api/schedule", { method: "GET" });

  scheduleEntries = entries.map((entry) => ({
    id: String(entry.id),
    date: entry.date,
    startTime: entry.startTime,
    endTime: entry.endTime,
    plannedHours: entry.plannedHours,
    taskId: String(entry.taskId),
    taskTitle: entry.taskTitle,
    taskDueDate: entry.taskDueDate,
  }));
}

async function loadTasksFromStorage() {
  if (!currentUser) return;

  await loadUserTasks(currentUser.username);
  await loadScheduleEntries();

  updateTasksDisplay();
  renderCurrentScheduleView();
  renderTimeline(scheduleEntries);

  if (scheduleEntries.length > 0) {
    hideScheduleNotice();
  }

  const generateBtn = document.getElementById("generateSchedule");
  if (generateBtn && tasks.length > 0) {
    generateBtn.disabled = false;
  }

  refreshDashboardIfVisible();
}

// ============================
// UTILITY FUNCTIONS
// ============================

function getMonday(d) {
  d = new Date(d);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  return new Date(d.setDate(diff));
}

function getStartOfMonth(date) {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function getEndOfMonth(date) {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}

function isSameDate(dateA, dateB) {
  return (
    dateA.getFullYear() === dateB.getFullYear() &&
    dateA.getMonth() === dateB.getMonth() &&
    dateA.getDate() === dateB.getDate()
  );
}

function getMonthGridStart(date) {
  const firstDay = getStartOfMonth(date);
  return getMonday(firstDay);
}

function updateViewToggleButtons() {
  const weeklyBtn = document.getElementById("weeklyViewBtn");
  const monthlyBtn = document.getElementById("monthlyViewBtn");

  if (weeklyBtn) {
    weeklyBtn.classList.toggle("active-pill", currentView === "week");
  }

  if (monthlyBtn) {
    monthlyBtn.classList.toggle("active-pill", currentView === "month");
  }
}

function updateScheduleSectionTitle() {
  const title = document.getElementById("scheduleSectionTitle");
  if (!title) return;

  title.textContent = currentView === "week" ? "This Week" : "This Month";
}

function updatePeriodDisplay() {
  const periodDisplay = document.getElementById("periodDisplay");
  if (!periodDisplay) return;

  if (currentView === "week") {
    const weekEnd = addDays(currentWeekStart, 6);
    periodDisplay.textContent = `${formatDateDisplay(currentWeekStart)} - ${formatDateDisplay(weekEnd)}`;
  } else {
    periodDisplay.textContent = currentMonthDate.toLocaleDateString("en-US", {
      month: "long",
      year: "numeric",
    });
  }
}

function renderCurrentScheduleView() {
  if (currentView === "week") {
    renderScheduleGrid();
  } else {
    renderMonthlyGrid();
  }
}


function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatDateDisplay(date) {
  const options = { month: "short", day: "numeric", year: "numeric" };
  return date.toLocaleDateString("en-US", options);
}

function getDayName(date) {
  const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  return days[date.getDay()];
}

function addDays(date, days) {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

function shortDayToBackend(value) {
  const map = {
    Sun: "SUNDAY",
    Mon: "MONDAY",
    Tue: "TUESDAY",
    Wed: "WEDNESDAY",
    Thu: "THURSDAY",
    Fri: "FRIDAY",
    Sat: "SATURDAY",
  };
  return map[value];
}

function dayOfWeekToShort(value) {
  const map = {
    SUNDAY: "Sun",
    MONDAY: "Mon",
    TUESDAY: "Tue",
    WEDNESDAY: "Wed",
    THURSDAY: "Thu",
    FRIDAY: "Fri",
    SATURDAY: "Sat",
  };
  return map[value] || value;
}

function showScheduleNotice(message) {
  const notice = document.getElementById("scheduleNotice");
  const text = document.getElementById("scheduleNoticeText");
  if (text) text.textContent = message;
  if (notice) notice.style.display = "block";
}

function hideScheduleNotice() {
  const notice = document.getElementById("scheduleNotice");
  if (notice) notice.style.display = "none";
}

function markScheduleAsStale(
  message = "Schedule changed. Please generate again.",
) {
  if (scheduleEntries.length === 0) {
    showScheduleNotice(message);
    return;
  }

  scheduleEntries = [];
  renderTimeline(scheduleEntries);
  showScheduleNotice(message);
}

function shouldShowRecurringEventOnDate(eventItem, dateStr) {
  const current = new Date(`${dateStr}T12:00:00`);
  const start = new Date(`${eventItem.date}T12:00:00`);

  if (current < start) return false;

  if (eventItem.recurrenceEnd) {
    const recurrenceEnd = new Date(`${eventItem.recurrenceEnd}T12:00:00`);
    if (current > recurrenceEnd) return false;
  }

  if (!eventItem.isRecurring || !eventItem.recurrenceType) {
    return eventItem.date === dateStr;
  }

  if (eventItem.recurrenceType === "daily") {
    return true;
  }

  const currentShortDay = getDayName(current);

  if (eventItem.recurrenceType === "weekly") {
    if (!eventItem.recurrenceDays || eventItem.recurrenceDays.length === 0) {
      return getDayName(start) === currentShortDay;
    }
    return eventItem.recurrenceDays.includes(currentShortDay);
  }

  if (eventItem.recurrenceType === "biweekly") {
    const diffMs = current - start;
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    const diffWeeks = Math.floor(diffDays / 7);

    const matchesDay =
      !eventItem.recurrenceDays || eventItem.recurrenceDays.length === 0
        ? getDayName(start) === currentShortDay
        : eventItem.recurrenceDays.includes(currentShortDay);

    return matchesDay && diffWeeks % 2 === 0;
  }

  return eventItem.date === dateStr;
}

function parseTimeToMinutes(timeString) {
  if (!timeString) return null;
  const [hours, minutes] = timeString.split(":").map(Number);
  return hours * 60 + minutes;
}

function timesOverlap(startA, endA, startB, endB) {
  return startA < endB && endA > startB;
}

function findOverlappingEvent(dateStr, startTime, endTime, ignoreTaskId) {
  const startMinutes = parseTimeToMinutes(startTime);
  const endMinutes = parseTimeToMinutes(endTime);
  if (startMinutes === null || endMinutes === null) return null;

  return tasks.find((task) => {
    if (task.type !== "event") return false;
    if (task.id === ignoreTaskId) return false;
    if (!shouldShowRecurringEventOnDate(task, dateStr)) return false;

    const otherStart = parseTimeToMinutes(task.startTime);
    const otherEnd = parseTimeToMinutes(task.endTime);
    if (otherStart === null || otherEnd === null) return false;

    return timesOverlap(startMinutes, endMinutes, otherStart, otherEnd);
  });
}

function showTimeConflictWarning(message) {
  const notice = document.getElementById("scheduleNotice");
  const text = document.getElementById("scheduleNoticeText");
  if (!notice || !text) return;

  notice.classList.add("warning-box");
  text.textContent = message;
  notice.style.display = "block";
}

function hideTimeConflictWarning() {
  const notice = document.getElementById("scheduleNotice");
  const text = document.getElementById("scheduleNoticeText");
  if (!notice || !text) return;

  notice.style.display = "none";
  notice.classList.remove("warning-box");
  text.textContent = "";
}

function exitEditMode() {
  selectedTaskId = null;

  const taskForm = document.getElementById("taskForm");
  if (taskForm) taskForm.reset();

  const formTitle = document.getElementById("taskFormTitle");
  if (formTitle) formTitle.textContent = "Add / Edit Task";

  const recurrenceOptions = document.getElementById("recurrenceOptions");
  const startTimeGroup = document.getElementById("startTimeGroup");
  const endTimeGroup = document.getElementById("endTimeGroup");
  const daysOfWeekGroup = document.getElementById("daysOfWeekGroup");
  const recurringGroup = document.getElementById("recurringGroup");
  const estimatedHoursGroup = document.getElementById("estimatedHoursGroup");
  const difficultyGroup = document.getElementById("difficultyGroup");
  const taskSchedulingFields = document.getElementById("taskSchedulingFields");
  const taskType = document.getElementById("taskType");
  const difficultyField = document.getElementById("difficulty");

  if (difficultyField) difficultyField.value = "MEDIUM";
  if (taskType) taskType.value = "task";
  if (recurrenceOptions) recurrenceOptions.style.display = "none";
  if (startTimeGroup) startTimeGroup.style.display = "none";
  if (endTimeGroup) endTimeGroup.style.display = "none";
  if (daysOfWeekGroup) daysOfWeekGroup.style.display = "none";
  if (recurringGroup) recurringGroup.style.display = "none";
  if (estimatedHoursGroup) estimatedHoursGroup.style.display = "block";
  if (difficultyGroup) difficultyGroup.style.display = "block";
  if (taskSchedulingFields) taskSchedulingFields.style.display = "block";

  const preferredStartTime = document.getElementById("preferredStartTime");
  const preferredEndTime = document.getElementById("preferredEndTime");
  const taskMaxHoursPerDay = document.getElementById("taskMaxHoursPerDay");
  const taskMinBlockHours = document.getElementById("taskMinBlockHours");
  const taskMaxBlockHours = document.getElementById("taskMaxBlockHours");
  const difficulty = document.getElementById("difficulty");

  if (preferredStartTime) preferredStartTime.value = "09:00";
  if (preferredEndTime) preferredEndTime.value = "21:00";
  if (taskMaxHoursPerDay) taskMaxHoursPerDay.value = "3";
  if (taskMinBlockHours) taskMinBlockHours.value = "1";
  if (taskMaxBlockHours) taskMaxBlockHours.value = "3";
  if (difficulty) difficulty.value = "MEDIUM";
  if (difficultyGroup) difficultyGroup.style.display = "block";

  document
    .querySelectorAll('input[name="recurrenceDays"]')
    .forEach((cb) => (cb.checked = false));

  const submitBtn = document.querySelector('#taskForm button[type="submit"]');
  if (submitBtn) submitBtn.textContent = "Save Task";
  // NEW: Reset color picker back to default purple when exiting edit mode
  const taskColorPicker = document.getElementById("taskColor");
  if (taskColorPicker) taskColorPicker.value = "#6366f1";
  hideTimeConflictWarning();
}

// ============================
// DASHBOARD FUNCTIONS
// ============================

async function initializeDashboard() {
  if (!isLoggedIn()) return;

  currentUser = getCurrentUser();
  await loadUserTasks(currentUser.username);
  await loadScheduleEntries();

  updateDashboardStats();
  updateUpcomingTasks();
  updateWeekScheduleMini();
  updateTaskBreakdown();
}

function refreshDashboardIfVisible() {
  if (document.getElementById("totalTasksCount")) {
    updateDashboardStats();
    updateUpcomingTasks();
    updateWeekScheduleMini();
    updateTaskBreakdown();
  }
}

function updateDashboardStats() {
  const today = new Date();
  const weekFromNow = addDays(today, 7);

  const totalTasksCount = document.getElementById("totalTasksCount");
  const upcomingCount = document.getElementById("upcomingCount");
  const overdueCount = document.getElementById("overdueCount");
  const weekHoursCount = document.getElementById("weekHoursCount");

  if (!totalTasksCount || !upcomingCount || !overdueCount || !weekHoursCount)
    return;

  totalTasksCount.textContent = tasks.length;

  const upcoming = tasks.filter((task) => {
    const dueDate = new Date(task.type === "event" ? task.date : task.dueDate);
    return dueDate > today && dueDate <= weekFromNow;
  });
  upcomingCount.textContent = upcoming.length;

  const overdue = tasks.filter((task) => {
    const dueDate = new Date(task.type === "event" ? task.date : task.dueDate);
    return dueDate < today && !task.completed;
  });
  overdueCount.textContent = overdue.length;

  let totalHours = 0;
  tasks.forEach((task) => {
    const dueDate = new Date(task.type === "event" ? task.date : task.dueDate);
    if (
      dueDate >= getMonday(today) &&
      dueDate <= addDays(getMonday(today), 6)
    ) {
      totalHours += task.estimatedHours || 0;
    }
  });
  weekHoursCount.textContent = totalHours + "h";
}

function updateUpcomingTasks() {
  const upcomingTasksList = document.getElementById("upcomingTasksList");
  if (!upcomingTasksList) return;

  const today = new Date();
  const weekFromNow = addDays(today, 7);

  const upcoming = tasks
    .filter((task) => {
      const dueDate = new Date(task.type === "event" ? task.date : task.dueDate);
      return dueDate > today && dueDate <= weekFromNow && !task.completed;
    })
    .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
    .slice(0, 5);

  if (upcoming.length === 0) {
    upcomingTasksList.innerHTML =
      '<p class="empty-state">No upcoming tasks. <a href="timetable.html">Add a task</a>!</p>';
    return;
  }

  upcomingTasksList.innerHTML = upcoming
    .map(
      (task) => `
        <div class="task-item-dashboard ${task.type}" onclick="viewTaskDetails('${task.id}')">
          <div class="task-item-badge ${task.type}">${task.type}</div>
          <div class="task-item-title">${task.title}</div>
          <div class="task-item-dueDate">
            ${task.type === "event" ? "Date" : "Due"}: 
            ${formatDateDisplay(new Date(task.type === "event" ? task.date : task.dueDate))}
          </div>        
        </div>
      `,
    )
    .join("");
}

function updateWeekScheduleMini() {
  const weekScheduleMini = document.getElementById("weekScheduleMini");
  if (!weekScheduleMini) return;

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
        <div class="day-mini-box ${eventCount > 0 ? "has-events" : "empty"}">
          ${eventCount > 0 ? eventCount + " tasks" : "—"}
        </div>
      </div>
    `);
  }

  if (!hasEvents) {
    weekScheduleMini.innerHTML =
      '<p class="empty-state">No events scheduled. <a href="timetable.html">Create your schedule</a>!</p>';
  } else {
    weekScheduleMini.innerHTML =
      '<div style="display: grid; grid-template-columns: repeat(7, 1fr); gap: 0.75rem;">' +
      weekDays.join("") +
      "</div>";
  }
}

function updateTaskBreakdown() {
  const taskBreakdown = document.getElementById("taskBreakdown");
  if (!taskBreakdown) return;

  const breakdown = {
    task: tasks.filter((t) => t.type === "task").length,
    event: tasks.filter((t) => t.type === "event").length,
  };

  const hasAnyTasks = Object.values(breakdown).some((count) => count > 0);

  if (!hasAnyTasks) {
    taskBreakdown.innerHTML =
      '<p class="empty-state">Add tasks to see breakdown</p>';
    return;
  }

  const typeLabels = {
    task: "Tasks",
    event: "Events",
  };

  taskBreakdown.innerHTML = Object.keys(breakdown)
    .filter((type) => breakdown[type] > 0)
    .map(
      (type) => `
        <div class="breakdown-item ${type}">
          <div class="breakdown-label">${typeLabels[type]}</div>
          <div class="breakdown-count">${breakdown[type]}</div>
        </div>
      `,
    )
    .join("");
}

// ============================
// FORM HANDLING
// ============================

function initializeFormHandlers() {
  const taskForm = document.getElementById("taskForm");
  const taskTypeSelect = document.getElementById("taskType");
  const isRecurringCheckbox = document.getElementById("isRecurring");
  const recurrenceTypeSelect = document.getElementById("recurrenceType");

  if (
    !taskForm ||
    !taskTypeSelect ||
    !isRecurringCheckbox ||
    !recurrenceTypeSelect
  )
    return;

  taskTypeSelect.addEventListener("change", function () {
    const startTimeGroup = document.getElementById("startTimeGroup");
    const endTimeGroup = document.getElementById("endTimeGroup");
    const estimatedHoursGroup = document.getElementById("estimatedHoursGroup");
    const recurringGroup = document.getElementById("recurringGroup");
    const recurrenceOptions = document.getElementById("recurrenceOptions");
    const daysOfWeekGroup = document.getElementById("daysOfWeekGroup");
    const difficultyGroup = document.getElementById("difficultyGroup");
    const taskSchedulingFields = document.getElementById(
      "taskSchedulingFields",
    );
    const recurringCheckbox = document.getElementById("isRecurring");

    if (
      !startTimeGroup ||
      !endTimeGroup ||
      !estimatedHoursGroup ||
      !recurringGroup ||
      !recurrenceOptions ||
      !daysOfWeekGroup ||
      !difficultyGroup ||
      !taskSchedulingFields ||
      !recurringCheckbox
    ) {
      return;
    }

    if (this.value === "task") {
      startTimeGroup.style.display = "none";
      endTimeGroup.style.display = "none";
      estimatedHoursGroup.style.display = "block";
      difficultyGroup.style.display = "block";
      taskSchedulingFields.style.display = "block";
      recurringGroup.style.display = "none";
      recurringCheckbox.checked = false;
      recurrenceOptions.style.display = "none";
      daysOfWeekGroup.style.display = "none";
    } else {
      startTimeGroup.style.display = "flex";
      endTimeGroup.style.display = "flex";
      estimatedHoursGroup.style.display = "none";
      difficultyGroup.style.display = "none";
      taskSchedulingFields.style.display = "none";
      recurringGroup.style.display = "block";
      recurrenceOptions.style.display = recurringCheckbox.checked
        ? "block"
        : "none";
    }
  });

  isRecurringCheckbox.addEventListener("change", function () {
    if (taskTypeSelect.value !== "event") return;

    const recurrenceOptions = document.getElementById("recurrenceOptions");
    const daysOfWeekGroup = document.getElementById("daysOfWeekGroup");

    if (!recurrenceOptions || !daysOfWeekGroup) return;

    recurrenceOptions.style.display = this.checked ? "block" : "none";

    if (!this.checked) {
      daysOfWeekGroup.style.display = "none";
    }
  });

  recurrenceTypeSelect.addEventListener("change", function () {
    const daysOfWeekGroup = document.getElementById("daysOfWeekGroup");
    if (!daysOfWeekGroup) return;

    daysOfWeekGroup.style.display =
      this.value === "weekly" || this.value === "biweekly" ? "flex" : "none";
  });

  taskForm.addEventListener("submit", function (e) {
    e.preventDefault();
    addTask();
  });

  const startTimeInput = document.getElementById("startTime");
  const endTimeInput = document.getElementById("endTime");
  const dueDateInput = document.getElementById("dueDate");

  [startTimeInput, endTimeInput, dueDateInput, taskTypeSelect].forEach((el) => {
    if (el) {
      el.addEventListener("input", hideTimeConflictWarning);
      el.addEventListener("change", hideTimeConflictWarning);
    }
  });

  taskTypeSelect.dispatchEvent(new Event("change"));
}

async function addTask() {
  if (!currentUser) { alert("You are not logged in."); return; }
  const v = getFormValues();
  hideTimeConflictWarning();

  if (!v.title || !v.type || !v.dueDate) {
    alert("Please fill in Title, Type, and Date.");
    return;
  }

  if (v.type === "task" && !validateTaskInput(v)) return;
  if (v.type === "event" && !validateEventInput(v)) return;

  const originalItem = selectedTaskId ? tasks.find((t) => t.id === selectedTaskId) : null;

  try {
    if (v.type === "task") {
      const payload = {
        title: v.title, dueDate: v.dueDate, estimatedHours: v.estimatedHours,
        difficulty: v.difficulty, preferredStartTime: v.preferredStartTime,
        preferredEndTime: v.preferredEndTime, maxHoursPerDay: v.taskMaxHoursPerDay,
        minBlockHours: v.taskMinBlockHours, maxBlockHours: v.taskMaxBlockHours,
      };
      localStorage.setItem(`taskColor_${v.title}_${v.dueDate}`, v.taskColor);
      if (originalItem?.type === "task") {
        await apiFetch(`/api/tasks/${selectedTaskId}`, { method: "PUT", body: JSON.stringify(payload) });
      } else {
        if (originalItem?.type === "event") await apiFetch(`/api/events/${selectedTaskId}`, { method: "DELETE" });
        await apiFetch("/api/tasks", { method: "POST", body: JSON.stringify(payload) });
      }
    } else {
      const payload = {
        title: v.title, date: v.dueDate, startTime: v.startTime, endTime: v.endTime,
        recurring: v.isRecurring,
        recurrenceType: v.isRecurring ? v.recurrenceType.toUpperCase() : null,
        recurrenceEnd: v.isRecurring && v.recurrenceEnd ? v.recurrenceEnd : null,
        recurrenceDays: v.isRecurring ? v.selectedRecurrenceDays.map(shortDayToBackend) : [],
      };
      localStorage.setItem(`taskColor_${v.title}_${v.dueDate}`, v.taskColor);
      if (originalItem?.type === "event") {
        await apiFetch(`/api/events/${selectedTaskId}`, { method: "PUT", body: JSON.stringify(payload) });
      } else {
        if (originalItem?.type === "task") await apiFetch(`/api/tasks/${selectedTaskId}`, { method: "DELETE" });
        await apiFetch("/api/events", { method: "POST", body: JSON.stringify(payload) });
      }
    }
    await reloadAndRefreshAll();
    markScheduleAsStale("Tasks or events changed. Please generate again.");
    exitEditMode();
  } catch (err) {
    alert(err.message);
  }
}

// ============================
// DISPLAY / MODAL
// ============================

function updateTasksDisplay() {
  const tasksList = document.getElementById("tasksList");
  if (!tasksList) return;

  if (tasks.length === 0) {
    tasksList.innerHTML =
      '<p class="empty-state">No tasks added yet. Add a task to get started!</p>';
    return;
  }

  tasksList.innerHTML = tasks
    .map((task) => {
      const dateKey = task.type === "event" ? task.date : task.dueDate;

      const savedColor =
        localStorage.getItem(`taskColor_${task.title}_${dateKey}`) ||
        (task.type === "event" ? "#10b981" : "#6366f1");

      const difficultyColor = {
        LOW: "#10b981",
        MEDIUM: "#f59e0b",
        HIGH: "#ef4444",
      }[task.difficulty] || "#64748b";

      return `
    <div class="task-card ${task.type}" onclick="viewTaskDetails('${task.id}')"
      style="border-left-color: ${savedColor};">

      <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:0.35rem;">
        <div class="task-card-type" style="background-color: ${savedColor};">
          ${task.type.charAt(0).toUpperCase() + task.type.slice(1)}
        </div>

        ${task.type === "task" ? `
          <span style="
            font-size: 0.7rem; font-weight: 700; padding: 0.2rem 0.5rem;
            border-radius: 20px; color: white; background-color: ${difficultyColor};
            white-space: nowrap;
          ">${task.difficulty || "MEDIUM"}</span>
        ` : ""}
      </div>

      <div class="task-card-title">${task.title}</div>

      <div class="task-card-dueDate">
        ${task.type === "event" ? "Date" : "Due"}: 
        ${formatDateDisplay(new Date(task.type === "event" ? task.date : task.dueDate))}
      </div>

      ${task.type === "task"
          ? `<div class="task-card-time">${task.estimatedHours}h</div>`
          : task.startTime
            ? `<div class="task-card-time">${task.startTime} - ${task.endTime}</div>`
            : ""
        }

      ${task.type === "event"
          ? `<div class="task-card-time">Recurring: ${task.isRecurring ? task.recurrenceType : "No"}</div>`
          : ""
        }

      ${task.description ? `
        <div style="
          font-size: 0.8rem; color: var(--text-light); margin-top: 0.4rem;
          overflow: hidden; display: -webkit-box;
          -webkit-line-clamp: 2; -webkit-box-orient: vertical;
        ">${task.description}</div>
      ` : ""}
    </div>
  `;
    })
    .join("");
}

function viewTaskDetails(taskId) {
  const task = tasks.find((t) => t.id === taskId);
  if (!task) return;

  selectedTaskId = taskId;
  const modal = document.getElementById("taskModal");
  const modalBody = document.getElementById("modalBody");
  if (!modal || !modalBody) return;

  const recurrenceText = task.isRecurring
    ? `${task.recurrenceType.charAt(0).toUpperCase() + task.recurrenceType.slice(1)}${task.recurrenceDays && task.recurrenceDays.length > 0
      ? ` (${task.recurrenceDays.join(", ")})`
      : ""
    }`
    : "No";

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
  <span class="modal-detail-label">
    ${task.type === "event" ? "Date" : "Due Date"}:
  </span>
  <span class="modal-detail-value">
    ${formatDateDisplay(new Date(task.type === "event" ? task.date : task.dueDate))}
  </span>
</div>
    ${task.type === "task"
      ? `
    <div class="modal-detail">
      <span class="modal-detail-label">Estimated Time:</span>
      <span class="modal-detail-value">${task.estimatedHours} hours</span>
    </div>
    <div class="modal-detail">
      <span class="modal-detail-label">Difficulty:</span>
      <span class="modal-detail-value">${task.difficulty || "MEDIUM"}</span>
    </div>
    <div class="modal-detail">
      <span class="modal-detail-label">Preferred Hours:</span>
      <span class="modal-detail-value">${task.preferredStartTime || "09:00"} - ${task.preferredEndTime || "21:00"}</span>
    </div>
    <div class="modal-detail">
      <span class="modal-detail-label">Max Hours/Day:</span>
      <span class="modal-detail-value">${task.maxHoursPerDay ?? 3}</span>
    </div>
    <div class="modal-detail">
      <span class="modal-detail-label">Block Range:</span>
      <span class="modal-detail-value">${task.minBlockHours ?? 1} - ${task.maxBlockHours ?? 3} hours</span>
    </div>
    `
      : ""
    }
    ${task.startTime
      ? `
    <div class="modal-detail">
      <span class="modal-detail-label">Start Time:</span>
      <span class="modal-detail-value">${task.startTime}</span>
    </div>`
      : ""
    }
    ${task.endTime
      ? `
    <div class="modal-detail">
      <span class="modal-detail-label">End Time:</span>
      <span class="modal-detail-value">${task.endTime}</span>
    </div>`
      : ""
    }
 ${task.type === "event" ? `
  <div class="modal-detail">
    <span class="modal-detail-label">Recurring:</span>
    <span class="modal-detail-value">
      ${task.isRecurring ? recurrenceText : "No"}
    </span>
  </div>
` : ""}
  `;

  modal.classList.add("active");
}

document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("taskModal");
  if (!modal) return;

  const closeBtn = document.querySelector(".close-modal");
  const closeModalBtn = document.getElementById("closeModalBtn");
  const deleteTaskBtn = document.getElementById("deleteTaskBtn");
  const editTaskBtn = document.getElementById("editTaskBtn");

  if (closeBtn)
    closeBtn.addEventListener("click", () => modal.classList.remove("active"));
  if (closeModalBtn)
    closeModalBtn.addEventListener("click", () =>
      modal.classList.remove("active"),
    );

  window.addEventListener("click", (e) => {
    if (e.target === modal) modal.classList.remove("active");
  });

  if (deleteTaskBtn)
    deleteTaskBtn.addEventListener("click", deleteTaskListener);
  if (editTaskBtn) editTaskBtn.addEventListener("click", editSelectedTask);
});

async function deleteTaskListener() {
  if (!selectedTaskId) return;

  const item = tasks.find((t) => t.id === selectedTaskId);
  if (!item) return;

  if (!confirm("Are you sure you want to delete this task?")) return;

  try {
    if (item.type === "event") {
      await apiFetch(`/api/events/${selectedTaskId}`, { method: "DELETE" });
    } else {
      await apiFetch(`/api/tasks/${selectedTaskId}`, { method: "DELETE" });
    }

    await loadUserTasks(currentUser.username);
    await loadScheduleEntries();

    updateTasksDisplay();
    renderCurrentScheduleView();
    renderTimeline(scheduleEntries);
    renderTimeline(scheduleEntries);
    refreshDashboardIfVisible();

    const modal = document.getElementById("taskModal");
    if (modal) modal.classList.remove("active");

    markScheduleAsStale("Tasks or events changed. Please generate again.");
    exitEditMode();
  } catch (err) {
    alert(err.message);
  }
}

function editSelectedTask() {
  const task = tasks.find((t) => t.id === selectedTaskId);
  if (!task) {
    alert("Could not find the selected item to edit.");
    return;
  }

  const formTitle = document.getElementById("taskFormTitle");
  const taskTitle = document.getElementById("taskTitle");
  const taskType = document.getElementById("taskType");
  const dueDate = document.getElementById("dueDate");
  const estimatedHours = document.getElementById("estimatedHours");
  const difficulty = document.getElementById("difficulty");
  const preferredStartTime = document.getElementById("preferredStartTime");
  const preferredEndTime = document.getElementById("preferredEndTime");
  const taskMaxHoursPerDay = document.getElementById("taskMaxHoursPerDay");
  const taskMinBlockHours = document.getElementById("taskMinBlockHours");
  const taskMaxBlockHours = document.getElementById("taskMaxBlockHours");
  const startTime = document.getElementById("startTime");
  const endTime = document.getElementById("endTime");
  const description = document.getElementById("description");
  const isRecurring = document.getElementById("isRecurring");
  const recurrenceType = document.getElementById("recurrenceType");
  const recurrenceEnd = document.getElementById("recurrenceEnd");
  const difficultyField = document.getElementById("difficulty");

  const startTimeGroup = document.getElementById("startTimeGroup");
  const endTimeGroup = document.getElementById("endTimeGroup");
  const estimatedHoursGroup = document.getElementById("estimatedHoursGroup");
  const difficultyGroup = document.getElementById("difficultyGroup");
  const taskSchedulingFields = document.getElementById("taskSchedulingFields");
  const recurringGroup = document.getElementById("recurringGroup");
  const recurrenceOptions = document.getElementById("recurrenceOptions");
  const daysOfWeekGroup = document.getElementById("daysOfWeekGroup");

  if (formTitle) formTitle.textContent = `Editing: ${task.title}`;
  if (taskTitle) taskTitle.value = task.title;
  if (taskType) taskType.value = task.type;
  if (dueDate) dueDate.value = task.type === "event" ? task.date : task.dueDate; if (estimatedHours) estimatedHours.value = task.estimatedHours || "";
  if (difficulty) difficulty.value = task.difficulty || "MEDIUM";
  if (preferredStartTime)
    preferredStartTime.value = task.preferredStartTime || "09:00";
  if (preferredEndTime)
    preferredEndTime.value = task.preferredEndTime || "21:00";
  if (taskMaxHoursPerDay) taskMaxHoursPerDay.value = task.maxHoursPerDay ?? 3;
  if (taskMinBlockHours) taskMinBlockHours.value = task.minBlockHours ?? 1;
  if (taskMaxBlockHours) taskMaxBlockHours.value = task.maxBlockHours ?? 3;
  if (startTime) startTime.value = task.startTime || "";
  if (endTime) endTime.value = task.endTime || "";
  if (description) description.value = task.description || "";
  if (isRecurring) isRecurring.checked = !!task.isRecurring;
  if (recurrenceType) recurrenceType.value = task.recurrenceType || "";
  if (recurrenceEnd) recurrenceEnd.value = task.recurrenceEnd || "";
  if (difficultyField) difficultyField.value = task.difficulty || "MEDIUM";

  document.querySelectorAll('input[name="recurrenceDays"]').forEach((cb) => {
    cb.checked = task.recurrenceDays?.includes(cb.value) || false;
  });

  if (task.type === "event") {
    if (difficultyGroup) difficultyGroup.style.display = "none";
    if (startTimeGroup) startTimeGroup.style.display = "flex";
    if (endTimeGroup) endTimeGroup.style.display = "flex";
    if (estimatedHoursGroup) estimatedHoursGroup.style.display = "none";
    if (difficultyGroup) difficultyGroup.style.display = "none";
    if (taskSchedulingFields) taskSchedulingFields.style.display = "none";
    if (recurringGroup) recurringGroup.style.display = "block";

    if (task.isRecurring) {
      if (recurrenceOptions) recurrenceOptions.style.display = "block";
      const showDays =
        task.recurrenceType === "weekly" || task.recurrenceType === "biweekly";
      if (daysOfWeekGroup)
        daysOfWeekGroup.style.display = showDays ? "flex" : "none";
    } else {
      if (recurrenceOptions) recurrenceOptions.style.display = "none";
      if (daysOfWeekGroup) daysOfWeekGroup.style.display = "none";
    }
  } else {
    if (difficultyGroup) difficultyGroup.style.display = "block";
    if (startTimeGroup) startTimeGroup.style.display = "none";
    if (endTimeGroup) endTimeGroup.style.display = "none";
    if (estimatedHoursGroup) estimatedHoursGroup.style.display = "block";
    if (difficultyGroup) difficultyGroup.style.display = "block";
    if (taskSchedulingFields) taskSchedulingFields.style.display = "block";
    if (recurringGroup) recurringGroup.style.display = "none";
    if (recurrenceOptions) recurrenceOptions.style.display = "none";
    if (daysOfWeekGroup) daysOfWeekGroup.style.display = "none";
  }

  const submitBtn = document.querySelector('#taskForm button[type="submit"]');
  if (submitBtn) submitBtn.textContent = "Update Item";

  const modal = document.getElementById("taskModal");
  if (modal) modal.classList.remove("active");

  window.scrollTo({ top: 0, behavior: "smooth" });
  if (taskTitle) taskTitle.focus();
}

// ============================
// SCHEDULE GENERATION
// ============================

function initializeScheduleDisplay() {
  updatePeriodDisplay();
  renderCurrentScheduleView();
  updateViewToggleButtons();
  updateScheduleSectionTitle();

  const prevPeriod = document.getElementById("prevPeriod");
  const nextPeriod = document.getElementById("nextPeriod");
  const weeklyViewBtn = document.getElementById("weeklyViewBtn");
  const monthlyViewBtn = document.getElementById("monthlyViewBtn");
  const generateScheduleBtn = document.getElementById("generateSchedule");
  const clearAllBtn = document.getElementById("clearAll");

  if (prevPeriod) {
    prevPeriod.addEventListener("click", () => {
      if (currentView === "week") {
        currentWeekStart = addDays(currentWeekStart, -7);
      } else {
        currentMonthDate = new Date(
          currentMonthDate.getFullYear(),
          currentMonthDate.getMonth() - 1,
          1,
        );
      }

      updatePeriodDisplay();
      renderCurrentScheduleView();
    });
  }

  if (nextPeriod) {
    nextPeriod.addEventListener("click", () => {
      if (currentView === "week") {
        currentWeekStart = addDays(currentWeekStart, 7);
      } else {
        currentMonthDate = new Date(
          currentMonthDate.getFullYear(),
          currentMonthDate.getMonth() + 1,
          1,
        );
      }

      updatePeriodDisplay();
      renderCurrentScheduleView();
    });
  }

  if (weeklyViewBtn) {
    weeklyViewBtn.addEventListener("click", () => {
      currentView = "week";
      currentWeekStart = getMonday(new Date(currentMonthDate));
      updatePeriodDisplay();
      renderCurrentScheduleView();
      updateViewToggleButtons();
      updateScheduleSectionTitle();
    });
  }

  if (monthlyViewBtn) {
    monthlyViewBtn.addEventListener("click", () => {
      currentView = "month";
      currentMonthDate = new Date(currentWeekStart);
      updatePeriodDisplay();
      renderCurrentScheduleView();
      updateViewToggleButtons();
      updateScheduleSectionTitle();
    });
  }

  if (generateScheduleBtn) {
    generateScheduleBtn.addEventListener("click", generateSchedule);
  }

  if (clearAllBtn) {
    clearAllBtn.addEventListener("click", clearAllItems);
  }
}

function renderScheduleGrid() {
  const scheduleGrid = document.getElementById("scheduleGrid");
  if (!scheduleGrid) return;

  scheduleGrid.className = "schedule-grid";
  scheduleGrid.innerHTML = "";

  for (let i = 0; i < 7; i++) {
    const dayDate = addDays(currentWeekStart, i);
    const dayName = getDayName(dayDate);
    const formattedDate = formatDate(dayDate);
    const dayEvents = getEventsForDay(formattedDate);

    const dayColumn = document.createElement("div");
    dayColumn.className = "day-column";
    dayColumn.innerHTML = `
      <div class="day-header">${dayName}<br>${dayDate.getDate()}</div>
      <div class="day-content">
        ${dayEvents.length > 0
        ? dayEvents
          .map(
            (event) => `
                    <div class="schedule-event ${event.type}" onclick="viewTaskDetails('${event.id}')" style="border-left: 3px solid ${event.color};">
                      <div class="schedule-event-title">${event.title}</div>
                      ${event.startTime
                ? `<div class="schedule-event-time">${event.startTime} - ${event.endTime}</div>`
                : `<div class="schedule-event-time">${event.label || "Due"}</div>`
              }
                      ${event.description ? `
                        <!-- NEW: Description on weekly calendar cards -->
                        <div style="font-size:0.72rem; color:var(--text-light); margin-top:0.2rem;
                          overflow:hidden; display:-webkit-box;
                          -webkit-line-clamp:2; -webkit-box-orient:vertical;">
                          ${event.description}
                        </div>` : ""}
                      ${event.difficulty ? `
                        <!-- NEW: Difficulty badge on weekly calendar cards -->
                        <span style="font-size:0.65rem; font-weight:700; padding:0.15rem 0.4rem;
                          border-radius:10px; color:white; margin-top:0.2rem; display:inline-block;
                          background-color:${{ LOW: "#10b981", MEDIUM: "#f59e0b", HIGH: "#ef4444" }[event.difficulty] || "#64748b"};">
                          ${event.difficulty}
                        </span>` : ""}
                    </div>
                  `,
          )
          .join("")
        : '<p class="empty-state">No events</p>'
      }
      </div>
    `;

    scheduleGrid.appendChild(dayColumn);
  }
}

function renderMonthlyGrid() {
  const scheduleGrid = document.getElementById("scheduleGrid");
  if (!scheduleGrid) return;

  scheduleGrid.className = "monthly-grid";
  scheduleGrid.innerHTML = "";

  const gridStart = getMonthGridStart(currentMonthDate);
  const today = new Date();

  for (let i = 0; i < 42; i++) {
    const dayDate = addDays(gridStart, i);
    const formattedDate = formatDate(dayDate);
    const dayEvents = getEventsForDay(formattedDate);
    const isCurrentMonth = dayDate.getMonth() === currentMonthDate.getMonth();

    const dayColumn = document.createElement("div");
    dayColumn.className = "month-day-column";

    if (!isCurrentMonth) {
      dayColumn.classList.add("other-month");
    }

    if (isSameDate(dayDate, today)) {
      dayColumn.classList.add("today");
    }

    dayColumn.innerHTML = `
      <div class="month-day-header">
        <div class="month-day-name">${getDayName(dayDate)}</div>
        <div class="month-day-number">${dayDate.getDate()}</div>
      </div>
      <div class="month-day-content">
        ${dayEvents.length > 0
        ? dayEvents
          .slice(0, 3)
          .map(
            (event) => `
                    <div class="schedule-event ${event.type}" onclick="viewTaskDetails('${event.id}')" style="border-left: 3px solid ${event.color};">
                      <div class="schedule-event-title">${event.title}</div>
                      ${event.startTime
                ? `<div class="schedule-event-time">${event.startTime}${event.endTime ? ` - ${event.endTime}` : ""}</div>`
                : `<div class="schedule-event-time">${event.label || "Due"}</div>`
              }
                      ${event.description ? `
                        <!-- NEW: Description on monthly calendar cards -->
                        <div style="font-size:0.68rem; color:var(--text-light); margin-top:0.15rem;
                          overflow:hidden; display:-webkit-box;
                          -webkit-line-clamp:1; -webkit-box-orient:vertical;">
                          ${event.description}
                        </div>` : ""}
                    </div>
                  `,
          )
          .join("") +
        (dayEvents.length > 3
          ? `<div class="schedule-event-time">+${dayEvents.length - 3} more</div>`
          : "")
        : '<p class="empty-state">No events</p>'
      }
      </div>
    `;

    scheduleGrid.appendChild(dayColumn);
  }
}

function getEventsForDay(dateStr) {
  const items = [];

  tasks.forEach((item) => {
    if (item.type === "event") {
      if (shouldShowRecurringEventOnDate(item, dateStr)) {
        items.push({
          id: item.id,
          title: item.title,
          type: "event",
          startTime: item.startTime,
          endTime: item.endTime,
          label: null,
          description: item.description || "",
          difficulty: item.difficulty || null,
          color: localStorage.getItem(`taskColor_${item.title}_${item.dueDate}`) || "#10b981",
        });
      }
    }

    if (item.type === "task" && item.dueDate === dateStr) {
      items.push({
        id: item.id,
        title: item.title,
        type: "task",
        startTime: null,
        endTime: null,
        label: "Due",
        description: item.description || "",  // NEW: Pass description to calendar cards
        difficulty: item.difficulty || null,  // NEW: Pass difficulty to calendar cards
        color: localStorage.getItem(`taskColor_${item.title}_${item.dueDate}`) || "#10b981",
      });
    }
  });

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

async function generateSchedule() {
  if (tasks.length === 0) {
    alert("Please add at least one task before generating a schedule");
    return;
  }

  const tasksToSchedule = tasks.filter((t) => t.type === "task");
  if (tasksToSchedule.length === 0) {
    alert("Add tasks to generate a schedule");
    return;
  }

  try {
    const result = await apiFetch("/api/schedule/generate", {
      method: "POST",
      body: JSON.stringify({
        startDate: formatDate(new Date()),
      }),
    });

    scheduleEntries = (result.entries || []).map((entry) => ({
      id: String(entry.id),
      date: entry.date,
      startTime: entry.startTime,
      endTime: entry.endTime,
      plannedHours: entry.plannedHours,
      taskId: String(entry.taskId),
      taskTitle: entry.taskTitle,
      taskDueDate: entry.taskDueDate,
    }));

    renderTimeline(scheduleEntries);
    hideScheduleNotice();

    let message = "";

    if (result.status === "FEASIBLE") {
      message = "Schedule is fully feasible.";
    } else if (result.status === "PARTIALLY_FEASIBLE") {
      message =
        "Schedule is partially feasible.\n\nSome work was scheduled, but some tasks could not be fully placed before their deadlines.\n\n";
      if (result.warnings && result.warnings.length > 0) {
        message += result.warnings.join("\n");
      }
    } else {
      message =
        "Schedule is infeasible.\n\nThe system could not fit all required work before the deadlines.\n\n";
      if (result.warnings && result.warnings.length > 0) {
        message += result.warnings.join("\n");
      }
    }

    alert(message);
  } catch (err) {
    alert(err.message);
  }
}

function renderTimeline(entries) {
  const timeline = document.getElementById("timeline");
  if (!timeline) return;

  if (!entries || entries.length === 0) {
    timeline.innerHTML = '<p class="empty-state">No tasks to schedule.</p>';
    return;
  }

  const groupedByDate = {};
  entries.forEach((entry) => {
    const key = entry.date;
    if (!groupedByDate[key]) groupedByDate[key] = [];
    groupedByDate[key].push(entry);
  });

  timeline.innerHTML = Object.keys(groupedByDate)
    .sort((a, b) => new Date(a) - new Date(b))
    .map((dateKey) => {
      const dateItems = groupedByDate[dateKey].sort((a, b) =>
        a.startTime.localeCompare(b.startTime),
      );

      return `
        <div class="timeline-item">
          <div class="timeline-date">${formatDateDisplay(new Date(dateKey + "T12:00:00"))}</div>
          ${dateItems
          .map(
            (item) => `
                <div class="timeline-task task">
                  <div class="timeline-task-title">${item.taskTitle}</div>
                  <div class="timeline-task-info">
                    ${item.startTime} - ${item.endTime} |
                    Planned: ${item.plannedHours} hour${item.plannedHours === 1 ? "" : "s"} |
                    Due: ${formatDateDisplay(new Date(item.taskDueDate + "T12:00:00"))}
                  </div>
                </div>
              `,
          )
          .join("")}
        </div>
      `;
    })
    .join("");
}

async function clearAllItems() {
  if (
    !confirm(
      "Are you sure you want to clear all tasks, events, and generated schedule?\nThis cannot be undone.",
    )
  ) {
    return;
  }

  try {
    await apiFetch("/api/schedule", { method: "DELETE" });
    await apiFetch("/api/tasks", { method: "DELETE" });
    await apiFetch("/api/events", { method: "DELETE" });

    tasks = [];
    scheduleEntries = [];
    updateTasksDisplay();

    const timeline = document.getElementById("timeline");
    if (timeline) {
      timeline.innerHTML =
        '<p class="empty-state">Tasks will appear here once you add them and generate the schedule.</p>';
    }

    renderCurrentScheduleView();

    const generateBtn = document.getElementById("generateSchedule");
    if (generateBtn) generateBtn.disabled = true;

    refreshDashboardIfVisible();
    hideScheduleNotice();
    exitEditMode();
    alert("All backend tasks, events, and schedule entries cleared!");
  } catch (err) {
    alert(err.message);
  }
}

function printScheduleArea() {
  // NEW: Call the browser's built-in print function
  // The @media print styles in style.css handle hiding/showing the right elements
  window.print();
}