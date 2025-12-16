package com.example.mobilesender;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // ==================== UI COMPONENTS ====================
    private EditText etIpAddress;
    private EditText etPort;
    private EditText etCustomMessage;
    private Button btnSend;
    private Button btnSendDefault;
    private TextView tvStatus;
    private TextView tvLastSent;
    private TextView tvDeviceIp;
    private ProgressBar progressBar;
    private CardView statusCard;

    // ==================== THREADING ====================
    private ExecutorService executorService;
    private Handler mainHandler;

    // ==================== CONNECTION STATE ====================
    private boolean isSending = false;
    private int messagesSentCount = 0;
    private long lastSentTime = 0;

    // ==================== DEFAULT CONFIGURATION ====================
    private static final String DEFAULT_IP = "192.168.0.102";
    private static final int DEFAULT_PORT = 3005;
    private static final String DEFAULT_MESSAGE = "Data Send";
    private static final int CONNECTION_TIMEOUT = 5000;

    // ==================== IGNITE COLOR CONSTANTS ====================
    private static final String COLOR_PRIMARY = "#2563eb";
    private static final String COLOR_ACCENT = "#0891b2";
    private static final String COLOR_SUCCESS = "#22c55e";
    private static final String COLOR_SUCCESS_LIGHT = "#f0fdf4";
    private static final String COLOR_WARNING = "#f59e0b";
    private static final String COLOR_WARNING_LIGHT = "#fffbeb";
    private static final String COLOR_DANGER = "#ef4444";
    private static final String COLOR_DANGER_LIGHT = "#fef2f2";
    private static final String COLOR_INFO_LIGHT = "#eff6ff";
    private static final String COLOR_TEXT_MUTED = "#94a3b8";

    // ==================== LIFECYCLE ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup window - Keep status bar, remove toolbar only
        setupWindow();

        setContentView(R.layout.activity_main);

        initializeViews();
        initializeExecutor();
        setupDefaultValues();
        displayDeviceIp();
        setupClickListeners();
        showWelcomeAnimation();
        updateStatusWithColor("⚡ IGNITE Ready", COLOR_INFO_LIGHT, COLOR_PRIMARY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayDeviceIp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // ==================== WINDOW SETUP ====================

    /**
     * Setup window - Keep status bar with IGNITE color, remove toolbar only
     */
    private void setupWindow() {
        // Hide ActionBar/Toolbar only
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Style the status bar with IGNITE color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#1d4ed8")); // primary_dark

        // Light status bar icons (dark icons on light background) - optional
        // Uncomment below if you want light status bar with dark icons
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //     window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        // }
    }

    // ==================== INITIALIZATION ====================

    private void initializeViews() {
        etIpAddress = findViewById(R.id.et_ip_address);
        etPort = findViewById(R.id.et_port);
        etCustomMessage = findViewById(R.id.et_custom_message);
        btnSend = findViewById(R.id.btn_send);
        btnSendDefault = findViewById(R.id.btn_send_default);
        tvStatus = findViewById(R.id.tv_status);
        tvLastSent = findViewById(R.id.tv_last_sent);
        tvDeviceIp = findViewById(R.id.tv_device_ip);
        progressBar = findViewById(R.id.progress_bar);
        statusCard = findViewById(R.id.status_card);
    }

    private void initializeExecutor() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void setupDefaultValues() {
        String localIp = getDeviceIpAddress();
        if (localIp != null && !localIp.isEmpty()) {
            String[] parts = localIp.split("\\.");
            if (parts.length == 4) {
                String suggestedIp = parts[0] + "." + parts[1] + "." + parts[2] + ".102";
                etIpAddress.setText(suggestedIp);
            }
        } else {
            etIpAddress.setText(DEFAULT_IP);
        }

        etPort.setText(String.valueOf(DEFAULT_PORT));
        etCustomMessage.setText("");
    }

    private void displayDeviceIp() {
        if (tvDeviceIp == null) return;

        try {
            String deviceIp = getDeviceIpAddress();

            if (deviceIp != null && !deviceIp.isEmpty() && !deviceIp.equals("0.0.0.0")) {
                tvDeviceIp.setText("📱 " + deviceIp);
                tvDeviceIp.setTextColor(Color.parseColor(COLOR_ACCENT));
                tvDeviceIp.setBackgroundColor(Color.parseColor("#ecfeff"));
                tvDeviceIp.setPadding(32, 12, 32, 12);
            } else {
                tvDeviceIp.setText("📱 WiFi Disconnected");
                tvDeviceIp.setTextColor(Color.parseColor(COLOR_WARNING));
                tvDeviceIp.setBackgroundColor(Color.parseColor(COLOR_WARNING_LIGHT));
                tvDeviceIp.setPadding(32, 12, 32, 12);
            }
            tvDeviceIp.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            tvDeviceIp.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private String getDeviceIpAddress() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);

            if (wifiManager != null) {
                int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

                if (ipAddress != 0) {
                    return String.format(Locale.US, "%d.%d.%d.%d",
                            (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff),
                            (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== CLICK LISTENERS ====================

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> {
            String message = etCustomMessage.getText().toString().trim();
            if (message.isEmpty()) {
                showIgniteToast("⚠️ Please enter a message");
                shakeView(etCustomMessage);
                return;
            }
            sendData(message);
        });

        btnSendDefault.setOnClickListener(v -> {
            sendData(DEFAULT_MESSAGE);
            pulseView(btnSendDefault);
        });
    }

    // ==================== DATA TRANSMISSION ====================

    private void sendData(String message) {
        if (isSending) {
            showIgniteToast("⏳ Please wait...");
            return;
        }

        String ipAddress = etIpAddress.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();

        if (ipAddress.isEmpty()) {
            showIgniteError("Please enter IP Address");
            shakeView(etIpAddress);
            return;
        }

        if (portStr.isEmpty()) {
            showIgniteError("Please enter Port number");
            shakeView(etPort);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                showIgniteError("Port must be between 1 and 65535");
                shakeView(etPort);
                return;
            }
        } catch (NumberFormatException e) {
            showIgniteError("Invalid port number");
            shakeView(etPort);
            return;
        }

        isSending = true;
        setLoadingState(true);
        updateStatusWithColor("📡 Connecting to " + ipAddress + ":" + port + "...",
                COLOR_INFO_LIGHT, COLOR_PRIMARY);

        final String finalIpAddress = ipAddress;
        final int finalPort = port;
        final String finalMessage = message;
        final long startTime = System.currentTimeMillis();

        executorService.execute(() -> {
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(finalIpAddress, finalPort), CONNECTION_TIMEOUT);
                socket.setSoTimeout(CONNECTION_TIMEOUT);

                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                writer.println(finalMessage);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String response = null;
                try {
                    response = reader.readLine();
                } catch (Exception e) {
                    // No response is acceptable
                }

                writer.close();
                reader.close();
                socket.close();

                long duration = System.currentTimeMillis() - startTime;

                final String finalResponse = response;
                mainHandler.post(() -> {
                    isSending = false;
                    setLoadingState(false);
                    messagesSentCount++;
                    lastSentTime = System.currentTimeMillis();
                    onSendSuccess(finalMessage, finalResponse, duration);
                });

            } catch (java.net.ConnectException e) {
                handleIgniteError("❌ Connection refused",
                        "Desktop server not running.\nPlease start the IGNITE Desktop app first.");
            } catch (java.net.SocketTimeoutException e) {
                handleIgniteError("⏱️ Connection timeout",
                        "Check IP address and ensure both devices are on same network.");
            } catch (java.net.UnknownHostException e) {
                handleIgniteError("❓ Unknown host",
                        "Invalid IP address. Check desktop IP.");
            } catch (java.io.IOException e) {
                handleIgniteError("📡 Network error", e.getMessage());
            } catch (Exception e) {
                handleIgniteError("⚠️ Error", e.getMessage());
            } finally {
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    private void onSendSuccess(String message, String response, long duration) {
        String successMsg = "✅ Message sent successfully!";
        if (response != null && !response.isEmpty()) {
            successMsg += "\n📥 Server: " + response;
        }
        updateStatusWithColor(successMsg, COLOR_SUCCESS_LIGHT, COLOR_SUCCESS);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String lastSentMsg = String.format(Locale.US,
                "📤 Sent at %s • %dms • Total: %d",
                timestamp, duration, messagesSentCount);
        tvLastSent.setText(lastSentMsg);
        tvLastSent.setTextColor(Color.parseColor("#64748b"));
        tvLastSent.setVisibility(View.VISIBLE);

        showIgniteToast("✅ Sent: " + truncateText(message, 30));
        pulseView(statusCard);

        if (etCustomMessage.getText().toString().trim().equals(message) &&
                !message.equals(DEFAULT_MESSAGE)) {
            etCustomMessage.setText("");
        }

        mainHandler.postDelayed(() -> {
            updateStatusWithColor("⚡ IGNITE Ready", COLOR_INFO_LIGHT, COLOR_PRIMARY);
        }, 3000);
    }

    private void handleIgniteError(String title, String errorMessage) {
        mainHandler.post(() -> {
            isSending = false;
            setLoadingState(false);

            updateStatusWithColor(title + "\n" + errorMessage,
                    COLOR_DANGER_LIGHT, COLOR_DANGER);

            showIgniteToast(title);
            shakeView(statusCard);

            if (!tvLastSent.isShown() || messagesSentCount == 0) {
                tvLastSent.setText("💡 Tip: Ensure desktop server is running on port " +
                        etPort.getText().toString());
                tvLastSent.setTextColor(Color.parseColor(COLOR_WARNING));
                tvLastSent.setVisibility(View.VISIBLE);
            }
        });
    }

    // ==================== UI HELPERS ====================

    private void setLoadingState(boolean isLoading) {
        btnSend.setEnabled(!isLoading);
        btnSendDefault.setEnabled(!isLoading);
        btnSend.setAlpha(isLoading ? 0.6f : 1.0f);
        btnSendDefault.setAlpha(isLoading ? 0.6f : 1.0f);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading && progressBar != null) {
            progressBar.getIndeterminateDrawable()
                    .setColorFilter(Color.parseColor(COLOR_PRIMARY),
                            android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void updateStatusWithColor(String message, String bgColor, String textColor) {
        tvStatus.setText(message);
        tvStatus.setTextColor(Color.parseColor(textColor));
        statusCard.setCardBackgroundColor(Color.parseColor(bgColor));
    }

    private void showIgniteToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showIgniteError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    // ==================== ANIMATIONS ====================

    private void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }

    private void pulseView(View view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.05f,
                1.0f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(200);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(1);
        view.startAnimation(scaleAnimation);
    }

    private void showWelcomeAnimation() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setAlpha(0f);
            rootView.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .start();
        }

        if (tvDeviceIp != null) {
            tvDeviceIp.setScaleX(0.8f);
            tvDeviceIp.setScaleY(0.8f);
            tvDeviceIp.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setStartDelay(300)
                    .start();
        }
    }
}