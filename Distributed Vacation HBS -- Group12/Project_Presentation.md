# Distributed Vacation HBS (Hotel Booking System)
## Group 12 - Comprehensive Project Presentation

---

## 🏗️ **Project Overview**

### **What is Distributed Vacation HBS?**
A **distributed vacation home booking system** built using **microservices architecture** that allows users to browse, search, and book vacation properties.

### **Key Features**
- 🔐 **User Authentication & Authorization**
- 🏠 **Property Listings Management**
- 🔍 **Advanced Search & Filtering**
- ⭐ **Reviews System**
- 📱 **Android Mobile Application**
- 🛠️ **Admin CLI Management Tool**



### **Technology Stack**
- **Backend**: Django (Auth) + Flask (Listings)
- **Databases**: PostgreSQL + MongoDB
- **Frontend**: Android (Kotlin)
- **Admin Tool**: Python CLI
- **Containerization**: Docker & Docker Compose

---

## 🔧 **Backend Microservices**

### **1. Authentication Service (Django)**
**Location**: `Backend/auth_microservice/`

#### **Key Components**
- **Framework**: Django REST Framework
- **Database**: PostgreSQL
- **Authentication**: JWT Tokens
- **Port**: 8000

#### **API Endpoints**
```python
POST /api/auth/register/     # User registration
POST /api/auth/login/        # User login (JWT)
POST /api/auth/logout/       # User logout
GET  /api/auth/user/         # Get user profile
GET  /api/auth/verify-token/ # Verify JWT token
GET  /api/auth/verify/<token>/ # Email verification
```

#### **Key Features**
- ✅ **User Registration & Login**
- ✅ **JWT Token Management**
- ✅ **Password Validation**
- ✅ **Admin User Management**


### **2. Listings Service (Flask)**
**Location**: `Backend/listings_service/`

#### **Key Components**
- **Framework**: Flask
- **Database**: MongoDB (sample_airbnb dataset)
- **Authentication**: JWT Token Verification
- **Port**: 8001

#### **API Endpoints**
```python
GET    /api/listings/                    # Get all listings (with filters)
GET    /api/listing/<listing_id>/        # Get specific listing
GET    /api/host/<host_id>/              # Get host information
GET    /api/host/<host_id>/listings/     # Get host's listings
POST   /api/listing/<id>/review/         # Add review (auth required)
PUT    /api/listing/<id>/review/<id>/    # Update review (auth required)
DELETE /api/listing/<id>/review/<id>/    # Delete review (auth required)
GET    /api/listing/<id>/reviews/        # Get listing reviews
```

#### **Advanced Features**
- 🔍 **Multi-criteria Search & Filtering**
- 📄 **Pagination Support**
- 🏷️ **Property Type Filtering**
- 💰 **Price Range Filtering**
- 🛏️ **Bedroom Count Filtering**
- 🌍 **Country-based Filtering**


## 📱 **Android Frontend**

### **Location**: `Frontend/Test_Build_Authentication/`

#### **Technology Stack**
- **Language**: Kotlin
- **Architecture**: MVVM Pattern
- **Networking**: Volley + Retrofit
- **UI**: Material Design Components
- **Target SDK**: API 34

#### **Key Activities**
1. **MainActivity** - User Registration
2. **LoginActivity** - User Authentication
3. **VacationHomeListActivity** - Property Browsing
4. **VacationHomeDetailActivity** - Property Details
5. **GuestViewActivity** - Guest Dashboard
Disabled: **EmailVerificationActivity** - Email Verification

#### **Features**
- ✅ **User Registration & Login**
- ✅ **JWT Token Management**
- ✅ **Property Listings Display**
- ✅ **Advanced Search & Filtering**
- ✅ **Property Details View**
- ✅ **Review System**
- ✅ **Responsive UI Design**


## 🛠️ **Admin CLI Tool**

### **Location**: `Admin-CLI/`

#### **Technology Stack**
- **Language**: Python 3.8+
- **UI**: Rich Console Interface
- **Dependencies**: requests, rich, tabulate

#### **Features**
- 👥 **User Management** (CRUD operations)
- 🏠 **Listing Management**
- ⭐ **Review Management**
- 🔐 **Admin Authentication**
- 📊 **Data Visualization**


## 🐳 **Docker & Containerization**

### **Key Benefits**
- ✅ **Consistent Environment**
- ✅ **Easy Deployment**
- ✅ **Service Isolation**
- ✅ **Scalability**
- ✅ **Health Checks**

---

## 🔐 **Security Features**

### **Authentication & Authorization**
- **JWT Token-based Authentication**
- **Token Verification Between Services**
- **Password Validation Rules**
- **Email Verification System**
- **Admin Role Management**

## ⚖️ Distributed System Optimization: Key Trade-offs

**Advantages:**
- Microservices: Independent scaling, clear separation
- Stateless JWT Auth: Easy scaling, no session storage
- Docker: Fast deployment
- MongoDB (Listings): Flexible, horizontally scalable
- PostgreSQL (Auth): Strong consistency
- Synchronous Calls: Simple, easy to debug
- App: Connection pooling, efficient HTTP, async UI

**Disadvantages / Risks:**
- Microservices: More complex ops, network overhead
- JWT Auth: Blocking token checks, single point of failure (SPoF)
- MongoDB: No multi-document transactions
- PostgreSQL: Can bottleneck if overloaded
- Synchronous Calls: Latency, single point of failure
- No caching or rate limiting: Higher latency, risk of overload
- No event-driven/circuit breaker: No resilience to service failures
- App: No offline queue, no push updates

**Risks:**
- Auth service downtime blocks all protected endpoints.
- Synchronous calls and lack of caching increase latency and load.
---

## 🚀 **Deployment Instructions**


# 1. Start Backend Services
cd Backend
docker-compose up --build

# 2. Start Android App
# Open in Android Studio: Frontend/Test_Build_Authentication

# 3. Start Admin CLI
cd Admin-CLI
pip install -r requirements.txt
python menu_cli.py
```


## 👥 **Team Members & Contributions**

### **Team Structure**
- **Pavan Sai Kappiri** (Team Lead) - Android Frontend
- **Mohammad Shajadul Karim** - Admin CLI Tool
- **Shahinur Rahman** - Authentication Microservice
- **Md Wafiul Abire Aonkon** - Listings Microservice

---

### **Technical Improvements**
- **GraphQL API** - More efficient data fetching
- **Redis Caching** - Performance optimization
- **Message Queues** - Asynchronous processing
- **Microservices Communication** - Event-driven architecture
- **Kubernetes Deployment** - Advanced orchestration

### **Documentation**
- **Main README**: Complete setup and usage instructions
- **Backend README**: Microservices architecture details
- **Frontend README**: Android app setup guide
- **Admin CLI README**: CLI tool usage instructions

---

## 📚 References

- [Android Studio Developer Documentation](https://developer.android.com/studio)
- [OpenAI ChatGPT](https://chat.openai.com/) (for icon generation and content assistance)
- [Flask Documentation](https://flask.palletsprojects.com/)
- [Django Documentation](https://docs.djangoproject.com/)
- [MongoDB Documentation](https://www.mongodb.com/docs/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [AWS Documentation](https://docs.aws.amazon.com/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [OkHttp](https://square.github.io/okhttp/)
- [Retrofit](https://square.github.io/retrofit/)

---

*Thank you for your attention! Questions and feedback are welcome.* 