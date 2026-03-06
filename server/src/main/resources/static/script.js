// ============================
// SCHEDULE LYNX - MAIN SCRIPT
// ============================

// Global Variables
let tasks = [];
let currentWeekStart = getMonday(new Date());
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

// Check if user is logged in
function isLoggedIn() {
  const storedUser = localStorage.getItem("schedulelynxUser");
  return storedUser !== null;
}

// Get current logged in user
function getCurrentUser() {
  const storedUser = localStorage.getItem("schedulelynxUser");
  return storedUser ? JSON.parse(storedUser) : null;
}

// Legacy login handler kept for compatibility.
// Real login should be done from login.html backend auth flow.
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
    username: username,
    email: username.includes("@") ? username : username + "@schedulelynx.app",
    loginTime: new Date().toISOString(),
    rememberMe: rememberMe,
  };

  localStorage.setItem("schedulelynxUser", JSON.stringify(user));
  window.location.href = "index.html";
}

// Legacy demo login
function loginDemo() {
  const usernameField = document.getElementById("username");
  const passwordField = document.getElementById("password");

  if (usernameField) usernameField.value = "demo";
  if (passwordField) passwordField.value = "demo123";
  handleLogin();
}

// Legacy signup handler kept for compatibility.
// Real signup should be done from login.html backend auth flow.
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
    username: username,
    email: email,
    name: name,
    loginTime: new Date().toISOString(),
    rememberMe: true,
  };

  localStorage.setItem("schedulelynxUser", JSON.stringify(user));
  localStorage.setItem(`tasks_${username}`, JSON.stringify([]));

  alert("Account created successfully! Logging in...");
  window.location.href = "index.html";
}

// Logout
async function logout() {
  if (!confirm("Are you sure you want to logout?")) return;

  try {
    await apiFetch("/api/auth/logout", {
      method: "POST",
    });
  } catch (err) {
    // even if backend logout fails, clear local frontend state
  }

  localStorage.removeItem("schedulelynxUser");
  window.location.href = "login.html";
}

// Toggle signup form
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

  if (currentPage === "login.html") {
    initializeLoginHandlers();
  } else if (currentPage === "index.html") {
    initializeDashboard();
  } else if (currentPage === "timetable.html") {
    initializeFormHandlers();
    initializeScheduleDisplay();
    loadTasksFromStorage();
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

// Initialize login form handlers
function initializeLoginHandlers() {
  const loginForm = document.getElementById("loginForm");
  const signupForm = document.getElementById("signupFormElement");

  if (loginForm) {
    loginForm.addEventListener("submit", handleLogin);
  }

  if (signupForm) {
    signupForm.addEventListener("submit", handleSignup);
  }
}

// ============================
// TASK LOADING / SAVING
// ============================

async function loadUserTasks(username) {
  const raw = localStorage.getItem(`tasks_${username}`);
  let legacyItems = [];

  try {
    legacyItems = raw ? JSON.parse(raw) : [];
  } catch (e) {
    legacyItems = [];
  }

  const localEvents = legacyItems.filter((item) => item.type === "event");

  const backendTasks = await apiFetch("/api/tasks", {
    method: "GET",
  });

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
    difficulty: task.difficulty,
  }));

  tasks = [...normalizedTasks, ...localEvents];
}

function saveUserTasks(username) {
  const localEventsOnly = tasks.filter((item) => item.type === "event");
  localStorage.setItem(`tasks_${username}`, JSON.stringify(localEventsOnly));
}

function saveTasksToStorage() {
  if (currentUser) {
    saveUserTasks(currentUser.username);
    refreshDashboardIfVisible();
  }
}

