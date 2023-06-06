package ua.klesaak.mineperms.manager.storage.redis.messenger;

import lombok.Setter;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.UUID;

@Setter
public class MessageData {
    private final String object;
    private final String subChannel;
    private final MessageType messageType;

    ///UUID для того, чтобы дать уникальность объекту MessageData и он не принимал сам себя, входя в рекурсию.
    private UUID uuid;

    public MessageData(User user, MessageType messageType, String subChannel) {
        this(JsonData.GSON.toJson(user), messageType, subChannel);
    }

    public MessageData(Group group, MessageType messageType, String subChannel) {
        this(JsonData.GSON.toJson(group), messageType, subChannel);
    }

    public MessageData(String data, MessageType messageType, String subChannel) {
        this.object = data;
        this.messageType = messageType;
        this.subChannel = subChannel;
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

    public String getSubChannel() {
        return subChannel;
    }

    public static MessageData fromJson(String data) {
        return JsonData.GSON.fromJson(data, MessageData.class);
    }

    public static MessageData goUpdateUserPacket(User user, String subChannel) {
        return new MessageData(user, MessageType.USER_UPDATE, subChannel);
    }

    public static MessageData goDeleteUserPacket(String user, String subChannel) {
        return new MessageData(user, MessageType.USER_DELETE, subChannel);
    }

    public static MessageData goUpdateGroupPacket(Group group, String subChannel) {
        return new MessageData(group , MessageType.GROUP_UPDATE, subChannel);
    }

    public static MessageData goDeleteGroupPacket(String group, String subChannel) {
        return new MessageData(group, MessageType.GROUP_DELETE, subChannel);
    }
}
