/**
 * Converts hours past midnight to a time string in hh:mm:ss format.
 * @param hoursPastMidnight an int or float representing hours past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @author Eric Hanson
 */
export function hoursToTime(hoursPastMidnight) {

    let hours   = Math.floor(hoursPastMidnight);
    let minutes = Math.floor((hoursPastMidnight - hours) * 60);
    let seconds = Math.floor((minutes % 1) * 60);

    let hh = String(hours  ).padStart(2, '0');
    let mm = String(minutes).padStart(2, '0');
    let ss = String(seconds).padStart(2, '0');

    return `${hh}:${mm}:${ss}`;
}

/**
 * Converts minutes past midnight to a time string in hh:mm:ss format.
 * @param minutesPastMidnight an int or float representing minutes past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @author Eric Hanson
 */
export function minutesToTime(minutesPastMidnight) {

    let hours   = Math.floor(minutesPastMidnight / 60);
    let minutes = minutesPastMidnight % 60;
    let seconds = Math.floor((minutes % 1) * 60);

    let hh = String(hours  ).padStart(2, '0');
    let mm = String(minutes).padStart(2, '0');
    let ss = String(seconds).padStart(2, '0');

    return `${hh}:${mm}:${ss}`;
}

/**
 * Converts seconds past midnight to a time string in hh:mm:ss format.
 * @param secondsPastMidnight an int or float representing seconds past midnight.
 * @returns {string} the time in hh:mm:ss format.
 * @author Eric Hanson
 */
export function secondsToTime(secondsPastMidnight) {

    let hours   = Math.floor(secondsPastMidnight / 3600);
    let minutes = Math.floor((secondsPastMidnight % 3600) / 60);
    let seconds = Math.floor(secondsPastMidnight % 60);

    let hh = String(hours  ).padStart(2, '0');
    let mm = String(minutes).padStart(2, '0');
    let ss = String(seconds).padStart(2, '0');

    return `${hh}:${mm}:${ss}`;
}