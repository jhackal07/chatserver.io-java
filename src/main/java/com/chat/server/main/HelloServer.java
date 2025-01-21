package com.chat.server.main;

import java.io.IOException;

import com.chat.server.impl.ChatServer;

public class HelloServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        ChatServer server = new ChatServer();
        server.start();
    }
}