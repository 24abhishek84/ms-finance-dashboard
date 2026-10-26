# 💰 Financial Dashboard - Microservice Backend

A robust Spring Boot microservice for managing personal finances with category-based transaction tracking, JWT authentication, and comprehensive test coverage.

## 🎯 Features

- **User Management** - Secure registration & login with JWT
- **Transaction Management** - CRUD operations with date range filtering
- **Category Management** - Hierarchical categories (parent/child)
- **Security** - JWT auth, rate limiting, account lockout
- **Testing** - Unit & component tests with Mockito

## 🛠 Tech Stack

- **Backend**: Spring Boot 4.1.1 (Java 21)
- **Database**: PostgreSQL 13+
- **Security**: JWT + Spring Security
- **Testing**: JUnit 5 + Mockito
- **Build**: Gradle 9.7.1

## 🚀 Quick Start

### 1. Prerequisites
- Java 21+
- PostgreSQL 13+
- Gradle 9.7.1

### 2. Setup Database
```bash
# Create database
createdb financial_db
```

### 3. Configure Connection
Edit `application.yml`:
```yaml
datasource:
  url: jdbc:postgresql://localhost:5433/financial_db
  username: postgres
  password: password
```

### 4. Run Application
```bash
./gradlew bootRun
```
Server: `http://localhost:8080/api`

## 📚 API Endpoints

### Authentication
- `POST /auth/register` - Register user
- `POST /auth/login` - Login user
- `GET /auth/validate` - Validate token

### Categories
- `GET /categories` - Get all categories
- `POST /categories` - Create category
- `PUT /categories/{id}` - Update category
- `DELETE /categories/{id}` - Delete category
- `GET /categories/type/{type}` - Get by type

### Transactions
- `GET /transactions` - Get all transactions
- `POST /transactions` - Create transaction
- `GET /transactions/range` - Get by date range
- `PUT /transactions/{id}` - Update transaction
- `DELETE /transactions/{id}` - Delete transaction

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests CategoryServiceTest

# View report
start build\reports\tests\test\index.html
```

See [TESTING_GUIDE.md](./TESTING_GUIDE.md) for comprehensive testing patterns.

## 📝 Database Schema

**Users**: email, password, firstName, lastName, isLocked
**Categories**: name, type, color, userId, parentCategoryId
**Transactions**: description, amount, transactionDate, categoryId, userId

## 🔐 Security

- JWT token authentication
- BCrypt password encryption
- Account lockout after 5 failed attempts
- Rate limiting by IP address
- CORS enabled for `http://localhost:3000`

## 📖 Documentation

- [TESTING_GUIDE.md](./TESTING_GUIDE.md) - Testing patterns & examples
- [FRONTEND_INTEGRATION_GUIDE.md](./FRONTEND_INTEGRATION_GUIDE.md) - GraphQL integration
- [SECURITY_IMPLEMENTATION.md](./SECURITY_IMPLEMENTATION.md) - Security details

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/YourFeature`
3. Commit: `git commit -m "Add YourFeature"`
4. Push: `git push origin feature/YourFeature`
5. Open Pull Request

### Standards
- Follow Google Java Style Guide
- Write unit tests (80%+ coverage)
- Meaningful commit messages

## 📄 License

MIT License - see LICENSE file

## 🎯 Roadmap

- [ ] React frontend integration
- [ ] Advanced filtering & analytics
- [ ] Budget tracking & alerts
- [ ] Multi-currency support
- [ ] Mobile app support

---

**Made with ❤️ for personal finance management**
