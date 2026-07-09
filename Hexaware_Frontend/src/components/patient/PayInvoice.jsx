import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import { getMyInvoices } from "../../services/invoiceService.js";
import { processInvoicePayment } from "../../services/invoicePaymentService.js";
import { money } from "../../utils/date.js";
import {
  formatInvoiceDate,
  getInvoiceBadgeClass,
  getInvoicePaymentState,
  getPaymentMethodLabel,
  normalizeInvoiceStatus,
  sortInvoicesNewestFirst,
} from "../../utils/invoice.js";

const PAYMENT_METHODS = [
  { value: "CARD", label: "Card", help: "Credit or debit card" },
  { value: "UPI", label: "UPI", help: "Use a UPI payment method" },
  { value: "NET_BANKING", label: "Net banking", help: "Pay through online banking" },
  { value: "CASH", label: "Cash", help: "Record payment using cash" },
];

const FILTERS = [
  ["ELIGIBLE", "Ready to pay"],
  ["WAITING", "Not payable yet"],
  ["PAID", "Paid"],
];

function PayInvoice() {
  const { invoiceId } = useParams();
  const autoOpenHandled = useRef(false);
  const [invoices, setInvoices] = useState([]);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState("CARD");
  const [activeFilter, setActiveFilter] = useState("ELIGIBLE");
  const [requestedInvoiceMessage, setRequestedInvoiceMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    autoOpenHandled.current = false;
  }, [invoiceId]);

  const loadInvoices = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const data = await getMyInvoices();
      const rows = sortInvoicesNewestFirst(Array.isArray(data) ? data : []);
      setInvoices(rows);

      if (invoiceId && !autoOpenHandled.current) {
        autoOpenHandled.current = true;
        const requested = rows.find(
          (item) => String(item.invoiceId) === String(invoiceId),
        );

        if (!requested) {
          setRequestedInvoiceMessage(
            "That invoice was not found in your patient account.",
          );
          return;
        }

        const paymentState = getInvoicePaymentState(requested);
        setActiveFilter(paymentState.key);

        if (paymentState.eligible) {
          setSelectedInvoice(requested);
          setRequestedInvoiceMessage("");
        } else {
          setRequestedInvoiceMessage(paymentState.reason);
        }
      }
    } catch (err) {
      setError(err.userMessage || "Unable to load invoices available for payment.");
    } finally {
      setLoading(false);
    }
  }, [invoiceId]);

  useEffect(() => {
    loadInvoices();
  }, [loadInvoices]);

  const groupedInvoices = useMemo(() => {
    const groups = { ELIGIBLE: [], WAITING: [], PAID: [] };

    invoices.forEach((invoice) => {
      groups[getInvoicePaymentState(invoice).key].push(invoice);
    });

    return groups;
  }, [invoices]);

  const visibleInvoices = groupedInvoices[activeFilter] || [];
  const payableTotal = useMemo(
    () =>
      groupedInvoices.ELIGIBLE.reduce(
        (total, invoice) => total + Number(invoice.remainingAmount ?? invoice.totalAmount ?? 0),
        0,
      ),
    [groupedInvoices],
  );

  const openPayment = (invoice) => {
    const paymentState = getInvoicePaymentState(invoice);
    if (!paymentState.eligible) {
      setError(paymentState.reason);
      return;
    }

    setSelectedInvoice(invoice);
    setPaymentMethod("CARD");
    setRequestedInvoiceMessage("");
    setError("");
    setMessage("");
  };

  const closePayment = () => {
    if (!paying) setSelectedInvoice(null);
  };

  const confirmPayment = async () => {
    if (!selectedInvoice || paying) return;

    const paymentState = getInvoicePaymentState(selectedInvoice);
    if (!paymentState.eligible) {
      setSelectedInvoice(null);
      setError(paymentState.reason);
      return;
    }

    setPaying(true);
    setError("");
    setMessage("");

    try {
      const payment = await processInvoicePayment({
        invoiceId: selectedInvoice.invoiceId,
        paymentMethod,
      });

      setSelectedInvoice(null);
      setMessage(
        `Payment completed successfully${
          payment?.transactionReference
            ? `. Reference: ${payment.transactionReference}`
            : "."
        }`,
      );
      setActiveFilter("PAID");
      await loadInvoices();
    } catch (err) {
      setError(err.userMessage || "Payment could not be processed.");
    } finally {
      setPaying(false);
    }
  };

  return (
    <Layout
      title="Pay Invoice"
      subtitle="Pay an unpaid invoice in full without insurance, or pay only the remaining balance after insurance settles an approved claim."
    >
      <div className="patient-invoice-page patient-payment-page">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>
        <Message type="warning">{requestedInvoiceMessage}</Message>

        <section className="payment-overview-card">
          <div>
            <span className="invoice-card-eyebrow">Payment overview</span>
            <h4>{groupedInvoices.ELIGIBLE.length} invoice(s) ready</h4>
            <p>
              Insurance is optional. Direct payments cover the full invoice; insured
              invoices become payable after the insurer settles the approved amount.
            </p>
          </div>
          <div className="payment-overview-total">
            <span>Total currently payable</span>
            <strong>{money(payableTotal)}</strong>
          </div>
        </section>

        <section className="invoice-toolbar" aria-label="Payment filters">
          <div className="invoice-filter-group">
            {FILTERS.map(([key, label]) => (
              <button
                key={key}
                type="button"
                className={`invoice-filter-button ${
                  activeFilter === key ? "active" : ""
                }`}
                onClick={() => setActiveFilter(key)}
              >
                {label}
                <span>{groupedInvoices[key].length}</span>
              </button>
            ))}
          </div>

          <div className="invoice-toolbar-actions">
            <Link to="/patient/invoices" className="btn btn-outline-primary">
              View all invoices
            </Link>
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
            <span>Checking invoice eligibility…</span>
          </div>
        ) : visibleInvoices.length === 0 ? (
          <div className="invoice-empty-state">
            <h5>
              {activeFilter === "ELIGIBLE"
                ? "No invoices are ready for payment"
                : "No invoices in this section"}
            </h5>
            <p>
              {activeFilter === "ELIGIBLE"
                ? "Unclaimed invoices can be paid in full. Insured invoices appear after the insurer pays its approved amount."
                : "Choose another section to review your invoices."}
            </p>
          </div>
        ) : (
          <div className="payment-invoice-grid">
            {visibleInvoices.map((invoice) => {
              const paymentState = getInvoicePaymentState(invoice);
              const invoiceStatus = normalizeInvoiceStatus(invoice.status);

              return (
                <article
                  className={`payment-invoice-card ${
                    String(invoice.invoiceId) === String(invoiceId)
                      ? "payment-invoice-card--requested"
                      : ""
                  }`}
                  key={invoice.invoiceId}
                >
                  <header>
                    <div>
                      <span className="invoice-card-eyebrow">
                        Invoice #{invoice.invoiceId}
                      </span>
                      <h5>
                        {invoice.invoiceNumber || `Invoice ${invoice.invoiceId}`}
                      </h5>
                      <p>{invoice.providerName || "Healthcare provider"}</p>
                    </div>
                    <span className={`badge ${getInvoiceBadgeClass(invoiceStatus)}`}>
                      {invoiceStatus || "UNKNOWN"}
                    </span>
                  </header>

                  <div className="payment-amount-block">
                    <span>{invoice.insurancePaymentProcessed ? "Patient balance" : "Amount to pay"}</span>
                    <strong>{money(invoice.remainingAmount ?? invoice.totalAmount)}</strong>
                  </div>

                  <dl className="payment-invoice-details">
                    <div><dt>Due date</dt><dd>{formatInvoiceDate(invoice.dueDate)}</dd></div>
                    <div><dt>Claim ID</dt><dd>{invoice.claimId || "Not created"}</dd></div>
                    <div><dt>Claim status</dt><dd>{normalizeInvoiceStatus(invoice.claimStatus) || "No claim"}</dd></div>
                    <div><dt>Invoice total</dt><dd>{money(invoice.totalAmount)}</dd></div>
                    <div><dt>Insurance paid</dt><dd>{money(invoice.insurancePaidAmount)}</dd></div>
                    {invoice.paymentId && (
                      <div><dt>Payment method</dt><dd>{getPaymentMethodLabel(invoice.paymentMethod)}</dd></div>
                    )}
                  </dl>

                  <div
                    className={`invoice-eligibility-note invoice-eligibility-note--${paymentState.key.toLowerCase()}`}
                  >
                    <strong>{paymentState.label}</strong>
                    <span>{paymentState.reason}</span>
                  </div>

                  {paymentState.eligible ? (
                    <div className="d-grid gap-2">
                      <button
                        type="button"
                        className="btn btn-success w-100"
                        onClick={() => openPayment(invoice)}
                      >
                        {invoice.insurancePaymentProcessed
                          ? `Pay remaining ${money(invoice.remainingAmount ?? invoice.totalAmount)}`
                          : `Pay full ${money(invoice.remainingAmount ?? invoice.totalAmount)}`}
                      </button>
                      {!invoice.claimId && (
                        <Link
                          to="/patient/submit-claim"
                          className="btn btn-outline-success w-100"
                        >
                          Use insurance instead
                        </Link>
                      )}
                    </div>
                  ) : invoice.paymentId ? (
                    <Link
                      to="/patient/invoices"
                      className="btn btn-outline-secondary w-100"
                    >
                      View receipt details
                    </Link>
                  ) : null}
                </article>
              );
            })}
          </div>
        )}

        {selectedInvoice && (
          <>
            <div
              className="modal d-block invoice-payment-modal"
              tabIndex="-1"
              role="dialog"
              aria-modal="true"
              aria-labelledby="invoice-payment-title"
            >
              <div className="modal-dialog modal-dialog-centered modal-lg">
                <div className="modal-content">
                  <div className="modal-header">
                    <div>
                      <span className="invoice-card-eyebrow">Final confirmation</span>
                      <h5 className="modal-title" id="invoice-payment-title">
                        Pay {selectedInvoice.invoiceNumber || `Invoice #${selectedInvoice.invoiceId}`}
                      </h5>
                    </div>
                    <button
                      type="button"
                      className="btn-close"
                      aria-label="Close"
                      onClick={closePayment}
                      disabled={paying}
                    />
                  </div>

                  <div className="modal-body">
                    <div className="payment-confirmation-summary">
                      <div>
                        <span>Provider</span>
                        <strong>
                          {selectedInvoice.providerName ||
                            selectedInvoice.providerId ||
                            "—"}
                        </strong>
                      </div>
                      <div>
                        <span>Due date</span>
                        <strong>{formatInvoiceDate(selectedInvoice.dueDate)}</strong>
                      </div>
                      <div>
                        <span>Invoice total</span>
                        <strong>{money(selectedInvoice.totalAmount)}</strong>
                      </div>
                      <div>
                        <span>Insurance paid</span>
                        <strong>- {money(selectedInvoice.insurancePaidAmount)}</strong>
                      </div>
                      <div>
                        <span>Payment type</span>
                        <strong>
                          {selectedInvoice.insurancePaymentProcessed
                            ? "Remaining balance after insurance"
                            : "Full direct payment"}
                        </strong>
                      </div>
                      <div className="payment-confirmation-amount">
                        <span>Patient amount to pay</span>
                        <strong>{money(selectedInvoice.remainingAmount ?? selectedInvoice.totalAmount)}</strong>
                      </div>
                    </div>

                    <fieldset className="payment-method-fieldset" disabled={paying}>
                      <legend>Choose payment method</legend>
                      <div className="payment-method-grid">
                        {PAYMENT_METHODS.map((method) => (
                          <label
                            className={`payment-method-option ${
                              paymentMethod === method.value ? "selected" : ""
                            }`}
                            key={method.value}
                          >
                            <input
                              type="radio"
                              name="paymentMethod"
                              value={method.value}
                              checked={paymentMethod === method.value}
                              onChange={(event) =>
                                setPaymentMethod(event.target.value)
                              }
                            />
                            <span>
                              <strong>{method.label}</strong>
                              <small>{method.help}</small>
                            </span>
                          </label>
                        ))}
                      </div>
                    </fieldset>

                    <div className="payment-security-note">
                      <strong>Important</strong>
                      <span>
                        The server recalculates the amount. Without a claim, it charges the full
                        invoice. After an insurance payment, it charges only the remaining balance.
                        A claim under review temporarily blocks payment to prevent duplicate settlement.
                        This project records only the selected method and does not integrate a real gateway.
                      </span>
                    </div>
                  </div>

                  <div className="modal-footer">
                    <button
                      type="button"
                      className="btn btn-outline-secondary"
                      onClick={closePayment}
                      disabled={paying}
                    >
                      Cancel
                    </button>
                    <button
                      type="button"
                      className="btn btn-success"
                      onClick={confirmPayment}
                      disabled={paying}
                    >
                      {paying
                        ? "Processing payment…"
                        : `Confirm ${money(selectedInvoice.remainingAmount ?? selectedInvoice.totalAmount)} payment`}
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <div className="modal-backdrop fade show" />
          </>
        )}
      </div>
    </Layout>
  );
}

export default PayInvoice;
