const MAX_FILES = 5;
const MAX_FILE_SIZE = 5 * 1024 * 1024;
const ALLOWED_TYPES = new Set([
  'application/pdf',
  'image/jpeg',
  'image/png',
]);

function ClaimDocumentInput({ files = [], onChange, required = true }) {
  const handleChange = (event) => {
    const selected = Array.from(event.target.files || []);
    let message = '';

    if (required && selected.length === 0) {
      message = 'At least one medical document is required.';
    } else if (selected.length > MAX_FILES) {
      message = 'You can upload a maximum of 5 medical documents.';
    } else if (selected.some((file) => !ALLOWED_TYPES.has(file.type))) {
      message = 'Only PDF, JPG, JPEG, and PNG medical documents are allowed.';
    } else if (selected.some((file) => file.size > MAX_FILE_SIZE)) {
      message = 'Each medical document must be 5 MB or smaller.';
    }

    event.target.setCustomValidity(message);
    onChange(message ? [] : selected, message);
  };

  return (
    <div className="mb-3">
      <label className="form-label" htmlFor="claimDocuments">
        Medical Documents
      </label>
      <input
        id="claimDocuments"
        className="form-control"
        type="file"
        name="documents"
        accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
        multiple
        required={required}
        onChange={handleChange}
      />
      <div className="form-text">
        Upload 1–5 PDF, JPG, JPEG, or PNG files. Maximum 5 MB per file.
      </div>
      {files.length > 0 && (
        <ul className="small mt-2 mb-0">
          {files.map((file) => (
            <li key={`${file.name}-${file.size}`}>
              {file.name} ({(file.size / 1024).toFixed(1)} KB)
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default ClaimDocumentInput;
