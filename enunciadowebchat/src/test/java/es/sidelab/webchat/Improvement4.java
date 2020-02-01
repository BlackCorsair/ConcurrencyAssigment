package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.junit.Test;
import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public class Improvement4 {
    public static CountDownLatch countdown;

    @Test
    public void testConcurrencyInMessages() throws InterruptedException, TimeoutException {
        // Setup
        ChatManager chatManager = new ChatManager(50);
        Chat chat = populateChatManager(chatManager);
        countdown = new CountDownLatch(4);

        // Execute
        long tStart = System.currentTimeMillis();

        chat.sendMessage(chat.getUser("user0"), "hello there");
        countdown.await();

        long tEnd = System.currentTimeMillis();
        double delta = (tEnd - tStart) / 1000.0;


        // assert
        assertTrue("Time '" + delta + "' is greater than 1.5s",
                delta < 1.1);
    }

    private Chat populateChatManager(ChatManager chatManager) throws InterruptedException, TimeoutException {
        chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
        final String[] chatName = new String[1];

        for (int i = 0; i < 4; i++) {
            chatManager.newUser(new TestUser("user" + i) {
                public void newChat(Chat chat) {
                    chatName[0] = chat.getName();
                }

                @Override
                public void newMessage(Chat chat, User user, String message) {
                    Thread t = Thread.currentThread();
                    System.out.println("New message '" + message + "' from user " + user.getName()
                            + " in chat " + chat.getName() + " from thread " + t.getName());

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    countdown.countDown();
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
