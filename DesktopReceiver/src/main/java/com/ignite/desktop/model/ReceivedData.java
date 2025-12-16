package com.ignite.desktop.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceivedData {

    private int id;
    private String dataContent;
    private String senderIp;
    private LocalDateTime receivedAt;
    private String status;

    // Constructors
    public ReceivedData() {}

    public ReceivedData(String dataContent, String senderIp) {
        this.dataContent = dataContent;
        this.senderIp = senderIp;
        this.receivedAt = LocalDateTime.now();
        this.status = "RECEIVED";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDataContent() {
        return dataContent;
    }

    public void setDataContent(String dataContent) {
        this.dataContent = dataContent;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedDateTime() {
        if (receivedAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return receivedAt.format(formatter);
        }
        return "";
    }

    @Override
    public String toString() {
        return "ReceivedData{" +
                "id=" + id +
                ", dataContent='" + dataContent + '\'' +
                ", senderIp='" + senderIp + '\'' +
                ", receivedAt=" + receivedAt +
                ", status='" + status + '\'' +
                '}';
    }
}