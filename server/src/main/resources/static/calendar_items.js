const baseURL = "http://localhost:8080";

/** the date should be in the format of "YYYY-MM-DD."
 *  @return the day of the week's full name in all caps. */
function convertDateToWeekday(date) {

    const localDate = date.replaceAll('-', '/');
    const dateObject = new Date(localDate);
    const day = dateObject.toLocaleDateString("en-US", { weekday: "long" });

    return day.toUpperCase();
}

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

    if (!response.ok) return;
    // TODO
}

async function postCalendarItem(dataObject) {

    if (!currentUser) {
        alert("Please log-in to use this feature.");
        return;
    }

    if (dataObject == null) {
        console.error("Cannot post new calendar object; no data was provided!")
        return;
    }

    const type = dataObject.type;

    if (type.toLowerCase() === "event")
        await postCalendarEvent(dataObject);
    else if (type.toLowerCase() === "task")
        await postCalendarTask(dataObject);
    else throw Error("Invalid type: " + type);

    // Enable generate schedule button
    document.getElementById('generateSchedule').disabled = false;
}