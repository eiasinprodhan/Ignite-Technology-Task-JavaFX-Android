# 🚀 Desktop-Mobile Communication System: Real-Time Data Transfer

## 🎯 Project Overview

This repository presents the solution for the Java Developer technical assignment: a robust client-server system designed for real-time data communication between a mobile client and a desktop server. The system efficiently handles network transmission, real-time data display, and persistent storage in a centralized **MySQL** database.

---

## ✨ Key Features & Technologies

The solution is built on modern Java technologies, ensuring stability and performance.

| Component                | Role                                          | Technology Stack                                             |
| :----------------------- | :-------------------------------------------- | :----------------------------------------------------------- |
| **Server** (Desktop App) | Data Listener, UI Dashboard, Database Manager | **JavaFX**, **JDBC**, **MySQL Connector/J**, **TCP Sockets** |
| **Client** (Mobile App)  | Data Sender with Dynamic Input                | **Android SDK**, **Java**, **TCP Sockets**                   |
| **Persistence**          | Centralized Data Storage                      | **MySQL RDBMS**                                              |

### 🛠️ Core Technology Tags

**`JAVA`** $\cdot$ **`JavaFX`** $\cdot$ **`Android`** $\cdot$ **`TCP/IP`** $\cdot$ **`MySQL`** $\cdot$ **`JDBC`**

---

## 📱 Part 1: Mobile Application (Client)

The Android-based client is responsible for initiating communication and sending custom, user-defined data.

### Functionality

- **Dynamic Messaging:** Users can input custom text to send to the server, enhancing testing flexibility.
- **Targeted Transmission:** Configured to communicate with the Desktop Server at the designated address: `192.168.0.105:3005`.
- **Asynchronous Networking:** Uses background threads to handle network operations, keeping the UI responsive.
- **Feedback System:** Provides simple alerts (Toast messages) for successful data transmission or connection failures.

### Previews (Client UI)

|                      1. Message Input Screen                      |                    2. Data Sent Confirmation                    |
| :---------------------------------------------------------------: | :-------------------------------------------------------------: |
| ![Mobile App with Message Input Field](<Previews/Android(1).jpg>) | ![Mobile App Data Sent Confirmation](<Previews/Android(2).jpg>) |

---

## 💻 Part 2: Desktop Application (Server)

The JavaFX application acts as a multi-stage server, separating connection setup from operational monitoring.

### 1. Connection Setup (`ConnectionPage.fxml`)

This initial screen ensures all necessary system components are active before monitoring begins.

- ✅ **MySQL Connection:** Establishes a connection to the configured MySQL database instance via JDBC.
- ✅ **Server Initialization:** Starts the `ServerSocket` listener on the specified IP and Port in a dedicated background thread.
- ✅ **Guarded Navigation:** A **GO TO MESSAGE VIEWER** button only becomes active once both the database and the network server are successfully connected.

### 2. Data Viewer & Storage (`DataViewer.fxml`)

This is the operational dashboard for real-time data management.

- **📈 Real-Time Preview:** Displays incoming messages from mobile clients instantly in a JavaFX `TableView`.
- **💾 Persistent Storage:** Upon receiving a message, the server immediately inserts the data (message content + timestamp) as a new, unique record into the MySQL database.
- **Concurrent Handling:** Uses multi-threading to continuously listen for new client connections while ensuring UI remains updated and responsive.

### Previews (Server UI Flow)

|    Step    | Description                        |                                     Screenshot                                     |
| :--------: | :--------------------------------- | :--------------------------------------------------------------------------------: |
| **Step 1** | Initial Connection Screen          |        ![Desktop App - Initial Connection View](<Previews/Desktop(1).png>)         |
| **Step 2** | Both Database and Server Connected | ![Desktop App - Server Started and Ready for Next Page](<Previews/Desktop(2).png>) |
| **Step 3** | Message Viewer Dashboard (Ready)   |        ![Desktop App - Data Viewer Page (Empty)](<Previews/Desktop(3).png>)        |
| **Step 4** | Real-Time Data Reception           |   ![Desktop App - New Message Received and Displayed](<Previews/Desktop(4).png>)   |
| **Step 5** | Stored Data Proof (Table View)     |          ![Desktop App - Data Stored in Table](<Previews/Desktop(5).png>)          |

---

## ⚙️ Running the Application

### Prerequisites

Ensure you have the following installed and configured:

1.  **Java Development Kit (JDK) 17+**
2.  **Android Studio** (for Client development)
3.  **MySQL Server** (Running and accessible)
4.  **MySQL Connector/J JAR** (Included in the Desktop App's classpath)
5.  **JavaFX SDK** (Set up in your Desktop IDE, e.g., IntelliJ IDEA)

### Execution Steps

1.  **Start MySQL:** Ensure your MySQL service is running and the target table structure is created.
2.  **Start Desktop Server:**
    - Run the main JavaFX application.
    - Click **CONNECT DB**.
    - Click **START SERVER**.
    - Click **GO TO MESSAGE VIEWER**.
3.  **Start Mobile Client:**
    - Run the Android application on a device/emulator **on the same local network** as the Desktop Server.
    - Enter a message and click **SEND DATA**.
    - Verify the message appears instantly in the Desktop App's viewer and is saved to MySQL.

---

## ✅ Assignment Fulfillment Summary

| Criteria                     | Implementation Highlights                                                                                         |
| :--------------------------- | :---------------------------------------------------------------------------------------------------------------- |
| **Network Communication**    | Robust, multi-threaded TCP/IP socket implementation for reliable data transfer.                                   |
| **Database Persistence**     | Successful integration with **MySQL** using JDBC, ensuring every message is stored as a new, time-stamped record. |
| **Code Structure & Quality** | Clean separation of concerns (Network, DB, UI) using modular Java classes and FXML controllers.                   |
| **UI/UX**                    | Professional JavaFX interface with a two-stage connection process for improved stability and user feedback.       |
| **Functionality**            | Supports custom, dynamic messaging from the Mobile Client.                                                        |
