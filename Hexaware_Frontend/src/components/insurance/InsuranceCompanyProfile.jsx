import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import ProfileImage from "../common/ProfileImage.jsx";
import { getAuth, getUserId } from "../../utils/auth.js";
import {
  createCompanyProfile,
  getCompanyByUserId,
  updateCompanyProfile,
} from "../../services/insuranceCompanyService.js";

const empty = {
  userId: "",
  companyName: "",
  registrationNumber: "",
  address: "",
  contactEmail: "",
};

function InsuranceCompanyProfile() {
  const auth = getAuth();

  const [form, setForm] = useState(empty);
  const [companyId, setCompanyId] = useState(null);
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
        const data = await getCompanyByUserId(userId);
        setCompanyId(data.companyId);
        setForm(data);
      } catch {
        setMessage("Add your insurance company profile.");
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
      const saved = companyId
        ? await updateCompanyProfile(companyId, form)
        : await createCompanyProfile(form);

      setCompanyId(saved.companyId);
      setForm(saved);
      setMessage("Insurance company profile saved successfully.");
    } catch (err) {
      setError(err.userMessage || "Unable to save profile");
    }
  };

  return (
    <Layout title="Insurance Company Profile">
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
              <label className="form-label">Company Name</label>
              <input
                className="form-control"
                name="companyName"
                value={form.companyName || ""}
                onChange={handleChange}
                required
              />
            </div>

            <div className="col-md-6 mb-3">
              <label className="form-label">Registration Number</label>
              <input
                className="form-control"
                name="registrationNumber"
                value={form.registrationNumber || ""}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Contact Email</label>
            <input
              className="form-control"
              type="email"
              name="contactEmail"
              value={form.contactEmail || ""}
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
            {companyId ? "Update Profile" : "Add Profile"}
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default InsuranceCompanyProfile;
