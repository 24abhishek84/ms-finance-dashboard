# Finance Dashboard - Categories & Transactions Documentation

## Overview

The Finance Dashboard now supports hierarchical categories with sub-categories. When you register a new user, the system automatically creates sample categories and transactions for demonstration purposes.

## Category Hierarchy

### Expense Categories

#### Groceries (Parent)
- Coles
- Woolworths
- Sri Kumaran

#### Fees (Parent)
- Direct Debit
- Bank Fees

#### Fuel (Parent)
- Petrol Station

#### Utilities (Parent)
- Electricity
- Water

### Income Categories

#### Income (Parent)
- Salary
- Bonus
- Freelance

## How to Use

### 1. Register a New User (Auto-creates Categories & Transactions)

**Request:**
```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true,
  "createdAt": "2026-10-24T10:30:00"
}
```

**What happens internally:**
- User is created
- Sample categories are created (parent and sub-categories)
- Sample transactions are created across different categories

### 2. Get All Categories (With Sub-Categories)

**Request:**
```bash
GET /api/categories
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Groceries",
    "type": "EXPENSE",
    "color": "#FF6B6B",
    "parentCategory": null,
    "subCategories": [
      {
        "id": 2,
        "name": "Coles",
        "type": "EXPENSE",
        "color": "#FFB3B3",
        "parentCategoryId": 1
      },
      {
        "id": 3,
        "name": "Woolworths",
        "type": "EXPENSE",
        "color": "#FFB3B3",
        "parentCategoryId": 1
      },
      {
        "id": 4,
        "name": "Sri Kumaran",
        "type": "EXPENSE",
        "color": "#FFB3B3",
        "parentCategoryId": 1
      }
    ]
  }
]
```

### 3. Create a New Sub-Category

**Request:**
```bash
POST /api/categories
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "name": "Aldi",
  "description": "Grocery shopping at Aldi",
  "type": "EXPENSE",
  "color": "#FFB3B3",
  "parentCategoryId": 1
}
```

**Response:**
```json
{
  "id": 14,
  "name": "Aldi",
  "description": "Grocery shopping at Aldi",
  "type": "EXPENSE",
  "color": "#FFB3B3",
  "parentCategoryId": 1,
  "createdAt": "2026-10-24T14:30:00"
}
```

### 4. Create a New Parent Category

**Request:**
```bash
POST /api/categories
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "name": "Entertainment",
  "description": "Movies, games, and entertainment expenses",
  "type": "EXPENSE",
  "color": "#FF69B4"
}
```

Note: Omit `parentCategoryId` to create a parent category.

### 5. Create a Transaction

**Request:**
```bash
POST /api/transactions
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "description": "Weekly groceries at Coles",
  "amount": 85.50,
  "transactionDate": "2026-10-24",
  "categoryId": 2,
  "notes": "Weekly shopping for family"
}
```

**Response:**
```json
{
  "id": 10,
  "description": "Weekly groceries at Coles",
  "amount": 85.50,
  "transactionDate": "2026-10-24",
  "notes": "Weekly shopping for family",
  "categoryId": 2,
  "categoryName": "Coles",
  "categoryType": "EXPENSE",
  "userId": 1,
  "createdAt": "2026-10-24T14:20:00",
  "updatedAt": "2026-10-24T14:20:00"
}
```

### 6. Get All Transactions

**Request:**
```bash
GET /api/transactions
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response:**
```json
[
  {
    "id": 1,
    "description": "Weekly groceries at Coles",
    "amount": 85.50,
    "transactionDate": "2026-10-22",
    "notes": "Weekly shopping",
    "categoryId": 2,
    "categoryName": "Coles",
    "categoryType": "EXPENSE",
    "createdAt": "2026-10-24T10:30:00"
  }
]
```

## Testing with Postman

1. Import the `postman-stub.json` file into Postman
2. Register a user (this will auto-create categories and transactions)
3. Copy the JWT token from the auth response
4. Add the token to the Authorization header for subsequent requests
5. Test various endpoints to view and manage categories/transactions

## Database Schema Changes

### Categories Table
```sql
ALTER TABLE categories ADD COLUMN parent_category_id BIGINT;
ALTER TABLE categories ADD FOREIGN KEY (parent_category_id) REFERENCES categories(id);
```

The category hierarchy allows for:
- **Parent Categories**: Have no parent, contain sub-categories
- **Sub-Categories**: Have a parent category, contain transactions

## Sample Color Scheme

| Category Type | Color |
|---|---|
| Groceries (Parent) | #FF6B6B |
| Groceries Sub | #FFB3B3 |
| Fees (Parent) | #4ECDC4 |
| Fees Sub | #9EEDDF |
| Fuel (Parent) | #FFE66D |
| Fuel Sub | #FFF4B3 |
| Utilities (Parent) | #95E1D3 |
| Utilities Sub | #C8F3EE |
| Income (Parent) | #90EE90 |
| Income Sub | #C8E6C9 |

## Key Features

✅ Hierarchical categories with parent-child relationships
✅ Automatic sample data initialization on user registration
✅ Sub-category support for granular expense tracking
✅ Transaction creation under specific sub-categories
✅ Flexible color coding for category visualization
✅ Full CRUD operations on categories and transactions
