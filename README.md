# Transport Management System (TMS) - Backend API

A comprehensive backend system for managing transport logistics with Load, Transporter, Bid, and Booking management featuring concurrent operations and optimistic locking.

## 📋 Table of Contents
- [Tech Stack](#tech-stack)
- [Database Schema](#database-schema)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Business Rules](#business-rules)
- [Exception Handling](#exception-handling)
- [Testing](#testing)
- [Project Structure](#project-structure)

## 🛠️ Tech Stack
| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.2.0 |
| Spring Data JPA | Hibernate ORM |
| PostgreSQL | 14+ |
| Maven | 3.9+ |
| OpenAPI/Swagger | 2.3.0 |

 

### Entity Relationship Diagram
<img width="980" height="558" alt="2025-12-02_16-55-34" src="https://github.com/user-attachments/assets/63fa301f-1072-4283-951d-4eddbe0c4638" />

```

### Database Tables DDL

```sql
-- Transporters Table
CREATE TABLE transporters (
    transporter_id SERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    rating DECIMAL(2,1) CHECK (rating >= 1.0 AND rating <= 5.0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0  -- Optimistic locking
);

-- Truck Capacity Table (One-to-Many with Transporters)
CREATE TABLE truck_capacity (
    id SERIAL PRIMARY KEY,
    transporter_id INTEGER NOT NULL REFERENCES transporters(transporter_id) ON DELETE CASCADE,
    truck_type VARCHAR(50) NOT NULL,
    count INTEGER NOT NULL CHECK (count >= 0),
    version INTEGER DEFAULT 0,
    UNIQUE(transporter_id, truck_type)
);

-- Loads Table
CREATE TABLE loads (
    load_id SERIAL PRIMARY KEY,
    shipper_id VARCHAR(100) NOT NULL,
    loading_city VARCHAR(100) NOT NULL,
    unloading_city VARCHAR(100) NOT NULL,
    loading_date TIMESTAMP NOT NULL,
    product_type VARCHAR(100) NOT NULL,
    weight DECIMAL(10,2) NOT NULL,
    weight_unit VARCHAR(10) NOT NULL CHECK (weight_unit IN ('KG', 'TON')),
    truck_type VARCHAR(50) NOT NULL,
    no_of_trucks INTEGER NOT NULL CHECK (no_of_trucks > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'POSTED' 
           CHECK (status IN ('POSTED', 'OPEN_FOR_BIDS', 'BOOKED', 'CANCELLED')),
    date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0  -- Optimistic locking
);

-- Indexes for performance
CREATE INDEX idx_loads_shipper ON loads(shipper_id);
CREATE INDEX idx_loads_status ON loads(status);
CREATE INDEX idx_loads_loading_date ON loads(loading_date);

-- Bids Table
CREATE TABLE bids (
    bid_id SERIAL PRIMARY KEY,
    load_id INTEGER NOT NULL REFERENCES loads(load_id) ON DELETE CASCADE,
    transporter_id INTEGER NOT NULL REFERENCES transporters(transporter_id) ON DELETE CASCADE,
    proposed_rate DECIMAL(10,2) NOT NULL,
    trucks_offered INTEGER NOT NULL CHECK (trucks_offered > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
           CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bids_load ON bids(load_id);
CREATE INDEX idx_bids_transporter ON bids(transporter_id);
CREATE INDEX idx_bids_status ON bids(status);

-- Bookings Table
CREATE TABLE bookings (
    booking_id SERIAL PRIMARY KEY,
    load_id INTEGER NOT NULL REFERENCES loads(load_id) ON DELETE CASCADE,
    bid_id INTEGER NOT NULL REFERENCES bids(bid_id) ON DELETE CASCADE,
    transporter_id INTEGER NOT NULL REFERENCES transporters(transporter_id) ON DELETE CASCADE,
    allocated_trucks INTEGER NOT NULL CHECK (allocated_trucks > 0),
    final_rate DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' 
           CHECK (status IN ('CONFIRMED', 'COMPLETED', 'CANCELLED')),
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookings_load ON bookings(load_id);
CREATE INDEX idx_bookings_transporter ON bookings(transporter_id);
CREATE INDEX idx_bookings_status ON bookings(status);
```

## 🚀 Setup Instructions

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 14+

### 1. Database Setup
```bash
# Create database
psql -U postgres -c "CREATE DATABASE tms;"

# Run schema setup
psql -U postgres -d tms -f complete-database-setup.sql
```

### 2. Configure Application
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tms
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build and Run
```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/transport-management-system-1.0.0.jar

# Application starts at http://localhost:8080
```

### 4. Access Swagger UI
Open browser: **http://localhost:8080/swagger-ui.html**

## 📚 API Documentation

### Swagger/OpenAPI
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Load APIs (5 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/load` | Create a new load (status=POSTED) |
| GET | `/load` | Get loads with pagination & filters |
| GET | `/load/{loadId}` | Get load by ID with active bids |
| PATCH | `/load/{loadId}/cancel` | Cancel a load |
| GET | `/load/{loadId}/best-bids` | Get sorted bid suggestions |

#### Create Load
```http
POST /load
Content-Type: application/json

{
  "shipperId": "SHIP001",
  "loadingCity": "Mumbai",
  "unloadingCity": "Delhi",
  "loadingDate": "2025-12-10T08:00:00",
  "productType": "Electronics",
  "weight": 5000.0,
  "weightUnit": "KG",
  "truckType": "CONTAINER-20FT",
  "noOfTrucks": 3
}
```

#### Get Loads (Paginated)
```http
GET /load?shipperId=SHIP001&status=POSTED&page=0&size=10
```

### Transporter APIs (3 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transporter` | Register transporter with trucks |
| GET | `/transporter/{transporterId}` | Get transporter details |
| PUT | `/transporter/{transporterId}/trucks` | Update truck capacity |

#### Create Transporter
```http
POST /transporter
Content-Type: application/json

{
  "companyName": "Fast Logistics",
  "rating": 4.5,
  "availableTrucks": [
    {"truckType": "CONTAINER-20FT", "count": 10},
    {"truckType": "CONTAINER-40FT", "count": 5}
  ]
}
```

### Bid APIs (4 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/bid` | Submit bid (validates capacity & status) |
| GET | `/bid` | Filter bids by loadId/transporterId/status |
| GET | `/bid/{bidId}` | Get bid details |
| PATCH | `/bid/{bidId}/reject` | Reject a bid |

#### Create Bid
```http
POST /bid
Content-Type: application/json

{
  "loadId": 1,
  "transporterId": 2,
  "proposedRate": 52000.0,
  "trucksOffered": 3
}
```

### Booking APIs (3 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/booking` | Accept bid & create booking |
| GET | `/booking/{bookingId}` | Get booking details |
| PATCH | `/booking/{bookingId}/cancel` | Cancel booking (restores trucks) |

#### Create Booking
```http
POST /booking
Content-Type: application/json

{
  "bidId": 2,
  "allocatedTrucks": 3
}
```

## 📋 Business Rules

### Rule 1: Capacity Validation 
- Bid: `trucksOffered ≤ availableTrucks` for truck type
- Booking: Deducts trucks from transporter's capacity
- Cancel: Restores trucks to available pool

### Rule 2: Load Status Transitions 
```
POSTED → OPEN_FOR_BIDS (first bid received)
OPEN_FOR_BIDS → BOOKED (fully allocated)
BOOKED → OPEN_FOR_BIDS (booking cancelled)
Any → CANCELLED (explicit cancellation, if not BOOKED)
```

### Rule 3: Multi-Truck Allocation 
- `remainingTrucks = noOfTrucks - SUM(allocatedTrucks)`
- Multiple bookings allowed until fully allocated
- Load becomes BOOKED when `remainingTrucks == 0`

### Rule 4: Concurrent Booking Prevention 
- `@Version` column on Load, Transporter, TruckCapacity
- First transaction wins on concurrent booking
- Second fails with `409 Conflict`

### Rule 5: Best Bid Calculation 
```
score = (1 / proposedRate) * 0.7 + (rating / 5) * 0.3
```
Higher score = better bid

## ⚠️ Exception Handling

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `ResourceNotFoundException` | 404 | Entity not found |
| `InvalidStatusTransitionException` | 400 | Invalid status change |
| `InsufficientCapacityException` | 400 | Not enough trucks |
| `LoadAlreadyBookedException` | 409 | Booking conflict |
| `OptimisticLockException` | 409 | Concurrent modification |

## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
```

### Test Coverage
- **Unit Tests**: Service layer (LoadService, BidService, BookingService, TransporterService)
- **Controller Tests**: REST endpoint validation
- **Coverage Areas**:
  - ✅ All 15 API endpoints
  - ✅ Capacity validation
  - ✅ Status transitions
  - ✅ Multi-truck allocation
  - ✅ Concurrency handling
  - ✅ Error scenarios

### Concurrency Test (Manual)
```bash
# Terminal 1 & 2: Simultaneously book same load
curl -X POST http://localhost:8080/booking \
  -H "Content-Type: application/json" \
  -d '{"bidId":1,"allocatedTrucks":3}'
```
**Expected**: One succeeds (201), one fails (409)

## 📁 Project Structure
```
src/
├── main/java/com/tms/
│   ├── config/
│   │   └── OpenApiConfig.java
│   ├── controller/
│   │   ├── LoadController.java
│   │   ├── TransporterController.java
│   │   ├── BidController.java
│   │   └── BookingController.java
│   ├── service/
│   │   ├── LoadService.java
│   │   ├── TransporterService.java
│   │   ├── BidService.java
│   │   └── BookingService.java
│   ├── repository/
│   │   ├── LoadRepository.java
│   │   ├── TransporterRepository.java
│   │   ├── TruckCapacityRepository.java
│   │   ├── BidRepository.java
│   │   └── BookingRepository.java
│   ├── entity/
│   │   ├── Load.java
│   │   ├── Transporter.java
│   │   ├── TruckCapacity.java
│   │   ├── Bid.java
│   │   └── Booking.java
│   ├── dto/
│   │   ├── LoadRequest.java / LoadResponse.java
│   │   ├── TransporterRequest.java / TransporterResponse.java
│   │   ├── BidRequest.java / BidResponse.java
│   │   └── BookingRequest.java / BookingResponse.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── InvalidStatusTransitionException.java
│   │   ├── InsufficientCapacityException.java
│   │   └── LoadAlreadyBookedException.java
│   └── TransportManagementSystemApplication.java
├── test/java/com/tms/
│   ├── service/
│   │   ├── LoadServiceTest.java
│   │   ├── BidServiceTest.java
│   │   ├── BookingServiceTest.java
│   │   └── TransporterServiceTest.java
│   └── controller/
│       ├── LoadControllerTest.java
│       └── BookingControllerTest.java
└── resources/
    └── application.properties
```

## 🎯 Design Decisions

### 1. Optimistic Locking
Used `@Version` instead of pessimistic locking for better performance with low contention scenarios.

### 2. Transaction Management
`@Transactional` on service methods ensuring atomicity across booking creation, capacity deduction, and status updates.

### 3. DTO Pattern
Separate request/response DTOs to decouple API contract from internal entity structure.

### 4. Global Exception Handler
Consistent error responses with meaningful HTTP status codes via `@ControllerAdvice`.

### 5. Database Indexes
Strategic indexes on frequently queried columns (shipper_id, status, load_id, transporter_id).

## 👤 Author
**Vikas**

## 📧 Contact
- Email: careers@cargopro.ai

