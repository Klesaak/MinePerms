package ua.klesaak.mineperms.manager.storage.redismessenger;

import lombok.Getter;

@Getter
public class RedisConfig {
    public static final String UPDATE_CHANNEL_NAME = "mineperms-update";
    private final String address, password;
    private final int port;

    public RedisConfig(String address, String password, int port) {
        this.address = address;
        this.port = port;
        this.password = password;
    }

    @Override
    public String toString() {
        return "RedisConfig{" +
                "address='" + address + '\'' +
                ", password='" + password + '\'' +
                ", port=" + port +
                '}';
    }
}
