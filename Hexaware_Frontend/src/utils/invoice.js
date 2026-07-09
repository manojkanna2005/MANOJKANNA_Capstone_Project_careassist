export const normalizeInvoiceStatus = (value) =>
  String(value || "")
    .trim()
    .toUpperCase();

export const formatInvoiceDate = (value, includeTime = false) => {
  if (!value) return "—";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);

  return includeTime
    ? date.toLocaleString("en-IN", {
        dateStyle: "medium",
        timeStyle: "short",
      })
    : date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      });
};

export const getInvoiceBadgeClass = (status) => {
  const value = normalizeInvoiceStatus(status);

  if (["PAID", "APPROVED", "SUCCESS"].includes(value)) {
    return "text-bg-success";
  }
  if (["REJECTED", "DENIED", "CANCELLED", "FAILED"].includes(value)) {
    return "text-bg-danger";
  }
  if (["OVERDUE"].includes(value)) {
    return "text-bg-danger";
  }
  if (["PENDING", "UNPAID", "SUBMITTED", "UNDER_REVIEW"].includes(value)) {
    return "text-bg-warning";
  }

  return "text-bg-secondary";
};

export const getPaymentMethodLabel = (method) => {
  const labels = {
    CARD: "Card",
    UPI: "UPI",
    NET_BANKING: "Net banking",
    CASH: "Cash",
  };

  return labels[normalizeInvoiceStatus(method)] || method || "—";
};

export const getInvoicePaymentState = (invoice = {}) => {
  const invoiceStatus = normalizeInvoiceStatus(
    invoice.status || invoice.paidStatus,
  );
  const claimStatus = normalizeInvoiceStatus(invoice.claimStatus);
  const alreadyPaid =
    Boolean(invoice.paymentId) ||
    invoiceStatus === "PAID" ||
    Number(invoice.remainingAmount ?? invoice.totalAmount ?? 0) <= 0;

  if (alreadyPaid) {
    return {
      key: "PAID",
      eligible: false,
      label: "Paid",
      reason: "This invoice has already been paid. No insurance claim is needed.",
    };
  }

  if (typeof invoice.paymentEligible === "boolean") {
    return invoice.paymentEligible
      ? {
          key: "ELIGIBLE",
          eligible: true,
          label: invoice.insurancePaymentProcessed
            ? "Pay remaining balance"
            : "Pay full amount",
          reason:
            invoice.paymentEligibilityReason ||
            (invoice.insurancePaymentProcessed
              ? "Insurance has paid its approved amount. Pay the remaining balance."
              : "No active insurance claim is blocking payment. Pay the full invoice directly."),
        }
      : {
          key: "WAITING",
          eligible: false,
          label: "Payment unavailable",
          reason:
            invoice.paymentEligibilityReason ||
            "This invoice is not currently eligible for payment.",
        };
  }

  if (invoiceStatus === "CANCELLED") {
    return {
      key: "WAITING",
      eligible: false,
      label: "Cancelled",
      reason: "A cancelled invoice cannot be paid.",
    };
  }

  if (!invoice.claimId) {
    return {
      key: "ELIGIBLE",
      eligible: true,
      label: "Pay full amount",
      reason: "You can pay this invoice directly without submitting an insurance claim.",
    };
  }

  if (["PENDING", "SUBMITTED", "UNDER_REVIEW"].includes(claimStatus)) {
    return {
      key: "WAITING",
      eligible: false,
      label: "Claim in progress",
      reason: "Wait for the insurance decision before paying to avoid duplicate settlement.",
    };
  }

  if (["REJECTED", "DENIED", "CANCELLED", "WITHDRAWN"].includes(claimStatus)) {
    return {
      key: "ELIGIBLE",
      eligible: true,
      label: "Pay full amount",
      reason: "The claim will not pay this invoice. You can pay the full remaining amount directly.",
    };
  }

  if (claimStatus === "APPROVED") {
    if (!invoice.insurancePaymentProcessed && !invoice.insurancePaymentId) {
      return {
        key: "WAITING",
        eligible: false,
        label: "Awaiting insurance payment",
        reason: "The insurer must process the approved amount before you pay the balance.",
      };
    }

    return {
      key: "ELIGIBLE",
      eligible: true,
      label: "Pay remaining balance",
      reason: "Insurance has paid the approved amount. Pay only the remaining balance.",
    };
  }

  if (!["PENDING", "UNPAID", "OVERDUE"].includes(invoiceStatus)) {
    return {
      key: "WAITING",
      eligible: false,
      label: "Payment unavailable",
      reason: "The current invoice status does not allow payment.",
    };
  }

  return {
    key: "WAITING",
    eligible: false,
    label: "Payment unavailable",
    reason: `Claim status is ${claimStatus || "unknown"}.`,
  };
};

export const sortInvoicesNewestFirst = (rows = []) =>
  [...rows].sort((first, second) => {
    const firstDate = new Date(first.invoiceDate || first.createdAt || 0).getTime();
    const secondDate = new Date(second.invoiceDate || second.createdAt || 0).getTime();

    if (secondDate !== firstDate) return secondDate - firstDate;
    return Number(second.invoiceId || 0) - Number(first.invoiceId || 0);
  });
