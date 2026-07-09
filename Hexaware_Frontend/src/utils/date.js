export function today() {
  return new Date().toISOString().slice(0, 10);
}

export function nowDateTime() {
  return new Date().toISOString().slice(0, 16);
}

export function addMonths(dateString, months) {
  const source = dateString || today();
  const [year, month, day] = source.split('-').map(Number);
  const monthOffset = Number(months || 1);

  if (![year, month, day, monthOffset].every(Number.isFinite)) {
    return '';
  }

  const targetMonthIndex = month - 1 + monthOffset;
  const targetYear = year + Math.floor(targetMonthIndex / 12);
  const normalizedMonthIndex = ((targetMonthIndex % 12) + 12) % 12;
  const lastDayOfTargetMonth = new Date(
    Date.UTC(targetYear, normalizedMonthIndex + 1, 0),
  ).getUTCDate();
  const targetDay = Math.min(day, lastDayOfTargetMonth);

  return new Date(
    Date.UTC(targetYear, normalizedMonthIndex, targetDay),
  )
    .toISOString()
    .slice(0, 10);
}

export function money(value) {
  if (value === null || value === undefined || value === '') return '-';
  return Number(value).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
}
