package com.chat.server.util;

public class Constants {
    // Server status messages
    public static final String SERVER_HTTP_STARTED = "HTTP Server started on port 3000";
    public static final String SERVER_SOCKET_STARTED = "Socket.IO Server started on port 3001";
    
    // Connection messages
    public static final String CLIENT_CONNECTED = "Client connected: %s";
    public static final String CLIENT_DISCONNECTED = "Client disconnected: %s";
    
    // Chat messages
    public static final String USER_MESSAGE = "%s: %s";
    public static final String USER_JOINED = "Welcome %s!";
    public static final String USER_LEFT = "%s left";
    public static final String USER_JOINED_BROADCAST = "%s joined";
    
    // Error messages
    public static final String USER_NOT_FOUND = "User %s not found or offline";
} 