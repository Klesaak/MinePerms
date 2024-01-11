package ua.klesaak.mineperms.manager.log;

@FunctionalInterface
public interface MinePermsLogger {
    void log(String message, Throwable throwable);
}
