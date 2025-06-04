# Distributed Vacation HBS - Listings Microservice

## Authors
- **Listings Microservice**: Wafiul Abire Aonkon
- **Auth Microservice**: Shahinur Rahman

## Description
This is the Listings Microservice for the Distributed Vacation HBS (Hotel Booking System). It handles all property listing-related operations including property management, search, bookings, and reviews.

## Features
- Property listing management (CRUD operations)
- Advanced search and filtering
- Booking management
- Review and rating system
- Image upload and management
- Integration with other microservices

## Prerequisites
- Python 3.8 or higher
- Docker and Docker Compose
- MongoDB
- AWS S3 account (for image storage)

## Installation and Setup

### Using Docker (Recommended)
1. Clone the repository:
```bash
git clone <repository-url>
cd listings_service
```

2. Build and start the containers:
```bash
docker-compose up --build
```

3. The service will be available at `http://localhost:5000`

### Manual Setup
1. Create and activate a virtual environment:
```bash
python -m venv env
source env/bin/activate  # On Windows: env\Scripts\activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Set up environment variables:
```bash
cp .env.example .env
# Edit .env with your configuration
```

4. Start the development server:
```bash
python app.py
```

## API Endpoints

### Listings
- `GET /listings` - Get all listings
- `POST /listings` - Create new listing
- `GET /listings/{id}` - Get specific listing
- `PUT /listings/{id}` - Update listing
- `DELETE /listings/{id}` - Delete listing

### Search
- `GET /listings/search` - Search listings with filters
- `GET /listings/filter` - Filter listings by criteria

### Bookings
- `POST /listings/{id}/book` - Book a listing
- `GET /bookings` - Get user's bookings
- `PUT /bookings/{id}` - Update booking
- `DELETE /bookings/{id}` - Cancel booking

### Reviews
- `POST /listings/{id}/review` - Add review
- `GET /listings/{id}/reviews` - Get listing reviews
- `PUT /reviews/{id}` - Update review
- `DELETE /reviews/{id}` - Delete review


## Environment Variables
- `FLASK_APP`: Flask application entry point
- `FLASK_ENV`: Development/Production mode
- `MONGODB_URI`: MongoDB connection string
- `AWS_ACCESS_KEY`: AWS access key
- `AWS_SECRET_KEY`: AWS secret key
- `AWS_BUCKET_NAME`: S3 bucket name
- `JWT_SECRET_KEY`: Secret key for JWT tokens
