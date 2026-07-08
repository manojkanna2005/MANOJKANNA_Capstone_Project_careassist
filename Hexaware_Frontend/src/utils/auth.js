export function normalizeRole(role) {
  if (!role) return "";
  const value = String(role).replace("ROLE_", "").trim().toUpperCase();
  if (value === "HEALTHCARE_PROVIDER") return "PROVIDER";
  if (value === "INSURANCE_COMPANY") return "INSURANCE";
  return value;
}

export function setAuth(loginResponse) {
  const role = normalizeRole(loginResponse.role);

  localStorage.setItem("token", loginResponse.token || "");
  localStorage.setItem("tokenType", loginResponse.tokenType || "Bearer");
  localStorage.setItem("userId", String(loginResponse.userId || ""));
  localStorage.setItem("username", loginResponse.username || "");
  localStorage.setItem("email", loginResponse.email || "");
  localStorage.setItem("role", role);
  localStorage.setItem("profilePicture", loginResponse.profilePicture || "");
}

export function clearAuth() {
  localStorage.removeItem("token");
  localStorage.removeItem("tokenType");
  localStorage.removeItem("userId");
  localStorage.removeItem("username");
  localStorage.removeItem("email");
  localStorage.removeItem("role");
  localStorage.removeItem("profilePicture");
}

export function getAuth() {
  return {
    token: localStorage.getItem("token"),
    tokenType: localStorage.getItem("tokenType") || "Bearer",
    userId: Number(localStorage.getItem("userId")),
    username: localStorage.getItem("username"),
    email: localStorage.getItem("email"),
    role: normalizeRole(localStorage.getItem("role")),
    profilePicture: localStorage.getItem("profilePicture"),
  };
}

export function getToken() {
  return localStorage.getItem("token");
}

export function getRole() {
  return normalizeRole(localStorage.getItem("role"));
}

export function getUserId() {
  return Number(localStorage.getItem("userId"));
}

export function isLoggedIn() {
  return Boolean(getToken());
}