async function loadTasksFromStorage() {
  if (currentUser) {
    await loadUserTasks(currentUser.username);
    updateTasksDisplay();
    renderScheduleGrid();
    const generateBtn = document.getElementById("generateSchedule");
    if (generateBtn && tasks.length > 0) {
      generateBtn.disabled = false;
    }
    refreshDashboardIfVisible();
  }
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

function generateId() {
  return Date.now() + Math.random().toString(36).substring(2, 11);
}

// ============================
// DASHBOARD FUNCTIONS
// ============================

async function initializeDashboard() {
  if (!isLoggedIn()) return;

  currentUser = getCurrentUser();
  await loadUserTasks(currentUser.username);
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
    const dueDate = new Date(task.dueDate);
    return dueDate > today && dueDate <= weekFromNow;
  });
  upcomingCount.textContent = upcoming.length;

  const overdue = tasks.filter((task) => {
    const dueDate = new Date(task.dueDate);
    return dueDate < today && !task.completed;
  });
  overdueCount.textContent = overdue.length;

  let totalHours = 0;
  tasks.forEach((task) => {
    const dueDate = new Date(task.dueDate);
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
      const dueDate = new Date(task.dueDate);
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
          <div class="task-item-dueDate">Due: ${formatDateDisplay(new Date(task.dueDate))}</div>
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
  ) {
    return;
  }

  taskTypeSelect.addEventListener("change", function () {
    const startTimeGroup = document.getElementById("startTimeGroup");
    const endTimeGroup = document.getElementById("endTimeGroup");
    const estimatedHoursGroup = document.getElementById("estimatedHoursGroup");
    const recurringGroup = document.getElementById("recurringGroup");
    const recurrenceOptions = document.getElementById("recurrenceOptions");
    const daysOfWeekGroup = document.getElementById("daysOfWeekGroup");
    const recurringCheckbox = document.getElementById("isRecurring");

    if (
      !startTimeGroup ||
      !endTimeGroup ||
      !estimatedHoursGroup ||
      !recurringGroup ||
      !recurrenceOptions ||
      !daysOfWeekGroup ||
      !recurringCheckbox
    ) {
      return;
    }

    if (this.value === "task") {
      startTimeGroup.style.display = "none";
      endTimeGroup.style.display = "none";
      estimatedHoursGroup.style.display = "block";
      recurringGroup.style.display = "none";
      recurringCheckbox.checked = false;
      recurrenceOptions.style.display = "none";
      daysOfWeekGroup.style.display = "none";
    } else {
      startTimeGroup.style.display = "flex";
      endTimeGroup.style.display = "flex";
      estimatedHoursGroup.style.display = "none";
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

  taskTypeSelect.dispatchEvent(new Event("change"));
}

async function addTask() {
  if (!currentUser) {
    alert("You are not logged in.");
    return;
  }

  const title = document.getElementById("taskTitle")?.value.trim();
  const type = document.getElementById("taskType")?.value;
  const dueDate = document.getElementById("dueDate")?.value;

  const estimatedHoursInput = document.getElementById("estimatedHours");
  const estimatedHours = estimatedHoursInput
    ? parseFloat(estimatedHoursInput.value)
    : 0;

  const startTime = document.getElementById("startTime")?.value || "";
  const endTime = document.getElementById("endTime")?.value || "";
  const description =
    document.getElementById("description")?.value.trim() || "";

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

  function toBackendWeekdayEnum(dateStr) {
    const d = new Date(`${dateStr}T12:00:00`);
    const jsDay = d.getDay();
    const map = {
      0: "SUNDAY",
      1: "MONDAY",
      2: "TUESDAY",
      3: "WEDNESDAY",
      4: "THURSDAY",
      5: "FRIDAY",
      6: "SATURDAY",
    };
    return map[jsDay];
  }

  let item = {
    id: generateId(),
    title: title,
    type: type,
    dueDate: dueDate,
    description: description,
    completed: false,
    createdAt: new Date().toISOString(),
  };

  const generateBtn = document.getElementById("generateSchedule");
  if (generateBtn) {
    generateBtn.disabled = false;
  }

  try {
    if (type === "event") {
      item.startTime = startTime;
      item.endTime = endTime;
      item.estimatedHours = 0;
      item.isRecurring = false;
      item.recurrenceType = null;
      item.recurrenceEnd = null;
      item.recurrenceDays = [];

      tasks.push(item);
      saveUserTasks(currentUser.username);
    } else {
      const existingTask = selectedTaskId
        ? tasks.find((t) => t.id === selectedTaskId && t.type === "task")
        : null;

      const payload = {
        title: title,
        dueDate: dueDate,
        estimatedHours: Math.round(estimatedHours),
        difficulty: "MEDIUM",
      };

      let saved;
      if (existingTask) {
        saved = await apiFetch(`/api/tasks/${selectedTaskId}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        saved = await apiFetch("/api/tasks", {
          method: "POST",
          body: JSON.stringify(payload),
        });
      }

      item = {
        id: String(saved.id),
        title: saved.title,
        type: "task",
        dueDate: saved.dueDate,
        description: description,
        completed: false,
        createdAt: new Date().toISOString(),
        estimatedHours: saved.estimatedHours,
        startTime: null,
        endTime: null,
        isRecurring: false,
        recurrenceType: null,
        recurrenceEnd: null,
        recurrenceDays: [],
        difficulty: saved.difficulty,
      };

      if (existingTask) {
        tasks = tasks.filter((t) => t.id !== selectedTaskId);
      }

      tasks.push(item);
    }

    selectedTaskId = null;
    updateTasksDisplay();
    renderScheduleGrid();
    refreshDashboardIfVisible();

    const taskForm = document.getElementById("taskForm");
    if (taskForm) taskForm.reset();

    const recurrenceOptions = document.getElementById("recurrenceOptions");
    const startTimeGroup = document.getElementById("startTimeGroup");
    const endTimeGroup = document.getElementById("endTimeGroup");

    if (recurrenceOptions) recurrenceOptions.style.display = "none";
    if (startTimeGroup) startTimeGroup.style.display = "none";
    if (endTimeGroup) endTimeGroup.style.display = "none";
  } catch (err) {
    alert(err.message);
  }
}

// ============================
// TASK DISPLAY
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
    .map(
      (task) => `
        <div class="task-card ${task.type}" onclick="viewTaskDetails('${task.id}')">
          <div class="task-card-type">${task.type.charAt(0).toUpperCase() + task.type.slice(1)}</div>
          <div class="task-card-title">${task.title}</div>
          <div class="task-card-dueDate">Due: ${formatDateDisplay(new Date(task.dueDate))}</div>
          ${
            task.type === "task"
              ? `<div class="task-card-time">${task.estimatedHours} hours</div>`
              : task.startTime
                ? `<div class="task-card-time">${task.startTime} - ${task.endTime}</div>`
                : ""
          }
          ${task.isRecurring ? `<div class="task-card-time">Recurring: ${task.recurrenceType}</div>` : ""}
        </div>
      `,
    )
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
    ? `${task.recurrenceType.charAt(0).toUpperCase() + task.recurrenceType.slice(1)} (${task.recurrenceDays.join(", ")})`
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
      <span class="modal-detail-label">Due Date:</span>
      <span class="modal-detail-value">${formatDateDisplay(new Date(task.dueDate))}</span>
    </div>
    <div class="modal-detail">
      <span class="modal-detail-label">Estimated Time:</span>
      <span class="modal-detail-value">${task.estimatedHours || 0} hours</span>
    </div>
    ${
      task.startTime
        ? `
    <div class="modal-detail">
      <span class="modal-detail-label">Start Time:</span>
      <span class="modal-detail-value">${task.startTime}</span>
    </div>
    `
        : ""
    }
    ${
      task.endTime
        ? `
    <div class="modal-detail">
      <span class="modal-detail-label">End Time:</span>
      <span class="modal-detail-value">${task.endTime}</span>
    </div>
    `
        : ""
    }
    <div class="modal-detail">
      <span class="modal-detail-label">Recurring:</span>
      <span class="modal-detail-value">${recurrenceText}</span>
    </div>
    ${
      task.description
        ? `
    <div class="modal-detail">
      <span class="modal-detail-label">Description:</span>
      <span class="modal-detail-value">${task.description}</span>
    </div>
    `
        : ""
    }
  `;

  modal.classList.add("active");
}

// ============================
// MODAL HANDLING
// ============================

document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("taskModal");

  if (modal) {
    const closeBtn = document.querySelector(".close-modal");
    const closeModalBtn = document.getElementById("closeModalBtn");
    const deleteTaskBtn = document.getElementById("deleteTaskBtn");
    const editTaskBtn = document.getElementById("editTaskBtn");

    if (closeBtn) {
      closeBtn.addEventListener("click", () =>
        modal.classList.remove("active"),
      );
    }

    if (closeModalBtn) {
      closeModalBtn.addEventListener("click", () =>
        modal.classList.remove("active"),
      );
    }

    window.addEventListener("click", (e) => {
      if (e.target === modal) {
        modal.classList.remove("active");
      }
    });

    if (deleteTaskBtn) {
      deleteTaskBtn.addEventListener("click", deleteTaskListener);
    }

    if (editTaskBtn) {
      editTaskBtn.addEventListener("click", editSelectedTask);
    }
  }
});

async function deleteTaskListener() {
  if (!selectedTaskId) return;

  const item = tasks.find((t) => t.id === selectedTaskId);
  if (!item) return;

  if (!confirm("Are you sure you want to delete this task?")) return;

  try {
    if (item.type === "event") {
      tasks = tasks.filter((t) => t.id !== selectedTaskId);
      saveTasksToStorage();
    } else {
      await apiFetch(`/api/tasks/${selectedTaskId}`, {
        method: "DELETE",
      });
      tasks = tasks.filter((t) => t.id !== selectedTaskId);
    }

    updateTasksDisplay();
    renderScheduleGrid();
    refreshDashboardIfVisible();

    const modal = document.getElementById("taskModal");
    if (modal) modal.classList.remove("active");

    selectedTaskId = null;
  } catch (err) {
    alert(err.message);
  }
}

function editSelectedTask() {
  const task = tasks.find((t) => t.id === selectedTaskId);
  if (!task) return;

  const taskTitle = document.getElementById("taskTitle");
  const taskType = document.getElementById("taskType");
  const dueDate = document.getElementById("dueDate");
  const estimatedHours = document.getElementById("estimatedHours");
  const startTime = document.getElementById("startTime");
  const endTime = document.getElementById("endTime");
  const description = document.getElementById("description");

  if (taskTitle) taskTitle.value = task.title;
  if (taskType) taskType.value = task.type;
  if (dueDate) dueDate.value = task.dueDate;
  if (estimatedHours) estimatedHours.value = task.estimatedHours || "";
  if (startTime) startTime.value = task.startTime || "";
  if (endTime) endTime.value = task.endTime || "";
  if (description) description.value = task.description || "";

  const startTimeGroup = document.getElementById("startTimeGroup");
  const endTimeGroup = document.getElementById("endTimeGroup");

  if (task.type === "event") {
    if (startTimeGroup) startTimeGroup.style.display = "flex";
    if (endTimeGroup) endTimeGroup.style.display = "flex";
  } else {
    if (startTimeGroup) startTimeGroup.style.display = "none";
    if (endTimeGroup) endTimeGroup.style.display = "none";
  }

  const modal = document.getElementById("taskModal");
  if (modal) modal.classList.remove("active");

  if (taskTitle) taskTitle.focus();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

// ============================
// SCHEDULE GENERATION & DISPLAY
// ============================

function initializeScheduleDisplay() {
  updateWeekDisplay();
  renderScheduleGrid();

  const prevWeek = document.getElementById("prevWeek");
  const nextWeek = document.getElementById("nextWeek");
  const generateScheduleBtn = document.getElementById("generateSchedule");
  const clearAllBtn = document.getElementById("clearAll");

  if (prevWeek) {
    prevWeek.addEventListener("click", () => {
      currentWeekStart = addDays(currentWeekStart, -7);
      updateWeekDisplay();
      renderScheduleGrid();
    });
  }

  if (nextWeek) {
    nextWeek.addEventListener("click", () => {
      currentWeekStart = addDays(currentWeekStart, 7);
      updateWeekDisplay();
      renderScheduleGrid();
    });
  }

  if (generateScheduleBtn) {
    generateScheduleBtn.addEventListener("click", generateSchedule);
  }

  if (clearAllBtn) {
    clearAllBtn.addEventListener("click", clearAllItems);
  }
}

function updateWeekDisplay() {
  const weekDisplay = document.getElementById("weekDisplay");
  if (!weekDisplay) return;

  const weekEnd = addDays(currentWeekStart, 6);
  weekDisplay.textContent = `Week of ${formatDateDisplay(currentWeekStart)} - ${formatDateDisplay(weekEnd)}`;
}

function renderScheduleGrid() {
  const scheduleGrid = document.getElementById("scheduleGrid");
  if (!scheduleGrid) return;

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
        ${
          dayEvents.length > 0
            ? dayEvents
                .map(
                  (event) => `
                    <div class="schedule-event ${event.type}" onclick="viewTaskDetails('${event.id}')">
                      <div class="schedule-event-title">${event.title}</div>
                      ${
                        event.startTime
                          ? `<div class="schedule-event-time">${event.startTime} - ${event.endTime}</div>`
                          : `<div class="schedule-event-time">${event.label || "Due"}</div>`
                      }
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

function getEventsForDay(dateStr) {
  const items = [];

  tasks.forEach((item) => {
    if (item.type === "event") {
      if (item.isRecurring) {
        const dayName = getDayName(new Date(dateStr));
        const end = item.recurrenceEnd ? new Date(item.recurrenceEnd) : null;

        const allowed =
          !item.recurrenceDays || item.recurrenceDays.length === 0
            ? true
            : item.recurrenceDays.includes(dayName);

        const start = item.dueDate ? new Date(item.dueDate) : null;
        const cur = new Date(dateStr);

        if (allowed && (!start || cur >= start) && (!end || cur <= end)) {
          items.push({
            id: item.id,
            title: item.title,
            type: "event",
            startTime: item.startTime,
            endTime: item.endTime,
            label: null,
          });
        }
      } else {
        if (item.dueDate === dateStr) {
          items.push({
            id: item.id,
            title: item.title,
            type: "event",
            startTime: item.startTime,
            endTime: item.endTime,
            label: null,
          });
        }
      }
    }

    if (item.type === "task") {
      if (item.dueDate === dateStr) {
        items.push({
          id: item.id,
          title: item.title,
          type: "task",
          startTime: null,
          endTime: null,
          label: "Due",
        });
      }
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

function generateSchedule() {
  if (tasks.length === 0) {
    alert("Please add at least one task before generating a schedule");
    return;
  }

  const tasksToSchedule = tasks.filter((t) => t.type === "task");
  if (tasksToSchedule.length === 0) {
    alert("Add tasks to generate a schedule");
    return;
  }

  const scheduledTasks = distributeTasksAcrossTime(tasksToSchedule);
  renderTimeline(scheduledTasks);
  alert("Schedule generated successfully! Check the timeline below.");
}

function distributeTasksAcrossTime(tasksToSchedule) {
  const sortedTasks = [...tasksToSchedule].sort((a, b) => {
    return new Date(a.dueDate) - new Date(b.dueDate);
  });

  const scheduledItems = [];

  sortedTasks.forEach((task) => {
    const dueDate = new Date(task.dueDate);
    const hoursNeeded = task.estimatedHours || 0;
    const daysAvailable = Math.ceil(
      (dueDate - new Date()) / (1000 * 60 * 60 * 24),
    );

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
          hours: scheduledHours,
        });

        remainingHours -= scheduledHours;
        currentDate = addDays(currentDate, 1);
      }
    }
  });

  return scheduledItems;
}

function renderTimeline(scheduledItems) {
  const timeline = document.getElementById("timeline");
  if (!timeline) return;

  if (scheduledItems.length === 0) {
    timeline.innerHTML = '<p class="empty-state">No tasks to schedule.</p>';
    return;
  }

  const groupedByDate = {};
  scheduledItems.forEach((item) => {
    if (!groupedByDate[item.date]) {
      groupedByDate[item.date] = [];
    }
    groupedByDate[item.date].push(item);
  });

  timeline.innerHTML = Object.keys(groupedByDate)
    .sort((a, b) => new Date(a) - new Date(b))
    .map((date) => {
      const dateItems = groupedByDate[date];
      return `
        <div class="timeline-item">
          <div class="timeline-date">${date}</div>
          ${dateItems
            .map(
              (item) => `
                <div class="timeline-task ${item.task.type}" onclick="viewTaskDetails('${item.task.id}')">
                  <div class="timeline-task-title">${item.task.title}</div>
                  <div class="timeline-task-info">
                    Scheduled: ${item.hours} hours |
                    Due: ${formatDateDisplay(new Date(item.task.dueDate))}
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

// ============================
// CLEAR ALL
// ============================

async function clearAllItems() {
  if (
    !confirm(
      "Are you sure you want to clear all tasks?\nThis cannot be undone.",
    )
  ) {
    return;
  }

  try {
    await apiFetch("/api/tasks", {
      method: "DELETE",
    });

    tasks = tasks.filter((item) => item.type === "event");
    saveTasksToStorage();
    updateTasksDisplay();

    const timeline = document.getElementById("timeline");
    if (timeline) {
      timeline.innerHTML =
        '<p class="empty-state">Tasks will appear here once you add them and generate the schedule.</p>';
    }

    renderScheduleGrid();

    const generateBtn = document.getElementById("generateSchedule");
    if (generateBtn) {
      generateBtn.disabled = tasks.length === 0;
    }

    refreshDashboardIfVisible();
    alert("All backend tasks cleared!");
  } catch (err) {
    alert(err.message);
  }
}
