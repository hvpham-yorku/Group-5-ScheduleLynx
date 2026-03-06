const baseURL = "http://localhost:8080";


// ================================
// Post Requests
// ================================

/**
 * @param title {string} what you wish to name the event.
 * @param day {string} full name of the day of the week in all caps.
 * @param start {string} when the event begins in HH:MM format.
 * @param end {string} when the event ends in HH:MM format.
 */
export async function postEvent(title, day, start, end) {

    const eventData = {
        title : title,
        day   : day,
        start : start,
        end   : end
    };
    const eventPayload = JSON.stringify(eventData);

    console.log("postCalendarEvent created payload:\n" + eventPayload);

    const input = baseURL + "/api/events";
    const response = await fetch(input, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: eventPayload
    });

    console.log("postCalendarEvent received response:");
    console.log(response);

    return response.ok;
}

/** @param formData is a dictionary of key-value pairs. */
export async function postTask(formData) {

    const { title, date, estimatedHours } = formData;

    console.log(formData);

    const taskData = {
        id             : null,
        title          : title,
        dueDate        : date,
        estimatedHours : Math.round(estimatedHours),
        difficulty     : "MEDIUM" // TODO: Add input field to the UI
    };
    const taskPayload = JSON.stringify(taskData);

    console.log("postCalendarTask created payload:\n" + taskPayload);

    const input = baseURL + "/api/tasks";
    const response = await fetch(input, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: taskPayload
    });

    console.log("postCalendarTask received response:");
    console.log(response);

    return response.ok;
}


// ================================
// Put Requests
// ================================

/**
 * @param id {number} is the unique identifier for the event to be updated.
 * @param title {string} what you wish to name the event.
 * @param day {string} full name of the day of the week in all caps.
 * @param startTime {string} when the event begins in HH:MM format.
 * @param endTime {string} when the event ends in HH:MM format.
 */
export async function sendEventUpdate(id, title, day, startTime, endTime) {

    const data = {
        id:    id,
        title: title,
        day:   day,
        start: startTime,
        end:   endTime,
        type:  "event"
    };
    const payload = JSON.stringify(data);

    console.log("sendEventUpdate created payload:\n" + payload);

    const url = baseURL + "/api/events/" + id.toString();
    const message = {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: payload
    }

    const response = await fetch(url, message);

    console.log("postCalendarTask received response:");
    console.log(response);

    return response.ok;
}

/**
 * @param id {number} is the unique identifier for the task to be updated.
 * @param title {string} what you wish to name the task.
 * @param dueDate {string} the date the task should be completed by formatted as YYYY-MM-DD.
 * @param estHours {number} the number of hours estimated to complete the task.
 * @param difficulty {string} "LOW", "MEDIUM", or "HIGH"
 */
export async function sendTaskUpdate(id, title, dueDate, estHours, difficulty) {

    const data = {
        id : id,
        title : title,
        dueDate : dueDate,
        estimatedHours : estHours,
        difficulty : difficulty
    }
    const payload = JSON.stringify(data);

    console.log("sendTaskUpdate created payload:\n" + payload);

    const url = baseURL + "/api/tasks/" + id.toString();
    const message = {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: payload
    }

    const response = await fetch(url, message);

    console.log("postCalendarTask received response:");
    console.log(response);

    return response.ok;
}


// ================================
// Delete Requests
// ================================

export async function requestDeleteEvent(eventID) {

    if (!eventID) return;

    if (!confirm("Are you sure you want to delete this task?")) return;

    const request = "http://localhost:8080/api/events/" + eventID;
    const response = await fetch(request, {method: 'DELETE',});

    console.log(response);
    return response.ok;
}

export async function requestDeleteTask(taskID) {

    if (!taskID) return;

    if (!confirm("Are you sure you want to delete this task?")) return;

    const request = "http://localhost:8080/api/tasks/" + taskID;
    const response = await fetch(request, {method: 'DELETE',});

    console.log(response);
    return response.ok;
}

export async function requestDeleteAll() {

    const taskRequest = baseURL + "/api/tasks";
    const eventRequest = baseURL + "/api/events";

    const taskResponse = await fetch(taskRequest, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    });
    console.log("Delete tasks response:", taskResponse);

    const eventResponse = await fetch(eventRequest, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    });
    console.log("Delete events response:", eventResponse);

    return taskResponse.ok && eventResponse.ok;
}


// ================================
// Fetch Requests
// ================================

export async function fetchEvents() {
    const request = baseURL + "/api/events";
    const response = await fetch(request);

    console.log("fetchEvents received response:", response);

    if (!response.ok) return [];

    const data = await response.json();
    return data;
}

export async function fetchTasks() {
    const request = baseURL + "/api/tasks";
    const response = await fetch(request);

    console.log("fetchTasks received response:", response);

    if (!response.ok) return [];

    const data = await response.json();
    return data;
}