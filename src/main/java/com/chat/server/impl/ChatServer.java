package com.chat.server.impl;

import com.chat.server.impl.vo.ChatMessage;
import com.chat.server.impl.vo.Nickname;
import com.chat.server.impl.vo.PrivateMessage;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static com.chat.server.util.Constants.*;

public class ChatServer {
    private final ConcurrentMap<String, String> onlineUsers = new ConcurrentHashMap<>(); // sessionId -> nickname
    private final ConcurrentMap<String, String> nicknameToSessionId = new ConcurrentHashMap<>(); // nickname ->
                                                                                                 // sessionId
    private final SocketIOServer socketServer;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public ChatServer() {
        // Configure Socket.IO server
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(3001);
        config.setOrigin("*");

        this.socketServer = new SocketIOServer(config);
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();

        configureSocketEvents();
    }

    private void configureSocketEvents() {
        // Handle socket connection
        socketServer.addConnectListener(client -> {
            System.out.println(String.format(CLIENT_CONNECTED, client.getSessionId()));
        });

        // Handle socket disconnection
        socketServer.addDisconnectListener(client -> {
            String nickname = client.get("nickname");
            if (nickname != null) {
                handleDisconnect(client, nickname);
            }
        });

        // Handle nickname setting
        socketServer.addEventListener("add user", Nickname.class, (client, nickname, ackRequest) -> {
            handleNewUser(client, nickname);
        });

        // Handle chat messages
        socketServer.addEventListener("chat message", String.class, (client, message, ackRequest) -> {
            handleChatMessage(client, message);
        });

        // Handle typing events
        socketServer.addEventListener("typing", Object.class, (client, data, ackRequest) -> {
            handleTyping(client);
        });

        socketServer.addEventListener("stop typing", Object.class, (client, data, ackRequest) -> {
            handleStopTyping(client);
        });

        // Handle private messages
        socketServer.addEventListener("private message", PrivateMessage.class, (client, privateMessage, ackRequest) -> {
            handlePrivateMessage(client, privateMessage);
        });

        socketServer.addEventListener("nick", Nickname.class, (client, nickname, ackRequest) -> {
            handleChangeNickname(client, nickname);
        });
    }

    private void handleChangeNickname(SocketIOClient client, Nickname nickname) {
        client.set("nickname", nickname.getNickname());
        System.out.println(
                String.format("Nickname change from %s to %s", nickname.getOldNickname(), nickname.getNickname()));
        onlineUsers.put(client.getSessionId().toString(), nickname.getNickname());
        nicknameToSessionId.remove(nickname.getOldNickname());
        nicknameToSessionId.put(nickname.getNickname(), client.getSessionId().toString());
        nickname.setSessionId(client.getSessionId().toString());
        broadcastToOthers(client, "user changed nickname", nickname);
        broadcastOnlineUsers(client);
    }

    private void handleDisconnect(com.corundumstudio.socketio.SocketIOClient client, String nickname) {
        System.out.println(String.format(CLIENT_DISCONNECTED, nickname));
        onlineUsers.remove(client.getSessionId().toString());
        nicknameToSessionId.remove(nickname);

        broadcastToOthers(client, "user disconnected", String.format(USER_LEFT, nickname));
        broadcastOnlineUsers(client);
    }

    private void handleNewUser(SocketIOClient client, Nickname nickname) {
        client.set("nickname", nickname.getNickname());
        onlineUsers.put(client.getSessionId().toString(), nickname.getNickname());
        nicknameToSessionId.put(nickname.getNickname(), client.getSessionId().toString());

        client.sendEvent("login", String.format(USER_JOINED, nickname.getNickname()));
        client.sendEvent("online users", new HashSet<>(onlineUsers.values()));

        broadcastToOthers(client, "user connected", String.format(USER_JOINED_BROADCAST, nickname.getNickname()));
        broadcastOnlineUsers(client);
    }

    private void handleChatMessage(SocketIOClient client, String message) {
        String nickname = client.get("nickname");
        if (nickname != null) {
            System.out.println(String.format(USER_MESSAGE, nickname, message));
            broadcastToOthers(client, "chat message", new ChatMessage(nickname, message));
        }
    }

    private void handleTyping(SocketIOClient client) {
        String nickname = client.get("nickname");
        if (nickname != null) {
            // Broadcast to everyone EXCEPT the sender
            broadcastToOthers(client, "typing", nickname);
        }
    }

    private void handleStopTyping(SocketIOClient client) {
        String nickname = client.get("nickname");
        if (nickname != null) {
            // Broadcast to everyone EXCEPT the sender
            broadcastToOthers(client, "stop typing", nickname);
        }
    }

    private void handlePrivateMessage(SocketIOClient client, PrivateMessage privateMessage) {
        String senderNickname = client.get("nickname");
        if (senderNickname != null) {
            String targetSessionId = nicknameToSessionId.get(privateMessage.getTo());
            if (targetSessionId != null) {
                sendPrivateMessage(client, senderNickname, privateMessage, targetSessionId);
            } else {
                client.sendEvent("private message error", String.format(USER_NOT_FOUND, privateMessage.getTo()));
            }
        }
    }

    private void sendPrivateMessage(SocketIOClient sender, String senderNickname,
            PrivateMessage privateMessage, String targetSessionId) {
        for (var targetClient : sender.getNamespace().getAllClients()) {
            if (targetClient.getSessionId().toString().equals(targetSessionId)) {
                targetClient.sendEvent("private message",
                        new PrivateMessage(senderNickname, privateMessage.getTo(), privateMessage.getMessage()));
                sender.sendEvent("private message sent",
                        new PrivateMessage(senderNickname, privateMessage.getTo(), privateMessage.getMessage()));
                return;
            }
        }
    }

    private void broadcastToOthers(com.corundumstudio.socketio.SocketIOClient client, String event, Object data) {
        for (var otherClient : client.getNamespace().getAllClients()) {
            if (!otherClient.getSessionId().equals(client.getSessionId())) {
                otherClient.sendEvent(event, data);
            }
        }
    }

    private void broadcastOnlineUsers(com.corundumstudio.socketio.SocketIOClient client) {
        for (var otherClient : client.getNamespace().getAllClients()) {
            if (!otherClient.getSessionId().equals(client.getSessionId())) {
                otherClient.sendEvent("online users", new HashSet<>(onlineUsers.values()));
            }
        }
    }

    public void start() throws InterruptedException {
        socketServer.start();

        // Configure and start HTTP server
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            configureHttpPipeline(ch.pipeline());
                        }
                    });

            Channel ch = b.bind(3000).sync().channel();
            System.out.println(SERVER_HTTP_STARTED);
            System.out.println(SERVER_SOCKET_STARTED);

            ch.closeFuture().sync();
        } finally {
            shutdown();
        }
    }

    private void configureHttpPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
                handleHttpRequest(ctx, request);
            }
        });
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws IOException {
        if (request.uri().equals("/")) {
            serveIndexHtml(ctx);
        } else {
            sendNotFound(ctx);
        }
    }

    private void serveIndexHtml(ChannelHandlerContext ctx) throws IOException {
        File file = new File("index.html");
        byte[] content = Files.readAllBytes(file.toPath());

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        response.content().writeBytes(content);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendNotFound(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        socketServer.stop();
    }
}