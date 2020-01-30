package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Improvement1 {
    private CyclicBarrier barrier = new CyclicBarrier(4);

    private void createUser(ChatManager cm) throws InterruptedException {
        Thread t = Thread.currentThread();
        final String[] chatName = new String[1];
        cm.newUser(new TestUser("user" + t.getName()) {
            public void newChat(Chat chat) {
                chatName[0] = chat.getName();
            }
        });
    }

    private void showUsers(Map<String, User> users) {
        System.out.println(users.toString());
    }

    private void createChat(ChatManager cm) throws InterruptedException, TimeoutException {
        Thread t = Thread.currentThread();
        for (int i = 0; i < 5; i++) {
            Chat chat = cm.newChat("Chat" + i, 5, TimeUnit.SECONDS);
            for (User user : cm.getUsers()) {
                chat.addUser(user);
            }
        }
    }

    @Test
    public void testIfThereAreConcurrentErrorsInChatClasses() throws InterruptedException, TimeoutException {
        ChatManager chatManager = new ChatManager(50);

        for (int i = 0; i < 4; i++) {
            Thread t = new Thread(() -> {
                try {
                    createUser(chatManager);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    assertTrue(e.getMessage(), false);
                }
            });
            t.start();
        }

        for (int i = 0; i < 4; i++) {
            Thread t = new Thread(() -> {
                try {
                    createChat(chatManager);
                }  catch (InterruptedException | TimeoutException e) {
                    e.printStackTrace();
                    assertTrue(e.getMessage(), false);
                }
            });
            t.start();
        }
    }
}
