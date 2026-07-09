import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import Table from "../common/Table.jsx";
import { getUserId } from "../../utils/auth.js";
import { getCompanyByUserId } from "../../services/insuranceCompanyService.js";
import {
  createPlan,
  getPlansByCompanyId,
  activatePlan,
  deactivatePlan,
  deletePlan,
} from "../../services/insurancePlanService.js";
import { money } from "../../utils/date.js";

const MAX_MONEY = 9999999999.99;
const empty = {
  companyId: "",
  planName: "",
  planDescription: "",
  coverageAmount: "",
  premiumAmount: "",
  validityMonths: "12",
  active: true,
};
const hasAtMostTwoDecimals = (value) =>
  /^\d+(?:\.\d{1,2})?$/.test(String(value));

function ManageInsurancePlans() {
  const [form, setForm] = useState(empty);
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const company = await getCompanyByUserId(getUserId());
      setForm((current) => ({ ...current, companyId: company.companyId }));
      setPlans(await getPlansByCompanyId(company.companyId));
    } catch (loadError) {
      setError(
        loadError.userMessage ||
          "Complete your company profile before managing plans.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleChange = (event) => {
    setError("");
    setMessage("");
    setForm((current) => ({
      ...current,
      [event.target.name]:
        event.target.type === "checkbox"
          ? event.target.checked
          : event.target.value,
    }));
  };

  const validate = () => {
    const name = form.planName.trim();
    const description = form.planDescription.trim();
    const coverageText = String(form.coverageAmount).trim();
    const premiumText = String(form.premiumAmount).trim();
    const coverage = Number(coverageText);
    const premium = Number(premiumText);
    const months = Number(form.validityMonths);

    if (!form.companyId) return "Complete the insurance company profile first.";
    if (name.length < 3 || name.length > 100) {
      return "Plan name must be between 3 and 100 characters.";
    }
    if (description.length < 10 || description.length > 500) {
      return "Plan description must be between 10 and 500 characters.";
    }
    if (!coverageText || !Number.isFinite(coverage) || coverage <= 0) {
      return "Coverage amount must be greater than zero.";
    }
    if (!hasAtMostTwoDecimals(coverageText) || coverage > MAX_MONEY) {
      return "Coverage amount must be within range and use at most 2 decimal places.";
    }
    if (!premiumText || !Number.isFinite(premium) || premium <= 0) {
      return "Premium amount must be greater than zero.";
    }
    if (!hasAtMostTwoDecimals(premiumText) || premium > MAX_MONEY) {
      return "Premium amount must be within range and use at most 2 decimal places.";
    }
    if (premium > coverage) {
      return "Premium amount cannot exceed coverage amount.";
    }
    if (!Number.isInteger(months) || months < 1 || months > 120) {
      return "Validity months must be a whole number between 1 and 120.";
    }
    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");

    const validationMessage = validate();
    if (validationMessage) {
      setError(validationMessage);
      return;
    }

    setSaving(true);
    try {
      await createPlan({
        ...form,
        companyId: Number(form.companyId),
        planName: form.planName.trim(),
        planDescription: form.planDescription.trim(),
        coverageAmount: Number(form.coverageAmount),
        premiumAmount: Number(form.premiumAmount),
        validityMonths: Number(form.validityMonths),
      });
      setMessage("Plan created successfully.");
      setForm({ ...empty, companyId: form.companyId });
      await load();
    } catch (submitError) {
      setError(submitError.userMessage || "Unable to create plan.");
    } finally {
      setSaving(false);
    }
  };

  const toggle = async (plan) => {
    setError("");
    setMessage("");
    try {
      if (plan.active) await deactivatePlan(plan.planId);
      else await activatePlan(plan.planId);
      setMessage(plan.active ? "Plan deactivated." : "Plan activated.");
      await load();
    } catch (toggleError) {
      setError(toggleError.userMessage || "Unable to update plan status.");
    }
  };

  const handleDelete = async (plan) => {
    if (!window.confirm(`Delete plan "${plan.planName}"?`)) return;

    setError("");
    setMessage("");
    try {
      await deletePlan(plan.planId);
      setMessage("Plan deleted successfully.");
      await load();
    } catch (deleteError) {
      setError(deleteError.userMessage || "Unable to delete plan.");
    }
  };

  return (
    <Layout
      title="Manage Insurance Plans"
      subtitle="Coverage and premium amounts must be positive, use no more than 2 decimal places, and keep the premium at or below the coverage amount."
    >
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>

      <div className="card page-card p-4 mb-4">
        <h5>Add Plan</h5>
        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label" htmlFor="companyId">
                Company ID
              </label>
              <input
                id="companyId"
                className="form-control"
                name="companyId"
                value={form.companyId}
                readOnly
              />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label" htmlFor="planName">
                Plan Name
              </label>
              <input
                id="planName"
                className="form-control"
                name="planName"
                value={form.planName}
                onChange={handleChange}
                minLength="3"
                maxLength="100"
                required
                disabled={saving}
              />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label" htmlFor="validityMonths">
                Validity Months
              </label>
              <input
                id="validityMonths"
                className="form-control"
                type="number"
                name="validityMonths"
                value={form.validityMonths}
                onChange={handleChange}
                required
                min="1"
                max="120"
                step="1"
                disabled={saving}
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="planDescription">
              Description
            </label>
            <textarea
              id="planDescription"
              className="form-control"
              name="planDescription"
              value={form.planDescription}
              onChange={handleChange}
              required
              minLength="10"
              maxLength="500"
              rows="3"
              disabled={saving}
            />
            <div className="form-text">
              {form.planDescription.trim().length}/500 characters
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label" htmlFor="coverageAmount">
                Coverage Amount
              </label>
              <input
                id="coverageAmount"
                className="form-control"
                type="number"
                name="coverageAmount"
                value={form.coverageAmount}
                onChange={handleChange}
                required
                min="0.01"
                max={MAX_MONEY}
                step="0.01"
                inputMode="decimal"
                disabled={saving}
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label" htmlFor="premiumAmount">
                Premium Amount
              </label>
              <input
                id="premiumAmount"
                className="form-control"
                type="number"
                name="premiumAmount"
                value={form.premiumAmount}
                onChange={handleChange}
                required
                min="0.01"
                max={MAX_MONEY}
                step="0.01"
                inputMode="decimal"
                disabled={saving}
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={saving || loading || !form.companyId}
          >
            {saving ? "Adding…" : "Add Plan"}
          </button>
        </form>
      </div>

      <div className="card page-card p-4">
        <Table
          data={plans}
          columns={[
            { key: "planId", label: "ID" },
            { key: "planName", label: "Name" },
            {
              key: "coverageAmount",
              label: "Coverage",
              render: (row) => money(row.coverageAmount),
            },
            {
              key: "premiumAmount",
              label: "Premium",
              render: (row) => money(row.premiumAmount),
            },
            { key: "validityMonths", label: "Months" },
            {
              key: "active",
              label: "Active",
              render: (row) => (row.active ? "Yes" : "No"),
            },
          ]}
          actions={(row) => (
            <div className="d-flex gap-2">
              <button
                type="button"
                className="btn btn-sm btn-outline-primary"
                onClick={() => toggle(row)}
                disabled={loading || saving}
              >
                {row.active ? "Deactivate" : "Activate"}
              </button>
              <button
                type="button"
                className="btn btn-sm btn-outline-danger"
                onClick={() => handleDelete(row)}
                disabled={loading || saving}
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

export default ManageInsurancePlans;
