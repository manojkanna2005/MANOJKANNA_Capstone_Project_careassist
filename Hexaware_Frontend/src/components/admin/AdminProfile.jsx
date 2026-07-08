import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import ProfileImage from "../common/ProfileImage.jsx";
import {
  createAdmin,
  getAdminByUserId,
  updateAdmin,
} from "../../services/adminService.js";
import { getAuth, getUserId } from "../../utils/auth.js";

const emptyForm = {
  userId: "",
  fullName: "",
  department: "",
};

function AdminProfile() {
  const auth = getAuth();

  const [adminId, setAdminId] = useState(null);
  const [form, setForm] = useState({
    ...emptyForm,
    userId: getUserId(),
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadAdminProfile() {
      const userId = getUserId();

      setForm((prev) => ({
        ...prev,
        userId,
      }));

      try {
        const data = await getAdminByUserId(userId);
        setAdminId(data.adminId);
        setForm(data);
      } catch {
        setAdminId(null);
        setMessage("Create your admin profile.");
      }
    }

    loadAdminProfile();
  }, []);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setMessage("");
    setError("");

    try {
      const saved = adminId
        ? await updateAdmin(adminId, form)
        : await createAdmin(form);

      setAdminId(saved.adminId);
      setForm(saved);
      setMessage(adminId ? "Admin profile updated successfully." : "Admin profile created successfully.");
    } catch (err) {
      setError(err.userMessage || "Unable to save admin profile");
    }
  };

  return (
    <Layout title="Admin Profile">
      <div className="card page-card p-4">
        <div className="text-center mb-4">
          <ProfileImage
            src={auth.profilePicture}
            size={100}
            alt={auth.username}
          />
          <h5 className="mt-2 mb-0">{auth.username}</h5>
          <small className="text-muted">{auth.email}</small>
        </div>

        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        <form onSubmit={handleSubmit}>
          <input type="hidden" name="userId" value={form.userId || ""} />

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Full Name</label>
              <input
                className="form-control"
                name="fullName"
                value={form.fullName || ""}
                onChange={handleChange}
                required
              />
            </div>

            <div className="col-md-6 mb-3">
              <label className="form-label">Department</label>
              <input
                className="form-control"
                name="department"
                value={form.department || ""}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <button className="btn btn-primary">
            {adminId ? "Update Profile" : "Add Profile"}
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default AdminProfile;
