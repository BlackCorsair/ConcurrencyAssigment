package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public class Improvement1 {
    private ExecutorService executivePower = Executors.newFixedThreadPool(4);

    private void simulateUserUsage(ChatManager cm) throws InterruptedException, BrokenBarrierException, TimeoutException {
        Thread t = Thread.currentThread();

        final String[] chatName = new String[1];
        cm.newUser(new TestUser("user" + t.getName()) {
            public void newChat(Chat chat) {
                chatName[0] = chat.getName();
            }
        });

        System.out.println(t.getName() + " created user");

        for (int i = 0; i < 5; i++) {
            Chat chat = cm.newChat("Chat" + i, 5, TimeUnit.SECONDS);
            for (User user : cm.getUsers()) {
                chat.addUser(user);
            }
            for (User user : chat.getUsers()) {
                System.out.println("User: " + user.getName() + ", Chat: " + chat.getName());
            }
        }

        System.out.println(t.getName() + " done");
    }

    @Test
    public void testIfThereAreConcurrentErrorsInChatClasses() throws InterruptedException, TimeoutException {
        ChatManager chatManager = new ChatManager(50);
        for (int i = 0; i < 4; i++) {
            this.executivePower.execute(() -> {
                try {
                    simulateUserUsage(chatManager);
                } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                    synchronized (e) {
                        e.printStackTrace();
                    }

                    assertTrue(e.getMessage(), false);
                }
            });
        }
        this.executivePower.shutdown();
        this.executivePower.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Test finish.");
    }
}
