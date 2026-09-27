import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import Loading from '../components/Loading';
import { useToast } from '../context/ToastContext';
import * as fileService from '../services/fileService';

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function Files() {
  const { workspaceId } = useParams();
  const { showToast } = useToast();
  const inputRef = useRef(null);

  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    document.title = 'Files — VectaSheet';
    load();
  }, [workspaceId]);

  async function load() {
    setLoading(true);
    try {
      const data = await fileService.listFiles(workspaceId);
      setFiles(data);
    } catch (err) {
      showToast(err.message || 'Could not load files', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleFileChange(e) {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    setUploading(true);
    try {
      const uploaded = await fileService.uploadFile(workspaceId, file);
      setFiles((prev) => [uploaded, ...prev]);
      showToast('File uploaded', 'success');
    } catch (err) {
      showToast(err.message || 'Could not upload file', 'error');
    } finally {
      setUploading(false);
    }
  }

  async function handleDownload(f) {
    try {
      await fileService.downloadFile(f.id, f.originalFilename);
    } catch (err) {
      showToast(err.message || 'Could not download file', 'error');
    }
  }

  async function handleDelete(f) {
    if (!window.confirm(`Delete "${f.originalFilename}"?`)) return;
    try {
      await fileService.deleteFile(f.id);
      setFiles((prev) => prev.filter((x) => x.id !== f.id));
    } catch (err) {
      showToast(err.message || 'Could not delete file', 'error');
    }
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }}>
        <h1 style={{ fontSize: 20, fontWeight: 700 }}>Files</h1>
        <input ref={inputRef} type="file" onChange={handleFileChange} style={{ display: 'none' }} />
        <button className="btn btn-primary" onClick={() => inputRef.current?.click()} disabled={uploading}>
          {uploading ? 'Uploading…' : '+ Upload File'}
        </button>
      </div>

      {loading && <Loading />}

      {!loading && files.length === 0 && (
        <EmptyState
          title="No files yet."
          description="Upload a file to store it with this workspace. Files up to 25 MB are supported."
          actionLabel="Upload File"
          onAction={() => inputRef.current?.click()}
        />
      )}

      {!loading && files.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {files.map((f) => (
            <div key={f.id} className="card" style={{ padding: '12px 14px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 4 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, minWidth: 0 }}>
                <span style={{ fontSize: 16 }}>📎</span>
                <div style={{ minWidth: 0 }}>
                  <div style={{ fontSize: 13.5, fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {f.originalFilename}
                  </div>
                  <div style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>
                    {formatSize(f.sizeBytes)} · {new Date(f.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </div>
              <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                <button className="btn btn-secondary" style={{ padding: '5px 10px', fontSize: 12 }} onClick={() => handleDownload(f)}>Download</button>
                <button className="btn btn-danger" style={{ padding: '5px 10px', fontSize: 12 }} onClick={() => handleDelete(f)}>Delete</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
