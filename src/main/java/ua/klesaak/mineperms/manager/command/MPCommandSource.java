package ua.klesaak.mineperms.manager.command;

public interface MPCommandSource {
    boolean hasPermission(String permission);
    void sendMessage(String message);
}
