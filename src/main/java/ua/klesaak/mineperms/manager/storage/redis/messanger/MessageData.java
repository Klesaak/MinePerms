package ua.klesaak.mineperms.manager.storage.redis.messanger;

public class MessageData<T> {
    private final T object;
    private final MessageType messageType;

    public MessageData(T object, MessageType messageType) {
        this.object = object;
        this.messageType = messageType;
    }

    public T getObject() {
        return this.object;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }
}
