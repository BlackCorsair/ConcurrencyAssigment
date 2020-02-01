package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat {

    private class MessageTuple {
        public User origin_user;
        public User end_user;
        public String message;

        MessageTuple(User origin, User end, String message) {
            this.origin_user = origin;
            this.end_user = end;
            this.message = message;
        }
    }

    private ExecutorService msgExecutor = Executors.newCachedThreadPool();
    private ConcurrentLinkedQueue<MessageTuple> msgQueue = new ConcurrentLinkedQueue();

    private String name;
    private Map<String, User> users = Collections.synchronizedMap(new HashMap<>());

    private ChatManager chatManager;

    public Chat(ChatManager chatManager, String name) {
        this.chatManager = chatManager;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addUser(User user) {
        synchronized (users) {
            users.put(user.getName(), user);
            for (User u : users.values()) {
                if (u != user) {
                    msgExecutor.execute(() -> u.newUserInChat(this, user));
                }
            }
        }
    }

    public void removeUser(User user) {
        users.remove(user.getName());
        for (User u : users.values()) {
            msgExecutor.execute(() -> u.userExitedFromChat(this, user));
        }
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public User getUser(String name) {
        return users.get(name);
    }

    public void sendMessage(User user, String message) {
        for (User u : users.values()) {
            if (u != user) {
                this.msgQueue.add(new MessageTuple(u, user, message));
            }
        }
        sendMessagesInQueue();
    }

    private void sendMessagesInQueue() {
        while(!this.msgQueue.isEmpty()) {
            MessageTuple tuple = this.msgQueue.remove();
            msgExecutor.execute(() -> tuple.origin_user.newMessage(this, tuple.end_user, tuple.message));
        }
    }

    public void close() {
        this.chatManager.closeChat(this);
    }
}
