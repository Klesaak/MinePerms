package ua.klesaak.mineperms.manager.storage.mysql;

public class MySQLConfig {
    private final int port;
    private final String username, password, database, address, groupsTable, usersTable;
    private final boolean isUseSSL;

    public MySQLConfig(String username, String password, String database, String address, String groupsTable, String usersTable, int port, boolean isUseSSL) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.groupsTable = groupsTable;
        this.usersTable = usersTable;
        this.isUseSSL = isUseSSL;
    }

    @Override
    public String toString() {
        return "MySQLConfig{" +
                "port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", database='" + database + '\'' +
                ", address='" + address + '\'' +
                ", groupsTable='" + groupsTable + '\'' +
                ", usersTable='" + usersTable + '\'' +
                ", isUseSSL=" + isUseSSL +
                '}';
    }
}
