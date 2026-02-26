import {convertDateToWeekday} from "./utils.js";

const baseURL = "http://localhost:8080";


// ================================
// Post Requests
// ================================

/** Do NOT call this directly. Call postCalendarEvent() instead. */
async function postCalendarEvent(dataObject) {

    // TODO: for frontend people:
    // 'dueDate' is confusing and reallllly needs to be renamed to eventDate (without changing task's 'dueDate')
    const { title, dueDate, startTime, endTime } = dataObject;

    const day = convertDateToWeekday(dueDate);

    const eventData = {
        id    : null,
        title : title,
        day   : day,
        start : startTime,
        end   : endTime
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

/** Do NOT call this directly. Call postCalendarEvent() instead. */
async function postCalendarTask(dataObject) {

    const { title, dueDate, estimatedHours } = dataObject;

    console.log(dataObject);

    const taskData = {
        id             : null,
        title          : title,
        dueDate        : dueDate,
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

export async function postCalendarItem(dataObject) {

    if (dataObject == null) {
        console.error("Cannot post new calendar object; no data was provided!")
        return;
    }

    const type = dataObject.type;

    let responseOK = false;
    if (type.toLowerCase() === "event")
        responseOK = await postCalendarEvent(dataObject);
    else if (type.toLowerCase() === "task")
        responseOK = await postCalendarTask(dataObject);
    else throw Error("Invalid type: " + type);

    // Enable generate schedule button
    document.getElementById('generateSchedule').disabled = false;

    return responseOK;
}


// ================================
// Delete Requests
// ================================

export async function requestDeleteTask() {

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

    // Return true if both succeeded, false if either failed
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