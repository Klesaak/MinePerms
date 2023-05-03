package ua.klesaak.mineperms.manager.storage.redis;

public class RedisConfig {
    public static final String UPDATE_CHANNEL_NAME = "mineperms-update";
    private final String address, password, groupsKey, usersKey;
    private final int port, database;

    public RedisConfig(String address, String password, String groupsKey, String usersKey, int port, int database) {
        this.address = address;
        this.port = port;
        this.database = database;
        this.password = password;
        this.groupsKey = groupsKey;
        this.usersKey = usersKey;
    }

    @Override
    public String toString() {
        return "RedisConfig{" +
                "address='" + address + '\'' +
                ", password='" + password + '\'' +
                ", groupsKey='" + groupsKey + '\'' +
                ", usersKey='" + usersKey + '\'' +
                ", port=" + port +
                ", database=" + database +
                '}';
    }
}
