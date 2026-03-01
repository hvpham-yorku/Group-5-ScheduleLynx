import {fetchEvents, fetchTasks, requestDeleteAll} from "./requests.js";
import {dateToTime} from "./time.js";


// HTML Element References /////////////////////////////////////////////////////

// Containers
const tasksList      = document.getElementById("tasksList");

// Inputs
const dateInput      = document.getElementById("dateInput");
const startTimeInput = document.getElementById("startTimeInput");
const endTimeInput   = document.getElementById("endTimeInput");

// Alert Checks
if (!tasksList)      alert("Could not find tasksList element!");
if (!dateInput)      alert("Could not find dateInput element!");
if (!startTimeInput) alert("Could not find startTimeInput element!");
if (!endTimeInput)   alert("Could not find endTimeInput element!");


// Setter Functions ////////////////////////////////////////////////////////////

/** Sets the date field's value to today's date. */
export function setDefaultDate() {
    const today = new Date();
    dateInput.value = today.toLocaleDateString("en-CA");
}

/** Sets the start time field's value to the current time. */
export function setDefaultStartTime() {
    const today = new Date();
    startTimeInput.value = dateToTime(today);
}

/** Sets the end time field's value to the current time plus 1 hour. */
export function setDefaultEndTime() {
    const today = new Date();
    endTimeInput.value = dateToTime(today, 60);
}


// Rendering Functions /////////////////////////////////////////////////////////

function clearTimetable() {
    const days = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"];
    for (let day of days) {
        const el = document.getElementById(day + "Events");
        if (el) el.innerHTML = "";
    }
}

async function renderEventItems(events) {

    for (let event of events) {

        const columnId = event.day.toLowerCase().substring(0, 3) + "Events";
        const container = document.getElementById(columnId);

        if (!container) {
            console.warn(`Container not found for day: ${columnId}`);
            continue;
        }

        const eventDiv = document.createElement('div');
        eventDiv.className = 'schedule-event event';

        eventDiv.innerHTML = `
            <div class="schedule-event-title">${event.title}</div>
            <div class="schedule-event-time">${event.start} - ${event.end}</div>
        `;

        eventDiv.onclick = () => showEventModal(event);

        container.appendChild(eventDiv);
    }
}

async function renderTaskItems(tasks) {

    if (!tasksList) {
        alert("Could not find tasksList element!\n" +
            "Debugging required.\n" +
            "If you used AI to write code and broke this, that's on you.");
        return;
    }

    if (tasks.length > 0) {
        tasksList.innerHTML = "";
    }

    for (let task of tasks) {

        const taskItem = document.createElement('div');
        taskItem.className = 'schedule-event task';

        taskItem.innerHTML = `
            <div class="schedule-event-title">${task.title}</div>
            <div class="schedule-event-details">
                <span>Due: ${task.dueDate}</span> | 
                <span>${task.estimatedHours} hrs</span>
            </div>
        `;

        taskItem.onclick = () => {
            console.log("Clicked Task Object:", task);
        };

        tasksList.appendChild(taskItem);
    }
}

/** Fetches the data from the server and renders it on the page. */
export async function reloadDataFromServer() {

    const rawEvents = await fetchEvents();
    const rawTasks = await fetchTasks();

    const events = [];
    for (const rawEvent of rawEvents) {

        let event = {
            id    : rawEvent.id,
            title : rawEvent.title,
            type  : "event",
            day   : rawEvent.day,
            start : rawEvent.start.substring(0, 5), // need to truncate so seconds aren't included // TODO fix this properly
            end   : rawEvent.end.substring(0, 5)
        }
        events.push(event);
    }

    const tasks = [];
    for (const rawTask of rawTasks) {

        let task = {
            id             : rawTask.id,
            title          : rawTask.title,
            type           : "task",
            dueDate        : rawTask.dueDate,
            estimatedHours : rawTask.estimatedHours,
            difficulty     : rawTask.difficulty
        }
        tasks.push(task);
    }

    // sort the tasks in order of closest to furthest due date
    tasks.sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate));

    clearTimetable();
    await renderEventItems(events);
    await renderTaskItems(tasks);
}

/** Delete all events and tasks from the server. */
export async function deleteTimetable() {

    let confirmMessage = "Are you sure you want to clear all tasks and events?\n" +
                         "This cannot be undone."

    if (!(confirm(confirmMessage))) return;

    const success = await requestDeleteAll();

    if (!success) {
        alert("Failed to clear items from the server.");
        return;
    }

    await reloadDataFromServer();
}


// Modals //////////////////////////////////////////////////////////////////////

export function showEventModal(eventData) {

    const modal = document.getElementById('taskModal');
    const modalBody = document.getElementById('modalBody');

    if (!modal || !modalBody) return;

    modalBody.innerHTML = `
        <input type="hidden" id="modalEventId" value="${eventData.id}">
        <div class="form-group">
            <label>Title</label>
            <input type="text" id="modalEventTitle" value="${eventData.title}" class="form-control">
        </div>
        <div class="form-group">
            <label>Day</label>
            <select id="modalEventDay" class="form-control">
                ${['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
        .map(d => `<option value="${d}" ${eventData.day === d ? 'selected' : ''}>${d}</option>`).join('')}
            </select>
        </div>
        <div class="form-group">
            <label>Start Time</label>
            <input type="time" id="modalEventStart" value="${eventData.start}" class="form-control">
        </div>
        <div class="form-group">
            <label>End Time</label>
            <input type="time" id="modalEventEnd" value="${eventData.end}" class="form-control">
        </div>
    `;

    modal.classList.add('active');
}