package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class Improvement4 {
    public static CountDownLatch countdown;
    public static ConcurrentLinkedQueue messages;
    private ChatManager chatManager;
    private Chat chat;

    @Before
    public void setUp() {
        this.chatManager = new ChatManager(50);
        try {
            chat = populateChatManager(chatManager);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        countdown = new CountDownLatch(4);
        messages = new ConcurrentLinkedQueue<String>();
    }

    @Test
    public void testConcurrencyInMessages() throws InterruptedException, TimeoutException {
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

    @Test
    public void testConcurrencyOrderInMessages() throws InterruptedException, TimeoutException {
        // setup
        countdown = new CountDownLatch(3);
        chat.removeUser(chat.getUser("user2"));
        chat.removeUser(chat.getUser("user3"));

        // Execute
        long tStart = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            chat.sendMessage(chat.getUser("user0"), Integer.toString(i));
        }

        countdown.await();

        long tEnd = System.currentTimeMillis();
        double delta = (tEnd - tStart) / 1000.0;

        // assert
        String sorted = messages.stream().sorted().collect(Collectors.toList()).toString();
        assertTrue("Time '" + delta + "' is greater than 1.5s",
                delta < 1.1);
        assertEquals("Messages are not in order" + messages.toString(), messages.toString(), sorted);
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
                    messages.add(message);
                    System.out.println(message + " from user " + user.getName() + " to user " + this.name);

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
