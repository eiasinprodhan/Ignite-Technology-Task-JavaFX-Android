-- Create Database
CREATE DATABASE IF NOT EXISTS ignite_comm_db;

USE ignite_comm_db;

-- Create Table for Received Data
CREATE TABLE IF NOT EXISTS received_data (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             data_content VARCHAR(500) NOT NULL,
    sender_ip VARCHAR(50),
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'RECEIVED'
    );

-- Create index for faster queries
CREATE INDEX idx_received_at ON received_data(received_at);

-- Insert sample data (optional)
INSERT INTO received_data (data_content, sender_ip) VALUES
                                                        ('Sample Data 1', '192.168.0.100'),
                                                        ('Sample Data 2', '192.168.0.101');

-- Verify table creation
DESCRIBE received_data;

-- Select all data
SELECT * FROM received_data;