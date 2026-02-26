

/** the date should be in the format of "YYYY-MM-DD."
 *  @return the day of the week's full name in all caps. */
function convertDateToWeekday(date) {

    const localDate = date.replaceAll('-', '/');
    const dateObject = new Date(localDate);
    const day = dateObject.toLocaleDateString("en-US", { weekday: "long" });

    return day.toUpperCase();
}