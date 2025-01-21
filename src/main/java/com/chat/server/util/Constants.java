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
    public static final String NICKNAME_CHANGE = "Nickname change from %s to %s";

    // Error messages
    public static final String USER_NOT_FOUND = "User %s not found or offline";

    // Events
    public static final String ADD_USER = "add user";
    public static final String CHAT_MSG = "chat message";
    public static final String TYPING = "typing";
    public static final String STOP_TYPING = "stop typing";
    public static final String PRIV_MSG = "public message";
    public static final String NICK = "nick";
    public static final String USR_CH_NICK = "user changed nickname";
    public static final String LOGIN = "login";
    public static final String OL_USERS = "online users";
    public static final String USR_CONN = "user connected";
    public static final String USR_DCONN = "user disconnected";
    public static final String PRIV_MSG_ERR = "public message error";
    public static final String PRIV_MSG_SENT = "public message sent";

    // Keys
    public static final String NICKNAME = "nickname";
}