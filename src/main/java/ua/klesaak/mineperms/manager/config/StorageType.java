package ua.klesaak.mineperms.manager.config;

public enum StorageType {
    FILE, MYSQL, REDIS;

    public boolean isFile() {
        return this == FILE;
    }

    public boolean isMySQL() {
        return this == MYSQL;
    }

    public boolean isRedis() {
        return this == REDIS;
    }
}
