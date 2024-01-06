package ua.klesaak.mineperms.manager.storage.redismessenger;

import lombok.Getter;
import lombok.Setter;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.UUID;

@Setter @Getter
public class MessageData {
    private final String entityId; //id группы или ник игрока
    private final String object;
    private final String subChannel;
    private final MessageType messageType;

    ///UUID для того, чтобы дать уникальность объекту MessageData и он не принимал сам себя, входя в рекурсию.
    private UUID uuid;

    public MessageData(String entityId, String data, MessageType messageType, String subChannel) {
        this.entityId = entityId;
        this.object = data;
        this.messageType = messageType;
        this.subChannel = subChannel;
    }

    public String toJson() {
        return JsonData.GSON.toJson(this);
    }


    public static MessageData fromJson(String data) {
        return JsonData.GSON.fromJson(data, MessageData.class);
    }

    public static MessageData goUpdateUserPrefixPacket(String playerName, String prefix) {
        return new MessageData(playerName, prefix, MessageType.USER_PREFIX_UPDATE, null);
    }

    public static MessageData goUpdateUserSuffixPacket(String playerName, String suffix) {
        return new MessageData(playerName, suffix, MessageType.USER_SUFFIX_UPDATE, null);
    }

    public static MessageData goUpdateUserGroupPacket(String playerName, String groupId) {
        return new MessageData(playerName, groupId, MessageType.USER_GROUP_UPDATE, null);
    }

    public static MessageData goUpdateUserPermAddPacket(String playerName, String permission) {
        return new MessageData(playerName, permission, MessageType.USER_PERMISSION_ADD, null);
    }

    public static MessageData goUpdateUserPermRemovePacket(String playerName, String permission) {
        return new MessageData(playerName, permission, MessageType.USER_PERMISSION_REMOVE, null);
    }

    public static MessageData goDeleteUserPacket(String playerName) {
        return new MessageData(playerName, null, MessageType.USER_DELETE, null);
    }

    public static MessageData goUpdateGroupPrefixPacket(String groupId, String prefix) {
        return new MessageData(groupId, prefix, MessageType.GROUP_PREFIX_UPDATE, null);
    }

    public static MessageData goUpdateGroupSuffixPacket(String groupId, String suffix) {
        return new MessageData(groupId, suffix, MessageType.GROUP_SUFFIX_UPDATE, null);
    }

    public static MessageData goUpdateGroupParentAddPacket(String groupId, String parentId) {
        return new MessageData(groupId, parentId, MessageType.GROUP_PARENT_ADD, null);
    }

    public static MessageData goUpdateGroupParentRemovePacket(String groupId, String parentId) {
        return new MessageData(groupId, parentId, MessageType.GROUP_PARENT_REMOVE, null);
    }

    public static MessageData goUpdateGroupPermAddPacket(String playerName, String permission, String subChannel) {
        return new MessageData(playerName, permission, MessageType.GROUP_PERMISSION_ADD, subChannel);
    }

    public static MessageData goUpdateGroupPermRemovePacket(String playerName, String permission, String subChannel) {
        return new MessageData(playerName, permission, MessageType.GROUP_PERMISSION_REMOVE, subChannel);
    }

    public static MessageData goCreteGroupPacket(String groupId) {
        return new MessageData(groupId, null, MessageType.GROUP_CREATE, null);
    }

    public static MessageData goDeleteGroupPacket(String groupId) {
        return new MessageData(groupId, null, MessageType.GROUP_DELETE, null);
    }
}
