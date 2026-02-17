# Pizza Store

A full-stack pizza store application with a Java Spring Boot backend and an Angular frontend.

## Users

All passwords are Pizza123!


## Loggin
Log file = C:\logs\pizzastore

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
- **Restaurant Info** — View the restaurant name, address, phone, and hours
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

## Project Structure

```
PizzaStore/
├── src/main/java/
│   ├── SecurityBeans.java               # Spring Security config (BCrypt, CORS)
│   └── com/pizzastore/                  # Java backend (Spring Boot)
│       ├── PizzaStoreApplication.java   # Application entry point
│       ├── model/                       # Data models
│       │   ├── User.java               # Customer account model
│       │   ├── Employee.java           # Employee account model
│       │   ├── Address.java            # Customer address model
│       │   ├── CrustType.java          # Pizza crust option model
│       │   └── LoginType.java          # Enum: WORKER, CUSTOMER, UNKNOWN
│       ├── service/
│       │   └── UserTypeResolver.java    # Resolves login type from email domain
│       ├── repository/                  # Database repositories
│       │   ├── UserRepository.java      # Customer lookup and registration queries
│       │   ├── EmployeeRepository.java  # Employee lookup queries
│       │   └── CrustTypeRepository.java # Crust type queries
│       └── controller/
│           ├── PizzaController.java     # Pizzas, orders, stats endpoints
│           ├── AuthController.java      # Authentication, sign-in & registration
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
│       ├── auth.service.ts              # Shared auth state (logged-in user)
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
├── src/test/java/com/pizzastore/        # Unit tests (JUnit 5 + Mockito)
│   ├── service/
│   │   └── UserTypeResolverTest.java    # Tests for email domain routing logic
│   └── controller/
│       ├── AuthControllerTest.java      # Tests for login, registration, identify
│       ├── PizzaControllerTest.java     # Tests for menu, orders, stats endpoints
│       └── CheckoutControllerTest.java  # Tests for checkout summary math
├── .github/workflows/ci.yml            # GitHub Actions — runs tests on push/PR
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
| `POST /api/auth/identify` | Identify user type (WORKER/CUSTOMER) from email domain |
| `POST /api/auth/signin/customer` | Sign in as a customer with email and password |
| `POST /api/auth/signin/employee` | Sign in as an employee with email and password |
| `POST /api/auth/register/new/customer` | Register a new customer account with personal info, address, and credentials |
| `GET /api/restaurant-info` | Get restaurant name, address, phone, hours |
| `GET /api/profile` | Get customer profile |
| `GET /api/cart` | Get shopping cart items and total |
| `GET /api/checkout/summary` | Get order summary with subtotal, tax, total |
| `GET /api/reports` | Get store performance reports |

During development, the Angular proxy (`proxy.conf.json`) forwards `/api` requests from port 4200 to the backend on port 8080.

## Running Tests

The project uses **JUnit 5** for unit tests and **Mockito** for mocking dependencies (like the database). Tests live in `src/test/java/` and mirror the same package structure as the main code.

### Run tests from the command line

Open a Command Prompt in the project folder and run:

```cmd
mvnw.cmd test
```

This compiles the code and runs all tests. You will see output like:

```
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
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

## Building for Production

### Build the backend

```cmd
mvnw.cmd package
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

## Authentication Flow

Login uses a two-step process:

1. **Identify** — The user enters their email. The backend's `UserTypeResolver` checks the email domain against the company domain configured in `application.properties` (`company.email.domain`). Emails matching the company domain are routed to the employee login flow; all others are routed to the customer login flow.
2. **Sign in** — The frontend calls the appropriate endpoint (`/api/auth/signin/customer` or `/api/auth/signin/employee`) with the email and password. The backend verifies the password against the BCrypt hash in the database and returns a user DTO (excluding the password hash) on success.

New customers can register via the **New Account** page, which collects personal info, address, and credentials. After registration, the user is automatically logged in. The logged-in user's name is displayed in the nav bar across all pages via the shared `AuthService`.

## Troubleshooting

- **`mvnw.cmd` shows "Downloading Maven Wrapper..."** — This is normal on first run. It downloads the Maven wrapper jar automatically.
- **`npm install` is slow** — This is normal the first time. It downloads all Angular dependencies.
- **Port 8080 or 4200 already in use** — Another application is using that port. Stop it or change the port in `src/main/resources/application.properties` (backend) or use `npx ng serve --port 4300` (frontend).
- **Java not found** — Make sure Java 17 is installed and `java` is in your system PATH. You can also set `JAVA_HOME` to your JDK directory:
  ```cmd
  set JAVA_HOME=C:\Program Files\Java\jdk-17
  ```
- **"No compiler is provided" error** — You have a JRE but not a JDK. Make sure `JAVA_HOME` points to a JDK 17 (not JRE) installation, and that you set it before running `mvnw.cmd`.
- **Upgrading from Java 8** — If you previously had Java 8 installed, you need to install JDK 17 and update your `JAVA_HOME` environment variable. The old Java 8 JDK will no longer work with this project.
- **Frontend can't reach backend API** — Make sure the backend is running on port 8080 before starting the frontend. The Angular dev server proxies `/api` requests to `localhost:8080`.
