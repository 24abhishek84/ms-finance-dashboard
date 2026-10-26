# Frontend Integration Guide

## Backend Connection Setup

Your Finance Dashboard backend is running on `http://localhost:8080` and your frontend (financial-dashboard) is configured to connect to `http://localhost:3000`. CORS is already enabled for frontend integration.

### Key Backend Endpoints

#### Authentication
- **Register**: `POST /auth/register`
- **Login**: `POST /auth/login` → Returns JWT token
- **Validate**: `GET /auth/validate`

#### Categories  
- **Create**: `POST /categories`
- **Get All**: `GET /categories`
- **Get by Type**: `GET /categories/type/{type}`
- **Update**: `PUT /categories/{id}`
- **Delete**: `DELETE /categories/{id}`

#### Transactions
- **Create**: `POST /transactions`
- **Get All**: `GET /transactions`
- **Get by Date Range**: `GET /transactions/range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- **Update**: `PUT /transactions/{id}`
- **Delete**: `DELETE /transactions/{id}`

---

## Frontend Login Flow

### 1. Register User (if new)
```javascript
const registerUser = async (userData) => {
  const response = await fetch('http://localhost:8080/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: userData.email,
      password: userData.password,
      firstName: userData.firstName,
      lastName: userData.lastName
    })
  });
  return response.json();
};
```

### 2. Login & Store Token
```javascript
const loginUser = async (email, password) => {
  const response = await fetch('http://localhost:8080/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password })
  });
  
  const data = await response.json();
  
  // Store JWT token in localStorage or sessionStorage
  localStorage.setItem('jwt_token', data.token);
  localStorage.setItem('userId', data.userId);
  localStorage.setItem('userEmail', data.email);
  localStorage.setItem('userName', data.firstName);
  
  return data;
};
```

### 3. Use Token for API Calls
All subsequent requests require two headers:
- `Authorization: Bearer {jwt_token}` (for validation if needed)
- `X-User-Id: {userId}` (for Categories & Transactions endpoints)

```javascript
const fetchUserCategories = async () => {
  const token = localStorage.getItem('jwt_token');
  const userId = localStorage.getItem('userId');
  
  const response = await fetch('http://localhost:8080/categories', {
    method: 'GET',
    headers: {
      'X-User-Id': userId,
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.json();
};
```

---

## API Request Examples for Frontend

### Create a Transaction
```javascript
const createTransaction = async (transactionData) => {
  const userId = localStorage.getItem('userId');
  const token = localStorage.getItem('jwt_token');
  
  const response = await fetch('http://localhost:8080/transactions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      description: transactionData.description,
      amount: transactionData.amount,
      transactionDate: transactionData.date,
      notes: transactionData.notes,
      categoryId: transactionData.categoryId
    })
  });
  
  return response.json();
};
```

### Get Transactions by Date Range
```javascript
const getTransactionsByRange = async (startDate, endDate) => {
  const userId = localStorage.getItem('userId');
  
  const response = await fetch(
    `http://localhost:8080/transactions/range?startDate=${startDate}&endDate=${endDate}`,
    {
      method: 'GET',
      headers: {
        'X-User-Id': userId
      }
    }
  );
  
  return response.json();
};
```

### Create a Category
```javascript
const createCategory = async (categoryData) => {
  const userId = localStorage.getItem('userId');
  
  const response = await fetch('http://localhost:8080/categories', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId
    },
    body: JSON.stringify({
      name: categoryData.name,
      description: categoryData.description,
      type: categoryData.type, // 'INCOME' or 'EXPENSE'
      color: categoryData.color // Hex format: #FF6B6B
    })
  });
  
  return response.json();
};
```

---

## Environment Setup

### Backend Requirements
- PostgreSQL running on `localhost:5432`
- Database: `learning`
- User: `learning`
- Password: `learning`

### Frontend Requirements
- React running on `http://localhost:3000`
- Axios or Fetch API for HTTP requests
- localStorage for token persistence

---

## Testing with Postman

1. **Import the collection**: Open Postman → File → Import → Select `Finance_Dashboard_API.postman_collection.json`

2. **Set environment variables**:
   - `base_url`: `http://localhost:8080`
   - `jwt_token`: Will be auto-populated after login
   - `userId`: Will be auto-populated after login

3. **Test workflow**:
   - Run "Register User" (or use existing credentials)
   - Run "Login" (auto-saves token)
   - Test other endpoints with the saved token

---

## Error Handling

### Common Status Codes
- `200` - Success
- `201` - Created
- `204` - No Content (Delete success)
- `400` - Bad Request (Validation error)
- `401` - Unauthorized (Invalid token)
- `404` - Not Found (Resource doesn't exist)
- `500` - Server Error

### Example Error Response
```json
{
  "error": "Invalid email or password"
}
```

---

## Frontend Login Screen Flow

1. **Display login form** with email & password fields
2. **On submit**:
   - Call `loginUser(email, password)`
   - On success:
     - Store token in localStorage
     - Redirect to dashboard
   - On error:
     - Display error message
3. **On logout**:
   - Clear localStorage
   - Redirect to login
4. **Protected routes**:
   - Check if `jwt_token` exists in localStorage
   - If not, redirect to login

---

## Notes

- **CORS** is enabled for `http://localhost:3000`
- **JWT Token** is valid for the session (check `JwtTokenProvider` for expiration details)
- **User ID** is required in `X-User-Id` header for most endpoints
- **Transactions** must reference an existing **Category ID**
- **Password** must be at least 8 characters for registration
