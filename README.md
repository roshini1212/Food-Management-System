# Food Inventory System

Food Inventory System is an inventory platform for tracking food items, suppliers, reorder thresholds, and expiry-driven alerts. It uses:

- Spring Boot REST services for inventory and supplier APIs
- A responsive frontend dashboard served by Spring Boot static resources
- SQLite for lightweight relational storage and SQL trigger workflows
- Python ETL scripts for CSV ingestion and alert reporting
- Application logging for operational monitoring

## Features

- Manage suppliers and food inventory through REST endpoints
- Monitor inventory health in a live browser dashboard
- Track quantity, reorder level, batch code, and expiry date
- Generate automated SQL-trigger alerts for:
  - items expiring within 7 days
  - items falling below reorder thresholds
- Run Python ETL jobs to import supplier and stock data from CSV files
- Export alert summaries as JSON for downstream reporting or dashboards

## Project Structure

```text
FoodInventorySystem/
├── etl/
│   ├── alert_monitor.py
│   ├── import_inventory.py
│   ├── input/
│   └── output/
├── sql/
├── src/main/java/com/example/foodmanagement/
└── src/main/resources/
    └── static/
```

## Run the Spring Boot API

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8084`.

## Open the Frontend

Once the server is running, open:

```text
http://localhost:8084/
```

The frontend includes:

- summary cards for item, supplier, expiry, and low-stock counts
- a live alert feed for SQL-triggered issues
- supplier and inventory views
- forms to create suppliers and food items directly from the dashboard
- restaurant-specific sign up and log in pages before entering the dashboard

## Restaurant Authentication

Open one of these routes in the browser:

```text
http://localhost:8084/signup.html
http://localhost:8084/login.html
```

After signing up or logging in, the restaurant account is stored in browser local storage and used to open the main dashboard.

## Run the ETL Import

```bash
python3 etl/import_inventory.py
```

## Generate an Alert Report

```bash
python3 etl/alert_monitor.py
```

The report is written to `etl/output/alert_report.json`.

## Sample API Endpoints

### Suppliers

```bash
curl http://localhost:8084/api/suppliers
curl -X POST http://localhost:8084/api/suppliers \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Riverbank Foods",
    "contactPerson":"Ella Stone",
    "email":"ella@riverbank.com",
    "phone":"+1-202-555-0199"
  }'
```

### Food Items

```bash
curl http://localhost:8084/api/foods
curl -X POST http://localhost:8084/api/foods \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Cheese",
    "category":"Dairy",
    "supplierId":1,
    "quantity":14,
    "reorderLevel":10,
    "unit":"blocks",
    "expiryDate":"2026-05-02",
    "batchCode":"RB-CH-7781"
  }'
```

### Alerts and Dashboard

```bash
curl http://localhost:8084/api/alerts
curl http://localhost:8084/api/dashboard/summary
```

## Resume Alignment

This implementation reflects the project description you shared:

- Spring Boot REST services for inventory, suppliers, and alerts
- Python ETL scripts for ingesting food and supplier data
- SQL-triggered alert automation to reduce food wastage risk
- Logging built into the service layer for ongoing monitoring
