package ua.klesaak.mineperms.manager.storage.redis;

public class RedisConfig {
    private final String address, password, updateChanel, groupsKey, usersKey;
    private final int port, database;

    public RedisConfig(String address, String password, String updateChanel, String groupsKey, String usersKey, int port, int database) {
        this.address = address;
        this.port = port;
        this.database = database;
        this.password = password;
        this.updateChanel = updateChanel;
        this.groupsKey = groupsKey;
        this.usersKey = usersKey;
    }

    @Override
    public String toString() {
        return "RedisConfig{" +
                "address='" + address + '\'' +
                ", password='" + password + '\'' +
                ", updateChanel='" + updateChanel + '\'' +
                ", groupsKey='" + groupsKey + '\'' +
                ", usersKey='" + usersKey + '\'' +
                ", port=" + port +
                ", database=" + database +
                '}';
    }
}
