import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import { getProviderByUserId } from "../../services/healthcareProviderService.js";
import {
  downloadInvoicePdf,
  emailInvoicePdf,
  getInvoicesByProviderId,
  updateInvoiceStatus,
} from "../../services/invoiceService.js";
import { getUserId } from "../../utils/auth.js";
import { money } from "../../utils/date.js";
import {
  formatInvoiceDate,
  getInvoiceBadgeClass,
  normalizeInvoiceStatus,
  sortInvoicesNewestFirst,
} from "../../utils/invoice.js";

const FILTERS = [
  ["ALL", "All"],
  ["OPEN", "Open"],
  ["PAID", "Paid"],
  ["CANCELLED", "Cancelled"],
];

function isPaidInvoice(invoice) {
  return (
    normalizeInvoiceStatus(invoice.status) === "PAID" ||
    Number(invoice.remainingAmount ?? invoice.totalAmount ?? 0) <= 0
  );
}

function ProviderInvoices() {
  const [searchParams, setSearchParams] = useSearchParams();
  const requestedFilter = String(searchParams.get("filter") || "ALL").toUpperCase();
  const initialFilter = FILTERS.some(([key]) => key === requestedFilter)
    ? requestedFilter
    : "ALL";

  const [items, setItems] = useState([]);
  const [providerId, setProviderId] = useState("");
  const [activeFilter, setActiveFilter] = useState(initialFilter);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busyKey, setBusyKey] = useState("");

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const provider = await getProviderByUserId(getUserId());
      setProviderId(provider.providerId);
      const rows = await getInvoicesByProviderId(provider.providerId);
      setItems(sortInvoicesNewestFirst(Array.isArray(rows) ? rows : []));
    } catch (loadError) {
      setError(loadError.userMessage || "Unable to load invoices.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const grouped = useMemo(() => {
    const result = { ALL: items, OPEN: [], PAID: [], CANCELLED: [] };

    items.forEach((invoice) => {
      const status = normalizeInvoiceStatus(invoice.status);
      if (isPaidInvoice(invoice)) {
        result.PAID.push(invoice);
      } else if (status === "CANCELLED") {
        result.CANCELLED.push(invoice);
      } else {
        result.OPEN.push(invoice);
      }
    });

    return result;
  }, [items]);

  const visibleItems = useMemo(() => {
    const term = search.trim().toLowerCase();
    const rows = grouped[activeFilter] || [];
    if (!term) return rows;

    return rows.filter((invoice) =>
      [
        invoice.invoiceId,
        invoice.invoiceNumber,
        invoice.patientName,
        invoice.claimId,
        invoice.status,
      ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term)),
    );
  }, [activeFilter, grouped, search]);

  const totals = useMemo(
    () => ({
      billed: items.reduce(
        (sum, invoice) => sum + Number(invoice.totalAmount || 0),
        0,
      ),
      paid: grouped.PAID.reduce(
        (sum, invoice) => sum + Number(invoice.totalAmount || 0),
        0,
      ),
      outstanding: grouped.OPEN.reduce(
        (sum, invoice) =>
          sum + Number(invoice.remainingAmount ?? invoice.totalAmount ?? 0),
        0,
      ),
    }),
    [grouped, items],
  );

  const changeFilter = (filter) => {
    setActiveFilter(filter);
    const next = new URLSearchParams(searchParams);
    next.set("filter", filter);
    setSearchParams(next, { replace: true });
  };

  const changeStatus = async (id, status) => {
    setMessage("");
    setError("");
    setBusyKey(`status-${id}`);
    try {
      await updateInvoiceStatus(id, status);
      setMessage("Invoice status updated.");
      await load();
    } catch (statusError) {
      setError(statusError.userMessage || "Unable to update invoice status.");
    } finally {
      setBusyKey("");
    }
  };

  const handleDownload = async (invoiceId) => {
    setMessage("");
    setError("");
    setBusyKey(`pdf-${invoiceId}`);
    try {
      await downloadInvoicePdf(invoiceId);
    } catch (downloadError) {
      setError(downloadError.userMessage || "Unable to download invoice PDF.");
    } finally {
      setBusyKey("");
    }
  };

  const handleEmail = async (invoiceId) => {
    setMessage("");
    setError("");
    setBusyKey(`email-${invoiceId}`);
    try {
      await emailInvoicePdf(invoiceId);
      setMessage("Invoice PDF emailed to the patient.");
    } catch (emailError) {
      setError(emailError.userMessage || "Unable to email invoice PDF.");
    } finally {
      setBusyKey("");
    }
  };

  return (
    <Layout
      title="Provider Invoices"
      subtitle={`Provider ID: ${providerId || "—"}. Open and paid invoices remain available as billing records.`}
    >
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>

      <div className="row g-3 mb-4">
        <div className="col-sm-6 col-xl-3">
          <div className="card page-card p-3 h-100">
            <span className="text-muted">Total invoices</span>
            <strong className="fs-3">{items.length}</strong>
            <small>{money(totals.billed)} billed</small>
          </div>
        </div>
        <div className="col-sm-6 col-xl-3">
          <div className="card page-card p-3 h-100">
            <span className="text-muted">Open invoices</span>
            <strong className="fs-3">{grouped.OPEN.length}</strong>
            <small>{money(totals.outstanding)} outstanding</small>
          </div>
        </div>
        <div className="col-sm-6 col-xl-3">
          <div className="card page-card p-3 h-100 border-success">
            <span className="text-muted">Paid invoices</span>
            <strong className="fs-3 text-success">{grouped.PAID.length}</strong>
            <small>{money(totals.paid)} completed</small>
          </div>
        </div>
        <div className="col-sm-6 col-xl-3">
          <div className="card page-card p-3 h-100">
            <span className="text-muted">Cancelled</span>
            <strong className="fs-3">{grouped.CANCELLED.length}</strong>
            <small>Non-payable records</small>
          </div>
        </div>
      </div>

      <div className="card page-card p-4">
        <div className="alert alert-info mb-3">
          Paid invoices are retained for the provider and patient as read-only
          billing records. Providers cannot manually mark an invoice as paid.
        </div>

        <div className="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-3">
          <div className="btn-group flex-wrap" role="group" aria-label="Invoice filters">
            {FILTERS.map(([key, label]) => (
              <button
                key={key}
                type="button"
                className={`btn ${activeFilter === key ? "btn-primary" : "btn-outline-primary"}`}
                onClick={() => changeFilter(key)}
              >
                {label} ({grouped[key]?.length || 0})
              </button>
            ))}
          </div>

          <div className="d-flex gap-2 flex-wrap">
            <input
              type="search"
              className="form-control"
              style={{ minWidth: "240px" }}
              placeholder="Search patient, invoice, or claim"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />
            <button
              type="button"
              className="btn btn-outline-secondary"
              onClick={load}
              disabled={loading || Boolean(busyKey)}
            >
              {loading ? "Refreshing…" : "Refresh"}
            </button>
          </div>
        </div>

        <Table
          data={visibleItems}
          emptyMessage={
            loading ? "Loading invoices…" : "No invoices match this filter."
          }
          columns={[
            { key: "invoiceId", label: "ID" },
            { key: "patientName", label: "Patient" },
            { key: "invoiceNumber", label: "Number" },
            {
              key: "totalAmount",
              label: "Invoice Total",
              render: (row) => money(row.totalAmount),
            },
            {
              key: "insurancePaidAmount",
              label: "Insurance Paid",
              render: (row) => money(row.insurancePaidAmount || 0),
            },
            {
              key: "patientPaidAmount",
              label: "Patient Paid",
              render: (row) => money(row.patientPaidAmount || 0),
            },
            {
              key: "remainingAmount",
              label: "Balance",
              render: (row) => money(row.remainingAmount ?? row.totalAmount),
            },
            {
              key: "status",
              label: "Status",
              render: (row) => {
                const status = isPaidInvoice(row)
                  ? "PAID"
                  : normalizeInvoiceStatus(row.status) || "UNKNOWN";
                return (
                  <span className={`badge ${getInvoiceBadgeClass(status)}`}>
                    {status}
                  </span>
                );
              },
            },
            {
              key: "completedAt",
              label: "Paid Date",
              render: (row) =>
                isPaidInvoice(row)
                  ? formatInvoiceDate(
                      row.paymentDate || row.insurancePaymentDate,
                      true,
                    )
                  : "—",
            },
          ]}
          actions={(row) => {
            const settled = isPaidInvoice(row);
            return (
              <div className="d-flex flex-wrap gap-2 align-items-center">
                <select
                  className="form-select form-select-sm"
                  value={settled ? "PAID" : normalizeInvoiceStatus(row.status)}
                  disabled={settled || busyKey === `status-${row.invoiceId}`}
                  onChange={(event) =>
                    changeStatus(row.invoiceId, event.target.value)
                  }
                  aria-label={`Update invoice ${row.invoiceNumber} status`}
                >
                  <option value="PENDING">PENDING</option>
                  <option value="UNPAID">UNPAID</option>
                  <option value="OVERDUE">OVERDUE</option>
                  <option value="CANCELLED">CANCELLED</option>
                  {settled && <option value="PAID">PAID</option>}
                </select>
                <button
                  className="btn btn-sm btn-outline-primary"
                  type="button"
                  disabled={Boolean(busyKey)}
                  onClick={() => handleDownload(row.invoiceId)}
                >
                  {busyKey === `pdf-${row.invoiceId}` ? "Preparing…" : "PDF"}
                </button>
                <button
                  className="btn btn-sm btn-outline-secondary"
                  type="button"
                  disabled={Boolean(busyKey)}
                  onClick={() => handleEmail(row.invoiceId)}
                >
                  {busyKey === `email-${row.invoiceId}` ? "Sending…" : "Email"}
                </button>
              </div>
            );
          }}
        />
      </div>
    </Layout>
  );
}

export default ProviderInvoices;
