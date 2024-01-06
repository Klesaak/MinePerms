package ua.klesaak.mineperms.manager.storage.redismessenger;

public enum MessageType {
    USER_PREFIX_UPDATE,
    USER_SUFFIX_UPDATE,
    USER_GROUP_UPDATE,
    USER_PERMISSION_ADD,
    USER_PERMISSION_REMOVE,
    USER_DELETE,
    GROUP_PREFIX_UPDATE,
    GROUP_SUFFIX_UPDATE,
    GROUP_PARENT_ADD,
    GROUP_PARENT_REMOVE,
    GROUP_PERMISSION_ADD,
    GROUP_PERMISSION_REMOVE,
    GROUP_CREATE,
    GROUP_DELETE;

    public boolean isUserUpdate() {
        return this == USER_PREFIX_UPDATE || this == USER_SUFFIX_UPDATE || this == USER_GROUP_UPDATE || this == USER_PERMISSION_ADD || this == USER_PERMISSION_REMOVE || this == USER_DELETE;
    }
}
