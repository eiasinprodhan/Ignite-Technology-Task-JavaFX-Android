# 📱 Desktop-Mobile Communication System (Java Developer Technical Assignment)

This repository contains the solution for the Java Developer technical assignment: designing and developing a simple client-server communication system where a Mobile Application sends data to a Desktop Application using an IP address and port, and the Desktop Application receives, displays, and stores the data in a local database.

The solution is implemented using **Java** across both platforms, leveraging **Sockets** for network communication, **JavaFX** for the desktop GUI, and a **SQLite** local database for persistence.

## 🚀 Architecture and Technologies

The system follows a classic **Client-Server** model:

| Component | Role | Technology/Framework | Language |
| :--- | :--- | :--- | :--- |
| **Mobile App** (Client) | Sends the sample data over the network. | Android SDK | Java |
| **Desktop App** (Server) | Listens for data, displays it, and stores it in a database. | JavaFX (GUI) + Sockets (Networking) | Java |
| **Database** | Persistent storage for received data. | SQLite | SQL |



## ✨ Part 1: Mobile Application (Client)

The Mobile Application is a single-page Android app responsible for initiating the communication.

### Key Features

* **Single-Page Interface:** Simple, focused UI.
* **Network Transmission:** Uses an Android `AsyncTask` (or modern equivalent like `ExecutorService`) to establish a `Socket` connection and send the predefined sample data: `"Data Send"`.
* **Target:** Configured to send data to the static address `192.168.0.105:3005`.
* **Basic Error Handling:** Provides user feedback (e.g., via Toast messages) on successful data transmission or connection failure.

### Previews (Mobile App)

| **Android(1).jpg** | **Android(2).jpg** |
| :---: | :---: |
| ![Mobile App Initial View](Previews/Android(1).jpg) | ![Mobile App Data Sent Confirmation](Previews/Android(2).jpg) |

## ✨ Part 2: Desktop Application (Server)

The Desktop Application, built with **JavaFX**, acts as the server, database manager, and UI previewer.

### Pages & Functionality

#### 1. Connection Page (Initial View)

* **Database Connection:** Contains a **CONNECT DB** button to establish a connection with the local SQLite database. The connection status is displayed.
* **Mobile App Connection (Server Startup):** Contains a **START SERVER** button. Clicking this starts a `ServerSocket` listener on the specified IP and Port (`192.168.0.105:3005`) in a background thread, ready to accept incoming client connections.
* **Dynamic Configuration (Optional):** Placeholder fields allow for dynamic input of IP Address and Port, although the required configuration is set by default.

#### 2. Data Preview & Storage Page

This page is accessible once the server is successfully running and the database is connected.

* **Real-Time Preview:** Displays the data received from the Mobile App in real-time (or near real-time) within a table or dedicated display area.
* **Data Persistence:** A background thread handles incoming client connections and, upon receiving data, immediately inserts a new record (including a timestamp and the received data) into the local SQLite database table.

### Previews (Desktop App)

| Feature | Screenshot |
| :--- | :--- |
| **Desktop(1).png** - Initial Connection View | ![Desktop App - Initial Connection View](Previews/Desktop(1).png) |
| **Desktop(2).png** - Server Started and Listening | ![Desktop App - Server Started and Listening](Previews/Desktop(2).png) |
| **Desktop(3).png** - Database Connection Successful | ![Desktop App - Database Connection Successful](Previews/Desktop(3).png) |
| **Desktop(4).png** - Data Received from Mobile App | ![Desktop App - Data Received](Previews/Desktop(4).png) |
| **Desktop(5).png** - Data Table (Persistence) | ![Desktop App - Data Stored in Table](Previews/Desktop(5).png) |

## 🛠️ Setup and How to Run

### Prerequisites

* Java Development Kit (JDK) 17+
* Android Studio (for Mobile App)
* IDE supporting JavaFX (e.g., IntelliJ IDEA, Eclipse) (for Desktop App)

### Running the Desktop Server

1.  Open the Desktop App project (e.g., `DesktopApp/`).
2.  Ensure JavaFX libraries are correctly configured in your IDE.
3.  Run the main application class.
4.  Click **CONNECT DB** and then **START SERVER**. The server will be listening on `192.168.0.105:3005`.

### Running the Mobile Client

1.  Open the Mobile App project (e.g., `MobileApp/`) in Android Studio.
2.  Ensure the target server address in the code matches the required `192.168.0.105:3005`.
3.  Run the app on an Android device or emulator **that is on the same local network** as the Desktop Server.
4.  Click the **SEND DATA** button.

## ✅ Evaluation Criteria Fulfillment

| Criteria | Implementation Details |
| :--- | :--- |
| **Correct Network Communication** | Implemented using standard Java `Socket` (Client) and `ServerSocket` (Server) classes. Communication is synchronous and direct via TCP/IP. |
| **Database Connectivity** | Uses the **JDBC** driver for SQLite to establish a local connection. Database operations are handled asynchronously to prevent UI freezing. |
| **Data Persistence** | Every successful receipt of data triggers an `INSERT` SQL statement, ensuring persistent storage in the SQLite database table. |
| **Code Quality & Structure** | Code is separated into clear packages (e.g., `ui`, `network`, `database`), uses descriptive naming, and follows Java best practices. Desktop UI logic and background networking tasks are separated. |
| **Error Handling** | Includes `try-catch` blocks for common network exceptions (`IOException`) and database exceptions (`SQLException`), providing feedback to the user on failures. |