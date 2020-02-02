package es.codeurjc.webchat;

import java.util.*;
import java.util.concurrent.*;

public class Chat {

    private ExecutorService msgExecutor = Executors.newCachedThreadPool();
    private LinkedBlockingQueue<Runnable> callQueue;

    private String name;
    private Map<String, User> users = Collections.synchronizedMap(new HashMap<>());

    private ChatManager chatManager;

    public Chat(ChatManager chatManager, String name) {
        this.chatManager = chatManager;
        this.name = name;
        this.callQueue = new LinkedBlockingQueue<>();
    }

    public String getName() {
        return name;
    }

    public void addUser(User user) {
        synchronized (users) {
            users.put(user.getName(), user);
            for (User u : users.values()) {
                if (u != user) {
                    callQueue.add(() -> u.newUserInChat(this, user));
                }
            }
        }
        runCallQueue();
    }

    public void removeUser(User user) {
        users.remove(user.getName());
        for (User u : users.values()) {
            if(u != user) {
                callQueue.add(() -> u.userExitedFromChat(this, user));
            }
        }
        runCallQueue();
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public User getUser(String name) {
        return users.get(name);
    }

    public void sendMessage(User user, String message) {
        for (User u : users.values()) {
            this.callQueue.add(()->u.newMessage(this, user, message));
        }
        runCallQueue();
    }

    private void runCallQueue() {
        this.callQueue.forEach((c) -> {
            msgExecutor.execute(() -> c.run());
            this.callQueue.remove(c);
        });
    }

    public void close() {
        this.chatManager.closeChat(this);
    }
}
