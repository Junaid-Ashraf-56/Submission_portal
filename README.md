<div align="center">

# 📚 Assignment Portal

### A production-grade university assignment submission system

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

**[Live Demo](https://assignmentportal.live)** · **[Report Bug](https://github.com/Junaid-Ashraf-56/Submission_portal/issues)** · **[Request Feature](https://github.com/Junaid-Ashraf-56/Submission_portal/issues)**

![Assignment Portal Banner](https://img.shields.io/badge/Status-Live%20%F0%9F%9F%A2-success?style=for-the-badge)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [Deployment](#-deployment)
- [API Routes](#-api-routes)
- [Database Schema](#-database-schema)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)

---

## 🌟 Overview

Assignment Portal is a full-stack web application built for university environments. It enables a structured workflow between **Admins**, **Class Representatives (CRs)**, and **Students** — covering assignment creation, file submission, deadline management, and real-time class communication.

> Built as a first production project, deployed at [assignmentportal.live](https://assignmentportal.live) on DigitalOcean with Docker, Nginx, and Let's Encrypt SSL.

---

## ✨ Features

### 🔐 Authentication & Security
- Email-based OTP verification before account activation
- Role-based access control (`ROLE_ADMIN`, `ROLE_CR`, `ROLE_STUDENT`)
- `PENDING → ACTIVE` account approval workflow
- Forgot password via OTP with 1-minute expiry
- Spring Security session management with CSRF protection
- Async OTP email delivery for zero-latency redirects

### 👨‍💼 Admin
- Approve / reject / delete CR registration requests
- View all CRs with expandable student lists per section
- Dashboard stats — total CRs, students, pending requests
- Email-based actions (no userId exposure)

### 🎓 Class Representative (CR)
- Register with OTP verification → admin approval flow
- Create assignments with subject code, type (LAB/THEORY), and deadline
- Track submission progress per assignment with progress bar
- Download all student submissions as ZIP
- Extend assignment deadlines
- Edit profile — section/semester changes **cascade to all linked students**
- Real-time class chat via WebSocket

### 🧑‍🎓 Student
- View active assignments from their CR
- Submit assignments (file upload to Supabase Storage)
- Track submission status per assignment
- Real-time class chat with classmates

### 💬 Real-time Class Chat
- WebSocket (STOMP over SockJS)
- Auto-scoped chat rooms: `{admission}-{program}-{section}-{semester}`
- Messages persisted to PostgreSQL
- Loads last 50 messages on panel open
- Unread message badge when panel is closed
- Isolated — only classmates in the same room

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.6 |
| Security | Spring Security 6 |
| ORM | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 15 |
| File Storage | Supabase Storage |
| Real-time | WebSocket (STOMP + SockJS) |
| Templating | Thymeleaf |
| Email | Spring Mail (Gmail SMTP, async) |
| Frontend | Bootstrap 5.3, Bootstrap Icons |
| Containerization | Docker + Docker Compose |
| Reverse Proxy | Nginx |
| SSL | Let's Encrypt (Certbot) |
| Hosting | DigitalOcean Droplet |
| Build | Maven |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────┐
│                  Internet                    │
└──────────────────┬──────────────────────────┘
                   │ HTTPS :443
┌──────────────────▼──────────────────────────┐
│               Nginx (Reverse Proxy)          │
│         SSL Termination + WS Proxy           │
└──────────────────┬──────────────────────────┘
                   │ HTTP :8080
┌──────────────────▼──────────────────────────┐
│         Spring Boot Application              │
│  ┌─────────────┐  ┌──────────────────────┐  │
│  │  MVC Layer  │  │  WebSocket (STOMP)   │  │
│  │  Thymeleaf  │  │  /topic/chat/{room}  │  │
│  └──────┬──────┘  └──────────┬───────────┘  │
│         │                    │               │
│  ┌──────▼────────────────────▼───────────┐  │
│  │         Service + Repository Layer     │  │
│  └──────────────────┬────────────────────┘  │
└─────────────────────┼───────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
┌───────▼────────┐          ┌─────────▼──────────┐
│  PostgreSQL 15  │          │   Supabase Storage  │
│  (Docker)       │          │   (File Uploads)    │
└────────────────┘          └────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker + Docker Compose
- PostgreSQL 15 (or use Docker)

### Local Development

**1. Clone the repository**
```bash
git clone https://github.com/Junaid-Ashraf-56/Submission_portal.git
cd Submission_portal
```

**2. Create `src/main/resources/application.properties`**
```properties
spring.application.name=submission_portal

spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

supabase.url=${SUPABASE_URL}
supabase.key=${SUPABASE_KEY}
supabase.bucket.name=${SUPABASE_BUCKET_NAME}

spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

otp.expiry.minutes=1
```

**3. Create `docker-compose.yml`** with your environment variables (see [Environment Variables](#-environment-variables))

**4. Build and run**
```bash
mvn clean package -DskipTests
docker compose up --build
```

**5. Open** `http://localhost:8080`

---

## 🔑 Environment Variables

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/submission_portal` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |
| `SUPABASE_URL` | Supabase project storage URL |
| `SUPABASE_KEY` | Supabase service role key |
| `SUPABASE_BUCKET_NAME` | Storage bucket name |
| `MAIL_USERNAME` | Gmail address for OTP emails |
| `MAIL_PASSWORD` | Gmail app password (not account password) |

> ⚠️ Never commit `application.properties` or `docker-compose.yml` with real credentials. Both are in `.gitignore`.

---

## 🌐 Deployment

This project is deployed on DigitalOcean using Docker + Nginx + Let's Encrypt.

### Stack
```
assignmentportal.live
      ↓ DNS (Namecheap A Record)
DigitalOcean Droplet (Ubuntu 22.04)
      ↓ Nginx (port 443 → 8080, WebSocket proxy)
Docker Compose
      ├── submission_portal_app (Spring Boot)
      └── submission_portal_db  (PostgreSQL 15)
```

### Update Production
```bash
# SSH into server
ssh root@YOUR_DROPLET_IP

# Pull latest code
cd /opt/Submission_portal
git pull origin main

# Rebuild and restart
mvn clean package -DskipTests
docker compose down
docker compose up --build -d
```

---

## 🗺 API Routes

| Method | Route | Role | Description |
|---|---|---|---|
| GET | `/` | Public | Landing page |
| GET/POST | `/auth/login` | Public | Login |
| GET/POST | `/auth/register` | Public | CR registration |
| GET/POST | `/auth/verify-otp` | Public | OTP verification |
| GET | `/cr/dashboard` | CR | CR dashboard |
| GET | `/cr/manage-students` | CR | Student management |
| POST | `/cr/create-assignment` | CR | Create assignment |
| GET | `/cr/assignments/{id}/submissions` | CR | View submissions |
| GET | `/cr/assignments/{id}/download-all` | CR | Download ZIP |
| GET | `/student/dashboard` | Student | Student dashboard |
| POST | `/student/assignments/{id}/submit` | Student | Submit assignment |
| GET | `/admin/panel` | Admin | Admin dashboard |
| POST | `/admin/cr/approve` | Admin | Approve CR |
| POST | `/admin/cr/reject` | Admin | Reject CR |
| GET | `/chat/{roomId}/history` | Authenticated | Load chat history |
| WS | `/ws` | Authenticated | WebSocket endpoint |

---

## 🗄 Database Schema

```
users
├── id (PK)
├── email (unique)
├── password (BCrypt)
├── role (ROLE_ADMIN / ROLE_CR / ROLE_STUDENT)
└── status (PENDING / ACTIVE)

students
├── id (PK)
├── user_id (FK → users)
├── name, roll_no, gender, phone_number
├── section, program, semester, admission
└── university

assignments
├── assignment_id (PK)
├── created_by (FK → students)
├── subject_title, subject_code
├── assignment_type (LAB / THEORY)
├── description
└── end_time

submissions
├── id (PK)
├── assignment_id (FK → assignments)
├── student_id (FK → students)
├── file_url (Supabase)
└── submitted_at

chat_messages
├── id (PK)
├── room_id (admission-program-section-semester)
├── sender_name
├── content
└── sent_at
```

---

## 📁 Project Structure

```
src/main/java/com/web/submission_portal/
├── config/
│   ├── SecurityConfig.java
│   └── WebSocketConfig.java
├── controller/
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── ChatController.java
│   ├── CRController.java
│   ├── RegistrationController.java
│   └── StudentController.java
├── entity/
│   ├── Assignment.java
│   ├── ChatMessage.java
│   ├── Student.java
│   ├── Submission.java
│   └── User.java
├── repository/
│   ├── ChatMessageRepository.java
│   ├── StudentRepository.java
│   ├── SubmissionRepository.java
│   └── UserRepository.java
└── service/
    ├── EmailService.java          ← @Async OTP delivery
    ├── OTPGeneratorService.java
    ├── PasswordResetService.java
    └── StudentService.java

src/main/resources/
├── templates/
│   ├── admin/, auth/, cr/, student/
└── static/
    ├── css/  (theme.css, cr-dashboard.css, student-dashboard.css)
    └── js/   (cr-chat.js, student-chat.js)
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 👤 Author

**Junaid Ashraf**
- GitHub: [@Junaid-Ashraf-56](https://github.com/Junaid-Ashraf-56)
- Live Project: [assignmentportal.live](https://assignmentportal.live)

---

<div align="center">

⭐ Star this repo if you found it helpful!

</div>
