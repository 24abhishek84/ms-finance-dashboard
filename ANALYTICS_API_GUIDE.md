# Analytics API Documentation

## Overview
The Analytics API provides endpoints for retrieving expense aggregations grouped by category and time periods (yearly, monthly, weekly). This powers the financial dashboard visualizations.

## Base URL
```
http://localhost:8080/api/analytics
```

## Authentication
All endpoints require JWT authentication via `Authorization: Bearer {token}` header.

---

## Endpoints

### 1. Get Expenses by Category (Custom Date Range)
**Endpoint**: `GET /expenses-by-category`

**Purpose**: Get total expenses grouped by category for a custom date range. Perfect for pie-chart visualization.

**Query Parameters**:
- `startDate` (required) - ISO date format (YYYY-MM-DD)
- `endDate` (required) - ISO date format (YYYY-MM-DD)

**Example Request**:
```bash
GET /api/analytics/expenses-by-category?startDate=2024-01-01&endDate=2024-12-31
```

**Response**:
```json
[
  {
    "categoryId": 1,
    "categoryName": "Groceries",
    "categoryColor": "#FF5733",
    "totalAmount": 1250.50,
    "transactionCount": 15
  },
  {
    "categoryId": 2,
    "categoryName": "Entertainment",
    "categoryColor": "#33FF57",
    "totalAmount": 450.00,
    "transactionCount": 8
  }
]
```

---

### 2. Get Sub-Category Breakdown
**Endpoint**: `GET /category/{categoryId}/breakdown`

**Purpose**: When user clicks on a category in the pie-chart, get breakdown by sub-categories showing bar-chart data.

**Path Parameters**:
- `categoryId` (required) - Parent category ID

**Query Parameters**:
- `startDate` (required) - ISO date format (YYYY-MM-DD)
- `endDate` (required) - ISO date format (YYYY-MM-DD)

**Example Request**:
```bash
GET /api/analytics/category/1/breakdown?startDate=2024-01-01&endDate=2024-12-31
```

**Response**:
```json
[
  {
    "categoryId": 3,
    "categoryName": "Vegetables",
    "categoryColor": "#90EE90",
    "totalAmount": 500.00,
    "transactionCount": 10
  },
  {
    "categoryId": 4,
    "categoryName": "Fruits",
    "categoryColor": "#FFD700",
    "totalAmount": 350.00,
    "transactionCount": 7
  }
]
```

---

### 3. Get Yearly Expenses
**Endpoint**: `GET /yearly/{year}`

**Purpose**: Get total expenses for a specific year grouped by category.

**Path Parameters**:
- `year` (required) - 4-digit year (e.g., 2024)

**Example Request**:
```bash
GET /api/analytics/yearly/2024
```

**Response**:
```json
{
  "period": "2024",
  "totalAmount": 15000.00,
  "transactionCount": 120,
  "expensesByCategory": [
    {
      "categoryId": 1,
      "categoryName": "Groceries",
      "categoryColor": "#FF5733",
      "totalAmount": 5000.00,
      "transactionCount": 60
    }
  ]
}
```

---

### 4. Get Current Year Expenses
**Endpoint**: `GET /yearly/current`

**Purpose**: Get total expenses for the current year grouped by category.

**Example Request**:
```bash
GET /api/analytics/yearly/current
```

**Response**: Same format as `/yearly/{year}`

---

### 5. Get Monthly Expenses
**Endpoint**: `GET /monthly/{year}/{month}`

**Purpose**: Get total expenses for a specific month grouped by category.

**Path Parameters**:
- `year` (required) - 4-digit year (e.g., 2024)
- `month` (required) - Month number (1-12)

**Example Request**:
```bash
GET /api/analytics/monthly/2024/11
```

**Response**:
```json
{
  "period": "2024-11",
  "totalAmount": 1500.00,
  "transactionCount": 20,
  "expensesByCategory": [
    {
      "categoryId": 1,
      "categoryName": "Groceries",
      "categoryColor": "#FF5733",
      "totalAmount": 600.00,
      "transactionCount": 10
    }
  ]
}
```

---

### 6. Get Current Month Expenses
**Endpoint**: `GET /monthly/current`

**Purpose**: Get total expenses for the current month grouped by category.

