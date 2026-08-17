# TaskFlow API quick reference

Base URL: `http://localhost:8080/api`

Use `Authorization: Bearer <token>` after logging in.

| Group | Endpoints |
| --- | --- |
| Auth | `POST /auth/register`, `POST /auth/login` |
| Tasks | `GET/POST /tasks`, `GET/PUT/DELETE /tasks/{id}`, `PUT /tasks/{id}/assign/{userId}` |
| Dashboards | `GET /dashboard/user`, `GET /dashboard/admin` |
| Users | `GET /users`, `GET/PUT/DELETE /users/{id}` |

For request bodies, filters, status codes, role access, and error responses, see the root [README](../README.md).
