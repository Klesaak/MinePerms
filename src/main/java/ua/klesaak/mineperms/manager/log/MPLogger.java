package ua.klesaak.mineperms.manager.log;

import lombok.experimental.UtilityClass;

import java.util.logging.Level;
import java.util.logging.Logger;

@UtilityClass
public class MPLogger {
    private MinePermsLogger MINE_PERMS_LOGGER;

    public void register(Logger logger) {
        if (MINE_PERMS_LOGGER != null) throw new RuntimeException("MinePermsLogger is already registered!");
        MINE_PERMS_LOGGER = (message, throwable) -> logger.log(Level.SEVERE, message, throwable);
    }

    public void register(org.slf4j.Logger logger) {
        if (MINE_PERMS_LOGGER != null) throw new RuntimeException("MinePermsLogger is already registered!");
        MINE_PERMS_LOGGER = logger::error;
    }

    public void logError(Throwable throwable) {
        if (MINE_PERMS_LOGGER != null) MINE_PERMS_LOGGER.log(throwable.getMessage(), throwable.getCause());
    }
}
