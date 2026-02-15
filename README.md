# Library Management System  
  
A comprehensive library management system built with Spring Boot that enables libraries to manage their book inventory, user accounts, loans, reservations, and user engagement features.  
  
## Features  
  
### Core Library Operations  
- **Book Management**: Complete CRUD operations for managing the library's book catalog   
- **Book Loans**: Track book checkouts with due dates, renewals, and overdue management  
- **Reservations**: Queue system for books that are currently unavailable   
- **Fine Management**: Automated fine calculation for overdue books and damages  
  
### User Engagement  
- **Wishlist**: Users can save books for future reading  
- **Book Reviews**: Rate and review books (requires completed loan)  
  
## Technology Stack  
  
- **Backend**: Java, Spring Boot  
- **Database**: JPA/Hibernate with relational database  
- **Security**: JWT-based authentication  
- **Validation**: Jakarta Bean Validation  
  
## Key Business Rules  
  
### Book Loans  
- Maximum 2 renewals per loan  
- Automatic overdue tracking  
- Renewals only allowed for non-overdue books 
  
### Reservations  
- Queue-based system with position tracking  
- Automatic expiration of available reservations   
  
### Reviews  
- Users can only review books they've returned   
- Rating scale: 1-5 stars [0-cite-11]  
- Review text: 10-2000 characters  
  
## API Structure  
  
The system follows a layered architecture:  
- **Controllers**: REST endpoints under `/api/v1/`   
- **Services**: Business logic implementation   
- **Repositories**: Data access layer using Spring Data JPA   
- **DTOs**: Request/Response objects with validation   
  
## Getting Started  
  
### Prerequisites  
- Java 17 or higher  
- Maven  
- Database (PostgreSQL/MySQL recommended)  
  
### Installation  
```bash  
# Clone the repository  
git clone https://github.com/Gerardoprogramer/Library-Management-System.git  
  
# Navigate to project directory  
cd Library-Management-System  
  
# Build the project  
mvn clean install  
  
# Run the application  
mvn spring-boot:run  
```  
  
### Configuration  
Configure your database connection in `application.properties`:  
```properties  
spring.datasource.url=jdbc:postgresql://localhost:5432/library_db  
spring.datasource.username=your_username  
spring.datasource.password=your_password  
```  
  
