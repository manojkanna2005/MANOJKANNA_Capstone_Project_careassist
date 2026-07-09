import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import { getAllPayments } from "../../services/claimPaymentService.js";
import { money } from "../../utils/date.js";

function AllPayments() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    getAllPayments()
      .then(setItems)
      .catch((err) => setError(err.userMessage || "Unable to load payments"));
  }, []);

  return (
    <Layout title="Insurance Claim Payments" subtitle="Processed payments are immutable financial records.">
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={items}
          columns={[
            { key: "paymentId", label: "Payment ID" },
            { key: "claimId", label: "Claim ID" },
            { key: "invoiceNumber", label: "Invoice" },
            { key: "patientName", label: "Patient" },
            { key: "companyName", label: "Insurance Company" },
            { key: "approvedAmount", label: "Approved", render: (row) => money(row.approvedAmount) },
            { key: "paymentAmount", label: "Insurance Paid", render: (row) => money(row.paymentAmount) },
            { key: "paymentDate", label: "Date" },
            { key: "paymentMode", label: "Mode" },
            { key: "transactionReference", label: "Reference" },
          ]}
        />
      </div>
    </Layout>
  );
}

export default AllPayments;
