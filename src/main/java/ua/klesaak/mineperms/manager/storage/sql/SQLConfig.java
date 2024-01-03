package ua.klesaak.mineperms.manager.storage.sql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import ua.klesaak.mineperms.manager.storage.StorageType;
import ua.klesaak.mineperms.manager.storage.sql.driver.AbstractConnectionFactory;
import ua.klesaak.mineperms.manager.storage.sql.driver.MariaDbConnectionFactory;
import ua.klesaak.mineperms.manager.storage.sql.driver.MySqlConnectionFactory;
import ua.klesaak.mineperms.manager.storage.sql.driver.PostgresConnectionFactory;

@Getter
public class SQLConfig {
    private final String username, password, database, address, groupsPermissionsTableSuffix;
    private final int port;
    private final boolean isUseSSL;

    public SQLConfig(String username, String password, String database, String address, String groupsTableSuffix, int port, boolean isUseSSL) {
        this.username = username;
        this.password = password;
        this.database = database;
        this.address = address;
        this.groupsPermissionsTableSuffix = groupsTableSuffix;
        this.port = port;
        this.isUseSSL = isUseSSL;
    }

    public HikariDataSource getSource(StorageType storageType) {
        AbstractConnectionFactory connectionFactory = new MySqlConnectionFactory(null, null, null, null, null, false);
        switch (storageType) {
            case MYSQL: {
                connectionFactory = new MySqlConnectionFactory(this.username, this.password, this.address, String.valueOf(this.port), this.database, this.isUseSSL);
                break;
            }
            case MARIADB: {
                connectionFactory = new MariaDbConnectionFactory(this.username, this.password, this.address, String.valueOf(this.port), this.database, this.isUseSSL);
                break;
            }

            case POSTGRESQL: {
                connectionFactory = new PostgresConnectionFactory(this.username, this.password, this.address, String.valueOf(this.port), this.database, this.isUseSSL);
            }
        }
        return new HikariDataSource(connectionFactory.getHikariConfig());
    }

    @Override
    public String toString() {
        return "MySQLConfig{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", database='" + database + '\'' +
                ", address='" + address + '\'' +
                ", groupsPermissionsTableSuffix='" + groupsPermissionsTableSuffix + '\'' +
                ", port=" + port +
                ", isUseSSL=" + isUseSSL +
                '}';
    }
}
