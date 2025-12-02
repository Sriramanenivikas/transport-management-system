# Transport Management System (TMS)

A Spring Boot backend for managing transport logistics - loads, transporters, bids, and bookings.

## Tech Stack

- Java 17, Spring Boot 3.2.0
- PostgreSQL 14+, Spring Data JPA
- Maven 3.9+

## Quick Start

```bash
# Database
psql -U postgres -c "CREATE DATABASE tms;"
psql -U postgres -d tms -f complete-database-setup.sql

# Run
mvn clean package -DskipTests
java -jar target/transport-management-system-1.0.0.jar
```

App runs on http://localhost:8080

## Database Schema

![ER Diagram](https://github.com/user-attachments/assets/a5edae36-5825-4eae-8a6b-8bf27ea26d8a)

**Tables:** transporters, truck_capacity, loads, bids, bookings

Key constraints:
- Foreign keys with CASCADE delete
- Check constraints on status fields, rating (1-5), weight units (KG/TON)
- Indexes on shipper_id, status, load_id, transporter_id
- Version column for optimistic locking

## APIs

**Swagger:** http://localhost:8080/swagger-ui.html

**Postman:** Import `TMS-Postman-Collection.json`

### Endpoints (15 total)

| Load | Transporter | Bid | Booking |
|------|-------------|-----|---------|
| POST /load | POST /transporter | POST /bid | POST /booking |
| GET /load | GET /transporter/{id} | GET /bid | GET /booking/{id} |
| GET /load/{id} | PUT /transporter/{id}/trucks | GET /bid/{id} | PATCH /booking/{id}/cancel |
| PATCH /load/{id}/cancel | | PATCH /bid/{id}/reject | |
| GET /load/{id}/best-bids | | | |

## Business Logic

**Capacity:** Transporter can only bid if they have enough trucks. Booking deducts trucks, cancellation restores them.

**Load Status Flow:**
```
POSTED → OPEN_FOR_BIDS → BOOKED
                ↑______________|  (on cancel)
```

**Multi-truck:** If load needs 3 trucks, multiple transporters can book until all 3 allocated.

**Concurrency:** @Version prevents double-booking. First transaction wins, second gets 409 Conflict.

**Best Bid Score:** `(1/rate) * 0.7 + (rating/5) * 0.3`

## Testing

```bash
mvn test
```

![Test Results](https://github.com/user-attachments/assets/test-coverage-placeholder)

43 tests covering:
- Service layer unit tests
- Concurrency/locking integration tests
- All business rules validation

## Project Structure

```
src/main/java/com/tms/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── entity/         # JPA entities
├── dto/            # Request/Response objects
└── exception/      # Custom exceptions + handler
```

## Error Handling

| Status | Exception |
|--------|-----------|
| 400 | InvalidStatusTransitionException, InsufficientCapacityException |
| 404 | ResourceNotFoundException |
| 409 | LoadAlreadyBookedException, OptimisticLockException |
