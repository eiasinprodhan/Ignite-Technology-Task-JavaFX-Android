package com.ignite.desktop.service;

import com.ignite.desktop.model.ReceivedData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static DatabaseService instance;
    private Connection connection;

    private String host = "localhost";
    private String port = "3306";
    private String database = "ignite_comm_db";
    private String username = "root";
    private String password = "1234";

    private volatile boolean isConnected = false;

    private DatabaseService() {}

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public void setConnectionParams(String host, String port,
                                    String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public boolean connect() throws SQLException {
        try {
            // Load MySQL driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, database
            );

            System.out.println("🔄 Connecting to database: " + url);
            connection = DriverManager.getConnection(url, username, password);
            isConnected = true;

            System.out.println("✅ Database connected successfully!");

            // Verify table exists
            ensureTableExists();

            return true;

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found: " + e.getMessage());
            throw new SQLException("MySQL Driver not found", e);
        } catch (SQLException e) {
            isConnected = false;
            System.err.println("❌ Database connection failed: " + e.getMessage());
            throw e;
        }
    }

    private void ensureTableExists() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS received_data (
                id INT AUTO_INCREMENT PRIMARY KEY,
                data_content TEXT NOT NULL,
                sender_ip VARCHAR(50),
                received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(20) DEFAULT 'RECEIVED'
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("✅ Table 'received_data' verified/created");
        } catch (SQLException e) {
            System.err.println("⚠️ Could not verify/create table: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                isConnected = false;
                System.out.println("🔌 Database disconnected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            boolean connected = connection != null && !connection.isClosed() && isConnected;
            if (connected) {
                // Additional check - try a simple query
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SELECT 1");
                }
            }
            return connected;
        } catch (SQLException e) {
            isConnected = false;
            return false;
        }
    }

    public synchronized boolean saveReceivedData(ReceivedData data) {
        if (!isConnected()) {
            System.err.println("❌ Cannot save - database not connected!");
            return false;
        }

        String sql = "INSERT INTO received_data (data_content, sender_ip, status) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, data.getDataContent());
            stmt.setString(2, data.getSenderIp());
            stmt.setString(3, data.getStatus() != null ? data.getStatus() : "RECEIVED");

            System.out.println("💾 Executing INSERT: " + data.getDataContent());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        data.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Data saved to database with ID: " + data.getId());
                return true;
            } else {
                System.err.println("❌ No rows affected");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<ReceivedData> getAllReceivedData() {
        List<ReceivedData> dataList = new ArrayList<>();

        if (!isConnected()) {
            System.err.println("❌ Cannot fetch - database not connected!");
            return dataList;
        }

        String sql = "SELECT * FROM received_data ORDER BY received_at DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ReceivedData data = new ReceivedData();
                data.setId(rs.getInt("id"));
                data.setDataContent(rs.getString("data_content"));
                data.setSenderIp(rs.getString("sender_ip"));

                Timestamp timestamp = rs.getTimestamp("received_at");
                if (timestamp != null) {
                    data.setReceivedAt(timestamp.toLocalDateTime());
                }

                data.setStatus(rs.getString("status"));
                dataList.add(data);
            }

            System.out.println("📂 Fetched " + dataList.size() + " records");

        } catch (SQLException e) {
            System.err.println("❌ Error fetching data: " + e.getMessage());
            e.printStackTrace();
        }

        return dataList;
    }

    public boolean deleteData(int id) {
        if (!isConnected()) return false;

        String sql = "DELETE FROM received_data WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearAllData() {
        if (!isConnected()) return false;

        String sql = "DELETE FROM received_data";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getRecordCount() {
        if (!isConnected()) return 0;

        String sql = "SELECT COUNT(*) as count FROM received_data";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}