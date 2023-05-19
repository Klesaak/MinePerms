package ua.klesaak.mineperms.manager.storage.redis.messenger;

import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.utils.JsonData;

public class MessageData {
    private final String object;
    private final MessageType messageType;

    public MessageData(User user, MessageType messageType) {
        this.object = JsonData.GSON.toJson(user);
        this.messageType = messageType;
    }

    public MessageData(Group group, MessageType messageType) {
        this.object = JsonData.GSON.toJson(group);
        this.messageType = messageType;
    }

    public String getObject() {
        return this.object;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }
}
