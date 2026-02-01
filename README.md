# Store Microservices System

This project consists of two main microservices: **Authorization Service** and **Products Service**, providing a secure way to manage a product catalog.

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17 or higher (for local testing)
- Maven

### Installation & Setup
1. **Clone the repository:**
   ```bash
   git clone https://github.com/AhmedSM1/Store_console.git
   ```

2. **Spin up the infrastructure:**
   Use Docker Compose to start the databases, management tools, and the services:
   ```bash
   docker-compose up -d
   ```

---

## 🛠 Database Management Tools

You can visualize and manage your data using the following web interfaces:

### 1. PostgreSQL (via pgAdmin)
- **URL:** [http://localhost:5050](http://localhost:5050)
- **Login Email:** `admin@admin.com`
- **Password:** `admin`
- *Note: To connect to the DB, use host `store-postgres`, port `5432`, and maintenance DB `auth_db`.*

### 2. MongoDB (via Mongo Express)
- **URL:** [http://localhost:8081](http://localhost:8081)
- **Authentication:** Disabled (Default)

---

## 🧪 Testing & Coverage

### Running Tests Locally
To run the test suite and generate coverage reports for each service, navigate to the service directory and run:

```bash
# For Authorization Service
cd authorization
mvn clean test

# For Products Service
cd ../products
mvn clean test
```

### Viewing Coverage Reports
After running the tests, you can view the detailed **JaCoCo HTML reports** at:
- `authorization/target/site/jacoco/index.html`
- `products/target/site/jacoco/index.html`

---

## 🛰 API Documentation
- **Postman Collection:** A Postman collection is included in the root folder (`store_collection.json`). Import it into Postman to test all endpoints.
- **Auth Service:** Runs on `http://localhost:8070`
- **Products Service:** Runs on `http://localhost:8080`


---

## 🏗 Architecture
- **Auth Service:** Spring Boot, PostgreSQL (Handles users, roles, and JWT generation).
- **Products Service:** Spring Boot, MongoDB (Handles product CRUD operations).
- **Network:** All containers communicate over a private bridge network named `store-network`.


## 🚀 API Testing & Automation

### Postman Setup (Recommended)
This project is designed for automated testing using Postman Environments.

1. **Import the Collection:** Import `store_collection.json`.
2. **Import the Environment:** Import `store_environment.json`.
3. **Automated Authentication:** 
   - You **do not** need to copy-paste tokens.
   - The `Login` request in the collection contains a **Tests** script that automatically captures the JWT from the response and updates the `{{token}}` variable in your environment.
   - All subsequent requests are configured to use this variable in the `Authorization` header automatically.

Note: Ensure you have the "Store Environment" selected in the top-right corner of Postman before running requests.
---

## 🏗 System Architecture

| Service | Port | Database | Responsibilities |
| :--- | :--- | :--- | :--- |
| **Auth Service** | `8070` | PostgreSQL | User management, RBAC, JWT Issuance |
| **Products Service** | `8080` | MongoDB | Inventory management, Secure CRUD |
| **pgAdmin** | `5050` | - | PostgreSQL GUI |
| **Mongo Express** | `8081` | - | MongoDB GUI |
