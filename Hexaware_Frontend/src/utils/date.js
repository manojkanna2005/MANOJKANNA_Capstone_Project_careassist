export function today() {
  return new Date().toISOString().slice(0, 10);
}

export function nowDateTime() {
  return new Date().toISOString().slice(0, 16);
}

export function addMonths(dateString, months) {
  const date = dateString ? new Date(dateString) : new Date();
  date.setMonth(date.getMonth() + Number(months || 1));
  return date.toISOString().slice(0, 10);
}

export function money(value) {
  if (value === null || value === undefined || value === '') return '-';
  return Number(value).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
}
