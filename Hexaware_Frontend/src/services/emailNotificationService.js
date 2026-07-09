import api from "./api.js";

export const createNotification = (notification) =>
  api.post("/api/v1/email-notifications/add", notification).then((r) => r.data);
export const getNotificationById = (notificationId) =>
  api.get(`/api/v1/email-notifications/${notificationId}`).then((r) => r.data);
export const getNotificationsByUserId = (userId) =>
  api.get(`/api/v1/email-notifications/user/${userId}`).then((r) => r.data);
export const getAllNotifications = () =>
  api.get("/api/v1/email-notifications/all").then((r) => r.data);
export const deleteNotification = (notificationId) =>
  api
    .delete(`/api/v1/email-notifications/delete/${notificationId}`)
    .then((r) => r.data);
