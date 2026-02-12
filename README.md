# Pizza Store

A full-stack pizza store application with a Java Spring Boot backend and an Angular frontend.

#users
All passwords are Pizza123!

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
3. Start the Spring Boot backend:
   ```cmd
   mvnw.cmd spring-boot:run
   ```
4. The first time you run this it will download dependencies, which may take a minute or two. Wait until you see a line that says `Started PizzaStoreApplication` — the backend is now running on **http://localhost:8080**.

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
   npx ng serve --proxy-config proxy.conf.json
   ```
5. Wait until you see `Compiled successfully` or `Application bundle generation complete` — the frontend is now running on **http://localhost:4200**.

### Open the app

Open your browser and go to:

```
http://localhost:4200
```

You will see the **Pizza Store** header with three navigation links:

- **Menu** — Browse the pizza menu with prices
- **Orders** — View order history and delivery status
- **Admin** — Store dashboard with daily stats

Click each link to switch between pages.

### Stopping the application

Press `Ctrl+C` in each Command Prompt window to stop the servers.

## Project Structure

```
PizzaStore/
├── src/main/java/com/pizzastore/   # Java backend (Spring Boot)
│   ├── PizzaStoreApplication.java  # Application entry point
│   └── controller/
│       └── PizzaController.java    # REST API endpoints
├── src/main/resources/
│   └── application.properties      # Server configuration
├── frontend/                       # Angular frontend
│   └── src/app/
│       ├── menu/                   # Menu page
│       ├── orders/                 # Orders page
│       └── admin/                  # Admin dashboard
├── pom.xml                         # Maven build configuration
├── mvnw.cmd                        # Maven wrapper (no Maven install needed)
└── .mvn/wrapper/                   # Maven wrapper support files
```

## API Endpoints

The backend exposes the following REST endpoints:

- `GET /api/pizzas` — List all pizzas
- `GET /api/orders` — List all orders
- `GET /api/stats` — Get store statistics

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

## Troubleshooting

- **`mvnw.cmd` shows "Downloading Maven Wrapper..."** — This is normal on first run. It downloads the Maven wrapper jar automatically.
- **`npm install` is slow** — This is normal the first time. It downloads all Angular dependencies.
- **Port 8080 or 4200 already in use** — Another application is using that port. Stop it or change the port in `src/main/resources/application.properties` (backend) or use `npx ng serve --port 4300` (frontend).
- **Java not found** — Make sure Java 8 is installed and `java` is in your system PATH. You can also set `JAVA_HOME` to your JDK directory:
  ```cmd
  set JAVA_HOME=C:\Program Files\Java\jdk-1.8
  ```
