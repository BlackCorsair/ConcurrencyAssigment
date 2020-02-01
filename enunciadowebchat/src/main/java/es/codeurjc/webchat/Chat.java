package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat {

    private ExecutorService msgExecutor = Executors.newCachedThreadPool();

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
        synchronized (users)
        {
            users.put(user.getName(), user);
            for (User u : users.values()) {
                if (u != user) {
                    msgExecutor.execute(()-> u.newUserInChat(this, user));
                }
            }
        }
    }

    public void removeUser(User user) {
        users.remove(user.getName());
        for (User u : users.values()) {
            msgExecutor.execute(()-> u.userExitedFromChat(this, user));
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
            msgExecutor.execute(()-> u.newMessage(this, user, message));
        }
    }

    public void close() {
        this.chatManager.closeChat(this);
    }
}
