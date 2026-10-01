# Campus Queue Management System

A Java-based web application for managing campus service queues.

## Technology Stack

- Java 17
- Maven
- Jakarta Servlet
- MySQL
- JDBC
- Apache Tomcat 10.1
- HTML / CSS / JavaScript
- JSP

## Team Branches

| Member | Branch | Responsibility |
|---|---|---|
| Member 1 | `student-ui` | Student UI + JSP + JavaScript |
| Member 2 | `queue-backend` | Queue logic + Servlets + Integration |
| Member 3 | `staff-module` | Staff dashboard + Availability |
| Member 4 | `smart-module` | Booking + Waiting-time algorithm |

## Project Structure

### Java Backend

```text
src/main/java/com/queue/

├── controller/   → Servlets / request handling
├── service/      → Business logic
├── dao/          → Database operations
├── model/        → Data models / Java classes
├── util/         → Utility classes / database connection
└── filter/       → Servlet filters

src/main/webapp/

├── student/      → Student module
├── staff/        → Staff module
├── booking/      → Booking module
├── css/          → Common CSS
└── js/            → Common JavaScript