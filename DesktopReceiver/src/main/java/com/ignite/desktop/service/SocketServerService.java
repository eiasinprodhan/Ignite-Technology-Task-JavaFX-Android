package com.ignite.desktop.service;

import com.ignite.desktop.model.ReceivedData;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SocketServerService {

    private static SocketServerService instance;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;

    private String ipAddress = "192.168.0.101";
    private int port = 3005;

    private volatile Consumer<ReceivedData> onDataReceivedCallback;
    private volatile Consumer<String> onStatusChangeCallback;
    private volatile Consumer<String> onErrorCallback;

    private SocketServerService() {
        executorService = Executors.newCachedThreadPool();
    }

    public static synchronized SocketServerService getInstance() {
        if (instance == null) {
            instance = new SocketServerService();
        }
        return instance;
    }

    public void setConnectionParams(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void setOnDataReceivedCallback(Consumer<ReceivedData> callback) {
        this.onDataReceivedCallback = callback;
        System.out.println("📌 Data callback registered: " + (callback != null));
    }

    public void setOnStatusChangeCallback(Consumer<String> callback) {
        this.onStatusChangeCallback = callback;
    }

    public void setOnErrorCallback(Consumer<String> callback) {
        this.onErrorCallback = callback;
    }

    public int getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getServerInfo() {
        if (isRunning) {
            return ipAddress + ":" + port;
        }
        return "Not running";
    }

    public String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public String getSocketInfo() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            return "Listening on " + serverSocket.getLocalSocketAddress();
        }
        return null;
    }

    public int getBoundPort() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            return serverSocket.getLocalPort();
        }
        return -1;
    }


    public boolean startServer() {
        try {
            // Try binding to specific IP first
            try {
                InetAddress bindAddress = InetAddress.getByName(ipAddress);
                serverSocket = new ServerSocket(port, 50, bindAddress);
            } catch (BindException e) {
                // Fall back to all interfaces
                serverSocket = new ServerSocket(port);
                ipAddress = getLocalIpAddress();
            }

            // Update port to actual bound port (in case 0 was used)
            port = serverSocket.getLocalPort();

            isRunning = true;
            notifyStatus("✅ Server started on " + ipAddress + ":" + port);
            System.out.println("🚀 Server listening on " + ipAddress + ":" + port);

            // Start accepting connections in a separate thread
            executorService.submit(this::acceptConnections);

            return true;

        } catch (IOException e) {
            notifyError("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void acceptConnections() {
        while (isRunning && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();

                System.out.println("📱 Client connected: " + clientIp);
                notifyStatus("📱 Client connected: " + clientIp);

                // Handle client in a separate thread
                executorService.submit(() -> handleClient(clientSocket));

            } catch (SocketException e) {
                if (isRunning) {
                    System.err.println("Socket exception: " + e.getMessage());
                }
            } catch (IOException e) {
                if (isRunning) {
                    notifyError("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        String clientIp = clientSocket.getInetAddress().getHostAddress();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(
                     clientSocket.getOutputStream(), true)) {

            String receivedMessage;
            while ((receivedMessage = reader.readLine()) != null) {
                final String message = receivedMessage.trim();

                System.out.println("📩 Received: " + message + " from " + clientIp);

                // Create data object
                ReceivedData data = new ReceivedData(message, clientIp);

                // Check if callback is registered
                if (onDataReceivedCallback != null) {
                    System.out.println("📤 Sending data to callback...");

                    // Notify callback on JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            onDataReceivedCallback.accept(data);
                            System.out.println("✅ Callback executed successfully");
                        } catch (Exception e) {
                            System.err.println("❌ Callback error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } else {
                    System.err.println("⚠️ No callback registered! Data will not be shown in UI.");
                }

                // Send acknowledgment
                writer.println("ACK: Data received successfully");
                writer.flush();
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("🔌 Client disconnected: " + clientIp);
                notifyStatus("🔌 Client disconnected: " + clientIp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopServer() {
        isRunning = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            notifyStatus("🛑 Server stopped");
            System.out.println("🛑 Server stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyStatus(String status) {
        if (onStatusChangeCallback != null) {
            Platform.runLater(() -> onStatusChangeCallback.accept(status));
        }
    }

    private void notifyError(String error) {
        if (onErrorCallback != null) {
            Platform.runLater(() -> onErrorCallback.accept(error));
        }
    }

    public void shutdown() {
        stopServer();
        executorService.shutdown();
    }

    public String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Server Status: ").append(isRunning ? "Running" : "Stopped").append("\n");
        stats.append("IP Address: ").append(ipAddress).append("\n");
        stats.append("Port: ").append(port).append("\n");
        if (serverSocket != null && !serverSocket.isClosed()) {
            stats.append("Bound Address: ").append(serverSocket.getLocalSocketAddress()).append("\n");
        }
        return stats.toString();
    }
}