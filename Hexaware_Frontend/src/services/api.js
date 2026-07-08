import axios from "axios";
import { API_BASE_URL } from "../config/config.js";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  if (config.data instanceof FormData) {
    delete config.headers["Content-Type"];
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error.response?.data;

    let userMessage = "Something went wrong";

    const fieldErrors = data?.fieldErrors || data?.errors || null;

    if (typeof data === "string") {
      userMessage = data;
    } else if (data?.message) {
      userMessage = data.message;
    } else if (data?.error) {
      userMessage = data.error;
    } else if (error.message) {
      userMessage = error.message;
    }

    if (fieldErrors && typeof window !== "undefined") {
      window.dispatchEvent(
        new CustomEvent("careassist:server-validation", {
          detail: { fieldErrors },
        }),
      );
    }

    return Promise.reject({ ...error, userMessage, fieldErrors });
  },
);

export default api;
