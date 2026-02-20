# Pizza Store

A full-stack pizza store application with a Java Spring Boot backend and an Angular frontend.

## Users

All passwords are `Pizza123!`

## Logging

Log file location: `C:\logs\pizzastore`

---

## Prerequisites

You need two things installed on your machine before you can run this project:

1. **Java 17 (JDK 17)**
   - Download: https://www.oracle.com/java/technologies/downloads/#java17
   - If you previously had Java 8 installed, you must update your `JAVA_HOME` environment variable to point to the new JDK 17 directory (see Terminal 1 below).
   - After installing, verify by opening a Command Prompt and running:
     ```cmd
     java -version
     ```
     You should see something like `java version "17.0.x"`.

2. **Node.js 18 or higher** (includes npm)
   - Download: https://nodejs.org/
   - After installing, verify by opening a Command Prompt and running:
     ```cmd
     node --version
     npm --version
     ```

No Maven install is needed. The project includes a Maven wrapper (`mvnw.cmd`) that downloads Maven automatically on first run.

---

## Environment Setup

The app reads database credentials from a `.env` file in the project root. This file is **not** committed to git (it contains passwords). Copy the example file to get started:

```cmd
copy .env.example .env
```

Then open `.env` in a text editor and fill in your database username and password.

---

## Running the Application

You will need **two separate Command Prompt windows** open at the same time.

### Terminal 1: Start the backend

1. Open a **Command Prompt** window
2. Navigate to the project folder:
   ```cmd
   cd C:\Users\ericr\IdeaProjects\PizzaStore
   ```
