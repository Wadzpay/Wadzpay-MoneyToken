import dayjs from "dayjs";

export function compareDatesWithoutTime(date1: any, date2: any) {
  var d1 = new Date(date1);
  d1.setHours(0, 0, 0, 0);
  var d2 = new Date(date2);
  d2.setHours(0, 0, 0, 0);

  if (d1.getTime() === d2.getTime()) {
    return 0;
  } else if (d1.getTime() < d2.getTime()) {
    return -1;
  } else {
    return 1;
  }
}

export const dateTimeFormat = (time: any) => {
  if (!time) {
    return null;
  }

  const localStorageTime = localStorage.getItem("TimeZone");
  const timezone = localStorageTime
    ? JSON.parse(localStorageTime).value
    : "asia/kolkata";

  return dayjs(time).tz(timezone).format("D MMM YYYY, hh:mma");
};
function areAllValuesEmpty(obj:any) {
  return Object.values(obj).every(value => value === '');
}
