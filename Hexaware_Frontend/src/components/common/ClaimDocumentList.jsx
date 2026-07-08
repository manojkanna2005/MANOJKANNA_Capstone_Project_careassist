import { downloadClaimDocument } from '../../services/claimService.js';

function ClaimDocumentList({ documents = [], onError }) {
  const download = async (metadata) => {
    try {
      await downloadClaimDocument(metadata);
    } catch (error) {
      onError?.(error.userMessage || 'Unable to download medical document.');
    }
  };

  if (!documents.length) {
    return <div className="alert alert-light border">No medical documents attached.</div>;
  }

  return (
    <div className="list-group">
      {documents.map((metadata) => (
        <div
          key={metadata.documentId}
          className="list-group-item d-flex justify-content-between align-items-center gap-3"
        >
          <div>
            <div className="fw-semibold">{metadata.originalFileName}</div>
            <small className="text-muted">
              {metadata.contentType} · {(Number(metadata.fileSize || 0) / 1024).toFixed(1)} KB
            </small>
          </div>
          <button
            type="button"
            className="btn btn-sm btn-outline-primary"
            onClick={() => download(metadata)}
          >
            Download
          </button>
        </div>
      ))}
    </div>
  );
}

export default ClaimDocumentList;