3. Set JAVA_HOME to your Java 17 installation (adjust the path if yours is different):
   ```cmd
   set JAVA_HOME=C:\Program Files\Java\jdk-17
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

You will see the **Pizza Store** header with a navigation bar. After logging in or creating an account, the logged-in user's name is displayed in the nav bar. The nav contains:

- **Menu** — Browse the pizza menu with crust types and prices
- **Restaurant Info** — View the restaurant name, address, phone, hours, and current promotions
- **User Tools** (dropdown) — Hover to reveal:
  - **Previous Orders** — View order history and delivery status
  - **Profile** — View customer profile details
  - **Login** — Sign-in page with username/password fields and form validation
  - **New Account** — Create a new account with personal info, address, email, and password
  - **Admin** — Store dashboard with daily stats (links to Reporting)
- **Shopping Cart** (cart icon) — View cart items and total
  - Links to the **Order Confirmation & Payment** page (Checkout)

Additional pages not in the main nav:

- **Checkout** (`/checkout`) — Order summary with subtotal, tax, and total. Accessible from the Shopping Cart page.
- **Reporting** (`/reporting`) — Store performance metrics. Accessible from the Admin page via "View Reports".

### Stopping the application

Press `Ctrl+C` in each Command Prompt window to stop the servers.

---

## Project Structure

```
PizzaStore/
├── src/main/java/
│   └── com/pizzastore/                       # Java backend (Spring Boot)
│       ├── PizzaStoreApplication.java        # Application entry point
│       ├── SecurityBeans.java                # Spring Security config (BCrypt password hashing)
│       ├── model/                            # Data models (one class per database table)
│       │   ├── User.java                     # Customer account model
│       │   ├── Employee.java                 # Employee account model
│       │   ├── Address.java                  # Customer address model
│       │   ├── CrustType.java                # Pizza crust option model
│       │   ├── RestaurantInfo.java           # Restaurant name, address, phone, description
│       │   ├── RestaurantHours.java          # Restaurant hours rows (one row per display line)
│       │   └── LoginType.java                # Enum: WORKER, CUSTOMER, UNKNOWN
│       ├── service/
│       │   └── UserTypeResolver.java         # Resolves login type from email domain
│       ├── repository/                       # Database repositories (run the SQL queries)
│       │   ├── UserRepository.java           # Customer lookup and registration queries
│       │   ├── EmployeeRepository.java       # Employee lookup queries
│       │   ├── CrustTypeRepository.java      # Crust type queries
│       │   ├── RestaurantInfoRepository.java # Fetches restaurant details
│       │   ├── RestaurantHoursRepository.java# Fetches restaurant hours rows
│       │   └── PromotionRepository.java      # Fetches active promotions
│       └── controller/                       # REST controllers (handle HTTP requests)
│           ├── AuthController.java           # Authentication: sign-in & registration
│           ├── PizzaController.java          # Pizzas, orders, stats endpoints
│           ├── RestaurantInfoController.java # Restaurant details endpoint
│           ├── RestaurantHoursController.java# Restaurant hours endpoint
│           ├── PromotionController.java      # Promotions endpoint
│           ├── ProfileController.java        # Customer profile
│           ├── CartController.java           # Shopping cart
│           ├── CheckoutController.java       # Checkout / order summary
│           ├── ReportingController.java      # Store reports
│           └── GlobalExceptionHandler.java   # Catches unhandled errors and returns 500
├── src/main/resources/
│   └── application.properties               # Server and database configuration
├── frontend/                                # Angular frontend
│   ├── proxy.conf.json                      # Dev proxy (forwards /api to backend)
│   └── src/app/
│       ├── app.ts, app.html, app.css        # Root component with nav bar
│       ├── app.routes.ts                    # Route definitions
│       ├── auth.service.ts                  # Shared auth state (logged-in user)
│       ├── menu/                            # Menu page
│       ├── orders/                          # Previous Orders page
│       ├── admin/                           # Admin dashboard
│       ├── login/                           # Login page
│       ├── new-account/                     # New Account page
│       ├── restaurant-info/                 # Restaurant Info page
│       ├── profile/                         # Customer Profile page
│       ├── cart/                            # Shopping Cart page
│       ├── checkout/                        # Order Confirmation & Payment page
│       └── reporting/                       # Reporting page
├── src/test/java/com/pizzastore/            # Unit tests (JUnit 5 + Mockito)
│   ├── service/
│   │   └── UserTypeResolverTest.java        # Tests for email domain routing logic
│   └── controller/
│       ├── AuthControllerTest.java          # Tests for login, registration, identify
│       ├── PizzaControllerTest.java         # Tests for menu, orders, stats endpoints
│       ├── CheckoutControllerTest.java      # Tests for checkout summary math
│       ├── PromotionControllerTest.java     # Tests for promotions endpoint
│       ├── RestaurantInfoControllerTest.java# Tests for restaurant info endpoint
│       └── RestaurantHoursControllerTest.java # Tests for restaurant hours endpoint
├── .env.example                             # Template for your .env file (safe to commit)
├── .github/workflows/ci.yml                # GitHub Actions — runs tests on push/PR
├── pom.xml                                  # Maven build configuration
├── mvnw.cmd                                 # Maven wrapper (no Maven install needed)
└── .mvn/wrapper/                            # Maven wrapper support files
```

---

## API Endpoints

The backend exposes the following REST endpoints (all prefixed with `/api`):

| Endpoint | Description |
| --- | --- |
| `GET /api/pizzas` | List all pizzas |
| `GET /api/crust-types` | List crust types |
| `GET /api/orders` | List all orders |
| `GET /api/stats` | Get store statistics |
| `GET /api/restaurant-info` | Get restaurant name, address, and phone number |
| `GET /api/restaurant-hours` | Get restaurant hours (list of display lines) |
| `GET /api/promotions` | Get active promotions |
| `GET /api/profile` | Get customer profile |
| `GET /api/cart` | Get shopping cart items and total |
| `GET /api/checkout/summary` | Get order summary with subtotal, tax, and total |
| `GET /api/reports` | Get store performance reports |
| `GET /api/auth/status` | Get current authentication status |
| `POST /api/auth/identify` | Identify user type (WORKER/CUSTOMER) from email domain |
| `POST /api/auth/signIn/customer` | Sign in as a customer |
| `POST /api/auth/signIn/employee` | Sign in as an employee |
| `POST /api/auth/register/new/customer` | Register a new customer account |

During development, the Angular proxy (`proxy.conf.json`) forwards `/api` requests from port 4200 to the backend on port 8080.

---

## Running Tests

The project uses **JUnit 5** for unit tests and **Mockito** for mocking dependencies (like the database). Tests live in `src/test/java/` and mirror the same package structure as the main code.

### Run tests from the command line

Open a Command Prompt in the project folder and run:

```cmd
mvnw.cmd test
```

This compiles the code and runs all tests. You will see output like:

```
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

If any test fails, the output will show which test failed and why.

### Run tests in IntelliJ

