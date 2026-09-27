import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import * as docSvc from '../services/documentService';
import Loading from '../components/Loading';

const AUTOSAVE_DELAY_MS = 1500;

const TOOLBAR_GROUPS = [
  [
    { cmd: 'undo', label: '↶', title: 'Undo (Ctrl+Z)' },
    { cmd: 'redo', label: '↷', title: 'Redo (Ctrl+Y)' },
  ],
  [
    { cmd: 'formatBlock', value: 'H1', label: 'H1', title: 'Heading 1' },
    { cmd: 'formatBlock', value: 'H2', label: 'H2', title: 'Heading 2' },
    { cmd: 'formatBlock', value: 'H3', label: 'H3', title: 'Heading 3' },
    { cmd: 'formatBlock', value: 'P', label: '¶', title: 'Paragraph' },
  ],
  [
    { cmd: 'bold', label: 'B', title: 'Bold (Ctrl+B)', style: { fontWeight: 700 } },
    { cmd: 'italic', label: 'I', title: 'Italic (Ctrl+I)', style: { fontStyle: 'italic' } },
    { cmd: 'underline', label: 'U', title: 'Underline (Ctrl+U)', style: { textDecoration: 'underline' } },
    { cmd: 'strikeThrough', label: 'S', title: 'Strikethrough', style: { textDecoration: 'line-through' } },
  ],
  [
    { cmd: 'insertUnorderedList', label: '• List', title: 'Bulleted list' },
    { cmd: 'insertOrderedList', label: '1. List', title: 'Numbered list' },
    { cmd: 'formatBlock', value: 'BLOCKQUOTE', label: '" "', title: 'Quote' },
  ],
  [
    { cmd: 'justifyLeft', label: '⟸', title: 'Align left' },
    { cmd: 'justifyCenter', label: '⟺', title: 'Align center' },
    { cmd: 'justifyRight', label: '⟹', title: 'Align right' },
  ],
  [
    { cmd: 'createLink', label: '🔗', title: 'Insert link', needsValue: true },
    { cmd: 'removeFormat', label: 'Tx', title: 'Clear formatting' },
  ],
];

