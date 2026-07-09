import { useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import { createUser } from "../../services/userService.js";
import { createAdmin } from "../../services/adminService.js";
import { nowDateTime } from "../../utils/date.js";

function CreateAdmin() {
  const [user, setUser] = useState({
    username: "",
    email: "",
    password: "",
    role: "ADMIN",
    profilePicture: "",
    phoneNumber: "",
    createdAt: nowDateTime(),
    active: true,
  });
  const [admin, setAdmin] = useState({
    userId: "",
    fullName: "",
    department: "",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const changeUser = (e) =>
    setUser({ ...user, [e.target.name]: e.target.value });
  const changeAdmin = (e) =>
    setAdmin({ ...admin, [e.target.name]: e.target.value });
  const handleCreateUser = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    try {
      const saved = await createUser(user);
      setAdmin((a) => ({ ...a, userId: saved.userId }));
      setMessage(
        `Admin user created. User ID: ${saved.userId}. Now create admin profile.`,
      );
    } catch (err) {
      setError(err.userMessage || "Unable to create admin user");
    }
  };
  const handleCreateAdmin = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    try {
      await createAdmin(admin);
      setMessage("Admin profile created successfully.");
    } catch (err) {
      setError(err.userMessage || "Unable to create admin profile");
    }
  };
  return (
    <Layout
      title="Create Admin"
      subtitle="Only existing admin can create another admin."
    >
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="row g-4">
        <div className="col-lg-6">
          <div className="card page-card p-4">
            <h5>Step 1: Create User With ADMIN Role</h5>
            <form onSubmit={handleCreateUser}>
              <div className="mb-3">
                <label className="form-label">Username</label>
                <input
                  className="form-control"
                  name="username"
                  value={user.username}
                  onChange={changeUser}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="form-label">Email</label>
                <input
                  className="form-control"
                  type="email"
                  name="email"
                  value={user.email}
                  onChange={changeUser}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="form-label">Password</label>
                <input
                  className="form-control"
                  type="password"
                  name="password"
                  value={user.password}
                  onChange={changeUser}
                  required
                  minLength="8"
                />
              </div>
              <div className="mb-3">
                <label className="form-label">Phone</label>
                <input
                  className="form-control"
                  name="phoneNumber"
                  value={user.phoneNumber}
                  onChange={changeUser}
                  required
                />
              </div>
              <button className="btn btn-primary">Create Admin User</button>
            </form>
          </div>
        </div>
        <div className="col-lg-6">
          <div className="card page-card p-4">
            <h5>Step 2: Create Admin Profile</h5>
            <form onSubmit={handleCreateAdmin}>
              <div className="mb-3">
                <label className="form-label">User ID</label>
                <input
                  className="form-control"
                  name="userId"
                  value={admin.userId}
                  onChange={changeAdmin}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="form-label">Full Name</label>
                <input
                  className="form-control"
                  name="fullName"
                  value={admin.fullName}
                  onChange={changeAdmin}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="form-label">Department</label>
                <input
                  className="form-control"
                  name="department"
                  value={admin.department}
                  onChange={changeAdmin}
                  required
                />
              </div>
              <button className="btn btn-success">Create Admin Profile</button>
            </form>
          </div>
        </div>
      </div>
    </Layout>
  );
}
export default CreateAdmin;
