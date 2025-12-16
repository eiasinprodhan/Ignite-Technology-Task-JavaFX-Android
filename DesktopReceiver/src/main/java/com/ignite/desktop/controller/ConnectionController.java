package com.ignite.desktop.controller;

import com.ignite.desktop.model.ReceivedData;
import com.ignite.desktop.service.DatabaseService;
import com.ignite.desktop.service.SocketServerService;
import com.ignite.desktop.util.AlertHelper;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ConnectionController implements Initializable {

    private static ObservableList<ReceivedData> sharedDataList = FXCollections.observableArrayList();
    private static StringBuilder sharedLogBuffer = new StringBuilder();
    private static boolean isDatabaseConnected = false;
    private static boolean isServerRunning = false;

    // Track which view is active
    private ViewType currentView = ViewType.CONNECTION;

    private enum ViewType {
        CONNECTION,
        MESSAGE
    }

    // Database Fields
    @FXML private TextField dbHostField;
    @FXML private TextField dbPortField;
    @FXML private TextField dbNameField;
    @FXML private TextField dbUsernameField;
    @FXML private PasswordField dbPasswordField;

    // Server Fields
    @FXML private TextField serverIpField;
    @FXML private TextField serverPortField;

    // Buttons
    @FXML private Button connectDbBtn;
    @FXML private Button connectServerBtn;
    @FXML private Button refreshBtn;
    @FXML private Button clearBtn;
    @FXML private Button deleteBtn;
    @FXML private Button goToMessagesBtn;
    @FXML private Button backToConnectionBtn;
    @FXML private Button clearLogBtn;

    // Status Indicators (Circles)
    @FXML private Circle dbStatusIndicator;
    @FXML private Circle serverStatusIndicator;
    @FXML private Circle liveIndicator;

    // Mini Status Indicators (Message View)
    @FXML private Circle dbStatusIndicatorMini;
    @FXML private Circle serverStatusIndicatorMini;
    @FXML private HBox dbStatusBadgeMini;
    @FXML private HBox serverStatusBadgeMini;
    @FXML private Label dbStatusLabelMini;
    @FXML private Label serverStatusLabelMini;

    // Status Badges (HBox containers)
    @FXML private HBox dbStatusBadge;
    @FXML private HBox serverStatusBadge;

    // Cards (VBox containers)
    @FXML private VBox dbCard;
    @FXML private VBox serverCard;

    // Labels
    @FXML private Label dbStatusLabel;
    @FXML private Label serverStatusLabel;
    @FXML private Label localIpLabel;
    @FXML private Label localIpLabelMsg;
    @FXML private Label recordCountLabel;
    @FXML private Label lastReceivedLabel;
    @FXML private Label refreshIcon;

    // Message View Stats Labels
    @FXML private Label totalMessagesLabel;
    @FXML private Label savedCountLabel;
    @FXML private Label errorCountLabel;
    @FXML private Label lastActivityLabel;
    @FXML private Label tableRecordCountLabel;
    @FXML private Label connectionInfoLabel;
    @FXML private Label serverInfoLabel;
    @FXML private Label dbInfoLabel;
    @FXML private Label liveBadge;

    // Navigation Section
    @FXML private VBox navigationSection;
    @FXML private VBox mainContainer;

    // Table
    @FXML private TableView<ReceivedData> dataTable;
    @FXML private TableColumn<ReceivedData, Integer> idColumn;
    @FXML private TableColumn<ReceivedData, String> dataColumn;
    @FXML private TableColumn<ReceivedData, String> ipColumn;
    @FXML private TableColumn<ReceivedData, String> timeColumn;
    @FXML private TableColumn<ReceivedData, String> statusColumn;

    // Search
    @FXML private TextField searchField;
    private FilteredList<ReceivedData> filteredData;

    // Log Area
    @FXML private TextArea liveLogArea;

    private DatabaseService databaseService;
    private SocketServerService socketServerService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Timeline liveBlinkAnimation;
    private Timeline dbPulseAnimation;
    private Timeline serverPulseAnimation;

    private static final String COLOR_SUCCESS = "#22c55e";
    private static final String COLOR_DANGER = "#ef4444";
    private static final String COLOR_WARNING = "#f59e0b";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 ConnectionController initializing...");

        // Initialize services (singleton)
        databaseService = DatabaseService.getInstance();
        socketServerService = SocketServerService.getInstance();

        // Detect current view type
        detectCurrentView();

        // Setup based on current view
        if (currentView == ViewType.CONNECTION) {
            initializeConnectionView();
        } else {
            initializeMessageView();
        }

        // Common setup
        setupSocketCallbacks();

        System.out.println("✅ ConnectionController initialized for " + currentView + " view");
    }

    private void detectCurrentView() {
        // Detect which view we're in based on FXML elements
        if (dbHostField != null) {
            currentView = ViewType.CONNECTION;
        } else if (dataTable != null && backToConnectionBtn != null) {
            currentView = ViewType.MESSAGE;
        }
    }

    private void initializeConnectionView() {
        setupDefaultValues();
        displayLocalIp();
        setupInitialStatusIndicators();
        setupInitialAnimations();
        restoreConnectionState();
        updateNavigationVisibility();
        restoreLogBuffer();
    }

    private void setupDefaultValues() {
        if (dbHostField == null) return;

        // Database defaults
        dbHostField.setText("localhost");
        dbPortField.setText("3306");
        dbNameField.setText("ignite_comm_db");
        dbUsernameField.setText("root");
        dbPasswordField.setText("");

        // Server defaults - auto-detect local IP
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            serverIpField.setText(localIp);
        } catch (Exception e) {
            serverIpField.setText("192.168.0.105");
        }
        serverPortField.setText("3005");
    }

    private void displayLocalIp() {
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            if (localIpLabel != null) {
                localIpLabel.setText("📍 " + localIp);
            }
            if (localIpLabelMsg != null) {
                localIpLabelMsg.setText("📍 " + localIp);
            }
        } catch (Exception e) {
            if (localIpLabel != null) {
                localIpLabel.setText("📍 Unable to detect IP");
            }
        }
    }

    private void setupInitialStatusIndicators() {
        if (isDatabaseConnected) {
            setDatabaseStatus(ConnectionStatus.CONNECTED);
        } else {
            setDatabaseStatus(ConnectionStatus.DISCONNECTED);
        }

        if (isServerRunning) {
            setServerStatus(ConnectionStatus.RUNNING);
        } else {
            setServerStatus(ConnectionStatus.STOPPED);
        }
    }

    private void setupInitialAnimations() {
        if (mainContainer != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), mainContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void restoreConnectionState() {
        // Restore button states based on current connection status
        if (isDatabaseConnected && connectDbBtn != null) {
            connectDbBtn.setText("Disconnect");
            connectDbBtn.getStyleClass().add("btn-connected");
        }

        if (isServerRunning && connectServerBtn != null) {
            connectServerBtn.setText("Stop Server");
            connectServerBtn.getStyleClass().add("btn-running");
        }

        updateRecordCount();
    }

    private void initializeMessageView() {
        setupTable();
        setupSearch();
        displayLocalIp();
        updateMessageViewStatus();
        updateMessageStats();
        restoreLogBuffer();

        if (isServerRunning) {
            startLiveIndicatorAnimation();
        }

        // Fade in animation
        if (dataTable != null && dataTable.getParent() != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400),
                    dataTable.getParent().getParent());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void updateMessageViewStatus() {
        // Update mini status indicators
        if (dbStatusIndicatorMini != null) {
            if (isDatabaseConnected) {
                dbStatusIndicatorMini.setFill(Color.web(COLOR_SUCCESS));
                if (dbStatusLabelMini != null) dbStatusLabelMini.setText("DB Connected");
                if (dbStatusBadgeMini != null) {
                    dbStatusBadgeMini.getStyleClass().removeAll("status-disconnected");
                    dbStatusBadgeMini.getStyleClass().add("status-connected");
                }
            } else {
                dbStatusIndicatorMini.setFill(Color.web(COLOR_DANGER));
                if (dbStatusLabelMini != null) dbStatusLabelMini.setText("DB Offline");
                if (dbStatusBadgeMini != null) {
                    dbStatusBadgeMini.getStyleClass().removeAll("status-connected");
                    dbStatusBadgeMini.getStyleClass().add("status-disconnected");
                }
            }
        }

        if (serverStatusIndicatorMini != null) {
            if (isServerRunning) {
                serverStatusIndicatorMini.setFill(Color.web(COLOR_SUCCESS));
                if (serverStatusLabelMini != null) serverStatusLabelMini.setText("Server Running");
                if (serverStatusBadgeMini != null) {
                    serverStatusBadgeMini.getStyleClass().removeAll("status-disconnected");
                    serverStatusBadgeMini.getStyleClass().add("status-connected");
                }
            } else {
                serverStatusIndicatorMini.setFill(Color.web(COLOR_DANGER));
                if (serverStatusLabelMini != null) serverStatusLabelMini.setText("Server Stopped");
                if (serverStatusBadgeMini != null) {
                    serverStatusBadgeMini.getStyleClass().removeAll("status-connected");
                    serverStatusBadgeMini.getStyleClass().add("status-disconnected");
                }
            }
        }

        // Update footer info
        if (serverInfoLabel != null) {
            serverInfoLabel.setText(isServerRunning ?
                    "Server: Running on port " + socketServerService.getPort() : "Server: Stopped");
        }
        if (dbInfoLabel != null) {
            dbInfoLabel.setText(isDatabaseConnected ?
                    "Database: Connected" : "Database: Disconnected");
        }
    }

    private void updateMessageStats() {
        if (totalMessagesLabel != null) {
            totalMessagesLabel.setText(String.valueOf(sharedDataList.size()));
        }

        if (savedCountLabel != null) {
            long savedCount = sharedDataList.stream()
                    .filter(d -> "SAVED".equals(d.getStatus()))
                    .count();
            savedCountLabel.setText(String.valueOf(savedCount));
        }

        if (errorCountLabel != null) {
            long errorCount = sharedDataList.stream()
                    .filter(d -> "ERROR".equals(d.getStatus()) || "NOT_SAVED".equals(d.getStatus()))
                    .count();
            errorCountLabel.setText(String.valueOf(errorCount));
        }

        if (tableRecordCountLabel != null) {
            tableRecordCountLabel.setText("Showing " + sharedDataList.size() + " records");
        }
    }

    private void setupTable() {
        if (dataTable == null) return;

        // Setup column value factories
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dataColumn.setCellValueFactory(new PropertyValueFactory<>("dataContent"));
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("senderIp"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom cell factory for time column
        timeColumn.setCellValueFactory(cellData -> {
            LocalDateTime time = cellData.getValue().getReceivedAt();
            String formattedTime = time != null ? time.format(formatter) : "";
            return new javafx.beans.property.SimpleStringProperty(formattedTime);
        });

        // Style status column with colored badges
        statusColumn.setCellFactory(column -> new TableCell<ReceivedData, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label statusLabel = new Label(status);
                    statusLabel.getStyleClass().add("status-cell");

                    switch (status) {
                        case "RECEIVED":
                            statusLabel.setStyle(
                                    "-fx-background-color: rgba(251, 191, 36, 0.2); " +
                                            "-fx-text-fill: #fbbf24; " +
                                            "-fx-padding: 4 12; " +
                                            "-fx-background-radius: 12; " +
                                            "-fx-font-size: 11px; " +
                                            "-fx-font-weight: 600;"
                            );
                            break;
                        case "SAVED":
                            statusLabel.setStyle(
                                    "-fx-background-color: rgba(34, 197, 94, 0.2); " +
                                            "-fx-text-fill: #22c55e; " +
                                            "-fx-padding: 4 12; " +
                                            "-fx-background-radius: 12; " +
                                            "-fx-font-size: 11px; " +
                                            "-fx-font-weight: 600;"
                            );
                            break;
                        case "ERROR":
                        case "NOT_SAVED":
                            statusLabel.setStyle(
                                    "-fx-background-color: rgba(239, 68, 68, 0.2); " +
                                            "-fx-text-fill: #ef4444; " +
                                            "-fx-padding: 4 12; " +
                                            "-fx-background-radius: 12; " +
                                            "-fx-font-size: 11px; " +
                                            "-fx-font-weight: 600;"
                            );
                            break;
                        default:
                            statusLabel.setStyle(
                                    "-fx-background-color: rgba(148, 163, 184, 0.2); " +
                                            "-fx-text-fill: #94a3b8; " +
                                            "-fx-padding: 4 12; " +
                                            "-fx-background-radius: 12; " +
                                            "-fx-font-size: 11px;"
                            );
                    }

                    setText(null);
                    setGraphic(statusLabel);
                }
            }
        });

        // Use shared data list
        filteredData = new FilteredList<>(sharedDataList, p -> true);
        dataTable.setItems(filteredData);

        // Row selection listener for delete button
        dataTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (deleteBtn != null) {
                deleteBtn.setDisable(newVal == null);
            }
        });
    }

    private void setupSearch() {
        if (searchField == null || filteredData == null) return;

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(data -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (data.getDataContent() != null &&
                        data.getDataContent().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (data.getSenderIp() != null &&
                        data.getSenderIp().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (data.getStatus() != null &&
                        data.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });

            updateTableRecordCount();
        });
    }

    @FXML
    private void handleSearch() {
        // Search is handled by the listener in setupSearch()
    }

    private void updateTableRecordCount() {
        if (tableRecordCountLabel != null && filteredData != null) {
            int showing = filteredData.size();
            int total = sharedDataList.size();
            if (showing == total) {
                tableRecordCountLabel.setText("Showing " + total + " records");
            } else {
                tableRecordCountLabel.setText("Showing " + showing + " of " + total + " records");
            }
        }
    }

    private void setupSocketCallbacks() {
        System.out.println("📌 Setting up socket callbacks...");

        // Data received callback
        socketServerService.setOnDataReceivedCallback(data -> {
            System.out.println("📥 Callback received data: " + data.getDataContent());
            handleReceivedData(data);
        });

        // Status change callback
        socketServerService.setOnStatusChangeCallback(status -> {
            Platform.runLater(() -> {
                addLogEntry("📡 " + status);
            });
        });

        // Error callback
        socketServerService.setOnErrorCallback(error -> {
            Platform.runLater(() -> {
                addLogEntry("❌ ERROR: " + error);
                AlertHelper.showError("Connection Error", error);
            });
        });

        System.out.println("✅ Socket callbacks registered");
    }

    private enum ConnectionStatus {
        DISCONNECTED, CONNECTING, CONNECTED, STOPPED, STARTING, RUNNING, ERROR
    }

    private void setDatabaseStatus(ConnectionStatus status) {
        Platform.runLater(() -> {
            // Update Connection View elements
            if (dbStatusBadge != null) {
                dbStatusBadge.getStyleClass().removeAll(
                        "status-disconnected", "status-connected", "status-connecting"
                );

                if (dbCard != null) {
                    dbCard.getStyleClass().remove("card-connected");
                }

                stopPulseAnimation(dbPulseAnimation);

                switch (status) {
                    case CONNECTED:
                        dbStatusBadge.getStyleClass().add("status-connected");
                        if (dbCard != null) dbCard.getStyleClass().add("card-connected");
                        if (connectDbBtn != null) {
                            connectDbBtn.getStyleClass().add("btn-connected");
                            connectDbBtn.setText("Disconnect");
                            connectDbBtn.setDisable(false);
                        }
                        if (dbStatusLabel != null) dbStatusLabel.setText("Connected");
                        if (dbStatusIndicator != null) {
                            dbPulseAnimation = createPulseAnimation(dbStatusIndicator);
                            dbPulseAnimation.play();
                            flashIndicator(dbStatusIndicator, Color.web(COLOR_SUCCESS));
                        }
                        break;

                    case CONNECTING:
                        dbStatusBadge.getStyleClass().add("status-connecting");
                        if (dbStatusLabel != null) dbStatusLabel.setText("Connecting...");
                        if (connectDbBtn != null) {
                            connectDbBtn.setText("Connecting...");
                            connectDbBtn.setDisable(true);
                        }
                        break;

                    default:
                        dbStatusBadge.getStyleClass().add("status-disconnected");
                        if (dbStatusLabel != null) {
                            dbStatusLabel.setText(status == ConnectionStatus.ERROR ? "Error" : "Disconnected");
                        }
                        if (connectDbBtn != null) {
                            connectDbBtn.setText("Connect");
                            connectDbBtn.setDisable(false);
                            connectDbBtn.getStyleClass().remove("btn-connected");
                        }
                        break;
                }
            }

            // Update Message View mini indicators
            updateMessageViewStatus();
        });
    }

    private void setServerStatus(ConnectionStatus status) {
        Platform.runLater(() -> {
            // Update Connection View elements
            if (serverStatusBadge != null) {
                serverStatusBadge.getStyleClass().removeAll(
                        "status-disconnected", "status-connected", "status-connecting"
                );

                if (serverCard != null) {
                    serverCard.getStyleClass().remove("card-connected");
                }

                stopPulseAnimation(serverPulseAnimation);

                switch (status) {
                    case RUNNING:
                        serverStatusBadge.getStyleClass().add("status-connected");
                        if (serverCard != null) serverCard.getStyleClass().add("card-connected");
                        if (connectServerBtn != null) {
                            connectServerBtn.getStyleClass().add("btn-running");
                            connectServerBtn.setText("Stop Server");
                            connectServerBtn.setDisable(false);
                        }
                        if (serverStatusLabel != null) serverStatusLabel.setText("Running");
                        if (serverStatusIndicator != null) {
                            serverPulseAnimation = createPulseAnimation(serverStatusIndicator);
                            serverPulseAnimation.play();
                            flashIndicator(serverStatusIndicator, Color.web(COLOR_SUCCESS));
                        }
                        startLiveIndicatorAnimation();
                        break;

                    case STARTING:
                        serverStatusBadge.getStyleClass().add("status-connecting");
                        if (serverStatusLabel != null) serverStatusLabel.setText("Starting...");
                        if (connectServerBtn != null) {
                            connectServerBtn.setText("Starting...");
                            connectServerBtn.setDisable(true);
                        }
                        break;

                    default:
                        serverStatusBadge.getStyleClass().add("status-disconnected");
                        if (serverStatusLabel != null) {
                            serverStatusLabel.setText(status == ConnectionStatus.ERROR ? "Error" : "Stopped");
                        }
                        if (connectServerBtn != null) {
                            connectServerBtn.setText("Start Server");
                            connectServerBtn.setDisable(false);
                            connectServerBtn.getStyleClass().remove("btn-running");
                        }
                        stopLiveIndicatorAnimation();
                        break;
                }
            }

            // Update Message View mini indicators
            updateMessageViewStatus();
        });
    }

    private Timeline createPulseAnimation(Circle indicator) {
        if (indicator == null) return new Timeline();

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(indicator.scaleXProperty(), 1.0),
                        new KeyValue(indicator.scaleYProperty(), 1.0)
                ),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(indicator.scaleXProperty(), 1.15),
                        new KeyValue(indicator.scaleYProperty(), 1.15)
                ),
                new KeyFrame(Duration.millis(1200),
                        new KeyValue(indicator.scaleXProperty(), 1.0),
                        new KeyValue(indicator.scaleYProperty(), 1.0)
                )
        );
        pulse.setCycleCount(Animation.INDEFINITE);
        return pulse;
    }

    private void stopPulseAnimation(Timeline animation) {
        if (animation != null) {
            animation.stop();
        }
    }

    private void flashIndicator(Circle indicator, Color color) {
        if (indicator == null) return;

        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(indicator.scaleXProperty(), 1.0),
                        new KeyValue(indicator.scaleYProperty(), 1.0)
                ),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(indicator.scaleXProperty(), 1.5),
                        new KeyValue(indicator.scaleYProperty(), 1.5)
                ),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(indicator.scaleXProperty(), 1.0),
                        new KeyValue(indicator.scaleYProperty(), 1.0)
                )
        );
        flash.play();
    }

    private void startLiveIndicatorAnimation() {
        if (liveIndicator == null) return;

        stopLiveIndicatorAnimation();

        liveIndicator.setFill(Color.web(COLOR_SUCCESS));

        liveBlinkAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(liveIndicator.opacityProperty(), 1.0)
                ),
                new KeyFrame(Duration.millis(750),
                        new KeyValue(liveIndicator.opacityProperty(), 0.4)
                ),
                new KeyFrame(Duration.millis(1500),
                        new KeyValue(liveIndicator.opacityProperty(), 1.0)
                )
        );
        liveBlinkAnimation.setCycleCount(Animation.INDEFINITE);
        liveBlinkAnimation.play();

        if (liveBadge != null) {
            liveBadge.setVisible(true);
        }
    }

    private void stopLiveIndicatorAnimation() {
        if (liveBlinkAnimation != null) {
            liveBlinkAnimation.stop();
            liveBlinkAnimation = null;
        }
        if (liveIndicator != null) {
            liveIndicator.setOpacity(1.0);
            liveIndicator.setFill(Color.web("#94a3b8"));
        }
        if (liveBadge != null) {
            liveBadge.setVisible(false);
        }
    }

    private void animateDataReceived() {
        if (liveIndicator == null) return;

        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(liveIndicator.fillProperty(), Color.web("#facc15"))
                ),
                new KeyFrame(Duration.millis(100),
                        new KeyValue(liveIndicator.fillProperty(), Color.web(COLOR_SUCCESS))
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(liveIndicator.fillProperty(), Color.web("#facc15"))
                ),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(liveIndicator.fillProperty(), Color.web(COLOR_SUCCESS))
                )
        );
        flash.play();
    }

    private void handleReceivedData(ReceivedData data) {
        Platform.runLater(() -> {
            try {
                System.out.println("🔄 Processing data in UI: " + data.getDataContent());

                animateDataReceived();

                // Save to database if connected
                if (databaseService.isConnected()) {
                    System.out.println("💾 Saving to database...");
                    boolean saved = databaseService.saveReceivedData(data);

                    if (saved) {
                        data.setStatus("SAVED");
                        addLogEntry("✅ Saved: " + truncateText(data.getDataContent(), 40) +
                                " from " + data.getSenderIp());
                    } else {
                        data.setStatus("ERROR");
                        addLogEntry("⚠️ Failed to save: " + truncateText(data.getDataContent(), 40));
                    }
                } else {
                    data.setStatus("NOT_SAVED");
                    addLogEntry("⚠️ DB not connected - " + truncateText(data.getDataContent(), 40));
                }

                // Add to shared data list (newest first)
                sharedDataList.add(0, data);

                // Update UI elements
                updateRecordCount();
                updateLastReceived(data);
                updateMessageStats();

                // Scroll to top if table is visible
                if (dataTable != null) {
                    dataTable.scrollTo(0);
                    dataTable.refresh();
                }

            } catch (Exception e) {
                System.err.println("❌ Error processing data: " + e.getMessage());
                e.printStackTrace();
                addLogEntry("❌ Error: " + e.getMessage());
            }
        });
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    @FXML
    private void handleGoToMessages(ActionEvent event) {
        navigateToView("message-view.fxml", event);
    }

    @FXML
    private void handleBackToConnection(ActionEvent event) {
        navigateToView("connection-view.fxml", event);
    }

    private void navigateToView(String fxmlFileName, ActionEvent event) {
        try {
            // Stop animations before navigation
            stopPulseAnimation(dbPulseAnimation);
            stopPulseAnimation(serverPulseAnimation);
            stopLiveIndicatorAnimation();

            // Build the correct path for your package structure
            String fxmlPath = "/com/ignite/desktop/views/" + fxmlFileName;

            System.out.println("🔍 Loading FXML from: " + fxmlPath);

            // Load FXML
            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                throw new IOException("FXML file not found at: " + fxmlPath +
                        "\nMake sure the file exists at: resources/com/ignite/desktop/views/" + fxmlFileName);
            }

            System.out.println("✅ FXML found at: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            // Load CSS with correct path
            loadStylesheet(scene);

            stage.setScene(scene);
            stage.show();

            System.out.println("✅ Successfully navigated to: " + fxmlFileName);

        } catch (IOException e) {
            System.err.println("❌ Navigation Error: " + e.getMessage());
            e.printStackTrace();

            AlertHelper.showError("Navigation Error",
                    "Could not load view: " + fxmlFileName + "\n\n" +
                            "Error: " + e.getMessage() + "\n\n" +
                            "Expected location: resources/com/ignite/desktop/views/\n" +
                            "Please check:\n" +
                            "1. File exists in correct location\n" +
                            "2. File name matches exactly\n" +
                            "3. Project has been rebuilt");
        } catch (Exception e) {
            System.err.println("❌ Unexpected navigation error: " + e.getMessage());
            e.printStackTrace();
            AlertHelper.showError("Navigation Error",
                    "Unexpected error: " + e.getMessage());
        }
    }

    private void loadStylesheet(Scene scene) {
        // Correct path for your CSS file
        String cssPath = "/com/ignite/desktop/styles/modern-style.css";

        System.out.println("🎨 Loading CSS from: " + cssPath);

        URL cssUrl = getClass().getResource(cssPath);

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("✅ CSS loaded successfully from: " + cssUrl);
        } else {
            System.err.println("⚠️ CSS file not found at: " + cssPath);
            System.err.println("⚠️ Expected location: resources/com/ignite/desktop/styles/modern-style.css");

            // Try to continue without CSS
            AlertHelper.showWarning("CSS Not Found",
                    "Stylesheet not found. Application will continue with default styling.");
        }
    }

    private void updateNavigationVisibility() {
        if (navigationSection != null) {
            boolean showNav = isDatabaseConnected && isServerRunning;
            navigationSection.setVisible(showNav);
            navigationSection.setManaged(showNav);

            if (showNav) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), navigationSection);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        }
    }

    @FXML
    private void handleConnectDatabase() {
        if (isDatabaseConnected) {
            disconnectDatabase();
        } else {
            connectDatabase();
        }
    }

    private void connectDatabase() {
        try {
            String host = dbHostField.getText().trim();
            String port = dbPortField.getText().trim();
            String database = dbNameField.getText().trim();
            String username = dbUsernameField.getText().trim();
            String password = dbPasswordField.getText();

            if (host.isEmpty() || port.isEmpty() || database.isEmpty()) {
                AlertHelper.showWarning("Validation Error",
                        "Please fill in all required database fields.");
                return;
            }

            setDatabaseStatus(ConnectionStatus.CONNECTING);

            databaseService.setConnectionParams(host, port, database, username, password);

            new Thread(() -> {
                try {
                    boolean success = databaseService.connect();

                    Platform.runLater(() -> {
                        if (success) {
                            isDatabaseConnected = true;
                            setDatabaseStatus(ConnectionStatus.CONNECTED);
                            addLogEntry("✅ Database connected to " + database + "@" + host);
                            AlertHelper.showSuccess("Database Connected",
                                    "Successfully connected to database!");
                            loadExistingData();
                            updateNavigationVisibility();
                        } else {
                            setDatabaseStatus(ConnectionStatus.ERROR);
                            addLogEntry("❌ Database connection failed");
                            AlertHelper.showError("Connection Failed",
                                    "Failed to connect to database.");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        setDatabaseStatus(ConnectionStatus.ERROR);
                        addLogEntry("❌ Database error: " + e.getMessage());
                        AlertHelper.showError("Database Error", "Connection error: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            setDatabaseStatus(ConnectionStatus.ERROR);
            addLogEntry("❌ Database error: " + e.getMessage());
            AlertHelper.showError("Database Error", "Failed to connect: " + e.getMessage());
        }
    }

    private void disconnectDatabase() {
        databaseService.disconnect();
        isDatabaseConnected = false;
        setDatabaseStatus(ConnectionStatus.DISCONNECTED);
        addLogEntry("🔌 Database disconnected");
        AlertHelper.showInfo("Database Disconnected", "Database connection closed.");
        updateNavigationVisibility();
    }

    @FXML
    private void handleConnectServer() {
        if (isServerRunning) {
            stopServer();
        } else {
            startServer();
        }
    }

    private void startServer() {
        try {
            String ip = serverIpField.getText().trim();
            String portText = serverPortField.getText().trim();

            if (ip.isEmpty() || portText.isEmpty()) {
                AlertHelper.showWarning("Validation Error",
                        "Please enter both IP Address and Port.");
                return;
            }

            int port;
            try {
                port = Integer.parseInt(portText);
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException("Port out of range");
                }
            } catch (NumberFormatException e) {
                AlertHelper.showWarning("Invalid Port",
                        "Please enter a valid port number (1-65535).");
                return;
            }

            setServerStatus(ConnectionStatus.STARTING);

            socketServerService.setConnectionParams(ip, port);

            new Thread(() -> {
                try {
                    boolean success = socketServerService.startServer();

                    Platform.runLater(() -> {
                        if (success) {
                            isServerRunning = true;
                            setServerStatus(ConnectionStatus.RUNNING);
                            addLogEntry("✅ Server started on " + ip + ":" + port);
                            AlertHelper.showSuccess("Server Started",
                                    "Socket server is now running!\n\n" +
                                            "📡 Listening on: " + ip + ":" + port);
                            updateNavigationVisibility();
                        } else {
                            setServerStatus(ConnectionStatus.ERROR);
                            addLogEntry("❌ Failed to start server");
                            AlertHelper.showError("Server Error",
                                    "Failed to start server. Port might be in use.");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        setServerStatus(ConnectionStatus.ERROR);
                        addLogEntry("❌ Server error: " + e.getMessage());
                        AlertHelper.showError("Server Error", "Error: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            setServerStatus(ConnectionStatus.ERROR);
            addLogEntry("❌ Server error: " + e.getMessage());
            AlertHelper.showError("Server Error", "Failed to start server: " + e.getMessage());
        }
    }

    private void stopServer() {
        socketServerService.stopServer();
        isServerRunning = false;
        setServerStatus(ConnectionStatus.STOPPED);
        addLogEntry("🛑 Server stopped");
        AlertHelper.showInfo("Server Stopped", "Socket server has been stopped.");
        updateNavigationVisibility();
    }

    @FXML
    private void handleRefresh() {
        loadExistingData();
        updateMessageStats();
        addLogEntry("🔄 Data refreshed");

        // Animate refresh icon
        if (refreshIcon != null) {
            RotateTransition rotate = new RotateTransition(Duration.millis(500), refreshIcon);
            rotate.setByAngle(360);
            rotate.play();
        }
    }

    @FXML
    private void handleClear() {
        if (sharedDataList.isEmpty()) {
            AlertHelper.showInfo("No Data", "There is no data to clear.");
            return;
        }

        boolean confirmed = AlertHelper.showConfirmation("Clear All Data",
                "Are you sure you want to delete all received data?\n\n" +
                        "This will remove " + sharedDataList.size() + " record(s) and cannot be undone.");

        if (confirmed) {
            int count = sharedDataList.size();

            if (databaseService.isConnected()) {
                databaseService.clearAllData();
            }

            sharedDataList.clear();
            updateRecordCount();
            updateMessageStats();

            if (lastReceivedLabel != null) {
                lastReceivedLabel.setText("--:--");
            }
            if (lastActivityLabel != null) {
                lastActivityLabel.setText("--:--");
            }

            addLogEntry("🗑️ All data cleared (" + count + " records removed)");
            AlertHelper.showSuccess("Data Cleared", "All data has been successfully cleared.");
        }
    }

    @FXML
    private void handleDelete() {
        if (dataTable == null) return;

        ReceivedData selected = dataTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a record to delete.");
            return;
        }

        boolean confirmed = AlertHelper.showConfirmation("Delete Record",
                "Are you sure you want to delete this record?\n\n" +
                        "ID: " + selected.getId() + "\n" +
                        "Data: " + truncateText(selected.getDataContent(), 50));

        if (confirmed) {
            boolean success = true;

            if (databaseService.isConnected()) {
                success = databaseService.deleteData(selected.getId());
            }

            if (success) {
                sharedDataList.remove(selected);
                updateRecordCount();
                updateMessageStats();
                addLogEntry("🗑️ Deleted record ID: " + selected.getId());
                AlertHelper.showSuccess("Record Deleted", "Record has been successfully deleted.");
            } else {
                AlertHelper.showError("Delete Failed", "Failed to delete the record from database.");
            }
        }
    }

    @FXML
    private void handleClearLog() {
        if (liveLogArea != null) {
            liveLogArea.clear();
            sharedLogBuffer.setLength(0);
            addLogEntry("📋 Log cleared");
        }
    }

    private void loadExistingData() {
        if (databaseService.isConnected()) {
            sharedDataList.clear();
            sharedDataList.addAll(databaseService.getAllReceivedData());
            updateRecordCount();
            updateMessageStats();
            addLogEntry("📂 Loaded " + sharedDataList.size() + " existing records from database");
        }
    }

    private void updateRecordCount() {
        int count = databaseService.isConnected() ?
                databaseService.getRecordCount() : sharedDataList.size();

        if (recordCountLabel != null) {
            recordCountLabel.setText(String.valueOf(count));
        }
        if (totalMessagesLabel != null) {
            totalMessagesLabel.setText(String.valueOf(count));
        }
    }

    private void updateLastReceived(ReceivedData data) {
        if (data != null && data.getReceivedAt() != null) {
            String time = data.getReceivedAt().format(timeFormatter);
            if (lastReceivedLabel != null) {
                lastReceivedLabel.setText(time);
            }
            if (lastActivityLabel != null) {
                lastActivityLabel.setText(time);
            }
        }
    }

    private void addLogEntry(String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String logEntry = "[" + timestamp + "] " + message + "\n";

        // Store in shared buffer
        sharedLogBuffer.append(logEntry);

        Platform.runLater(() -> {
            if (liveLogArea != null) {
                liveLogArea.appendText(logEntry);
                liveLogArea.setScrollTop(Double.MAX_VALUE);
            }
        });

        System.out.println(logEntry.trim());
    }

    private void restoreLogBuffer() {
        if (liveLogArea != null && sharedLogBuffer.length() > 0) {
            liveLogArea.setText(sharedLogBuffer.toString());
            liveLogArea.setScrollTop(Double.MAX_VALUE);
        }
    }

    public void cleanup() {
        System.out.println("🧹 Cleaning up ConnectionController...");

        stopPulseAnimation(dbPulseAnimation);
        stopPulseAnimation(serverPulseAnimation);
        stopLiveIndicatorAnimation();

        if (isServerRunning) {
            socketServerService.stopServer();
        }
        if (isDatabaseConnected) {
            databaseService.disconnect();
        }

        System.out.println("✅ ConnectionController cleanup complete");
    }
    
    private void debugResourceLocations() {
        System.out.println("\n========== Resource Location Debug ==========");

        String[][] resources = {
                {"Connection View", "/com/ignite/desktop/views/connection-view.fxml"},
                {"Message View", "/com/ignite/desktop/views/message-view.fxml"},
                {"Stylesheet", "/com/ignite/desktop/styles/modern-style.css"}
        };

        for (String[] resource : resources) {
            String name = resource[0];
            String path = resource[1];
            URL url = getClass().getResource(path);

            if (url != null) {
                System.out.println("✅ " + name + ": FOUND");
                System.out.println("   Path: " + path);
                System.out.println("   URL: " + url);
            } else {
                System.out.println("❌ " + name + ": NOT FOUND");
                System.out.println("   Expected: " + path);
            }
            System.out.println();
        }

        System.out.println("=============================================\n");
    }
}