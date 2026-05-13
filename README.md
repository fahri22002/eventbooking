# Event Booking Management API

A RESTful backend service for managing events and ticket bookings, built for the PT Agora Techno Solution Backend Developer Challenge.

## Tech Stack
* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Build Tool:** Gradle
* **Database:** PostgreSQL
* **Security:** Spring Security & JWT Authentication
* **Migration:** Flyway / Liquibase *(To be configured)*

## Architecture & Project Structure
This project enforces a clean, layered architecture to maintain clear project boundaries and strict separation of concerns, ensuring high maintainability and robust documentation:
* **Controller:** Handles incoming HTTP requests and API routing.
* **Service:** Contains core business logic and orchestration.
* **Repository:** Manages database interactions via Spring Data JPA.
* **Entity:** Represents database models.
* **DTO:** Data Transfer Objects to separate API contracts from internal models.

## Branching Strategy
This project follows a feature-branch workflow to ensure an organized commit history:
* `main`: Stable production-ready code.
* `feature/*`: New features or setup tasks (e.g., `feature/database-setup`, `feature/user-auth`).

## Local Setup Instructions

### Prerequisites
* Java 21 installed
* PostgreSQL installed and running (or via Docker)
* Gradle

### Running the Application
1. Clone this repository:
   ```bash
   git clone https://github.com/fahri22002/eventbooking.git


## Database Schema & Constraints
The application uses PostgreSQL as its primary database, named `eventbooking`. The schema is composed of three main entities designed with strict constraints to ensure data integrity:

### 1. User
Stores user credentials and profile details.
* **Primary Key:** `userId` (Text/UUID)
* **Constraints:** * `email` must be **UNIQUE** to prevent duplicate registrations.

### 2. Event
Manages event details, capacity, and active status for soft deletion.
* **Primary Key:** `eventId` (Text/UUID)
* **Foreign Key:** `creatorId` references `User(userId)`. This ensures only the user who created the event has the authorization to modify or soft-delete it.
* **Constraints:** * `creatorId` cannot be null.

### 3. Booking
Acts as the transactional entity between Users and Events.
* **Primary Key:** `bookingId` (Text/UUID)
* **Foreign Keys:**
    * `eventId` references `Event(eventId)`
    * `userId` references `User(userId)`
* **Constraints:**
    * `bookingReference` must be **UNIQUE**. This serves as the immutable reference number returned to the customer upon successful reservation.
    * Ensures `eventId` and `userId` are explicitly mapped and cannot be null.

### Service Layer (Business Logic)
The Service layer handles the core orchestration of the application.
* **Transaction Management:** Uses `@Transactional` to ensure atomicity during complex operations like user registration and booking.
* **Security Integration:** Directly integrates with Spring Security's `PasswordEncoder` to ensure that raw passwords never touch the database, adhering to strict security standards.
* **Separation of Concerns:** Business rules (e.g., checking for duplicate emails or validating booking deadlines) are isolated here, keeping Controllers lean and focused solely on request handling.

### Exception Handling
A centralized `@RestControllerAdvice` is implemented to intercept and format all exceptions. This guarantees that API clients receive consistent and predictable JSON error structures, including field-level validation details for invalid request payloads.

### Event Management Logic
* **Context-Aware Creation:** Event creation automatically associates the event with the currently authenticated user by extracting their identity from the Security Context.
* **Validation:** Strict future-date validation and capacity constraints are enforced at the DTO level.

