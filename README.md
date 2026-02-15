# Library Management System

Enterprise library management system built with **Spring Boot 3.3.7** and **Java 21**.

## 🚀 Tech Stack

**Backend Core**
- Java 21 + Spring Boot 3.3.7
- Spring Data JPA + Hibernate 
- H2 Database (development) / PostgreSQL-ready (production)

**Security & Authentication**
- Spring Security + JWT (JJWT 0.12.6)
- Stateless token-based authentication
- Role-based access control (USER/ADMIN)

**Payments & Billing**
- **Stripe** payment gateway 
- Secure checkout sessions
- Asynchronous webhook confirmation
- Support for subscriptions and fines

**Notifications**
- Spring Mail + SMTP (Gmail) 
- Thymeleaf email templates 

## 📦 Architecture

```
src/main/java/com/pm/librarymanagementsystem/
├── configurations/    # Security, JWT, Stripe
├── controller/        # REST endpoints (admin/, users/)
├── domain/           # Enums (PaymentStatus, PaymentType, etc.)
├── exception/        # Global error handling
├── mapper/           # DTO ↔ Entity transformations
├── modal/            # JPA entities
├── payload/dto/      # Request/Response DTOs
├── repository/       # Spring Data repositories
├── scheduler/        # Scheduled tasks (@Scheduled)
├── security/         # JWT provider & utilities
├── service/          # Business interfaces
│   ├── impl/        # Implementations
│   ├── gateway/     # Stripe integration
│   └── webhook/     # Webhook handlers
```


**Pattern**: Layered architecture (Controller → Service → Repository → Entity)

## ⚙️ Configuration

### Environment Variables
```bash
JWT_SECRET=your-256-bit-secret-key
MAIL_PASSWORD=gmail-app-password
ADMIN_EMAIL=admin@library.com
ADMIN_PASSWORD=secure-admin-password
``` 

### Database
- **Development**: H2 in-memory (`jdbc:h2:mem:librarydb`)  
- **H2 Console**: `http://localhost:8080/h2-console`
- **Production**: Migrate to PostgreSQL/MySQL (JPA-compatible)

### Running the Application
```bash
# Clone
git clone https://github.com/Gerardoprogramer/Library-Management-System.git

# Set environment variables
export JWT_SECRET="..." MAIL_PASSWORD="..." ADMIN_EMAIL="..." ADMIN_PASSWORD="..."

# Run
mvn spring-boot:run
```


Application available at `http://localhost:8080`

## 🔐 Security

- **Stateless JWT**: No server-side sessions
- **BCrypt**: Password hashing 
- **CORS**: Configured for specific frontend 
- **JWT Validation**: Custom pre-authentication filter

## 💳 Payment System

**Payment Flow**:
1. Client initiates payment → `POST /api/v1/payments/initiate` 
2. Backend creates Stripe Checkout Session 
3. User completes payment on Stripe
4. Webhook confirms payment → updates status

**Features**:
- Webhook idempotency (prevents duplicates)  
- Refund support 
- Automatic subscription renewal 

## 📊 Key Features

**Catalog Management**
- Book CRUD with ISBN validation
- Hierarchical genres (parent/child)
- Soft delete 

**Circulation**
- Loans with status tracking
- Reservation queue system 
- Reviews (only for returned books)
- Personal wishlist

**Billing**
- Fines for late/damaged/lost items
- Subscription plans
- Payment history

## 🔄 Scheduled Tasks

- Automatic subscription renewal 
- Email notifications

## 📝 API Endpoints

**Public**
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/signup` - Registration
- `POST /api/v1/payments/webhook` - Stripe webhook 

**Authenticated (USER)**
- `GET /api/v1/books` - List books
- `POST /api/v1/reservations` - Create reservation
- `POST /api/v1/wishlist/{bookId}` - Add to wishlist 
- `POST /api/v1/payments/initiate` - Initiate payment

**Admin**
- `POST /api/v1/admin/books` - Create book
- `POST /api/v1/admin/reservations/user/{userId}` - Create reservation for user

## 🛡️ Validations

- Unique ISBN per book
- Maximum 5 active reservations per user 
- Only review returned books
- Non-duplicable fines

## Notes

This README is optimized for production, focusing on key technical aspects: architecture, security, Stripe payment integration, and deployment configuration. Internal implementation details were omitted, prioritizing information relevant for DevOps, architects, and developers who need to understand the system quickly.
