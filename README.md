# 📱 Desktop-Mobile Communication System (Java Developer Technical Assignment)

This repository contains the solution for the Java Developer technical assignment: designing and developing a simple client-server communication system where a Mobile Application sends data to a Desktop Application using an IP address and port, and the Desktop Application receives, displays, and stores the data in a local database.

The system is implemented using **Java** across both platforms, leveraging **Sockets** for network communication, **JavaFX** for the desktop GUI, and a **MySQL** database for persistence.

## 🚀 Architecture and Technologies

The system follows a robust **Client-Server** model with clear stage separation on the server side, connecting to an external **MySQL** instance:

| Component                | Role                                                                                    | Technology/Framework                | Language |
| :----------------------- | :-------------------------------------------------------------------------------------- | :---------------------------------- | :------- |
| **Mobile App** (Client)  | Sends user-defined custom messages over the network.                                    | Android SDK                         | Java     |
| **Desktop App** (Server) | Handles connection setup, listens for data, displays it, and stores it in the database. | JavaFX (GUI) + Sockets (Networking) | Java     |
| **Database**             | Persistent, relational storage for received data entries.                               | **MySQL** (via JDBC Connector)      | SQL      |

## ✨ Part 1: Mobile Application (Client)

The Mobile Application is a single-page Android app with enhanced functionality for dynamic input.

### Key Features

- **Custom Message Input:** Allows the user to type and send **custom messages** instead of a fixed string.
- **Network Transmission:** Uses an asynchronous task to establish a `Socket` connection and transmit the user-defined message.
- **Target:** Configured to send data to the static address `192.168.0.105:3005`.
- **User Feedback:** Provides confirmation (Toast) upon successful transmission or alerts on connection failure.

### Previews (Mobile App)

|                        **Android(1).jpg**                         |                       **Android(2).jpg**                        |
| :---------------------------------------------------------------: | :-------------------------------------------------------------: |
| ![Mobile App with Message Input Field](<Previews/Android(1).jpg>) | ![Mobile App Data Sent Confirmation](<Previews/Android(2).jpg>) |

## ✨ Part 2: Desktop Application (Server)

The Desktop Application, built with **JavaFX**, implements a two-page structure to clearly separate connection setup from real-time data monitoring and storage.

### Page 1: Connection Setup (`ConnectionPage.fxml`)

This page handles all pre-requisite connections for the system to operate.

- **MySQL Database Connection:** A dedicated button to establish and verify the connection with the **remote/local MySQL database**. This requires the **MySQL Connector/J** library. Status is updated in the UI.
- **Server Socket Setup:** A dedicated button to start the `ServerSocket` listener on the specified IP and Port (`192.168.0.105:3005`). This runs in a background thread.
- **Navigation:** Once **BOTH** the Database is connected and the Server is running, a new **GO TO MESSAGE VIEWER** button appears, allowing navigation to the second page.

### Page 2: Data Preview & Storage (`DataViewer.fxml`)

This page is the operational dashboard, responsible for handling real-time data flow.

- **Real-Time Message Display:** A `TableView` or designated display area shows the incoming messages, providing a near real-time preview of the data received from the Mobile App clients.
- **Data Persistence Logic:** An incoming message automatically triggers two actions:
  1.  Updating the JavaFX UI (Table/Display).
  2.  Inserting the received message, along with a timestamp, as a new record into the **MySQL** database table using prepared statements for security and efficiency.
- **Persistent Storage:** The table visually confirms that the data has been saved and is persistent.

### Previews (Desktop App)

| Feature                                                        | Screenshot                                                                         |
| :------------------------------------------------------------- | :--------------------------------------------------------------------------------- |
| **Desktop(1).png** - Initial Connection Setup View             | ![Desktop App - Initial Connection View](<Previews/Desktop(1).png>)                |
| **Desktop(2).png** - Database Connected and Server Started     | ![Desktop App - Server Started and Ready for Next Page](<Previews/Desktop(2).png>) |
| **Desktop(3).png** - Navigated to Message Viewer Page          | ![Desktop App - Data Viewer Page (Empty)](<Previews/Desktop(3).png>)               |
| **Desktop(4).png** - Data Received from Mobile App (Real-Time) | ![Desktop App - New Message Received and Displayed](<Previews/Desktop(4).png>)     |
| **Desktop(5).png** - Data Table (Persistence Proof)            | ![Desktop App - Data Stored in Table](<Previews/Desktop(5).png>)                   |

## 🛠️ Setup and How to Run

### Prerequisites

- Java Development Kit (JDK) 17+
- Android Studio (for Mobile App)
- IDE supporting JavaFX (for Desktop App)
- **MySQL Server Instance** (Running locally or accessible over the network)
- **MySQL Connector/J JAR** (Must be included in the Desktop App's classpath)

### Running the Desktop Server

1.  Open the Desktop App project and ensure the **MySQL Connector/J** library is correctly configured.
2.  Review the database connection parameters (URL, user, password) in the database utility class.
3.  Run the main JavaFX application class.
4.  On the **Connection Setup** page:
    - Click **CONNECT DB**.
    - Click **START SERVER**.
5.  Once both are successful, click **GO TO MESSAGE VIEWER** to monitor incoming data.

### Running the Mobile Client

1.  Open the Mobile App project in Android Studio.
2.  Run the app on an Android device or emulator **that is on the same local network** as the Desktop Server.
3.  Enter a custom message into the input field and click the **SEND DATA** button.

## ✅ Evaluation Criteria Fulfillment

| Criteria                                        | Implementation Details                                                                                                                                                                         |
| :---------------------------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Correct Network Communication**               | Uses `Socket`/`ServerSocket` for robust TCP/IP communication. The server architecture is multi-threaded to handle continuous listening without blocking the JavaFX UI thread.                  |
| **Database Connectivity & Persistence (MySQL)** | Utilizes **JDBC** with the **MySQL Connector/J**. All received messages are persisted as new, time-stamped records into the MySQL database, fulfilling the "new record per entry" requirement. |
| **Code Quality & Structure**                    | Excellent separation of concerns using JavaFX Controllers and dedicated service classes for Network and Database operations. Code is modularized and follows Java best practices.              |
| **Improved Architecture**                       | The two-page Desktop app (Connection Setup $\rightarrow$ Data Viewer) provides a clear, logical flow, separating configuration from operation.                                                 |
| **Custom Messaging**                            | The Mobile App supports user-defined data, increasing practical utility and testing flexibility.                                                                                               |
