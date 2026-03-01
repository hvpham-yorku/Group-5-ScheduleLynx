import {postEvent, postTask, requestDeleteEvent, sendEventUpdate} from "./requests.js";
import {reloadDataFromServer, setDefaultDate, setDefaultEndTime, setDefaultStartTime} from "./timetable-functions.js";


// HTML Element References /////////////////////////////////////////////////////

// Form
const timetableForm     = document.getElementById("timetableForm");

// Groups
const titleGroup        = document.getElementById("titleGroup");
const typeGroup         = document.getElementById("typeGroup");
const dateGroup         = document.getElementById("dateGroup");
const startTimeGroup    = document.getElementById("startTimeGroup");
const endTimeGroup      = document.getElementById("endTimeGroup");
const estHoursGroup     = document.getElementById("estHoursGroup");
const descGroup         = document.getElementById("descGroup");
const isRecurGroup      = document.getElementById("recurGroup");

// Inputs
const titleInput        = document.getElementById("titleInput");
const typeInput         = document.getElementById("typeInput");
const dateInput         = document.getElementById("dateInput");
const startTimeInput    = document.getElementById("startTimeInput");
const endTimeInput      = document.getElementById("endTimeInput");
const estHoursInput     = document.getElementById("estHoursInput");
const descInput         = document.getElementById("descInput");
const isRecurInput      = document.getElementById("isRecurInput");

// Alert Checks
if (!timetableForm)     alert("Could not find timetableForm element!");

if (!titleGroup)        alert("Could not find titleGroup element!");
if (!typeGroup)         alert("Could not find typeGroup element!");
if (!dateGroup)         alert("Could not find dateGroup element!");
if (!startTimeGroup)    alert("Could not find startTimeGroup element!");
if (!endTimeGroup)      alert("Could not find endTimeGroup element!");
if (!estHoursGroup)     alert("Could not find estHoursGroup element!");
if (!descGroup)         alert("Could not find descGroup element!");
if (!isRecurGroup)      alert("Could not find isRecurGroup element!");

if (!titleInput)        alert("Could not find titleInput element!");
if (!typeInput)         alert("Could not find typeInput element!");
if (!dateInput)         alert("Could not find dateInput element!");
if (!startTimeInput)    alert("Could not find startTimeInput element!");
if (!endTimeInput)      alert("Could not find endTimeInput element!");
if (!estHoursInput)     alert("Could not find estHoursInput element!");
if (!descInput)         alert("Could not find descInput element!");
if (!isRecurInput)      alert("Could not find isRecurInput element!");


// Actions /////////////////////////////////////////////////////////////////////

// Defines how the add button works
timetableForm.addEventListener("submit", async (event) => {

    event.preventDefault();                                             // apparently before modern API calls, it used to be you had
                                                                        // to reload the whole webpage to get the updated data,
    const formData = new FormData(timetableForm);                       // which is why we're calling preventDefault() to stop that from happening
    const dataObject = Object.fromEntries(formData);             // The more you know 🌈⭐

    let success = false;
    if (dataObject.type === "event") success = await postEvent(dataObject);
    else if (dataObject.type === "task") success = await postTask(dataObject);

    if (!success) {
        alert("It didn't work! Please try again.\n" +
              "Though it's likely a bug so it won't work anyway.");
        return;
    }

    // reset form with defaults
    timetableForm.reset();
    setDefaultDate();
    setDefaultStartTime();
    setDefaultEndTime();

    await reloadDataFromServer();
});

// Defines what happens when the user selects a type in the timetable form
typeInput.addEventListener("change", function () {

    const isEvent = typeInput.value === "event";

    // fields visible when 'event' is selected
    startTimeGroup.style.display = isEvent ? "block" : "none";
    endTimeGroup.style.display   = isEvent ? "block" : "none";

    // fields visible when 'task' is selected
    estHoursGroup.style.display = isEvent ? "none" : "block";

})


// Modal Actions ///////////////////////////////////////////////////////////////

async function updateEvent(event) {

    if (event) event.preventDefault();

    const modal = document.getElementById('taskModal');
    const id = document.getElementById('modalEventId').value;

    const updatedData = {
        id:        parseInt(id),
        title:     document.getElementById('modalEventTitle').value,
        day:       document.getElementById('modalEventDay')  .value,
        startTime: document.getElementById('modalEventStart').value,
        endTime:   document.getElementById('modalEventEnd')  .value,
        type:      "event"
    };

    let success = await sendEventUpdate(updatedData)

    if (success) {
        modal.classList.remove('active');
        await reloadDataFromServer();
    } else alert("Update failed!");
}

async function deleteEvent(event) {

    if (event) event.preventDefault();

    const modal = document.getElementById('taskModal');
    const id = document.getElementById('modalEventId').value;

    let success = await requestDeleteEvent(id);

    if (success) {
        modal.classList.remove('active');
        await reloadDataFromServer();
    } else alert("Delete failed!");
}

document.getElementById("updateTaskBtn").addEventListener("click", updateEvent);
document.getElementById("deleteTaskBtn").addEventListener("click", deleteEvent);