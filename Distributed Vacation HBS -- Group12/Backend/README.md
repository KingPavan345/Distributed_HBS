# Distributed Vacation HBS (Hotel Booking System)

## Authors
- **Auth Microservice**: Shahinur Rahman
- **Listings Microservice**: Wafiul Abire Aonkon

## Project Overview
This is a distributed Hotel Booking System built using microservices architecture. The system consists of two main microservices:
1. Authentication Service (Django)
2. Listings Service (Flask)

## System Architecture
```
┌─────────────────┐     ┌─────────────────┐
│  Auth Service   │     │ Listings Service│
│  (Django)       │     │ (Flask)         │
│  Port: 8000     │     │ Port: 8001      │
└────────┬────────┘     └────────┬────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│  PostgreSQL     │     │    MongoDB      │
│  Port: 5432     │     │    Port: 27017  │
└─────────────────┘     └─────────────────┘
```

## Prerequisites
- Docker and Docker Compose
- Git
- At least 4GB of free RAM
- 10GB of free disk space

## Quick Start
1. Clone the repository:
```bash
git clone <repository-url>
cd aws-deployment
```

2. Start all services:
```bash
docker-compose up --build
```

3. Access the services:
- Auth Service: http://localhost:8000
- Listings Service: http://localhost:8001

## Services Overview

### 1. Authentication Service
- **Port**: 8000
- **Framework**: Django
- **Database**: PostgreSQL
- **Features**:
  - User registration and authentication
  - JWT token management
  - Email verification
  - Password reset
  - User profile management

### 2. Listings Service
- **Port**: 8001
- **Framework**: Flask
- **Database**: MongoDB
- **Features**:
  - Property listings management
  - Search and filtering
  - Reviews and ratings
  - Host management
  - Image handling

## Docker Compose Configuration

### Services
```yaml
services:
  auth-service:
    build: ./auth_microservice
    ports:
      - "8000:8000"
    depends_on:
      - db
    environment:
      - DATABASE_URL=postgresql://auth_user:auth_password@db:5432/auth_db
      - DJANGO_SECRET_KEY=vhbs@12345
      - DEBUG=True

  listings-service:
    build: ./listings_service
    ports:
      - "8001:8001"
    depends_on:
      - mongodb
    environment:
      - MONGODB_URI=mongodb://mongodb:27017/listings
      - FLASK_SECRET_KEY=vhbs@12345

  db:
    image: postgres:13
    environment:
      - POSTGRES_DB=auth_db
      - POSTGRES_USER=auth_user
      - POSTGRES_PASSWORD=auth_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:latest
    volumes:
      - mongodb_data:/data/db
```

### Volumes
- `postgres_data`: Persistent storage for PostgreSQL
- `mongodb_data`: Persistent storage for MongoDB

## Environment Variables

### Auth Service
- `DATABASE_URL`: PostgreSQL connection string
- `DJANGO_SECRET_KEY`: Django secret key
- `DEBUG`: Development mode flag
- `EMAIL_HOST`: SMTP server
- `EMAIL_PORT`: SMTP port
- `EMAIL_HOST_USER`: SMTP username
- `EMAIL_HOST_PASSWORD`: SMTP password

### Listings Service
- `MONGODB_URI`: MongoDB connection string
- `FLASK_SECRET_KEY`: Flask secret key
- `AWS_ACCESS_KEY`: AWS access key (for image storage)
- `AWS_SECRET_KEY`: AWS secret key
- `AWS_BUCKET_NAME`: S3 bucket name

## API Documentation

### Auth Service Endpoints
- `POST /api/auth/register/` - Register new user
- `POST /api/auth/login/` - User login
- `POST /api/auth/logout/` - User logout
- `GET /api/auth/user/` - Get user profile

### Listings Service Endpoints
- `GET /api/listings/` - Get all listings
- `GET /api/listing/<id>/` - Get specific listing
- `GET /api/host/<id>/` - Get host information
- `POST /api/listing/<id>/review/` - Add review

## Development

### Running Tests
```bash
# Auth Service Tests
docker-compose exec auth-service python manage.py test

# Listings Service Tests
docker-compose exec listings-service python -m pytest
```

### Viewing Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service
docker-compose logs -f listings-service
```

### Database Management
```bash
# PostgreSQL
docker-compose exec db psql -U auth_user -d auth_db

# MongoDB
docker-compose exec mongodb mongosh
```

## Troubleshooting

### Common Issues
1. **Port Conflicts**
   - Ensure ports 8000 and 8001 are not in use
   - Check with `netstat -an | grep 8000` and `netstat -an | grep 8001`

2. **Database Connection Issues**
   - Wait for databases to initialize (30-60 seconds)
   - Check logs: `docker-compose logs db` or `docker-compose logs mongodb`

3. **Service Startup Order**
   - Services are configured to wait for databases
   - Check logs if services fail to start

### Reset Everything
```bash
# Stop all services
docker-compose down

# Remove all volumes
docker-compose down -v

# Rebuild and start
docker-compose up --build
```