- **Run all tests** — Right-click on the `src/test/java` folder in the Project panel and select **Run 'All Tests'**. If you don't see this option, first right-click the folder → **Mark Directory as** → **Test Sources Root** (the folder should turn green), then try again.
- **Run a single test class** — Open a test file (e.g. `UserTypeResolverTest.java`) and click the green play icon in the gutter next to the class name.
- **Run a single test method** — Click the green play icon next to any individual `@Test` method.

Results appear in IntelliJ's **Run** panel at the bottom with green checkmarks (pass) or red X's (fail).

### Tests run automatically during the build

When you build the project with `mvnw.cmd package`, Maven automatically runs all tests first. If any test fails, the build stops and no jar file is produced. This prevents broken code from being packaged.

### Tests run automatically on GitHub (CI)

The project includes a GitHub Actions workflow (`.github/workflows/ci.yml`) that runs all tests automatically whenever you:

- **Push** to `main` or any `feature/` branch
- **Open a pull request** targeting `main`

You can see the results on GitHub by clicking the **Actions** tab in the repository. Pull requests will show a green checkmark or red X next to each commit indicating whether tests passed.

---

## Building a Production Executable

Follow these steps in order to produce a single runnable `.jar` file that serves both the backend and frontend.

**Step 1 — Build the Angular frontend**

```cmd
cd frontend
npx ng build --configuration production
```

This creates optimized files in `frontend/dist/frontend/browser/`.

**Step 2 — Copy the Angular output into Spring Boot**

Copy everything inside that `browser/` folder into:

```
src/main/resources/static/
```

Spring Boot automatically serves any files placed in `static/` as web resources. When someone visits `http://localhost:8080/` it will serve `index.html`.

**Step 3 — Package the Spring Boot jar**

Go back to the project root and run:

```cmd
cd ..
mvnw.cmd package
```

This produces `target/pizza-store-0.0.1-SNAPSHOT.jar`.

**Step 4 — Run the jar**

```cmd
java -jar target/pizza-store-0.0.1-SNAPSHOT.jar
```

Open `http://localhost:8080` in your browser — no separate frontend server needed.

> **Note:** All of these steps can also be run from within IntelliJ using the Maven panel on the right side.

---

## Security

The app uses Spring Security with BCrypt password hashing. Passwords in the database are stored as BCrypt hashes, not plain text. The security configuration lives in `SecurityBeans.java`.

Database credentials are loaded from a `.env` file at startup. Never commit your `.env` file — it is listed in `.gitignore`. The `.env.example` file shows which variables are needed without exposing real values.

---

## Authentication Flow

Login uses a two-step process:

1. **Identify** — The user enters their email. The backend's `UserTypeResolver` checks the email domain against the company domain configured in `application.properties` (`company.email.domain`). Emails matching the company domain are routed to the employee login flow; all others go to the customer login flow.
2. **Sign in** — The frontend calls the appropriate endpoint (`/api/auth/signIn/customer` or `/api/auth/signIn/employee`) with the email and password. The backend verifies the password against the BCrypt hash in the database and returns a user object (without the password) on success.

New customers can register via the **New Account** page, which collects personal info, address, and credentials. After registration, the user is automatically logged in. The logged-in user's name is displayed in the nav bar across all pages via the shared `AuthService`.

---

## Troubleshooting

- **`mvnw.cmd` shows "Downloading Maven Wrapper..."** — This is normal on first run. It downloads the Maven wrapper jar automatically.
- **`npm install` is slow** — This is normal the first time. It downloads all Angular dependencies.
- **Port 8080 or 4200 already in use** — Another application is using that port. Stop it or change the port in `src/main/resources/application.properties` (backend) or use `npx ng serve --port 4300` (frontend).
- **Java not found** — Make sure Java 17 is installed and `java` is in your system PATH. You can also set `JAVA_HOME` before running the backend:
  ```cmd
  set JAVA_HOME=C:\Program Files\Java\jdk-17
  ```
- **"No compiler is provided" error** — You have a JRE but not a JDK. Make sure `JAVA_HOME` points to a JDK 17 (not a JRE) installation.
- **Upgrading from Java 8** — Install JDK 17 and update your `JAVA_HOME` environment variable. The old Java 8 JDK will not work with this project.
- **Frontend can't reach backend API** — Make sure the backend is running on port 8080 before starting the frontend. The Angular dev server proxies `/api` requests to `localhost:8080`.
- **Database connection error** — Make sure your `.env` file exists in the project root and contains the correct database username and password. Copy `.env.example` to `.env` as a starting point.
