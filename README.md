#Chat Server IO

Chat server implementation using SocketServer.IO in Java code for server-side and simple index.html for client-side. Added implementation improvement such as
- Broadcast a message to connected users when someone connects or disconnects.
- Add support for nicknames.
- Don’t send the same message to the user that sent it. Instead, append the message directly as soon as they press enter.
- Add “{user} is typing” functionality.
- Show who’s online.
- Add private messaging.

## How to run

1. Run the server by running the HelloServer.java file.
2. Open the index.html file in your browser.
3. Start chatting!

## How to use

1. Enter your nickname and press enter to join the chat.
2. Start chatting with your friends!
3. Click on the user's name to send a private message to them.
4. Unclick to stop sending private message.

### Commands (Future Implementation)
3. Use the /nick command to change your nickname.
4. Use the /join command to join a room.
5. Use the /leave command to leave a room.
6. Use the /private command to send a private message to a user.
7. Use the /public command to send a public message to all users.
8. Use the /help command to get help.
9. Use the /exit command to exit the chat.

###Future Implementation
- Create room and join room.
- Nickname change.
- Help command.

