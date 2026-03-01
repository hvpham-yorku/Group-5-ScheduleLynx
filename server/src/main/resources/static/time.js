/**
 * Calculates the time in hh:mm:ss format from the given number of hours, minutes, and seconds past midnight.
 * If a float is provided for an argument, its fractional part is included in the calculation.
 * If the sum of all arguments exceeds 24 hours, then the returned time will be normalized to the next day(s).
 * @param hoursPastMidnight an int or float representing hours past midnight.
 * @param plusMinutes an int or float representing the minutes past midnight.
 * @param plusSeconds an int or float representing seconds past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @author Eric Hanson
 */
export function numbersToTime(hoursPastMidnight, plusMinutes, plusSeconds) {

    const secondsInADay    = 60*60*24;
    const secondsInAnHour  = 60*60;
    const secondsInAMinute = 60;

    // convert everything to whole seconds, rounding down in case of floating point imprecision
    let totalSeconds = Math.floor(plusSeconds + // seconds
        plusMinutes * secondsInAMinute +           // minutes converted to seconds
        hoursPastMidnight * secondsInAnHour);      // hours converted to seconds

    // prevent overflow by wrapping around to the next day
    totalSeconds %= secondsInADay;

    // calculate the actual hours, minutes, and seconds
    let hours   = Math.floor(totalSeconds / secondsInAnHour);
    let minutes = Math.floor((totalSeconds % secondsInAnHour) / secondsInAMinute);
    let seconds = Math.floor((totalSeconds % secondsInAMinute));

    let hh = String(hours  ).padStart(2, '0');
    let mm = String(minutes).padStart(2, '0');
    let ss = String(seconds).padStart(2, '0');

    return `${hh}:${mm}:${ss}`;
}

/**
 * Converts a date plus an optional offset to a time string in hh:mm:ss format.
 * @param date the date to convert.
 * @param offsetMinutes an optional offset in minutes.
 * @returns {string} the time in hh:mm:ss format.
 * @see hoursMinutesAndSecondsToTime for more information.
 * @author Eric Hanson
 */
export function dateToTime(date, offsetMinutes = 0) {

    let hours = date.getHours();
    let minutes = date.getMinutes() + offsetMinutes;
    let seconds = date.getSeconds();

    return numbersToTime(hours, minutes, seconds);
}

/**
 * Converts hours and minutes past midnight to a time string in hh:mm:ss format.
 * @param hoursPastMidnight an int or float representing hours past midnight.
 * @param andMinutes an int or float representing minutes past midnight.
 * @returns {string} the time in hh:mm:ss format.

 * @author Eric Hanson
 */
export function hoursAndMinutesToTime(hoursPastMidnight, andMinutes) {
    return numbersToTime(hoursPastMidnight, andMinutes, 0)
}

/**
 * Converts hours and seconds past midnight to a time string in hh:mm:ss format.
 * @param hoursPastMidnight an int or float representing hours past midnight.
 * @param andSeconds an int or float representing seconds past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @see hoursMinutesAndSecondsToTime for more information.
 * @author Eric Hanson
 */
export function hoursAndSecondsToTime(hoursPastMidnight, andSeconds) {
    return numbersToTime(hoursPastMidnight, 0, andSeconds)
}

/**
 * Converts minutes and seconds past midnight to a time string in hh:mm:ss format.
 * @param minutesPastMidnight an int or float representing minutes past midnight.
 * @param andSeconds an int or float representing seconds past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @see hoursMinutesAndSecondsToTime for more information.
 * @author Eric Hanson
 */
export function minutesAndSecondsToTime(minutesPastMidnight, andSeconds) {
    return numbersToTime(0, minutesPastMidnight, andSeconds)
}

/**
 * Converts hours past midnight to a time string in hh:mm:ss format.
 * @param hoursPastMidnight an int or float representing hours past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @see hoursMinutesAndSecondsToTime for more information.
 * @author Eric Hanson
 */
export function hoursToTime(hoursPastMidnight) {
    return numbersToTime(hoursPastMidnight, 0, 0)
}

/**
 * Converts minutes past midnight to a time string in hh:mm:ss format.
 * @param minutesPastMidnight an int or float representing minutes past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @see hoursMinutesAndSecondsToTime for more information.
 * @author Eric Hanson
 */
export function minutesToTime(minutesPastMidnight) {
    return numbersToTime(0, minutesPastMidnight, 0)
}

/**
 * Converts seconds past midnight to a time string in hh:mm:ss format.
 * @param secondsPastMidnight an int or float representing seconds past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @see hoursMinutesAndSecondsToTime for more information.
 * @author Eric Hanson
 */
export function secondsToTime(secondsPastMidnight) {
    return numbersToTime(0, 0, secondsPastMidnight)
}