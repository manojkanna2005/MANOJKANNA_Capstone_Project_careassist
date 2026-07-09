import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import {
  downloadInvoicePdf,
  emailInvoicePdf,
  getMyInvoices,
} from "../../services/invoiceService.js";
import { money } from "../../utils/date.js";
import {
  formatInvoiceDate,
  getInvoiceBadgeClass,
  getInvoicePaymentState,
  getPaymentMethodLabel,
  normalizeInvoiceStatus,
  sortInvoicesNewestFirst,
} from "../../utils/invoice.js";

const FILTERS = [
  ["ALL", "All invoices"],
  ["ELIGIBLE", "Ready to pay"],
  ["WAITING", "Not payable yet"],
  ["PAID", "Paid"],
];

function MyInvoices() {
  const [searchParams, setSearchParams] = useSearchParams();
  const requestedFilter = String(searchParams.get("filter") || "ALL").toUpperCase();
  const initialFilter = FILTERS.some(([key]) => key === requestedFilter)
    ? requestedFilter
    : "ALL";

  const [invoices, setInvoices] = useState([]);
  const [activeFilter, setActiveFilter] = useState(initialFilter);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [activeAction, setActiveAction] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadInvoices = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const data = await getMyInvoices();
      setInvoices(sortInvoicesNewestFirst(Array.isArray(data) ? data : []));
    } catch (err) {
      setError(err.userMessage || "Unable to load your invoices.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadInvoices();
  }, [loadInvoices]);

  const invoiceGroups = useMemo(() => {
    const groups = { ALL: invoices, ELIGIBLE: [], WAITING: [], PAID: [] };

    invoices.forEach((invoice) => {
      groups[getInvoicePaymentState(invoice).key].push(invoice);
    });

    return groups;
  }, [invoices]);

  const visibleInvoices = useMemo(() => {
    const term = search.trim().toLowerCase();
    const rows = invoiceGroups[activeFilter] || [];

    if (!term) return rows;

    return rows.filter((invoice) =>
      [
        invoice.invoiceNumber,
        invoice.invoiceId,
        invoice.providerName,
        invoice.claimId,
        invoice.claimStatus,
        invoice.status,
      ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term)),
    );
  }, [activeFilter, invoiceGroups, search]);

  const totalOutstanding = useMemo(
    () =>
      invoiceGroups.ELIGIBLE.reduce(
        (total, invoice) => total + Number(invoice.remainingAmount ?? invoice.totalAmount ?? 0),
        0,
      ),
    [invoiceGroups],
  );

  const runInvoiceAction = async (type, invoiceId) => {
    setMessage("");
    setError("");
    setActiveAction({ type, invoiceId });

    try {
      if (type === "download") {
        await downloadInvoicePdf(invoiceId);
      } else {
        await emailInvoicePdf(invoiceId);
        setMessage("Invoice PDF was sent to your registered email address.");
      }
    } catch (err) {
      setError(
        err.userMessage ||
          (type === "download"
            ? "Unable to download the invoice PDF."
            : "Unable to email the invoice PDF."),
      );
    } finally {
      setActiveAction(null);
    }
  };

  const isRunning = (type, invoiceId) =>
    activeAction?.type === type && activeAction?.invoiceId === invoiceId;

  return (
    <Layout
      title="My Invoices"
      subtitle="Review your bills, claim progress, payment eligibility, and receipts."
    >
      <div className="patient-invoice-page">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        <section className="invoice-summary-grid" aria-label="Invoice summary">
          <article className="invoice-summary-card">
            <span className="invoice-summary-label">Total invoices</span>
            <strong>{invoices.length}</strong>
            <small>Invoices linked to your account</small>
          </article>
          <article className="invoice-summary-card invoice-summary-card--payable">
            <span className="invoice-summary-label">Ready to pay</span>
            <strong>{invoiceGroups.ELIGIBLE.length}</strong>
            <small>{money(totalOutstanding)} currently payable</small>
          </article>
          <article className="invoice-summary-card">
            <span className="invoice-summary-label">Not payable yet</span>
            <strong>{invoiceGroups.WAITING.length}</strong>
            <small>Waiting for claim action or unavailable</small>
          </article>
          <article className="invoice-summary-card invoice-summary-card--paid">
            <span className="invoice-summary-label">Paid</span>
            <strong>{invoiceGroups.PAID.length}</strong>
            <small>Completed billing records</small>
          </article>
        </section>

        <section className="invoice-toolbar" aria-label="Invoice filters">
          <div className="invoice-filter-group">
            {FILTERS.map(([key, label]) => (
              <button
                key={key}
                type="button"
                className={`invoice-filter-button ${
                  activeFilter === key ? "active" : ""
                }`}
                onClick={() => {
                  setActiveFilter(key);
                  const next = new URLSearchParams(searchParams);
                  next.set("filter", key);
                  setSearchParams(next, { replace: true });
                }}
              >
                {label}
                <span>{invoiceGroups[key]?.length || 0}</span>
              </button>
            ))}
          </div>

          <div className="invoice-toolbar-actions">
            <label className="invoice-search">
              <span className="visually-hidden">Search invoices</span>
              <input
                type="search"
                className="form-control"
                placeholder="Search invoice, provider, or claim"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
            </label>
            <button
              type="button"
              className="btn btn-outline-secondary"
              onClick={loadInvoices}
              disabled={loading}
            >
              {loading ? "Refreshing…" : "Refresh"}
            </button>
          </div>
        </section>

        {loading ? (
          <div className="invoice-loading" role="status" aria-live="polite">
            <div className="spinner-border text-primary" />
            <span>Loading your invoices…</span>
          </div>
        ) : visibleInvoices.length === 0 ? (
          <div className="invoice-empty-state">
            <h5>No matching invoices</h5>
            <p>
              {search
                ? "Try a different search term or clear the current filter."
                : activeFilter === "ALL"
                  ? "No invoices are linked to your patient account yet."
                  : "There are no invoices in this section."}
            </p>
          </div>
        ) : (
          <div className="invoice-list">
            {visibleInvoices.map((invoice) => {
              const paymentState = getInvoicePaymentState(invoice);
              const invoiceStatus = normalizeInvoiceStatus(invoice.status);

              return (
                <article className="invoice-card" key={invoice.invoiceId}>
                  <header className="invoice-card-header">
                    <div>
                      <div className="invoice-card-eyebrow">
                        Invoice #{invoice.invoiceId}
                      </div>
                      <h5>
                        {invoice.invoiceNumber || `Invoice ${invoice.invoiceId}`}
                      </h5>
                      <p>{invoice.providerName || "Healthcare provider"}</p>
                    </div>
                    <div className="invoice-card-amount">
                      <span>
                        {paymentState.key === "PAID"
                          ? "Paid total"
                          : invoice.insurancePaymentProcessed
                            ? "Patient balance"
                            : "Amount to pay"}
                      </span>
                      <strong>
                        {money(
                          paymentState.key === "PAID"
                            ? invoice.totalAmount
                            : invoice.remainingAmount ?? invoice.totalAmount,
                        )}
                      </strong>
                      {paymentState.key === "PAID" ? (
                        <small>
                          Completed {formatInvoiceDate(
                            invoice.paymentDate || invoice.insurancePaymentDate,
                            true,
                          )}
                        </small>
                      ) : invoice.insurancePaymentProcessed ? (
                        <small>of {money(invoice.totalAmount)} total</small>
                      ) : null}
                    </div>
                  </header>

                  <div className="invoice-status-row">
                    <span className={`badge ${getInvoiceBadgeClass(invoiceStatus)}`}>
                      {invoiceStatus || "UNKNOWN"}
                    </span>
                    <span
                      className={`invoice-payment-state invoice-payment-state--${paymentState.key.toLowerCase()}`}
                    >
                      {paymentState.label}
                    </span>
                    <span className="invoice-date-note">
                      Due {formatInvoiceDate(invoice.dueDate)}
                    </span>
                  </div>

                  <div className="invoice-detail-grid">
                    <div>
                      <span>Invoice date</span>
                      <strong>{formatInvoiceDate(invoice.invoiceDate)}</strong>
                    </div>
                    <div>
                      <span>Claim ID</span>
                      <strong>{invoice.claimId || "Not created"}</strong>
                    </div>
                    <div>
                      <span>Claim status</span>
                      <strong>{normalizeInvoiceStatus(invoice.claimStatus) || "No claim"}</strong>
                    </div>
                    <div>
                      <span>Insurance approved</span>
                      <strong>{money(invoice.approvedAmount)}</strong>
                    </div>
                    <div>
                      <span>Insurance paid</span>
                      <strong>{money(invoice.insurancePaidAmount)}</strong>
                    </div>
                    <div>
                      <span>Patient paid</span>
                      <strong>{money(invoice.patientPaidAmount)}</strong>
                    </div>
                    <div>
                      <span>Remaining balance</span>
                      <strong>{money(invoice.remainingAmount ?? invoice.totalAmount)}</strong>
                    </div>
                  </div>

                  <div
                    className={`invoice-eligibility-note invoice-eligibility-note--${paymentState.key.toLowerCase()}`}
                  >
                    <strong>{paymentState.label}</strong>
                    <span>{paymentState.reason}</span>
                  </div>

                  <details className="invoice-breakdown">
                    <summary>View amount breakdown and payment details</summary>
                    <div className="invoice-breakdown-content">
                      <div className="invoice-breakdown-column">
                        <h6>Amount breakdown</h6>
                        <dl>
                          <div><dt>Consultation</dt><dd>{money(invoice.consultationFee)}</dd></div>
                          <div><dt>Diagnostic tests</dt><dd>{money(invoice.diagnosticTestsFee)}</dd></div>
                          <div><dt>Diagnostic scans</dt><dd>{money(invoice.diagnosticScanFee)}</dd></div>
                          <div><dt>Medications</dt><dd>{money(invoice.medicationsFee)}</dd></div>
                          <div><dt>Tax</dt><dd>{money(invoice.taxAmount)}</dd></div>
                          <div className="invoice-breakdown-total"><dt>Invoice total</dt><dd>{money(invoice.totalAmount)}</dd></div>
                          <div><dt>Insurance approved</dt><dd>{money(invoice.approvedAmount)}</dd></div>
                          <div><dt>Insurance paid</dt><dd>- {money(invoice.insurancePaidAmount)}</dd></div>
                          <div><dt>Patient paid</dt><dd>- {money(invoice.patientPaidAmount)}</dd></div>
                          <div className="invoice-breakdown-total"><dt>Remaining</dt><dd>{money(invoice.remainingAmount ?? invoice.totalAmount)}</dd></div>
                        </dl>
                      </div>

                      <div className="invoice-breakdown-column">
                        <h6>Payment details</h6>
                        <dl>
                          <div><dt>Insurance payment</dt><dd>{invoice.insurancePaymentProcessed ? money(invoice.insurancePaidAmount) : "Not processed"}</dd></div>
                          <div><dt>Insurance reference</dt><dd className="text-break">{invoice.insuranceTransactionReference || "—"}</dd></div>
                          <div><dt>Patient payment</dt><dd>{invoice.paymentId ? money(invoice.paymentAmount) : "Not processed"}</dd></div>
                          <div><dt>Patient method</dt><dd>{invoice.paymentId ? getPaymentMethodLabel(invoice.paymentMethod) : "—"}</dd></div>
                          <div><dt>Patient reference</dt><dd className="text-break">{invoice.transactionReference || "—"}</dd></div>
                          <div><dt>Final status</dt><dd>{normalizeInvoiceStatus(invoice.paymentStatus || invoice.status) || "—"}</dd></div>
                        </dl>
                      </div>
                    </div>
                  </details>

                  <footer className="invoice-card-actions">
                    {paymentState.eligible && (
                      <Link
                        className="btn btn-success"
                        to={`/patient/pay-invoice/${invoice.invoiceId}`}
                      >
                        {invoice.insurancePaymentProcessed
                          ? "Pay remaining balance"
                          : "Pay full amount"}
                      </Link>
                    )}
                    {paymentState.eligible && !invoice.claimId && (
                      <Link
                        className="btn btn-outline-success"
                        to="/patient/submit-claim"
                      >
                        Use insurance
                      </Link>
                    )}
                    <button
                      className="btn btn-outline-primary"
                      type="button"
                      onClick={() => runInvoiceAction("download", invoice.invoiceId)}
                      disabled={Boolean(activeAction)}
                    >
                      {isRunning("download", invoice.invoiceId)
                        ? "Downloading…"
                        : "Download PDF"}
                    </button>
                    <button
                      className="btn btn-outline-secondary"
                      type="button"
                      onClick={() => runInvoiceAction("email", invoice.invoiceId)}
                      disabled={Boolean(activeAction)}
                    >
                      {isRunning("email", invoice.invoiceId)
                        ? "Sending…"
                        : "Email PDF"}
                    </button>
                  </footer>
                </article>
              );
            })}
          </div>
        )}
      </div>
    </Layout>
  );
}

export default MyInvoices;
