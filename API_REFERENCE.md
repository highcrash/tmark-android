# T-Mark Mobile API Reference

**Base URL:** `https://tmark.online/api/mobile/`
**Auth:** Bearer token in `Authorization` header (except public auth endpoints)
**Content-Type:** `application/json`
**Token Lifetime:** 30 days (2,592,000 seconds)

---

## Table of Contents

- [Authentication](#authentication)
- [Dashboard](#dashboard)
- [Catalog](#catalog)
- [Requests](#requests)
- [Orders](#orders)
- [Invoices](#invoices)
- [Profile](#profile)
- [Phone Lookup](#phone-lookup)
- [Notifications](#notifications)
- [Crew API](#crew-api)
- [Error Handling](#error-handling)
- [Phone Number Normalization](#phone-number-normalization)
- [Android Implementation Notes](#android-implementation-notes)

---

## Authentication

### POST `auth/send-otp`
Send OTP to phone number. No auth required.

**Request:**
```json
{ "phone": "01712345678" }
```
- `phone` (string, required, min 7 chars)

**Response:**
```json
{
  "success": true,
  "normalizedPhone": "+8801712345678",
  "newUser": false
}
```
- `newUser: true` means phone is not registered — show registration form

**Errors:** `422` validation | `429` rate limit (max 3 per 10 min) | `403` account inactive

---

### POST `auth/verify-otp`
Verify OTP and receive access token. No auth required.

**Request:**
```json
{ "phone": "01712345678", "otp": "123456" }
```

**Response:**
```json
{
  "accessToken": "eyJhbG...",
  "tokenType": "Bearer",
  "expiresIn": 2592000,
  "user": {
    "id": "clu...",
    "role": "client",
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+8801712345678"
  },
  "client": {
    "id": "clu...",
    "name": "John Doe",
    "phone": "+8801712345678",
    "email": "john@example.com",
    "address": "Dhaka"
  }
}
```

**Errors:** `401` invalid/expired OTP | `403` inactive | `404` account not found

---

### POST `auth/register`
Register new client. Sends OTP after creation. No auth required.

**Request:**
```json
{
  "phone": "01712345678",
  "name": "John Doe",
  "email": "john@example.com",
  "password": "min8chars"
}
```
- `phone` (string, required, min 7)
- `name` (string, required, min 2)
- `email` (string, optional, valid email)
- `password` (string, required, min 8)

**Response:**
```json
{ "success": true, "normalizedPhone": "+8801712345678" }
```

**Errors:** `409` phone/email taken | `422` validation | `429` OTP rate limit

---

### POST `auth/login-email`
Login with email and password. No auth required.

**Request:**
```json
{ "email": "john@example.com", "password": "mypassword" }
```

**Response:** Same format as `verify-otp`

**Errors:** `401` invalid credentials | `403` wrong role or inactive | `404` profile not found

---

### POST `auth/logout`
Invalidate all tokens. **Auth required.**

**Response:**
```json
{ "success": true }
```

---

### POST `auth/change-password`
Change password. **Auth required.**

**Request:**
```json
{
  "currentPassword": "oldpass",
  "newPassword": "newpass123"
}
```
- `currentPassword` (string, optional if user has no password — check `profile.hasPassword`)
- `newPassword` (string, required, min 8)

**Response:**
```json
{ "success": true }
```

**Errors:** `400` incorrect current password

---

## Dashboard

### GET `dashboard`
Client dashboard summary. **Auth required.**

**Response:**
```json
{
  "pendingRequests": 2,
  "activeOrders": 1,
  "outstandingBalance": 15000,
  "lastInvoice": {
    "id": "clu...",
    "invoiceCode": "INV-2026-001",
    "issueDate": "2026-03-15",
    "totalAmount": 25000,
    "balanceDue": 15000,
    "status": "sent"
  }
}
```
- `lastInvoice` can be `null`

---

## Catalog

### GET `catalog/packages`
List all active packages. **Auth required.**

**Response:**
```json
{
  "packages": [
    {
      "id": "clu...",
      "code": "PKG-001",
      "name": "Arri Alexa Mini Package",
      "description": "Full cinema camera package",
      "heroVideoUrl": "https://youtube.com/...",
      "pricePerDay": 15000,
      "maxQtyPerDay": 2,
      "type": "CAMERA_PACKAGES",
      "itemCount": 12
    }
  ]
}
```

---

### GET `catalog/packages/{id}`
Package detail with included items and sub-packages. **Auth required.**

**Response:**
```json
{
  "package": {
    "id": "clu...",
    "code": "PKG-001",
    "name": "Arri Alexa Mini Package",
    "description": "Full cinema camera package",
    "heroVideoUrl": null,
    "pricePerDay": 15000,
    "maxQtyPerDay": 2,
    "type": "CAMERA_PACKAGES",
    "includes": [
      {
        "id": "clu...",
        "name": "Arri Alexa Mini LF",
        "category": "Camera",
        "quantity": 1,
        "condition": "good"
      }
    ],
    "subPackages": [
      {
        "id": "clu...",
        "name": "Lens Package A",
        "type": "LENS_PACKAGES",
        "includes": [
          {
            "id": "clu...",
            "name": "Sigma Cine 35mm",
            "category": "Lens",
            "quantity": 1,
            "condition": "good"
          }
        ]
      }
    ]
  }
}
```
- Items in `includes` are ordered by category (ASC)
- `subPackages` are combo child packages — each has its own `includes`
- Direct `includes` on a combo package = "Additional Equipment"

---

### GET `catalog/items`
List all standalone rentable equipment. **Auth required.**

**Response:**
```json
{
  "items": [
    {
      "id": "clu...",
      "name": "Sennheiser MKH 416",
      "category": "Audio",
      "pricePerDay": 500,
      "quantity": 3,
      "condition": "good"
    }
  ]
}
```

---

## Requests

### GET `request/bootstrap`
Data needed to create a rental request. **Auth required.**

**Response:**
```json
{
  "client": {
    "id": "clu...",
    "name": "John Doe",
    "phone": "+8801712345678",
    "email": "john@example.com",
    "address": "Dhaka"
  },
  "packages": [
    { "id": "clu...", "name": "Arri Package", "type": "CAMERA_PACKAGES", "pricePerDay": 15000, "maxQtyPerDay": 2 }
  ],
  "items": [
    { "id": "clu...", "name": "Sennheiser 416", "category": "Audio", "pricePerDay": 500, "maxQtyPerDay": 3 }
  ],
  "designations": [
    { "id": "clu...", "name": "Director" },
    { "id": "clu...", "name": "Cinematographer / DOP" }
  ],
  "productionHouses": [
    { "id": "clu...", "name": "Ace Films", "phone": "01700000000", "address": "Gulshan", "contactPerson": "Rahim" }
  ]
}
```
- `designations` are sorted by `sortOrder` ASC
- `productionHouses` are only those linked to the authenticated client

---

### GET `requests`
List all rental requests for client. **Auth required.**

**Response:**
```json
{
  "requests": [
    {
      "id": "clu...",
      "projectName": "Ad Shoot",
      "projectType": "Commercial",
      "projectLocation": "Dhaka",
      "bookingRef": null,
      "notes": null,
      "status": "pending",
      "adminMessage": null,
      "createdAt": "2026-03-20T10:00:00.000Z",
      "requestedDates": ["2026-04-01", "2026-04-02"],
      "estimatedTotal": 30000,
      "items": [
        {
          "id": "clu...",
          "type": "package",
          "entityId": "clu...",
          "name": "Arri Package",
          "quantity": 1,
          "ratePerDay": 15000
        }
      ]
    }
  ]
}
```
- `status`: `"pending"` | `"confirmed"` | `"declined"` | `"converted"` | `"cancelled"`

---

### GET `requests/{id}`
Request detail with contacts and production house. **Auth required.**

**Response:** Same as list item, plus:
```json
{
  "request": {
    "...same fields...",
    "productionHouse": {
      "id": "clu...",
      "name": "Ace Films",
      "phone": "01700000000",
      "address": "Gulshan",
      "contactPerson": "Rahim"
    },
    "contacts": [
      {
        "id": "clu...",
        "designationId": "clu...",
        "designationName": "Director",
        "contactName": "Karim",
        "phone": "01711111111",
        "email": "karim@email.com"
      }
    ]
  }
}
```

---

### POST `requests`
Create rental request. **Auth required.** Returns `201`.

**Request:**
```json
{
  "projectName": "Ad Shoot",
  "requestedDates": ["2026-04-01", "2026-04-02"],
  "selectedItems": [
    { "type": "package", "entityId": "clu...", "quantity": 1 },
    { "type": "item", "entityId": "clu...", "quantity": 2 }
  ],
  "notes": "Need early delivery",
  "projectType": "Commercial",
  "projectLocation": "Dhaka",
  "bookingRef": "REF-123",
  "productionHouseId": "clu...",
  "productionHouse": {
    "name": "New Films Ltd",
    "phone": "01700000000",
    "address": "Banani",
    "contactPerson": "Rahim"
  },
  "contacts": [
    {
      "designationId": "clu...",
      "contactName": "Karim",
      "phone": "01711111111",
      "email": "karim@email.com"
    }
  ]
}
```
- `projectName` (string, required, min 2)
- `requestedDates` (string[], required, min 1, ISO date format)
- `selectedItems` (array, required, min 1)
  - `type`: `"package"` or `"item"`
  - `entityId` (string, required)
  - `quantity` (number, optional, default 1, min 1)
- `notes`, `projectType`, `projectLocation`, `bookingRef` (string, optional)
- `productionHouseId` (string, optional) — existing production house
- `productionHouse` (object, optional) — create new production house
- `contacts` (array, optional) — `designationId` + `contactName` (min 2) required per contact

**Response:**
```json
{ "request": { "...same as GET requests item..." } }
```

---

### POST `requests/{id}/cancel`
Cancel a pending request. **Auth required.**

**Response:**
```json
{ "success": true, "request": { "id": "clu...", "status": "cancelled" } }
```

**Errors:** `409` request not pending

---

## Orders

### GET `orders`
List all orders. **Auth required.**

**Response:**
```json
{
  "orders": [
    {
      "id": "clu...",
      "orderCode": "ORD-2026-001",
      "projectName": "Ad Shoot",
      "status": "active",
      "totalAmount": 35000,
      "amountPaid": 20000,
      "balanceDue": 15000,
      "createdAt": "2026-03-20T10:00:00.000Z",
      "dates": [
        { "date": "2026-04-01", "cancelled": false }
      ]
    }
  ]
}
```
- `status`: `"draft"` | `"confirmed"` | `"active"` | `"completed"` | `"cancelled"`

---

### GET `orders/{id}`
Full order detail. **Auth required.**

**Response:**
```json
{
  "order": {
    "id": "clu...",
    "orderCode": "ORD-2026-001",
    "projectName": "Ad Shoot",
    "projectType": "Commercial",
    "projectLocation": "Dhaka",
    "bookingRef": null,
    "status": "active",
    "notes": null,
    "createdAt": "2026-03-20T10:00:00.000Z",
    "updatedAt": "2026-03-21T10:00:00.000Z",
    "totalAmount": 35000,
    "amountPaid": 20000,
    "balanceDue": 15000,
    "dates": [
      { "id": "clu...", "date": "2026-04-01", "cancelled": false, "callTime": "06:00", "notes": null }
    ],
    "items": [
      {
        "id": "clu...",
        "type": "package",
        "name": "Arri Package",
        "quantity": 1,
        "ratePerDay": 15000,
        "subtotal": 30000,
        "activeDates": ["2026-04-01", "2026-04-02"]
      }
    ],
    "services": [
      { "id": "clu...", "name": "Delivery", "description": null, "price": 1000 }
    ],
    "customCharges": [
      { "id": "clu...", "name": "Extra battery", "quantity": 2, "unitPrice": 500, "subtotal": 1000, "notes": null }
    ],
    "payments": [
      {
        "id": "clu...",
        "amount": 20000,
        "dateReceived": "2026-03-25",
        "method": "bank_transfer",
        "reference": "TRX-123",
        "notes": null,
        "voided": false
      }
    ],
    "invoices": [
      { "id": "clu...", "invoiceCode": "INV-2026-001", "status": "sent", "totalAmount": 35000, "balanceDue": 15000 }
    ],
    "crew": [
      { "crewId": "clu...", "name": "Rahim", "designation": "Camera Operator", "phone": "01700000000" }
    ],
    "contacts": [
      {
        "id": "clu...",
        "designationId": "clu...",
        "designationName": "Director",
        "contactName": "Karim",
        "phone": "01711111111",
        "email": null
      }
    ]
  }
}
```

---

## Invoices

### GET `invoices`
List all invoices. **Auth required.**

**Response:**
```json
{
  "outstandingBalance": 15000,
  "invoices": [
    {
      "id": "clu...",
      "invoiceCode": "INV-2026-001",
      "status": "sent",
      "issueDate": "2026-03-25",
      "dueDate": "2026-04-10",
      "totalAmount": 35000,
      "amountPaid": 20000,
      "balanceDue": 15000,
      "order": { "orderCode": "ORD-2026-001", "projectName": "Ad Shoot" },
      "pdfUrl": "https://tmark.online/api/invoices/clu.../pdf"
    }
  ]
}
```
- `status`: `"draft"` | `"sent"` | `"paid"` | `"overdue"` | `"partial"` | `"void"`

---

### GET `invoices/{id}`
Invoice detail with order breakdown. **Auth required.**

**Response:**
```json
{
  "invoice": {
    "id": "clu...",
    "invoiceCode": "INV-2026-001",
    "status": "sent",
    "addressedTo": "Ace Films",
    "issueDate": "2026-03-25",
    "dueDate": "2026-04-10",
    "totalAmount": 35000,
    "amountPaid": 20000,
    "balanceDue": 15000,
    "pdfUrl": "https://...",
    "productionHouse": {
      "id": "clu...", "name": "Ace Films", "phone": "01700000000", "contactPerson": "Rahim"
    },
    "order": {
      "id": "clu...",
      "orderCode": "ORD-2026-001",
      "projectName": "Ad Shoot",
      "projectType": "Commercial",
      "projectLocation": "Dhaka",
      "status": "active",
      "dates": [{ "date": "2026-04-01", "cancelled": false }],
      "items": [
        { "id": "clu...", "name": "Arri Package", "type": "package", "ratePerDay": 15000, "quantity": 1, "activeDays": 2, "subtotal": 30000 }
      ],
      "services": [{ "id": "clu...", "name": "Delivery", "description": null, "price": 1000 }],
      "customCharges": [{ "id": "clu...", "name": "Battery", "quantity": 2, "unitPrice": 500, "subtotal": 1000, "notes": null }]
    },
    "payments": [
      { "id": "clu...", "amount": 20000, "dateReceived": "2026-03-25", "method": "bank_transfer", "reference": "TRX-123" }
    ]
  }
}
```

---

## Profile

### GET `profile`
Get client profile. **Auth required.**

**Response:**
```json
{
  "profile": {
    "id": "clu...",
    "name": "John Doe",
    "phone": "+8801712345678",
    "email": "john@example.com",
    "address": "Dhaka",
    "nid": null,
    "allowPhoneLookup": true,
    "memberSince": "2026-01-15T10:00:00.000Z",
    "designation": { "id": "clu...", "name": "Director" },
    "productionHouses": [
      { "id": "clu...", "name": "Ace Films", "phone": "01700000000", "contactPerson": "Rahim" }
    ],
    "hasPassword": true
  }
}
```
- `hasPassword`: use to determine if "Current Password" field is needed in change-password
- `designation` can be `null`

---

### PATCH `profile`
Update profile. **Auth required.**

**Request:**
```json
{
  "email": "new@example.com",
  "address": "New address",
  "designationId": "clu..."
}
```
All fields optional.

**Response:**
```json
{
  "profile": {
    "id": "clu...",
    "name": "John Doe",
    "phone": "+8801712345678",
    "email": "new@example.com",
    "address": "New address",
    "designation": { "id": "clu...", "name": "Director" }
  }
}
```

---

## Phone Lookup

### POST `phone-lookup`
Look up a client by phone number (for adding contacts). **Auth required.**

**Request:**
```json
{ "phone": "01711111111" }
```

**Response (found):**
```json
{
  "found": true,
  "blocked": false,
  "normalizedPhone": "+8801711111111",
  "client": {
    "id": "clu...",
    "name": "Karim",
    "email": "karim@email.com",
    "designationId": "clu...",
    "designationName": "Director"
  }
}
```

**Response (not found):**
```json
{ "found": false, "normalizedPhone": "+8801711111111" }
```

**Response (blocked by user):**
```json
{ "found": true, "blocked": true, "normalizedPhone": "+8801711111111" }
```

---

## Notifications

### GET `notifications`
Get notifications. **Auth required.**

**Query params:** `?limit=30&unread=false`
- `limit` (number, default 30, max 100)
- `unread` (boolean, default false)

**Response:**
```json
{
  "unreadCount": 3,
  "notifications": [
    {
      "id": "clu...",
      "type": "request_confirmed",
      "title": "Request Confirmed",
      "body": "Your request for Ad Shoot has been confirmed",
      "data": { "requestId": "clu..." },
      "read": false,
      "createdAt": "2026-03-20T10:00:00.000Z"
    }
  ]
}
```

---

### POST `notifications/register`
Register FCM push token. **Auth required.**

**Request:**
```json
{ "token": "fcm_device_token_here", "platform": "android" }
```
- `platform`: `"android"` or `"ios"`

**Response:**
```json
{ "success": true }
```

---

### PATCH `notifications/{id}/read`
Mark notification as read. **Auth required.**

**Response:**
```json
{ "success": true }
```

---

## Crew API

The crew API mirrors the client API structure but serves crew members (camera operators, gaffers, etc.).

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `crew/auth/send-otp` | Send OTP to crew phone |
| POST | `crew/auth/verify-otp` | Verify OTP, get token |
| POST | `crew/auth/logout` | Invalidate tokens |
| POST | `crew/auth/change-password` | Change password |

Request/response formats match client auth, except:
- `verify-otp` returns `crew` object instead of `client`: `{ id, name, designation, phone, photoUrl }`
- `send-otp` returns `404` if phone is not a crew account

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `crew/dashboard` | Today's assignments, attendance, payroll summary |

### Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `crew/profile` | Full profile with rates |
| PATCH | `crew/profile` | Update email, emergency contact |

### Schedule
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `crew/schedule` | Next 30 days of assignments |
| GET | `crew/schedule/{id}` | Assignment detail with order info, contacts, equipment |

### Attendance
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `crew/attendance` | Attendance records (query: `?month=2026-03`) |
| GET | `crew/attendance/today` | Today's status + assignment + work report |
| POST | `crew/attendance/checkin` | Check in to today's assignment |
| POST | `crew/attendance/checkout` | Check out |

### Work Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `crew/reports` | Last 30 work reports |
| POST | `crew/reports` | Create/update work report |

**POST body:**
```json
{
  "orderDateId": "clu...",
  "notes": "All good",
  "hasNightOt": false,
  "hasOvertime": false,
  "equipmentIssue": null
}
```

### Leave
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `crew/leave` | Leave requests |
| POST | `crew/leave` | Request leave |
| DELETE | `crew/leave/{id}` | Cancel pending leave |

**POST body:**
```json
{
  "leaveType": "annual",
  "startDate": "2026-04-01",
  "endDate": "2026-04-03",
  "reason": "Family event"
}
```
- `leaveType`: `"annual"` | `"sick"` | `"unpaid"`

### Payroll
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `crew/payroll` | Last 12 months of payroll |
| GET | `crew/payroll/{yearMonth}` | Detail for specific month (e.g., `2026-03`) |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `crew/notifications` | Crew notifications |
| POST | `crew/notifications/register` | Register FCM token |
| PATCH | `crew/notifications/{id}/read` | Mark as read |

---

## Error Handling

All errors return JSON with an `error` field:

```json
{ "error": "Human-readable error message" }
```

**Standard HTTP codes used:**
| Code | Meaning |
|------|---------|
| 400 | Bad request / invalid JSON |
| 401 | Unauthorized (missing or invalid token) |
| 403 | Forbidden (wrong role or inactive account) |
| 404 | Resource not found |
| 409 | Conflict (duplicate, wrong state) |
| 422 | Validation failed |
| 429 | Rate limited |
| 500 | Server error |

### Android error parsing
The `safeApiCall` wrapper in `ApiResult.kt` extracts the `error` field from JSON error bodies automatically.

---

## Phone Number Normalization

The server normalizes all phone numbers to international format:

| Input | Normalized |
|-------|-----------|
| `01712345678` | `+8801712345678` |
| `8801712345678` | `+8801712345678` |
| `+8801712345678` | `+8801712345678` |

**Bangladeshi format validation (client-side):** `^01[3-9]\d{8}$` (11 digits starting with 01)

---

## Android Implementation Notes

### Data Models
All API response models are in `data/model/` using Moshi `@JsonClass(generateAdapter = true)`:
- `AuthModels.kt` — OTP, register, email login, token responses
- `CatalogModels.kt` — Packages (with sub-packages), items
- `RequestModels.kt` — Requests CRUD, bootstrap, phone lookup, contacts
- `OrderModels.kt` — Orders, order detail with items/payments/crew
- `InvoiceModels.kt` — Invoices with PDF URLs
- `DashboardModels.kt` — Dashboard stats
- `ProfileModels.kt` — Profile with designation, hasPassword
- `CartModels.kt` — Local cart state (not an API model)

### Adding a new API endpoint
1. Add the endpoint to `data/api/ApiService.kt`
2. Add request/response models to `data/model/`
3. Add repository method in `data/repository/` using `safeApiCall`
4. Call from ViewModel, handle `ApiResult.Success` / `Error` / `Exception`

### Token management
- Stored in DataStore (`data/local/TokenStore.kt`)
- Auto-injected via OkHttp interceptor (`di/NetworkModule.kt`)
- On `401` response, redirect to login screen
- On logout, call `tokenStore.clear()` after `auth/logout`

### Decimal handling
- Server uses Prisma `Decimal` type for money fields
- All monetary values are converted to `Double` via `.toNumber()` before sending
- Display with: `"৳${"%,.0f".format(amount)}"`

### Date formats
- Dates: ISO format `"YYYY-MM-DD"` (e.g., `"2026-04-01"`)
- Datetimes: ISO 8601 `"YYYY-MM-DDTHH:mm:ss.sssZ"`
- Display dates with `.take(10)` to extract date portion from datetime strings

### Status enums (not enforced by API, but these are the values)
- **Request:** `pending`, `confirmed`, `declined`, `converted`, `cancelled`
- **Order:** `draft`, `confirmed`, `active`, `completed`, `cancelled`
- **Invoice:** `draft`, `sent`, `paid`, `overdue`, `partial`, `void`
- **Payment method:** `cash`, `bank_transfer`, `bkash`, `nagad`, `cheque`
- **Equipment condition:** `good`, `fair`, `damaged`, `out_for_service`
- **Leave type:** `annual`, `sick`, `unpaid`
- **Attendance:** `present`, `absent`, `leave`, `holiday`, `on_order`
