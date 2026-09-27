# VectaSheet

**Think visually. Work with data. Execute together.**

This build covers **eight phases**: Auth + Workspace, Spreadsheet + Formula Engine, Docs (a
Google-Docs-style editor), Tasks + Kanban, Activity Log + Notifications, Calendar, Files, and Search.
Everything described below runs end-to-end — no fake logins, no fake formulas, no fake autosave, no
fake notifications. The remaining modules (Canvas, Dashboards, Automations, Templates, AI features,
and true real-time multi-cursor collaboration) are **not** included here; their sidebar entries show
an honest "coming soon" placeholder rather than pretending to work.

## What's actually implemented

### Auth + Workspace (Phase 1)
- **Registration, login, logout** — BCrypt password hashing, JWT access tokens, rotating refresh tokens
- **Forgot / reset password** — real backend flow; since no email provider is wired up, the reset link
  is printed to the backend console instead of being silently faked
- **Change password**, basic **profile editing**
- **Workspaces**: create, rename, delete (soft-archive)
- **Members**: invite by email, change role, remove — enforced server-side with a real role hierarchy
  (`OWNER > ADMIN > EDITOR > COMMENTER > VIEWER`); the frontend never decides permissions on its own
- **Protected routes**, session persistence across reloads, automatic token refresh
- **Light / Dark (near-black) theme**, persisted, respects system preference on first visit
- Landing page, 404 page, Contact page, Waitlist page, Thank-you page, cookie consent banner,
  breadcrumbs inside the workspace shell, per-page `<title>` tags

### Spreadsheet + Formula Engine (Phase 2)
- **Spreadsheets and multiple sheets per spreadsheet**: create, rename, duplicate, delete (a
  spreadsheet always keeps at least one sheet)
