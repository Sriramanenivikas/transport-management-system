# Transport Management System (TMS)

A comprehensive backend system for managing transportation logistics, built with Spring Boot and PostgreSQL.

## Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Setup Instructions](#setup-instructions)
- [Database Schema](#database-schema)
- [API Documentation](#api-documentation)
- [Running Tests](#running-tests)

## Features

- **Load Management**: Create, list, and cancel loads with status tracking
- **Transporter Management**: Register transporters with truck capacity
- **Bidding System**: Submit and manage bids with capacity validation
- **Booking System**: Create bookings with concurrent booking prevention
- **Multi-Truck Allocation**: Support for partial load fulfillment
- **Best Bid Calculation**: Score-based bid ranking

## Technology Stack

- Java 17+
- Spring Boot 3.2+
- Spring Data JPA
- PostgreSQL
- Lombok
- OpenAPI/Swagger UI
- H2 (for testing)

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose (for PostgreSQL)

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd transport-management-system
   ```

2. **Start PostgreSQL with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`

## Database Schema

```
+----------------+       +------------------+       +----------------+
|     LOADS      |       |       BIDS       |       |   TRANSPORTERS |
+----------------+       +------------------+       +----------------+
| load_id (PK)   |<----->| bid_id (PK)      |<----->| transporter_id |
| shipper_id     |       | load_id (FK)     |       | (PK)           |
| loading_city   |       | transporter_id   |       | company_name   |
| unloading_city |       | (FK)             |       | rating         |
| loading_date   |       | proposed_rate    |       +----------------+
| product_type   |       | trucks_offered   |              |
| weight         |       | status           |              |
| weight_unit    |       | submitted_at     |              v
| truck_type     |       +------------------+       +------------------+
| no_of_trucks   |                                 | TRUCK_CAPACITIES |
| status         |                                 +------------------+
| date_posted    |       +------------------+      | id (PK)          |
| version        |       |     BOOKINGS     |      | transporter_id   |
+----------------+       +------------------+      | (FK)             |
        |                | booking_id (PK)  |      | truck_type       |
        |                | load_id (FK)     |      | count            |
        +--------------->| bid_id (FK)      |      +------------------+
                         | transporter_id   |
                         | (FK)             |
                         | allocated_trucks |
                         | final_rate       |
                         | status           |
                         | booked_at        |
                         +------------------+

Relationships:
- LOADS 1:N BIDS
- LOADS 1:N BOOKINGS
- TRANSPORTERS 1:N BIDS
- TRANSPORTERS 1:N BOOKINGS
- TRANSPORTERS 1:N TRUCK_CAPACITIES
- BIDS 1:1 BOOKINGS
```

## API Documentation

### Load APIs

#### 1. Create Load
```http
POST /load
Content-Type: application/json

{
  "shipperId": "SHIP001",
  "loadingCity": "Mumbai",
  "unloadingCity": "Delhi",
  "loadingDate": "2024-12-01T10:00:00",
  "productType": "Electronics",
  "weight": 5000.0,
  "weightUnit": "KG",
  "truckType": "Container",
  "noOfTrucks": 3
}

Response: 201 Created
{
  "loadId": "uuid",
  "shipperId": "SHIP001",
  "loadingCity": "Mumbai",
  "unloadingCity": "Delhi",
  "loadingDate": "2024-12-01T10:00:00",
  "productType": "Electronics",
  "weight": 5000.0,
  "weightUnit": "KG",
  "truckType": "Container",
  "noOfTrucks": 3,
  "status": "POSTED",
  "datePosted": "2024-11-29T12:00:00",
  "remainingTrucks": 3,
  "activeBids": null
}
```

#### 2. Get Loads with Pagination
```http
GET /load?shipperId=SHIP001&status=POSTED&page=0&size=10

Response: 200 OK
{
  "content": [...],
  "pageable": {...},
  "totalElements": 1,
  "totalPages": 1
}
```

#### 3. Get Load by ID
```http
GET /load/{loadId}

Response: 200 OK
{
  "loadId": "uuid",
  ...
  "activeBids": [...]
}
```

#### 4. Cancel Load
```http
PATCH /load/{loadId}/cancel

Response: 200 OK
{
  "loadId": "uuid",
  "status": "CANCELLED",
  ...
}
```

#### 5. Get Best Bids
```http
GET /load/{loadId}/best-bids

Response: 200 OK
[
  {
    "bidId": "uuid",
    "loadId": "uuid",
    "transporterId": "uuid",
    "transporterCompanyName": "ABC Transport",
    "proposedRate": 50000.0,
    "trucksOffered": 2,
    "status": "PENDING",
    "submittedAt": "2024-11-29T12:00:00",
    "score": 0.05
  }
]
```

### Transporter APIs

#### 1. Register Transporter
```http
POST /transporter
Content-Type: application/json

{
  "companyName": "ABC Transport",
  "rating": 4.5,
  "availableTrucks": [
    { "truckType": "Container", "count": 10 },
    { "truckType": "Flatbed", "count": 5 }
  ]
}

Response: 201 Created
{
  "transporterId": "uuid",
  "companyName": "ABC Transport",
  "rating": 4.5,
  "availableTrucks": [...]
}
```

#### 2. Get Transporter by ID
```http
GET /transporter/{transporterId}

Response: 200 OK
{
  "transporterId": "uuid",
  "companyName": "ABC Transport",
  "rating": 4.5,
  "availableTrucks": [...]
}
```

#### 3. Update Transporter Trucks
```http
PUT /transporter/{transporterId}/trucks
Content-Type: application/json

{
  "availableTrucks": [
    { "truckType": "Container", "count": 15 }
  ]
}

Response: 200 OK
{
  "transporterId": "uuid",
  ...
}
```

### Bid APIs

#### 1. Submit Bid
```http
POST /bid
Content-Type: application/json

{
  "loadId": "uuid",
  "transporterId": "uuid",
  "proposedRate": 50000.0,
  "trucksOffered": 2
}

Response: 201 Created
{
  "bidId": "uuid",
  "loadId": "uuid",
  "transporterId": "uuid",
  "transporterCompanyName": "ABC Transport",
  "proposedRate": 50000.0,
  "trucksOffered": 2,
  "status": "PENDING",
  "submittedAt": "2024-11-29T12:00:00"
}
```

#### 2. Get Bids with Filters
```http
GET /bid?loadId=uuid&transporterId=uuid&status=PENDING

Response: 200 OK
[...]
```

#### 3. Get Bid by ID
```http
GET /bid/{bidId}

Response: 200 OK
{
  "bidId": "uuid",
  ...
}
```

#### 4. Reject Bid
```http
PATCH /bid/{bidId}/reject

Response: 200 OK
{
  "bidId": "uuid",
  "status": "REJECTED",
  ...
}
```

### Booking APIs

#### 1. Create Booking
```http
POST /booking
Content-Type: application/json

{
  "bidId": "uuid"
}

Response: 201 Created
{
  "bookingId": "uuid",
  "loadId": "uuid",
  "bidId": "uuid",
  "transporterId": "uuid",
  "transporterCompanyName": "ABC Transport",
  "allocatedTrucks": 2,
  "finalRate": 50000.0,
  "status": "CONFIRMED",
  "bookedAt": "2024-11-29T12:00:00"
}
```

#### 2. Get Booking by ID
```http
GET /booking/{bookingId}

Response: 200 OK
{
  "bookingId": "uuid",
  ...
}
```

#### 3. Cancel Booking
```http
PATCH /booking/{bookingId}/cancel

Response: 200 OK
{
  "bookingId": "uuid",
  "status": "CANCELLED",
  ...
}
```

## Business Rules

1. **Capacity Validation**: Transporters can only bid if they have enough trucks available
2. **Load Status Transitions**: POSTED → OPEN_FOR_BIDS → BOOKED → CANCELLED
3. **Multi-Truck Allocation**: Loads can have multiple bookings until fully allocated
4. **Concurrent Booking Prevention**: Uses optimistic locking to prevent double-booking
5. **Best Bid Calculation**: score = (1 / proposedRate) * 0.7 + (rating / 5) * 0.3

## Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## License

Apache 2.0