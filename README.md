# Pizza Store

A full-stack pizza store application with a Java Spring Boot backend and an Angular frontend.

## Users

All passwords are Pizza123!


## Loggin
Log file = C:\logs\pizzastore

## Prerequisites

You need two things installed on your machine before you can run this project:

1. **Java 8 (JDK 1.8)**
   - Download: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
   - After installing, verify by opening a Command Prompt and running:
     ```cmd
     java -version
     ```
     You should see something like `java version "1.8.0_xxx"`.

2. **Node.js 18 or higher** (includes npm)
   - Download: https://nodejs.org/
   - After installing, verify by opening a Command Prompt and running:
     ```cmd
     node --version
     npm --version
     ```

No Maven install is needed. The project includes a Maven wrapper (`mvnw.cmd`) that downloads Maven automatically on first run.

## Running the Application

You will need **two separate Command Prompt windows** open at the same time.

### Terminal 1: Start the backend

1. Open a **Command Prompt** window
2. Navigate to the project folder:
   ```cmd
   cd C:\Users\ericr\IdeaProjects\PizzaStore
   ```
3. Set JAVA_HOME to your Java 8 installation (adjust the path if yours is different):
   ```cmd
   set JAVA_HOME=C:\Program Files\Java\jdk-1.8
   ```
4. Start the Spring Boot backend:
   ```cmd
   mvnw.cmd spring-boot:run
   ```
5. The first time you run this it will download dependencies, which may take a minute or two. Wait until you see a line that says `Started PizzaStoreApplication` — the backend is now running on **http://localhost:8080**.

### Terminal 2: Start the frontend

1. Open a **second Command Prompt** window
2. Navigate to the frontend folder:
   ```cmd
   cd C:\Users\ericr\IdeaProjects\PizzaStore\frontend
   ```
3. Install frontend dependencies (only needed the first time, or after pulling new changes):
   ```cmd
   npm install
   ```
4. Start the Angular development server:
   ```cmd
   npm start
   ```
5. Wait until you see `Compiled successfully` or `Application bundle generation complete` — the frontend is now running on **http://localhost:4200**.

### Open the app

Open your browser and go to:

```
http://localhost:4200
```

You will see the **Pizza Store** header with a navigation bar containing:

- **Menu** — Browse the pizza menu with crust types and prices
- **Restaurant Info** — View the restaurant name, address, phone, and hours
- **User Tools** (dropdown) — Hover to reveal:
  - **Previous Orders** — View order history and delivery status
  - **Profile** — View customer profile details
  - **Login** — Sign-in page with username/password fields
  - **New Account** — Create a new account with email and password
  - **Admin** — Store dashboard with daily stats (links to Reporting)
- **Shopping Cart** (cart icon) — View cart items and total
  - Links to the **Order Confirmation & Payment** page (Checkout)

Additional pages not in the main nav:

- **Checkout** (`/checkout`) — Order summary with subtotal, tax, and total. Accessible from the Shopping Cart page.
- **Reporting** (`/reporting`) — Store performance metrics. Accessible from the Admin page via "View Reports".

### Stopping the application

Press `Ctrl+C` in each Command Prompt window to stop the servers.

## Project Structure

```
PizzaStore/
├── src/main/java/
│   ├── SecurityBeans.java               # Spring Security config (BCrypt, CORS)
│   └── com/pizzastore/                  # Java backend (Spring Boot)
│       ├── PizzaStoreApplication.java   # Application entry point
│       ├── model/                       # Data models
│       │   └── User.java               # User account model
│       ├── repository/                  # Database repositories
│       │   └── UserRepository.java      # User lookup queries
│       └── controller/
│           ├── PizzaController.java     # Pizzas, orders, stats endpoints
│           ├── AuthController.java      # Authentication & sign-in
│           ├── RestaurantInfoController.java # Restaurant details
│           ├── ProfileController.java   # Customer profile
│           ├── CartController.java      # Shopping cart
│           ├── CheckoutController.java  # Checkout / order summary
│           └── ReportingController.java # Store reports
├── src/main/resources/
│   └── application.properties           # Server and database configuration
├── frontend/                            # Angular frontend
│   ├── proxy.conf.json                  # Dev proxy (forwards /api to backend)
│   └── src/app/
│       ├── app.ts, app.html, app.css    # Root component with nav bar
│       ├── app.routes.ts                # Route definitions
│       ├── menu/                        # Menu page
│       ├── orders/                      # Previous Orders page
│       ├── admin/                       # Admin dashboard
│       ├── login/                       # Login page
│       ├── newAccount/                  # New Account page
│       ├── restaurant-info/             # Restaurant Info page
│       ├── profile/                     # Customer Profile page
│       ├── cart/                        # Shopping Cart page
│       ├── checkout/                    # Order Confirmation & Payment page
│       └── reporting/                   # Reporting page
├── pom.xml                              # Maven build configuration
├── mvnw.cmd                             # Maven wrapper (no Maven install needed)
└── .mvn/wrapper/                        # Maven wrapper support files
```

## API Endpoints

The backend exposes the following REST endpoints:

| Endpoint | Description |
|---|---|
| `GET /api/pizzas` | List all pizzas |
| `GET /api/crust-types` | List crust types (from database) |
| `GET /api/orders` | List all orders |
| `GET /api/stats` | Get store statistics |
| `GET /api/auth/status` | Get authentication status |
| `POST /api/auth/signin` | Sign in with username and password |
| `GET /api/restaurant-info` | Get restaurant name, address, phone, hours |
| `GET /api/profile` | Get customer profile |
| `GET /api/cart` | Get shopping cart items and total |
| `GET /api/checkout/summary` | Get order summary with subtotal, tax, total |
| `GET /api/reports` | Get store performance reports |

During development, the Angular proxy (`proxy.conf.json`) forwards `/api` requests from port 4200 to the backend on port 8080.

## Building for Production

### Build the backend

```cmd
mvnw.cmd package -DskipTests
```

This creates `target/pizza-store-0.0.1-SNAPSHOT.jar`.

### Build the frontend

```cmd
cd frontend
npx ng build
```

This creates production files in `frontend/dist/frontend/`.

### Run the production jar

```cmd
java -jar target/pizza-store-0.0.1-SNAPSHOT.jar
```

## Security

The app uses Spring Security with BCrypt password hashing. Passwords in the database are stored as BCrypt hashes, not plain text. The security configuration lives in `SecurityBeans.java` in the root source package (`src/main/java/`).

## Troubleshooting

- **`mvnw.cmd` shows "Downloading Maven Wrapper..."** — This is normal on first run. It downloads the Maven wrapper jar automatically.
- **`npm install` is slow** — This is normal the first time. It downloads all Angular dependencies.
- **Port 8080 or 4200 already in use** — Another application is using that port. Stop it or change the port in `src/main/resources/application.properties` (backend) or use `npx ng serve --port 4300` (frontend).
- **Java not found** — Make sure Java 8 is installed and `java` is in your system PATH. You can also set `JAVA_HOME` to your JDK directory:
  ```cmd
  set JAVA_HOME=C:\Program Files\Java\jdk-1.8
  ```
- **"No compiler is provided" error** — You have a JRE but not a JDK. Make sure `JAVA_HOME` points to a JDK (not JRE) installation, and that you set it before running `mvnw.cmd`.
- **Frontend can't reach backend API** — Make sure the backend is running on port 8080 before starting the frontend. The Angular dev server proxies `/api` requests to `localhost:8080`.
