package ua.klesaak.mineperms.manager.command;

public interface IMPCommandSource {
    boolean hasPermission(String permission);
    void sendMessage(String message);
}