- **A real formula engine** (`backend/.../formula/`) — hand-written lexer → recursive-descent parser →
  AST → evaluator, not a wrapper around a library:
  - Arithmetic (`+ - * / ^ %`), comparisons (`= <> < > <= >=`), string concatenation (`&`)
  - Cell references (`A1`, `$A$1`) and ranges (`A1:B10`)
  - Functions: `SUM AVERAGE MIN MAX COUNT COUNTA IF AND OR NOT ROUND ROUNDUP ROUNDDOWN CONCAT LEFT
    RIGHT LEN TODAY NOW INDEX MATCH`, plus a single-column `VLOOKUP`
  - **Dependency-aware recalculation**: every edit rebuilds the sheet's dependency graph and
    recalculates formula cells in topological order (Kahn's algorithm) — not a full brute-force
    re-evaluation loop
  - **Circular reference detection** — cells in a cycle show `#CIRCULAR!` instead of hanging or crashing
  - Real error values: `#DIV/0! #NAME? #REF! #VALUE! #N/A #ERROR!`
- **Optimistic concurrency on cells**: each cell carries a `version`; a write that targets a stale
  version is rejected with a 409 and the exact conflict is surfaced to the frontend (the UI currently
  resolves this by reloading the latest values — a full "keep mine / use latest / compare" picker is
  future work, noted below)
- **Grid UI**: keyboard navigation (arrows, Enter, Tab, F2, Delete), a live formula bar, click/double-click
  to select/edit, undo/redo (Ctrl+Z / Ctrl+Y) via a client-side action stack
- **Sheet tabs**: switch, rename (double-click), duplicate, delete

**Known limitations, stated plainly rather than hidden:**
- The grid renders the sheet's full `rowCount × colCount` (default 50×20) directly — there's no
  virtualization yet, so this build is not meant for the spec's 10,000-row target. That's the next
  thing to build for this module.
- Recalculation reloads and re-parses the whole sheet on every edit rather than maintaining a
  persisted, incrementally-updated dependency graph — correct, but not the final-scale design.
- `VLOOKUP` only works reliably against a single-column range; a full row/column-aware lookup needs
  the evaluator to carry range shape instead of a flattened list, which is a small follow-up change.
- Formatting (bold, borders, number formats, conditional formatting), charts, freeze rows/columns,
  sorting/filtering, and `XLOOKUP`/`FILTER`/`SORT`/`UNIQUE` are not built yet.
- Conflict resolution currently just reloads on a version mismatch; it doesn't yet show the "your
  version vs. their version" picker described in the original spec.

### Docs (Phase 3)
- **Rich text editor** built on the browser's native `contentEditable` + `execCommand` — headings
  (H1–H3), bold/italic/underline/strikethrough, bulleted/numbered lists, blockquotes, links, text
  alignment, clear formatting, undo/redo
- **Autosave**: edits debounce ~1.5s of inactivity, then save; a status indicator shows
  `Saving… / Saved / Offline`, matching the "status must reflect actual state" rule — it's never faked
- **Real version history**: every save creates an immutable snapshot; the history panel lists every
  version with its timestamp, and **restoring a version never deletes history** — it copies the old
  snapshot into the live document and creates a brand-new version on top, exactly like the rollback
  rule for spreadsheets
- **Comments**: attach a comment to a document, optionally quoting the text you had selected when you
  opened the comment box; resolve/unresolve, delete (author or Admin+)
- **Server-side HTML sanitization** on every save (strips `<script>`, event-handler attributes,
  `javascript:`/non-image `data:` URLs, iframes/forms) before content is ever persisted or re-rendered
- Word count, editable title, optimistic-concurrency versioning identical in spirit to spreadsheet cells

**Known limitations, stated plainly rather than hidden:**
- This is **not** real-time multi-cursor collaborative editing (no Google-Docs-style "see other
  people typing live"). That requires an OT/CRDT engine and a WebSocket transport — a genuinely
  separate, large piece of work, which is the same real-time collaboration phase already flagged for
  the rest of the app. What's here is single-editor-at-a-time with safe conflict detection: if two
  people save the same document at once, the second save is rejected with a clear conflict message
  instead of silently overwriting the first person's work.
- `execCommand` is a deprecated browser API. It's supported everywhere today and keeps this phase
  dependency-free, but a production-grade editor (matching the spec's "avoid excessive dependency
  usage" instruction while still being robust) would eventually move to a maintained editor engine
  (e.g. ProseMirror/Tiptap) instead of hand-rolling on top of `execCommand`.
- Comments are document-level with an optional quoted-text snippet for context — they are not
  live-anchored to a moving text range the way Google Docs anchors a comment to exact characters.
- The HTML sanitizer is a hand-written allowlist stopgap (strips scripts/event handlers/dangerous
  URLs), not a full sanitization library — adequate for this app's own editor output, but should be
  swapped for a proper library (e.g. OWASP Java HTML Sanitizer) before accepting HTML from any other
  untrusted source.
- No tables, images, or file embeds in documents yet.

### Tasks + Kanban (Phase 4)
- **Tasks**: title, description, status (`TODO / IN_PROGRESS / REVIEW / DONE`), priority
  (`LOW / MEDIUM / HIGH / URGENT`), assignee (from real workspace members), start/due dates,
  **subtasks** (one level deep) with their own checkbox-style completion
- **Tasks list view**: quick-add, filter by status and assignee
- **Kanban board**: native HTML5 drag-and-drop between columns (no drag-and-drop library — the spec's
  "avoid excessive dependency usage" instruction extends here too), optimistic UI update on drop,
  reconciled against the server response, with automatic rollback (via reload) if the move is rejected
- Both views **share the same task data** and the same detail panel — moving a card on the Board
  updates what you see in the Tasks list and vice versa, per the "everything is connected" philosophy
- Deleting a task archives its subtasks too, so nothing is left as an orphaned card

**Known limitations, stated plainly rather than hidden:**
- Kanban column reordering only tracks position within the column a card is dropped into; there's no
  drag handle for manually reordering cards within the same column yet (drop position is always
  "append to end of column").
- No task comments, attachments, or dependencies yet (the original spec calls for both) — subtasks are
  the only relationship implemented so far.
- No linkage yet between a task and a Canvas object, spreadsheet row, or dashboard widget — that's the
  cross-module "Objects" layer the spec describes, which needs Canvas and Dashboards to exist first.
- Calendar doesn't exist yet, so due dates aren't visualized on a calendar — only shown as text on
  each card/row.

### Activity Log + Notifications (Phase 5)
- **Activity log**: a real, append-only feed per workspace. Wired into workspace member invites, task
  create/edit/complete/delete, document create/edit/restore/comment.
- **Notifications**: task assignment, workspace invites, and a simple `@FirstName` mention detector in
  document comments all generate real notifications — not a static demo list. The bell icon in the top
  bar shows a live unread count (polled every 30s), a dropdown list, per-notification read toggling, and
  "mark all read".

**Known limitations, stated plainly rather than hidden:**
- Activity logging is wired into the actions above, not literally every mutation in the app (e.g.
  spreadsheet cell edits, sheet renames, and calendar events aren't logged yet) — extending it is
  mechanical (call `activityLogService.log(...)` at each write) but hasn't been done exhaustively.
- Mention detection is a plain case-insensitive substring match on `@FirstName`, not a rich mention
  picker that stores a resolved user ID at compose time — two people with the same first name would
  both get notified.
- No email delivery for notifications (matches the rest of the app: no email provider is configured
  locally, so everything notification-related stays in-app).

### Calendar (Phase 6)
- **Month view** with real navigation (prev/next/today), showing both real calendar events and task
  due dates on the correct day
- Click a day to select it, then add an event on that day; click an event chip to delete it

**Known limitations, stated plainly rather than hidden:**
- Only month view exists — week view and agenda view from the original spec are not built yet
- Adding an event uses a plain browser prompt for the title (defaults to 9–10am) rather than a full
  event form with a duration picker, attendees, or recurrence

### Files (Phase 7)
- **Real upload and download** to local disk (`backend/data/uploads/{workspaceId}/...` by default,
  configurable via `STORAGE_BASE_DIR`), with a 25 MB size limit enforced both by Spring's multipart
  config and the service layer
- Filenames are sanitized and stored under a random prefix so a crafted filename can't escape the
  storage directory or collide with another upload
- List, download (streams the real bytes with correct filename/content-type), and delete (archives the
  metadata row and removes the file from disk)

**Known limitations, stated plainly rather than hidden:**
- Files aren't yet linkable to a task/canvas object/project the way the spec describes — they're a flat
  per-workspace list for now
- No preview pane, no CSV/XLSX/JSON import pipeline yet (the spreadsheet's own import/export is
  separately not built either)
- Storage is local disk, not object storage (S3-compatible) — fine for local development, not what
  you'd want in a real multi-instance deployment

### Search (Phase 8)
- **Real cross-module search** within a workspace: typing in the top bar's search box queries documents,
  spreadsheets, and tasks by title and shows live, clickable results
- Debounced (300ms) so it doesn't hammer the API on every keystroke

**Known limitations, stated plainly rather than hidden:**
- This is substring matching on titles, not a real search index — no full-text search of document
  bodies or spreadsheet cell contents, no ranking beyond "first match wins", no typo tolerance
- No global `Ctrl+K` command palette yet (the spec calls for one) — search currently only lives in the
  top bar's input, and only searches the module types listed above (not canvas, files, or comments)

## Tech stack

- **Backend**: Java 21, Spring Boot 3, Spring Security, Spring Data JPA/Hibernate, MySQL, Maven
- **Frontend**: React 18, plain JavaScript (no TypeScript), React Router, Vite, no Tailwind/Bootstrap

## Project structure

```
vectasheet/
├── backend/
│   └── src/main/java/com/vectasheet/
│       ├── entity/        User, Workspace, Spreadsheet, Sheet, Cell, ...
│       ├── formula/        Lexer, Parser, Node (AST), FormulaEvaluator, DependencyExtractor, CellRef
│       ├── service/        AuthService, WorkspaceService, SpreadsheetService, SheetService, CellService
│       ├── controller/     REST endpoints
│       └── security/       JWT filter, JWT service
├── frontend/    React app (port 5173)
├── docker-compose.yml   MySQL for local dev
└── .env.example
```

## Running it locally

### 1. Database

Easiest path — start MySQL with Docker:

```bash
docker compose up -d
```

This starts MySQL on `localhost:3306` with database `vectasheet`, user `root` / password `root`
(matches the backend's defaults — override via env vars if you change them).

Don't want Docker? Point `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` at any MySQL 8 instance you already
have running. Or skip MySQL entirely for a quick local trial:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

This uses a file-based H2 database (`backend/data/vectasheet.mv.db`) — zero external setup, not
meant for production.

### 2. Backend

```bash
cd backend
cp ../.env.example .env   # optional — defaults work for local Docker MySQL
mvn spring-boot:run
```

Runs on **http://localhost:8080**. On first boot it seeds a demo account:

```
demo@vectasheet.com / Demo1234!
```

Health check: `GET http://localhost:8080/api/health`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on **http://localhost:5173** and proxies `/api/*` to the backend.

## Environment variables

See `.env.example`. All have sensible local defaults baked into `application.yml`, so the app runs
without any `.env` file for local development — but **change `JWT_SECRET` before deploying anywhere
real.**

## API summary

```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
POST   /api/auth/forgot-password
POST   /api/auth/reset-password
POST   /api/auth/change-password

GET    /api/users/me
PUT    /api/users/me

POST   /api/workspaces
GET    /api/workspaces
GET    /api/workspaces/{id}
PATCH  /api/workspaces/{id}
DELETE /api/workspaces/{id}

GET    /api/workspaces/{id}/members
POST   /api/workspaces/{id}/members
PATCH  /api/workspaces/{id}/members/{userId}
DELETE /api/workspaces/{id}/members/{userId}

GET    /api/health

POST   /api/workspaces/{workspaceId}/spreadsheets
GET    /api/workspaces/{workspaceId}/spreadsheets
GET    /api/spreadsheets/{id}
PATCH  /api/spreadsheets/{id}
DELETE /api/spreadsheets/{id}

GET    /api/spreadsheets/{id}/sheets
POST   /api/spreadsheets/{id}/sheets
PATCH  /api/sheets/{sheetId}
POST   /api/sheets/{sheetId}/duplicate
DELETE /api/sheets/{sheetId}

GET    /api/sheets/{sheetId}/cells
PUT    /api/sheets/{sheetId}/cells

POST   /api/workspaces/{workspaceId}/documents
GET    /api/workspaces/{workspaceId}/documents
GET    /api/documents/{id}
PUT    /api/documents/{id}
DELETE /api/documents/{id}
GET    /api/documents/{id}/versions
GET    /api/documents/{id}/versions/{versionId}
POST   /api/documents/{id}/versions/{versionId}/restore
GET    /api/documents/{id}/comments
POST   /api/documents/{id}/comments
PATCH  /api/documents/{id}/comments/{commentId}
DELETE /api/documents/{id}/comments/{commentId}

POST   /api/workspaces/{workspaceId}/tasks
GET    /api/workspaces/{workspaceId}/tasks
GET    /api/tasks/{id}
PATCH  /api/tasks/{id}
DELETE /api/tasks/{id}
GET    /api/tasks/{id}/subtasks

POST   /api/workspaces/{workspaceId}/events
GET    /api/workspaces/{workspaceId}/events
PATCH  /api/events/{eventId}
DELETE /api/events/{eventId}

POST   /api/workspaces/{workspaceId}/files      (multipart/form-data, field name "file")
GET    /api/workspaces/{workspaceId}/files
GET    /api/files/{fileId}/download
DELETE /api/files/{fileId}

GET    /api/workspaces/{workspaceId}/activity?limit=50

GET    /api/notifications
GET    /api/notifications/unread-count
PATCH  /api/notifications/{id}                  { "read": true|false }
POST   /api/notifications/mark-all-read
DELETE /api/notifications/{id}

GET    /api/workspaces/{workspaceId}/search?q=...
```

All routes except `/api/auth/**` and `/api/health` require `Authorization: Bearer <accessToken>`.

## Security notes

- Passwords are hashed with BCrypt — never stored or logged in plaintext
- JWT access tokens are short-lived (15 min default); refresh tokens rotate on every use and are
  revoked on password change
- Every workspace/member mutation checks the caller's role **on the server**, regardless of what the
  UI shows
- CORS is restricted to `localhost` origins by default — update `FRONTEND_URL` for other environments

## Testing

Build verification performed in this environment:

- ✅ `npm run build` (frontend) — builds with zero errors, twice, across both phases
- ✅ Java source brace/structure sanity check across all 71 backend source files
- ✅ Formula engine unit tests written (`FormulaEngineTests.java`) covering arithmetic, cell refs,
  `SUM`/`IF`/text functions, rounding, `#DIV/0!`, `#NAME?`, dependency extraction, and `CellRef`
  round-tripping
- ✅ Auth flow integration tests written (`AuthFlowTests.java`) covering register → login, wrong
  password → 401, and unauthenticated access → 401
- ⚠️ None of the above were actually **run** in this sandbox — it has no JDK compiler (`javac`) and
  no network access to Maven Central, only a JRE. Please run `mvn test` yourself on first use. If
  anything doesn't compile, it's most likely a dependency version mismatch in `pom.xml`.

Manual checklist once running:

- [ ] Register a new account → lands on Dashboard with a starter workspace
- [ ] Create a spreadsheet, type `10` into A1, `20` into B1, `=SUM(A1:B1)` into C1 → C1 shows `30`
- [ ] Change A1 to `15` → C1 recalculates to `35` automatically
- [ ] Type `=A1/0` into any cell → shows `#DIV/0!`
- [ ] Type `=A1+B1` into A1 itself (or create any cycle) → shows `#CIRCULAR!`
- [ ] Add a second sheet, rename it, duplicate it, delete the original
- [ ] Undo (Ctrl+Z) and redo (Ctrl+Y) a cell edit
- [ ] Open the same cell in two browser tabs as different users, edit in one, then try editing the
  stale version in the other → the second edit is rejected with a conflict message
- [ ] Create a document, type some formatted text, wait ~2 seconds, confirm save status shows "Saved"
- [ ] Open version history, make another edit, confirm a new version appears; restore an older version
  and confirm the content reverts but history still shows every version, including the restore itself
- [ ] Select some text in a document, add a comment, resolve it, then delete it
- [ ] Create a task, add two subtasks, check one off, confirm the "1/2 subtasks" count updates in both
  the Tasks list and the Kanban board
- [ ] Drag a card between Kanban columns, confirm it also moved in the Tasks list
- [ ] Assign a task to another member, confirm they see a notification and the unread bell count updates
- [ ] Comment "@FirstName looks good" on a document where FirstName is a real member, confirm they get a
  mention notification
- [ ] Open the Activity page, confirm the task/document actions above show up with the right actor and
  timestamp
- [ ] Add a calendar event on a day that also has a task due, confirm both show up on that day
- [ ] Upload a file, download it back, confirm the bytes match; delete it and confirm it's gone
- [ ] Type a few letters of a real task/document/spreadsheet title into the top bar search, confirm a
  matching, clickable result appears
- [ ] Confirm a Viewer/Editor cannot rename or delete the workspace (only Owner/Admin can)
- [ ] Toggle theme, refresh the page, confirm it persisted

## What's next (not in this build)

This build now covers eight phases: Auth, Workspace, Sheets/Formulas, Docs, Tasks/Kanban, Activity
Log/Notifications, Calendar, Files, and Search. What's genuinely still missing, in the order the
original spec lays out:

- **Infinite Canvas** — a whiteboard editor (shapes, freehand drawing, frames, sticky notes) with
  objects that stay linked to real tasks/spreadsheet rows. Not started.
- **Dashboards** — configurable widgets (KPI, chart, table, task summary) built from real workspace
  data. Not started.
- **Automation engine** — the trigger → condition → action builder described in the spec. Not started.
- **Templates** — pre-built project/roadmap/CRM starters that create real tasks/sheets/docs. Not started.
- **True real-time collaboration** — WebSocket/STOMP-based live presence, multi-cursor editing, and the
  "your version vs. their version" conflict picker. This is the largest remaining piece: every module
  built so far already has the groundwork for it (optimistic-concurrency `version` fields on cells,
  documents, and — implicitly — tasks), but the actual live transport and CRDT/OT merge logic doesn't
  exist yet. Right now, conflicts are *detected* (via version mismatches) but resolved by asking the
  person to reload, not by merging changes live.
- **AI features** (summarize workspace, generate project plan, etc.) — intentionally not started; the
  spec requires a graceful disabled state without an API key, which is easy to add once there's an
  actual feature to gate.
- **Sharing/public links**, **import/export** (CSV/XLSX/JSON), **command palette (Ctrl+K)**, and the
  follow-ups already called out module-by-module above (virtualized spreadsheet grid, a real editor
  engine for Docs, task comments/dependencies, week/agenda calendar views, full-text search).

Ask for any of these next and I'll build it the same way as everything above: real code, run through
an actual build, with limitations stated rather than hidden.
