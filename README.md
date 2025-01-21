# Chat Server IO

Chat server implementation using [SocketServer.IO](https://socket.io/) in Java code for server-side and simple index.html for client-side. Added implementation improvement such as:
- Broadcast a message to connected users when someone connects or disconnects.
- Add support for nicknames.
- Don’t send the same message to the user that sent it. Instead, append the message directly as soon as they press enter.
- Add “{user} is typing” functionality.
- Show who’s online.
- Add private messaging.

## How to run

1. Run the server by running the HelloServer.java file.
2. Open the index.html file in your browser by accessing: http://localhost:3000/
4. Start chatting!

## How to use

1. Enter your nickname and press enter to join the chat.
2. Click on the user's name to send a private message to them.
3. Unclick to stop sending private message and continue to public message.
4. Start chatting with your friends!

### Commands (Existing)
1. Use the `/nick` command to change your nickname.
  ```powershell
  /nick Joeker
  ```

### Commands (Future implementation)
1. Use the /create command to create a room.
2. Use the /join command to join a room.
3. Use the /leave command to leave a room.
4. Use the /private command to send a private message to a user.
5. Use the /public command to send a public message to all users.
6. Use the /help command to get help.
7. Use the /exit command to exit the chat.

### Future Implementation
- Create room and join room.
- Help command.
