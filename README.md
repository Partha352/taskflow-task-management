# TaskFlow — Full Stack Task Management System

TaskFlow is a full-stack task management application built for the Kinetrexa Software Private Limited Java Full Stack Development internship capstone (Task 5). It provides JWT-secured authentication, role-based administration, task assignment, filters, dashboards, and a responsive React user interface.

## Current implementation

The backend phases through validation and exception handling, and the frontend phases through responsive UI refinement, are implemented. Swagger/OpenAPI, expanded test coverage, deployment assets, report, and demo-video deliverables remain planned work.

## Features

- Registration and BCrypt password hashing
- JWT login and stateless Spring Security authentication
- `USER` and `ADMIN` roles
- Task create, read, update, delete, status, priority, and due date management
- Admin-only task assignment
- Task title/description search and combined status, priority, assignee, and due-date filters
- User and admin task statistics dashboards
- Admin user search, role management, and guarded deletion
- Bean Validation and consistent JSON error responses
- Responsive React/Vite UI with authenticated routes

## Technology

| Area | Technology |
| --- | --- |
| Backend | Java 23, Spring Boot 3.4.3, Spring Web, Spring Data JPA |
| Security | Spring Security, JWT (JJWT), BCrypt |
| Database | MySQL 8 |
| Frontend | React, Vite, React Router, Axios |
| Testing | JUnit 5, Spring Boot Test, Mockito |

## Architecture

```text
React UI → Axios / JWT → Spring Controllers → Services → Repositories → MySQL
```

Controllers expose DTOs only. Services contain authorization and business rules. Repositories handle persistence. JPA entities are not returned directly from the API.

## Repository layout

```text
src/main/java/com/taskflow/backend/
  config/        security and password configuration
  controller/    REST endpoints
  dto/           request/response models
  entity/        JPA entities and enums
  exception/     global error handling
  repository/    Spring Data repositories
  security/      JWT filter, service, and user principal
  service/       business logic
frontend/
  src/components/ src/context/ src/pages/ src/services/
```

## Database schema

Database: `task_management`

```text
users
  id (PK)
  name
  email (unique)
  password (BCrypt hash)
  role (USER | ADMIN)
  created_at
  updated_at

tasks
  id (PK)
  title
  description
  status (TODO | IN_PROGRESS | COMPLETED)
  priority (LOW | MEDIUM | HIGH)
  due_date
  created_at
  updated_at
  created_by (FK → users.id, required)
  assigned_to (FK → users.id, optional)
```

## API reference

All endpoints except registration and login require `Authorization: Bearer <JWT>`.

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Public | Register a USER account |
| POST | `/api/auth/login` | Public | Login and receive JWT |
| GET | `/api/test` | Authenticated | Backend health check |
| GET | `/api/tasks` | Authenticated | List authorized tasks; supports filters |
| POST | `/api/tasks` | Authenticated | Create a task |
| GET | `/api/tasks/{id}` | Authorized owner/assignee/admin | Task details |
| PUT | `/api/tasks/{id}` | Creator/admin | Update task |
| DELETE | `/api/tasks/{id}` | Creator/admin | Delete task |
| PUT | `/api/tasks/{id}/assign/{userId}` | ADMIN | Assign task |
| GET | `/api/dashboard/user` | Authenticated | Personal task statistics |
| GET | `/api/dashboard/admin` | ADMIN | Workspace statistics |
| GET | `/api/users?search=` | ADMIN | Search/list users |
| GET | `/api/users/{id}` | ADMIN | User details |
| PUT | `/api/users/{id}` | Self or ADMIN | Update user; ADMIN may set role |
| DELETE | `/api/users/{id}` | ADMIN | Delete a user without related tasks |

Task filters:

```text
GET /api/tasks?status=IN_PROGRESS&priority=HIGH&search=launch
GET /api/tasks?assignedUserId=2&dueDate=2026-12-31
```

Example register body:

```json
{ "name": "Asha", "email": "asha@example.com", "password": "StrongPass123" }
```

Example task body:

```json
{
  "title": "Prepare project report",
  "description": "Draft the Kinetrexa final report",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-12-31"
}
```

## Error format

```json
{
  "timestamp": "2026-08-12T21:25:23",
  "status": 400,
  "message": "Validation failed",
  "path": "/api/auth/register",
  "fieldErrors": { "email": "Email must be valid" }
}
```

## Local installation

Prerequisites: JDK 23 (or a compatible configured JDK), MySQL 8, Maven 3.9+, Node.js 20+, and npm.

1. Create or start MySQL, then create a database account with access to `task_management`.
2. In a PowerShell session, set the backend configuration—never commit these values:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/task_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="your_mysql_user"
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET="replace-with-a-random-secret-of-at-least-32-characters"
```

3. Start the backend:

```powershell
mvn spring-boot:run
```

The backend runs on `http://localhost:8080` by default.

4. Configure and start the frontend:

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

The Vite frontend runs on `http://localhost:5173`, which is already allowed by backend CORS configuration.

## Testing

Backend:

```powershell
mvn test
```

Frontend production build:

```powershell
cd frontend
npm run build
```

## Security notes

- Passwords are saved only as BCrypt hashes.
- JWT secrets and database credentials are environment values, not source code.
- Do not commit `.env`, secrets, `target`, `node_modules`, or frontend `dist` output.
- The requested demo credentials must be created only through a secure development seed process and changed before production; they are not hardcoded in the application.

## Screenshots

Add screenshots here after running the application:

- Login and registration
- User dashboard
- Task list and task editor
- Admin dashboard and user management

## Deployment guidance

Build frontend with `npm run build`, serve the generated `frontend/dist` through a static host, and deploy the backend with environment variables set by the deployment platform. Use a managed MySQL instance, a strong generated `JWT_SECRET`, HTTPS, and production migrations/schema validation before public deployment.

## Future enhancements

- Springdoc Swagger/OpenAPI documentation
- Database migrations with Flyway
- Pagination and sorting
- Dedicated API integration tests
- Password reset and email verification
- File attachments and task comments
- CI/CD pipeline and cloud deployment
