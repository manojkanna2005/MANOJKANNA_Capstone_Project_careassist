import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../../services/authService.js";
import Message from "../common/Message.jsx";
import { nowDateTime } from "../../utils/date.js";

const defaultForm = {
  username: "",
  email: "",
  password: "",
  confirmPassword: "",
  role: "PATIENT",
  profilePicture: "",
  phoneNumber: "",
  createdAt: nowDateTime(),
  active: true,
};

function getPasswordStrength(password) {
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/[a-z]/.test(password)) score += 1;
  if (/\d/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;

  if (!password) return { label: '', className: '' };
  if (score <= 2) return { label: 'Weak password', className: 'text-danger' };
  if (score <= 4) return { label: 'Medium password', className: 'text-warning' };
  return { label: 'Strong password', className: 'text-success' };
}

function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState(defaultForm);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const passwordStrength = getPasswordStrength(form.password);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === "checkbox" ? checked : value });
  };
  const handleImageChange = (e) => {
    const file = e.target.files[0];

    if (!file) {
      return;
    }

    const reader = new FileReader();

    reader.onloadend = () => {
      setForm({
        ...form,
        profilePicture: reader.result,
      });
    };

    reader.readAsDataURL(file);
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    if (form.password !== form.confirmPassword) {
      setError("Password and confirm password must match");
      return;
    }
    setLoading(true);
    try {
      const { confirmPassword, ...payload } = form;
      const saved = await register(payload);
      setMessage(
        `Registration successful. Your user ID is ${saved.userId}. Please login and complete your profile.`,
      );
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(err.userMessage || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="card auth-card p-4">
        <h2 className="brand-logo text-center mb-2">Create Account</h2>

        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>
        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Username</label>
              <input
                className="form-control"
                name="username"
                value={form.username}
                onChange={handleChange}
                required
                minLength="3"
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Role</label>
              <select
                className="form-select"
                name="role"
                value={form.role}
                onChange={handleChange}
                required
              >
                <option value="PATIENT">Patient</option>
                <option value="PROVIDER">Healthcare Provider</option>
                <option value="INSURANCE">Insurance Company</option>
              </select>
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label">Email</label>
            <input
              className="form-control"
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Phone Number</label>
            <input
              className="form-control"
              name="phoneNumber"
              value={form.phoneNumber}
              onChange={handleChange}
              pattern="^[6-9]\d{9}$"
              title="Phone number must be a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9."
              required
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Profile Picture</label>
            <input
              className="form-control"
              type="file"
              accept="image/*"
              onChange={handleImageChange}
            />

            {form.profilePicture && (
              <div className="mt-3 text-center">
                <img
                  src={form.profilePicture}
                  alt="Profile Preview"
                  style={{
                    width: "100px",
                    height: "100px",
                    objectFit: "cover",
                    borderRadius: "50%",
                    border: "2px solid #ef5350",
                  }}
                />
              </div>
            )}
          </div>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Password</label>
              <input
                className="form-control"
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                required
                minLength="8"
                maxLength="20"
                pattern="^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).+$"
                title="Password must contain at least one uppercase letter, one lowercase letter, and one number."
              />
              <small className="text-muted">
                Use uppercase, lowercase and number.
              </small>
              {passwordStrength.label && (
                <div className={passwordStrength.className}>
                  {passwordStrength.label}
                </div>
              )}
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Confirm Password</label>
              <input
                className="form-control"
                name="confirmPassword"
                type="password"
                value={form.confirmPassword}
                onChange={handleChange}
                required
              />
            </div>
          </div>
          <button className="btn btn-primary w-100" disabled={loading}>
            {loading ? "Registering..." : "Register"}
          </button>
        </form>
        <div className="text-center mt-3">
          <Link to="/login">Already registered? Login</Link>
        </div>
      </div>
    </div>
  );
}

export default Register;
