# Smart Billing System - Web UI Version

A complete stationery shop billing system with Spring Boot, JDBC, and MySQL.

## Branches
- `main` - Terminal-based version (original)
- `web-ui` - Web-based version with Spring Boot + HTML/CSS/JS

## Quick Start

### Prerequisites
- Java JDK 17+
- MySQL 8.0+
- Maven 3.6+ (or use mvnw wrapper)

### Setup
1. Clone the repository
2. Create database: `stationery_billing`
3. Run `database/schema.sql` in MySQL
4. Update `DBConfig.java` with your MySQL password
5. Run: `mvn spring-boot:run` or `./mvnw spring-boot:run`

### Access
- Customer UI: http://localhost:8080/customer.html
- Admin UI: http://localhost:8080/admin.html

## Features
- Category-based product browsing
- Shopping cart management
- Bill generation with QR expiry
- Payment processing
- Stock management with audit logs
- Admin dashboard with statistics