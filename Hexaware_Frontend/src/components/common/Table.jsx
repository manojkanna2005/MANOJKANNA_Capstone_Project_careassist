function Table({ data = [], columns = [], actions, emptyMessage = 'No records found' }) {
  if (!data.length) {
    return <div className="alert alert-light border">{emptyMessage}</div>;
  }

  return (
    <div className="table-responsive">
      <table className="table table-striped table-hover align-middle">
        <thead className="table-primary">
          <tr>
            {columns.map((column) => (
              <th key={column.key}>{column.label}</th>
            ))}
            {actions && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {data.map((row, index) => (
            <tr key={row.id || row.userId || row.patientId || row.providerId || row.companyId || row.planId || row.invoiceId || row.claimId || row.paymentId || row.notificationId || row.enrollmentId || index}>
              {columns.map((column) => (
                <td key={column.key}>{column.render ? column.render(row) : String(row[column.key] ?? '-')}</td>
              ))}
              {actions && <td>{actions(row)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Table;
