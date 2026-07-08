import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import {
  getAllUsers,
  activateUser,
  deactivateUser,
  deleteUser,
} from "../../services/userService.js";
import ProfileImage from "../common/ProfileImage.jsx";

function ManageUsers() {
  const [users, setUsers] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const load = () =>
    getAllUsers()
      .then(setUsers)
      .catch((err) => setError(err.userMessage || "Unable to load users"));
  useEffect(() => {
    load();
  }, []);
  const toggle = async (u) => {
    u.active ? await deactivateUser(u.userId) : await activateUser(u.userId);
    setMessage("User status updated.");
    load();
  };
  const remove = async (id) => {
    if (!confirm("Delete this user?")) return;
    await deleteUser(id);
    setMessage("User deleted.");
    load();
  };
  const filtered = users.filter((u) =>
    `${u.username} ${u.email} ${u.role}`
      .toLowerCase()
      .includes(search.toLowerCase()),
  );
  return (
    <Layout title="Manage Users">
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <input
          className="form-control mb-3"
          placeholder="Search users"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <Table
          data={filtered}
          columns={[
            {
              key: "profilePicture",
              label: "Photo",
              render: (r) => (
                <ProfileImage
                  src={r.profilePicture}
                  size={42}
                  alt={r.username}
                />
              ),
            },
            { key: "userId", label: "ID" },
            { key: "username", label: "Username" },
            { key: "email", label: "Email" },
            { key: "role", label: "Role" },
            { key: "phoneNumber", label: "Phone" },
            {
              key: "active",
              label: "Active",
              render: (r) => (r.active ? "Yes" : "No"),
            },
          ]}
          actions={(row) => (
            <div className="d-flex gap-2">
              <button
                className="btn btn-sm btn-outline-primary"
                onClick={() => toggle(row)}
              >
                {row.active ? "Deactivate" : "Activate"}
              </button>
              <button
                className="btn btn-sm btn-outline-danger"
                onClick={() => remove(row.userId)}
              >
                Delete
              </button>
            </div>
          )}
        />
      </div>
    </Layout>
  );
}
export default ManageUsers;
