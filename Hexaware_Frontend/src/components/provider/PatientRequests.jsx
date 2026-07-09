import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import { getUserId } from "../../utils/auth.js";
import { getNotificationsByUserId } from "../../services/emailNotificationService.js";

function PatientRequests() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");
  useEffect(() => {
    getNotificationsByUserId(getUserId())
      .then(setItems)
      .catch((err) => setError(err.userMessage || "Unable to load requests"));
  }, []);
  return (
    <Layout
      title="Patient Requests"
      subtitle="Invoice requests received as notifications."
    >
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={items}
          columns={[
            { key: "notificationId", label: "ID" },
            { key: "subject", label: "Subject" },
            { key: "message", label: "Message" },
            { key: "sentAt", label: "Sent At" },
            { key: "status", label: "Status" },
          ]}
        />
      </div>
    </Layout>
  );
}
export default PatientRequests;
