import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import ProfileImage from "../common/ProfileImage.jsx";
import { getAuth, getUserId } from "../../utils/auth.js";
import {
  createProviderProfile,
  getProviderByUserId,
  updateProviderProfile,
} from "../../services/healthcareProviderService.js";

const empty = {
  userId: "",
  providerName: "",
  specialization: "",
  licenseNumber: "",
  address: "",
};

function ProviderProfile() {
  const auth = getAuth();

  const [form, setForm] = useState(empty);
  const [providerId, setProviderId] = useState(null);
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
        const data = await getProviderByUserId(userId);
        setProviderId(data.providerId);
        setForm(data);
      } catch {
        setMessage("Add your healthcare provider profile.");
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

    setError("");
    setMessage("");

    try {
      const saved = providerId
        ? await updateProviderProfile(providerId, form)
        : await createProviderProfile(form);

      setProviderId(saved.providerId);
      setForm(saved);
      setMessage("Provider profile saved successfully.");
    } catch (err) {
      setError(err.userMessage || "Unable to save profile");
    }
  };

  return (
    <Layout title="Provider Profile">
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
              <label className="form-label">Provider Name</label>
              <input
                className="form-control"
                name="providerName"
                value={form.providerName || ""}
                onChange={handleChange}
                required
              />
            </div>

            <div className="col-md-6 mb-3">
              <label className="form-label">Specialization</label>
              <input
                className="form-control"
                name="specialization"
                value={form.specialization || ""}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">License Number</label>
            <input
              className="form-control"
              name="licenseNumber"
              value={form.licenseNumber || ""}
              onChange={handleChange}
              required
            />
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

          <button className="btn btn-primary">
            {providerId ? "Update Profile" : "Add Profile"}
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default ProviderProfile;
