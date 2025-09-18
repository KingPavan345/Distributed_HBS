# Distributed Vacation HBS (Hotel Booking System) — Group 12

## Table of Contents
- [Project Overview](#project-overview)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
  - [1. Backend (Microservices)](#1-backend-microservices)
  - [2. Android Frontend](#2-android-frontend)
  - [3. Admin CLI Tool](#3-admin-cli-tool)
- [API Endpoints](#api-endpoints)
- [Development & Troubleshooting](#development--troubleshooting)
- [Authors](#authors)

---

## Project Overview

This project is a distributed vacation home booking system built using a microservices architecture. It consists of:
- **Authentication Service** (Django + PostgreSQL)
- **Listings Service** (Flask + MongoDB)
- **Android App** (Frontend for users)
- **Admin CLI Tool** (For admin management)

---

## Prerequisites

- **Backend**
  - **Docker & Docker Compose** (required to build and run the backend microservices; all backend services are containerized and must be run via Docker)

- **Android Frontend**
  - Android Studio (latest stable)
  - Android SDK (API 30+)
  - JDK 8+

- **Admin CLI**
  - Python 3.8+


## Setup Instructions

### 1. Backend (Microservices)

#### a. Clone the repository
```bash
git clone <repository-url>
cd backend
```

#### b. Start all services (Docker)
```bash
docker-compose up --build
```
- This will start:
  - Auth Service (Django, port 8000)
  - Listings Service (Flask, port 8001)
  - PostgreSQL (port 5432)
  - MongoDB (port 27017)

#### c. Access the services
- Auth Service: [http://localhost:8000](http://localhost:8000)
- Listings Service: [http://localhost:8001](http://localhost:8001)

#### d. Environment Variables
- See `backend/README.md` for all environment variables.
- Default values are set in the Docker Compose file.

#### e. Running Tests
```bash
# Auth Service
docker-compose exec auth-service python manage.py test

# Listings Service
docker-compose exec listings-service python -m pytest
```

---

### 2. Android Frontend

#### a. Prerequisites
- Android Studio, Android SDK (API 30+), JDK 8+

#### b. Setup
1. Open Android Studio.
2. Select "Open an existing project" and choose `Frontend/Test_Build_Authentication`.
3. Update API URLs in `Config.kt` (or relevant config file) to match your backend server address.
   - For emulator, use `10.0.2.2` for localhost.
4. Sync Gradle when prompted.
5. Connect a device or start an emulator.
6. Click "Run" or use `Shift+F10`.

#### c. Usage
- Register, log in, and browse vacation homes.

#### d. Troubleshooting
- Ensure backend is running and accessible.
- For emulator, use `10.0.2.2` for backend URLs.
- See `Frontend/Test_Build_Authentication/README.md` for more.

---

### 3. Admin CLI Tool

#### a. Prerequisites
- Python 3.8+

#### b. Setup
```bash
cd Admin-CLI
pip install -r requirements.txt
```

#### c. Run the CLI
```bash
python menu_cli.py
```

#### d. Usage
- Log in with admin credentials.
- Manage users, listings, and reviews via menu.
- Requires backend services to be running.

---

## API Endpoints

### Auth Service (Django)
- `POST /api/auth/register/` — Register new user
- `POST /api/auth/login/` — User login (JWT)
- `POST /api/auth/logout/` — User logout
- `GET /api/auth/user/` — Get user profile
- `GET /api/auth/verify-token/` — Verify JWT token (used by other services)
- `GET /api/auth/verify/<token>/` — Email verification

### Listings Service (Flask)
- `GET    http://localhost:8001/api/listings/` — Get all listings (with filters, pagination)
- `GET    http://localhost:8001/api/listing/<listing_id>/` — Get specific listing
- `GET    http://localhost:8001/api/host/<host_id>/` — Get host information
- `GET    http://localhost:8001/api/host/<host_id>/listings/` — Get all listings for a host
- `POST   http://localhost:8001/api/listing/<listing_id>/review/` — Add review to a listing (requires auth)
- `PUT    http://localhost:8001/api/listing/<listing_id>/review/<review_id>/` — Update a review (requires auth)
- `DELETE http://localhost:8001/api/listing/<listing_id>/review/<review_id>/` — Delete a review (requires auth)
- `GET    http://localhost:8001/api/listing/<listing_id>/reviews/` — Get all reviews for a listing


## Development & Troubleshooting

- **Logs:**  
  ```bash
  docker-compose logs -f
  ```
- **Database Management:**  
  - PostgreSQL:  
    `docker-compose exec db psql -U auth_user -d auth_db`
  - MongoDB:  
    `docker-compose exec mongodb mongosh`
- **Reset Everything:**  
  ```bash
  docker-compose down -v
  docker-compose up --build
  ```
- **Common Issues:**  
  - Ensure ports 8000, 8001 are free.
  - Wait for databases to initialize (30–60s).
  - For Android emulator, use `10.0.2.2` for backend URLs.


## Authors

- **Pavan Sai Kappiri** (Team Lead)(Frontend)
- **Mohammad Shajadul Karim**(Admin-CLI)
- **Shahinur Rahman** (Authentication Microservice)
- **Md Wafiul Abire Aonkon** (Listings Microservice)