export default function DocumentEditor() {
  const { workspaceId, documentId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showToast } = useToast();

  const [doc, setDoc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saveStatus, setSaveStatus] = useState('Saved');
  const [title, setTitle] = useState('');
  const [wordCount, setWordCount] = useState(0);
  const [panel, setPanel] = useState(null); // null | 'history' | 'comments'
  const [versions, setVersions] = useState([]);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [quotedSelection, setQuotedSelection] = useState('');

  const editorRef = useRef(null);
  const docVersionRef = useRef(0);
  const saveTimer = useRef(null);
  const conflictedRef = useRef(false);

  useEffect(() => {
    load();
    return () => clearTimeout(saveTimer.current);
  }, [documentId]);

  useEffect(() => {
    if (doc) document.title = `${doc.title || 'Untitled document'} — VectaSheet`;
  }, [doc?.title]);

  async function load() {
    setLoading(true);
    try {
      const d = await docSvc.getDocument(documentId);
      setDoc(d);
      setTitle(d.title || '');
      docVersionRef.current = d.version;
      conflictedRef.current = false;
      if (editorRef.current) {
        editorRef.current.innerHTML = d.contentHtml || '';
      }
      updateWordCount(d.contentHtml || '');
    } catch (err) {
      showToast(err.message || 'Could not load document', 'error');
    } finally {
      setLoading(false);
    }
  }

  // Set initial HTML once the editor DOM node exists (contentEditable is uncontrolled
  // to avoid caret jumps that come from re-rendering innerHTML on every keystroke).
  useEffect(() => {
    if (editorRef.current && doc) {
      editorRef.current.innerHTML = doc.contentHtml || '';
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [doc?.id, loading]);

  function updateWordCount(html) {
    const text = html.replace(/<[^>]+>/g, ' ').replace(/&nbsp;/g, ' ').trim();
    setWordCount(text ? text.split(/\s+/).length : 0);
  }

  const scheduleSave = useCallback(() => {
    if (conflictedRef.current) return;
    setSaveStatus('Saving...');
    clearTimeout(saveTimer.current);
    saveTimer.current = setTimeout(doSave, AUTOSAVE_DELAY_MS);
  }, []);

  async function doSave() {
    const html = editorRef.current?.innerHTML ?? '';
    updateWordCount(html);
    try {
      const saved = await docSvc.saveDocument(documentId, {
        title,
        contentHtml: html,
        expectedVersion: docVersionRef.current,
      });
      docVersionRef.current = saved.version;
      setSaveStatus('Saved');
    } catch (err) {
      if (err.status === 409) {
        conflictedRef.current = true;
        setSaveStatus('Offline');
        showToast('Someone else edited this document. Reload to see the latest version before continuing.', 'error');
      } else {
        setSaveStatus('Offline');
        showToast(err.message || 'Could not save document', 'error');
      }
    }
  }

  function handleTitleChange(e) {
    setTitle(e.target.value);
    scheduleSave();
  }

  function handleEditorInput() {
    scheduleSave();
  }

  function exec(cmd, value) {
    editorRef.current?.focus();
    if (cmd === 'createLink') {
      const url = window.prompt('Link URL', 'https://');
      if (!url) return;
      document.execCommand(cmd, false, url);
    } else {
      document.execCommand(cmd, false, value);
    }
    handleEditorInput();
  }

  function handleSelect() {
    const sel = window.getSelection();
    setQuotedSelection(sel && sel.toString().trim() ? sel.toString().trim().slice(0, 200) : '');
  }

  async function openHistory() {
    setPanel(panel === 'history' ? null : 'history');
    if (panel !== 'history') {
      try {
        const list = await docSvc.listVersions(documentId);
        setVersions(list);
      } catch (err) {
        showToast(err.message || 'Could not load version history', 'error');
      }
    }
  }

  async function openComments() {
    setPanel(panel === 'comments' ? null : 'comments');
    if (panel !== 'comments') {
      try {
        const list = await docSvc.listComments(documentId);
        setComments(list);
      } catch (err) {
        showToast(err.message || 'Could not load comments', 'error');
      }
    }
  }

  async function handleRestore(versionId) {
    if (!window.confirm('Restore this version? Your current content will be saved as a new version too, so nothing is lost.')) return;
    try {
      const restored = await docSvc.restoreVersion(documentId, versionId);
      setDoc(restored);
      setTitle(restored.title);
      docVersionRef.current = restored.version;
      conflictedRef.current = false;
      if (editorRef.current) editorRef.current.innerHTML = restored.contentHtml || '';
      updateWordCount(restored.contentHtml || '');
      setSaveStatus('Saved');
      showToast('Version restored', 'success');
      const list = await docSvc.listVersions(documentId);
      setVersions(list);
    } catch (err) {
      showToast(err.message || 'Could not restore version', 'error');
    }
  }

  async function handleAddComment() {
    if (!newComment.trim()) return;
    try {
      const comment = await docSvc.createComment(documentId, { body: newComment.trim(), quotedText: quotedSelection || null });
      setComments((prev) => [...prev, comment]);
      setNewComment('');
      setQuotedSelection('');
    } catch (err) {
      showToast(err.message || 'Could not add comment', 'error');
    }
  }

  async function handleResolveComment(comment) {
    try {
      const updated = await docSvc.resolveComment(documentId, comment.id, !comment.resolved);
      setComments((prev) => prev.map((c) => (c.id === comment.id ? updated : c)));
    } catch (err) {
      showToast(err.message || 'Could not update comment', 'error');
    }
  }

  async function handleDeleteComment(comment) {
    if (!window.confirm('Delete this comment?')) return;
    try {
      await docSvc.deleteComment(documentId, comment.id);
      setComments((prev) => prev.filter((c) => c.id !== comment.id));
    } catch (err) {
      showToast(err.message || 'Could not delete comment', 'error');
    }
  }

  if (loading) return <Loading full />;
  if (!doc) return null;

  return (
    <div style={{ display: 'flex', height: 'calc(100vh - 90px)', gap: 0 }}>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 10 }}>
          <button className="btn btn-ghost" onClick={() => navigate(`/w/${workspaceId}/docs`)}>← Docs</button>
          <input
            value={title}
            onChange={handleTitleChange}
            placeholder="Untitled document"
            style={{ fontSize: 16, fontWeight: 700, border: 'none', outline: 'none', background: 'transparent', flex: 1, minWidth: 0 }}
          />
          <span style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>{wordCount} words · {saveStatus}</span>
          <button className={`btn ${panel === 'history' ? 'btn-secondary' : 'btn-ghost'}`} onClick={openHistory}>History</button>
          <button className={`btn ${panel === 'comments' ? 'btn-secondary' : 'btn-ghost'}`} onClick={openComments}>
            Comments{comments.filter((c) => !c.resolved).length > 0 ? ` (${comments.filter((c) => !c.resolved).length})` : ''}
          </button>
        </div>

        {/* Toolbar */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'center', padding: '8px 10px', border: '1px solid var(--border)', borderRadius: 8, marginBottom: 10, background: 'var(--surface)' }}>
          {TOOLBAR_GROUPS.map((group, gi) => (
            <div key={gi} style={{ display: 'flex', gap: 2, paddingRight: 10, borderRight: gi < TOOLBAR_GROUPS.length - 1 ? '1px solid var(--border)' : 'none' }}>
              {group.map((btn) => (
                <button
                  key={btn.label}
                  title={btn.title}
                  onMouseDown={(e) => e.preventDefault()} // keep editor selection focused
                  onClick={() => exec(btn.cmd, btn.value)}
                  className="btn btn-ghost"
                  style={{ padding: '5px 9px', fontSize: 12.5, minWidth: 30, ...btn.style }}
                >
                  {btn.label}
                </button>
              ))}
            </div>
          ))}
        </div>

        {/* Editor surface */}
        <div style={{ flex: 1, overflow: 'auto', border: '1px solid var(--border)', borderRadius: 8, background: 'var(--surface)', display: 'flex', justifyContent: 'center', padding: '24px 0' }}>
          <div
            ref={editorRef}
            contentEditable
            suppressContentEditableWarning
            onInput={handleEditorInput}
            onMouseUp={handleSelect}
            onKeyUp={handleSelect}
            className="doc-editor-surface"
            style={{
              width: '100%',
              maxWidth: 720,
              minHeight: '100%',
              padding: '20px 28px',
              outline: 'none',
              fontSize: 14.5,
              lineHeight: 1.7,
            }}
          />
        </div>
      </div>

      {/* Side panel */}
      {panel && (
        <div style={{ width: 300, marginLeft: 14, borderLeft: '1px solid var(--border)', paddingLeft: 14, overflow: 'auto' }}>
          {panel === 'history' && (
            <div>
              <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>Version history</h3>
              {versions.length === 0 && <p style={{ fontSize: 12.5, color: 'var(--text-tertiary)' }}>No versions yet.</p>}
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {versions.map((v) => (
                  <div key={v.id} className="card" style={{ padding: 10 }}>
                    <div style={{ fontSize: 12.5, fontWeight: 600 }}>
                      v{v.versionNumber} {v.restoredFromVersion != null && <span style={{ color: 'var(--text-tertiary)', fontWeight: 400 }}> · restored from v{v.restoredFromVersion}</span>}
                    </div>
                    <div style={{ fontSize: 11.5, color: 'var(--text-tertiary)', margin: '4px 0 8px' }}>
                      {new Date(v.createdAt).toLocaleString()}
                    </div>
                    <button className="btn btn-secondary" style={{ padding: '4px 10px', fontSize: 12 }} onClick={() => handleRestore(v.id)}>
                      Restore this version
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {panel === 'comments' && (
            <div>
              <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>Comments</h3>

              {quotedSelection && (
                <div style={{ fontSize: 11.5, color: 'var(--text-secondary)', background: 'var(--sidebar-bg)', padding: 8, borderRadius: 6, marginBottom: 8 }}>
                  Replying to: "{quotedSelection}"
                </div>
              )}
              <textarea
                rows={3}
                placeholder="Add a comment…"
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                style={{ width: '100%', border: '1px solid var(--border-strong)', borderRadius: 6, padding: 8, fontSize: 12.5, marginBottom: 8, resize: 'vertical' }}
              />
              <button className="btn btn-primary" style={{ marginBottom: 16, fontSize: 12.5 }} onClick={handleAddComment}>
                Comment
              </button>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {comments.length === 0 && <p style={{ fontSize: 12.5, color: 'var(--text-tertiary)' }}>No comments yet.</p>}
                {comments.map((c) => (
                  <div key={c.id} className="card" style={{ padding: 10, opacity: c.resolved ? 0.6 : 1 }}>
                    {c.quotedText && (
                      <div style={{ fontSize: 11, fontStyle: 'italic', color: 'var(--text-tertiary)', marginBottom: 6, borderLeft: '2px solid var(--border-strong)', paddingLeft: 6 }}>
                        "{c.quotedText}"
                      </div>
                    )}
                    <div style={{ fontSize: 12.5, fontWeight: 600 }}>{c.authorName}</div>
                    <div style={{ fontSize: 12.5, margin: '4px 0 8px' }}>{c.body}</div>
                    <div style={{ display: 'flex', gap: 8 }}>
                      <button className="btn btn-ghost" style={{ padding: '2px 6px', fontSize: 11 }} onClick={() => handleResolveComment(c)}>
                        {c.resolved ? 'Unresolve' : 'Resolve'}
                      </button>
                      {c.authorId === user?.id && (
                        <button className="btn btn-ghost" style={{ padding: '2px 6px', fontSize: 11, color: 'var(--danger)' }} onClick={() => handleDeleteComment(c)}>
                          Delete
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
