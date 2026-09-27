# 📊 VectaSheet - Collaborative Workspace & Data Engine

An all-in-one collaborative workspace featuring real-time data sheets, formula execution, rich-text docs, Kanban workflows, and workspace activity feeds. Backend is Java (Spring Boot) with MySQL, frontend is React (Vite), communicating over a secure REST API.

---

## ✨ Features

* **Spreadsheet & Formula Engine** — Hand-written lexer, recursive-descent parser, AST evaluator, topological dependency-graph recalculation (Kahn’s algorithm), circular reference detection, and optimistic concurrency versioning.
* **Docs with Snapshot History** — Rich-text document editor with auto-save debouncing, server-side HTML sanitization, document-level comments with quoted text, and immutable snapshot version restoration.
* **Tasks & Native Kanban Board** — Full lifecycle task tracking (`TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`) with drag-and-drop board movements, subtask checklists, and unified data synchronization.
* **Activity Log & Smart Notifications** — Central workspace mutation feed tracking member invites, task events, and document updates with mention detection (`@Name`) and unread badges.
* **Integrated Calendar & Files** — Unified month view merging task deadlines with custom events; multi-part file storage with path-traversal protection and size caps.
* **Cross-Module Search** — Real-time debounced query engine scanning across documents, spreadsheets, and tasks.

---

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA, Hibernate, MySQL 8+, Maven.
* **Frontend:** React 18, Vite, React Router, CSS3, native HTML5 drag-and-drop.

---

## 📁 Project Structure
```bash
vectasheet/
├── backend/
│   ├── src/main/java/com/vectasheet/
│   │   ├── config/                    # Security, CORS, and multipart configurations
│   │   ├── controller/                # REST API controllers for all modules
│   │   ├── entity/                    # JPA Entities (User, Workspace, Sheet, Cell, Doc, Task)
│   │   ├── formula/                   # Lexer, Parser, AST nodes, evaluator, dependency graph
│   │   ├── repository/                # Spring Data JPA repositories
│   │   ├── security/                  # JWT filters, auth entry points, token provider
│   │   ├── service/                   # Business logic (Formula recalculation, auth, docs, tasks)
│   │   └── VectasheetApplication.java # Spring Boot application entry point
│   ├── src/main/resources/
│   │   ├── application.properties     # Database connection and environment profiles
│   │   └── application.yml            # Server and JWT application configurations
│   ├── pom.xml                        # Maven dependencies and build setup
│   └── README.md                      # Backend service documentation
│
├── frontend/
│   ├── public/                        # Static assets, logos, and favicon
│   ├── src/
│   │   ├── components/                # UI components (Grid, Kanban, DocEditor, Topbar, Sidebar)
│   │   ├── context/                   # Global state (AuthContext, WorkspaceContext, ThemeContext)
│   │   ├── hooks/                     # Custom hooks for debounced search, autosave, and keyboard nav
│   │   ├── pages/                     # Routed views (Dashboard, SpreadsheetView, DocView, TasksView)
│   │   ├── services/                  # API client services connecting to backend endpoints
│   │   ├── styles/                    # Global stylesheet tokens and themes
│   │   ├── App.jsx                    # Route definitions and layout shell
│   │   └── main.jsx                   # React mount point
│   ├── index.html                     # Frontend HTML template
│   ├── package.json                   # UI dependencies and build scripts
│   ├── package-lock.json              # Pinned dependency tree
│   └── vite.config.js                 # Vite bundler and development proxy setup
│
├── .env.example                       # Reference environment variables
├── .gitignore                         # Multi-tier Git exclusion rules
├── docker-compose.yml                 # Local MySQL service container
└── README.md                          # Full repository documentation

```

---

## 🔌 API Reference

| Method | Path | Purpose |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user account |
| `POST` | `/api/auth/login` | Authenticate user and issue JWT pair |
| `POST` | `/api/auth/refresh` | Rotate refresh token and issue new access token |
| `GET` | `/api/workspaces` | List all accessible workspaces |
| `POST` | `/api/workspaces` | Create new workspace |
| `GET` | `/api/spreadsheets/{id}` | Fetch spreadsheet structure and sheets |
| `GET` | `/api/sheets/{sheetId}/cells` | Fetch all populated cells and computed values |
| `PUT` | `/api/sheets/{sheetId}/cells` | Batch update cells and trigger topological recalculation |
| `GET` | `/api/documents/{id}` | Retrieve document body and metadata |
| `PUT` | `/api/documents/{id}` | Autosave sanitized HTML with optimistic lock |
| `POST` | `/api/documents/{id}/versions/{versionId}/restore` | Revert document content to an immutable historic version |
| `GET` | `/api/workspaces/{workspaceId}/tasks` | Fetch task backlog and Kanban cards |
| `PATCH`| `/api/tasks/{id}` | Update task status, priority, due date, or assignment |
| `POST` | `/api/workspaces/{workspaceId}/files` | Multipart file upload with directory escaping protection |
| `GET` | `/api/workspaces/{workspaceId}/search?q=` | Live cross-module query across sheets, docs, and tasks |

---

## 🚀 Getting Started

### 1. Clone the repository

```powershell
git clone https://github.com/Priyansh2811/VectaSheet.git
cd vectasheet