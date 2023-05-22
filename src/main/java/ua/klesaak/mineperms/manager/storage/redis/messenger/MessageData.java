package ua.klesaak.mineperms.manager.storage.redis.messenger;

import lombok.Setter;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.UUID;

@Setter
public class MessageData {
    private final String object;
    private final MessageType messageType;

    ///UUID для того, чтобы дать уникальность обьекту MessageData и он не принимал сам себя, входя в рекурсию.
    private UUID uuid;

    public MessageData(User user, MessageType messageType) {
        this.object = JsonData.GSON.toJson(user);
        this.messageType = messageType;
    }

    public MessageData(Group group, MessageType messageType) {
        this.object = JsonData.GSON.toJson(group);
        this.messageType = messageType;
    }

    public MessageData(String data, MessageType messageType) {
        this.object = data;
        this.messageType = messageType;
    }

    public User getUserObject() {
        return JsonData.GSON.fromJson(this.object, User.class);
    }

    public Group getGroupObject() {
        return JsonData.GSON.fromJson(this.object, Group.class);
    }

    public String getStringObject() {
        return this.object;
    }

    public UUID getUuid() {
        return uuid;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public String toJson() {
        return JsonData.GSON.toJson(this);
    }

    public static MessageData fromJson(String data) {
        return JsonData.GSON.fromJson(data, MessageData.class);
    }
}
