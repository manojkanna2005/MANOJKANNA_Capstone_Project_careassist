import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import ProfileImage from "../common/ProfileImage.jsx";
import { getAuth, getUserId } from "../../utils/auth.js";
import {
  createPatientProfile,
  getPatientByUserId,
  updatePatientProfile,
} from "../../services/patientService.js";

const emptyForm = {
  userId: "",
  fullName: "",
  dateOfBirth: "",
  gender: "MALE",
  address: "",
  symptoms: "",
  treatment: "",
};

function PatientProfile() {
  const auth = getAuth();

  const [form, setForm] = useState(emptyForm);
  const [patientId, setPatientId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      const userId = getUserId();

      setForm((prev) => ({
        ...prev,
        userId,
      }));

      try {
        const data = await getPatientByUserId(userId);
        setPatientId(data.patientId);
        setForm(data);
      } catch {
        setMessage("Add your patient profile.");
      }
    }

    load();
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
      const saved = patientId
        ? await updatePatientProfile(patientId, form)
        : await createPatientProfile(form);

      setPatientId(saved.patientId);
      setForm(saved);
      setMessage(
        patientId
          ? "Profile updated successfully."
          : "Profile created successfully.",
      );
    } catch (err) {
      setError(err.userMessage || "Unable to save patient profile");
    }
  };

  return (
    <Layout
      title="Patient Profile"
      subtitle="Create or update your patient information."
    >
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

            <div className="col-md-3 mb-3">
              <label className="form-label">Date of Birth</label>
              <input
                className="form-control"
                type="date"
                name="dateOfBirth"
                value={form.dateOfBirth || ""}
                onChange={handleChange}
                required
              />
            </div>

            <div className="col-md-3 mb-3">
              <label className="form-label">Gender</label>
              <select
                className="form-select"
                name="gender"
                value={form.gender || "MALE"}
                onChange={handleChange}
              >
                <option>MALE</option>
                <option>FEMALE</option>
                <option>OTHER</option>
              </select>
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Address</label>
            <textarea
              className="form-control"
              name="address"
              value={form.address || ""}
              onChange={handleChange}
              required
            />
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Symptoms</label>
              <textarea
                className="form-control"
                name="symptoms"
                value={form.symptoms || ""}
                onChange={handleChange}
                required
              />
            </div>

            <div className="col-md-6 mb-3">
              <label className="form-label">Treatment</label>
              <textarea
                className="form-control"
                name="treatment"
                value={form.treatment || ""}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <button className="btn btn-primary">
            {patientId ? "Update Profile" : "Add Profile"}
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default PatientProfile;
