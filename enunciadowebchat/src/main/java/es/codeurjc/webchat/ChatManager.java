package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatManager {

    private Map<String, Chat> chats = Collections.synchronizedMap(new HashMap<>());
    private Map<String, User> users = Collections.synchronizedMap(new HashMap<>());
    private int maxChats;

    public ChatManager(int maxChats) {
        this.maxChats = maxChats;
    }

    public void newUser(User user) {

        if (checkUserNames(user.getName())) {
            throw new IllegalArgumentException("There is already a user with name \'"
                    + user.getName() + "\'");
        } else {
            synchronized (users)
            {
                users.put(user.getName(), user);
            }
        }
    }

    private boolean checkUserNames(String username){
        synchronized (users) {
            return users.containsKey(username);
        }
    }

    public Chat newChat(String name, long timeout, TimeUnit unit) throws InterruptedException,
            TimeoutException {

        if (chats.size() == maxChats) {
            throw new TimeoutException("There is no enought capacity to create a new chat");
        }

        if (chats.containsKey(name)) {
            return chats.get(name);
        } else {
            Chat newChat = new Chat(this, name);
            chats.put(name, newChat);

            for (User user : users.values()) {
                user.newChat(newChat);
            }

            return newChat;
        }
    }

    public void closeChat(Chat chat) {
        Chat removedChat = chats.remove(chat.getName());
        if (removedChat == null) {
            throw new IllegalArgumentException("Trying to remove an unknown chat with name \'"
                    + chat.getName() + "\'");
        }

        for (User user : users.values()) {
            user.chatClosed(removedChat);
        }
    }

    public Collection<Chat> getChats() {
        return Collections.unmodifiableCollection(chats.values());
    }

    public Chat getChat(String chatName) {
        return chats.get(chatName);
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public User getUser(String userName) {
        return users.get(userName);
    }

    public void close() {
    }
}
