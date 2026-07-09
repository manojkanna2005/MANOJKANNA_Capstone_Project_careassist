import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import {
  getAllCompanies,
  deleteCompany,
} from "../../services/insuranceCompanyService.js";

function ManageInsuranceCompanies() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const load = () =>
    getAllCompanies()
      .then(setItems)
      .catch((err) => setError(err.userMessage || "Unable to load companies"));
  useEffect(() => {
    load();
  }, []);
  const remove = async (id) => {
    if (!confirm("Delete this insurance company profile?")) return;
    await deleteCompany(id);
    setMessage("Company deleted.");
    load();
  };
  return (
    <Layout title="Manage Insurance Companies">
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={items}
          columns={[
            { key: "companyId", label: "Company ID" },
            { key: "userId", label: "User ID" },
            { key: "companyName", label: "Company" },
            { key: "registrationNumber", label: "Reg No" },
            { key: "contactEmail", label: "Email" },
          ]}
          actions={(row) => (
            <button
              className="btn btn-sm btn-outline-danger"
              onClick={() => remove(row.companyId)}
            >
              Delete
            </button>
          )}
        />
      </div>
    </Layout>
  );
}
export default ManageInsuranceCompanies;