**Example Request**:
```bash
GET /api/analytics/monthly/current
```

**Response**: Same format as `/monthly/{year}/{month}`

---

### 7. Get Weekly Expenses
**Endpoint**: `GET /weekly`

**Purpose**: Get total expenses for a week (7 days) starting from a given date.

**Query Parameters**:
- `startDate` (required) - ISO date format (YYYY-MM-DD) - Monday of the week

**Example Request**:
```bash
GET /api/analytics/weekly?startDate=2024-11-11
```

**Response**:
```json
{
  "period": "2024-11-11 to 2024-11-17",
  "totalAmount": 300.00,
  "transactionCount": 5,
  "expensesByCategory": [
    {
      "categoryId": 1,
      "categoryName": "Groceries",
      "categoryColor": "#FF5733",
      "totalAmount": 150.00,
      "transactionCount": 2
    }
  ]
}
```

---

### 8. Get Current Week Expenses
**Endpoint**: `GET /weekly/current`

**Purpose**: Get total expenses for the current week (starting Monday).

**Example Request**:
```bash
GET /api/analytics/weekly/current
```

**Response**: Same format as `/weekly`

---

## Response DTO Structures

### DashboardExpenseDTO
```java
{
  "period": String,              // e.g., "2024", "2024-11", "2024-11-11 to 2024-11-17"
  "totalAmount": BigDecimal,     // Sum of all expenses
  "transactionCount": Integer,   // Number of transactions
  "expensesByCategory": List<CategoryExpenseDTO>
}
```

### CategoryExpenseDTO
```java
{
  "categoryId": Long,
  "categoryName": String,
  "categoryColor": String,       // Hex color code
  "totalAmount": BigDecimal,     // Sum for this category
  "transactionCount": Integer    // Number of transactions
}
```

---

## Usage Examples

### Frontend Integration - Pie Chart (Yearly)
```javascript
// Get current year expenses for pie chart
const response = await fetch('/api/analytics/yearly/current', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const data = await response.json();

// Transform for pie chart library
const pieData = data.expensesByCategory.map(cat => ({
  name: cat.categoryName,
  value: parseFloat(cat.totalAmount),
  color: cat.categoryColor
}));
```

### Frontend Integration - Bar Chart (Sub-categories)
```javascript
// When user clicks on a category in the pie chart
const categoryId = 1;
const response = await fetch(
  `/api/analytics/category/${categoryId}/breakdown?startDate=2024-01-01&endDate=2024-12-31`,
  { headers: { 'Authorization': `Bearer ${token}` } }
);
const data = await response.json();

// Transform for bar chart library
const barData = data.map(subCat => ({
  name: subCat.categoryName,
  value: parseFloat(subCat.totalAmount)
}));
```

### Frontend Integration - Dashboard (Monthly)
```javascript
// Get current month for dashboard view
const response = await fetch('/api/analytics/monthly/current', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const monthlyData = await response.json();
// Display monthlyData.totalAmount and monthlyData.expensesByCategory
```

---

## Caching
All endpoints use Spring Cache annotations for performance optimization. Cache keys include userId and date parameters, so each user has independent cache.

**Cache Durations**: Configure in `application.yml` (default: 5 minutes)

---

## Error Handling

**401 Unauthorized**: Missing or invalid JWT token
```json
{ "error": "Unauthorized" }
```

**400 Bad Request**: Invalid date format or parameters
```json
{ "error": "Invalid startDate format" }
```

---

## Testing with Postman

1. First authenticate and get JWT token from `/auth/login`
2. Set Authorization header with Bearer token
3. Test endpoints with date ranges:

```
GET http://localhost:8080/api/analytics/expenses-by-category?startDate=2024-01-01&endDate=2024-12-31
GET http://localhost:8080/api/analytics/yearly/2024
GET http://localhost:8080/api/analytics/monthly/2024/11
GET http://localhost:8080/api/analytics/weekly?startDate=2024-11-11
```

---

## Notes

- All amounts are in the same currency as your database (BigDecimal with 2 decimal places)
- Only EXPENSE category transactions are aggregated (INCOME type is filtered out)
- Categories are sorted by total amount (highest first)
- Transactions are assumed in user's timezone
