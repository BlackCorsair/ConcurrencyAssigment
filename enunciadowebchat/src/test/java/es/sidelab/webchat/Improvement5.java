package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Improvement5 {
    public static ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();

    @Test
    public void testAllWorksFine() throws InterruptedException, TimeoutException {
        // Setup
        ChatManager chatManager = new ChatManager(50);
        Chat chat = null;
        Chat toDeleteChat = chatManager.newChat("Chat2", 5, TimeUnit.SECONDS);
        try {
            chat = populateChat(chatManager);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        // Execute
        for (User user : chat.getUsers()) {
            toDeleteChat.addUser(user);
        }

        chatManager.closeChat(toDeleteChat);

        chat.removeUser(chat.getUser("user0"));

        chat.sendMessage(chat.getUser("user1"), "hello there!");

        // assert
        assertEquals("There must be exactly 5 messages: \n\t"
                        + messages.stream().map(String::toString)
                        .collect(Collectors.joining("\n")),
                        messages.size(), 5);
    }

    private Chat populateChat(ChatManager chatManager) throws InterruptedException, TimeoutException {
        final String[] chatName = new String[1];

        for (int i = 0; i < 2; i++) {
            chatManager.newUser(new TestUser("user" + i) {
                private void logMessage(String message) {
                    if (this.name.equals("user1"))
                        messages.add(message);

                }

                public void newChat(Chat chat) {
                    chatName[0] = chat.getName();
                }

                @Override
                public void chatClosed(Chat chat) {
                    this.logMessage("Chat " + chat.getName() + " closed ");
                }

                @Override
                public void newUserInChat(Chat chat, User user) {
                    this.logMessage("New user " + user.getName() + " in chat " + chat.getName());
                }

                @Override
                public void userExitedFromChat(Chat chat, User user) {
                    this.logMessage("User " + user.getName() + " exited from chat " + chat.getName());
                }

                @Override
                public void newMessage(Chat chat, User user, String message) {
                    this.logMessage(message);
                }
            });
        }

        Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
        for (User user : chatManager.getUsers()) {
            chat.addUser(user);
        }
        return chat;
    }
}
