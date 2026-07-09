import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import {
  getAllProviders,
  deleteProvider,
} from "../../services/healthcareProviderService.js";

function ManageProviders() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const load = () =>
    getAllProviders()
      .then(setItems)
      .catch((err) => setError(err.userMessage || "Unable to load providers"));
  useEffect(() => {
    load();
  }, []);
  const remove = async (id) => {
    if (!confirm("Delete this provider profile?")) return;
    await deleteProvider(id);
    setMessage("Provider deleted.");
    load();
  };
  return (
    <Layout title="Manage Providers">
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={items}
          columns={[
            { key: "providerId", label: "Provider ID" },
            { key: "userId", label: "User ID" },
            { key: "providerName", label: "Name" },
            { key: "specialization", label: "Specialization" },
            { key: "licenseNumber", label: "License" },
          ]}
          actions={(row) => (
            <button
              className="btn btn-sm btn-outline-danger"
              onClick={() => remove(row.providerId)}
            >
              Delete
            </button>
          )}
        />
      </div>
    </Layout>
  );
}
export default ManageProviders;
