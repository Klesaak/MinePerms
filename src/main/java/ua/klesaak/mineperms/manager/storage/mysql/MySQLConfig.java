package ua.klesaak.mineperms.manager.storage.mysql;

import lombok.val;

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

    public String getHost() {
        val builder = new StringBuilder("jdbc:mysql://");
        builder.append(this.username);
        builder.append(":");
        builder.append(this.password);
        builder.append("@");
        builder.append(this.address);
        builder.append("/");
        builder.append(this.database);
        builder.append("?useUnicode=true&");
        builder.append("characterEncoding=utf-8&");
        builder.append("prepStmtCacheSize=250&");
        builder.append("prepStmtCacheSqlLimit=2048&");
        builder.append("cachePrepStmts=true&");
        builder.append("useServerPrepStmts=true&");
        builder.append("cacheServerConfiguration=true&");
        builder.append("useLocalSessionState=true&");
        builder.append("rewriteBatchedStatements=true&");
        builder.append("maintainTimeStats=false&");
        builder.append("useUnbufferedInput=false&");
        builder.append("useReadAheadInput=false&");
        builder.append("useSSL=");
        builder.append(this.isUseSSL);
        builder.append("&autoReconnect=true");
        return builder.toString();
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
