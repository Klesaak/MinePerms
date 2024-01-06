package ua.klesaak.mineperms.manager.storage;

import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Getter
public enum StorageType {
    FILE("JSON", "file"),
    MYSQL("MySQL", "mysql"),
    POSTGRESQL("PostgresSQL", "postgresql"),
    MARIADB("MariaDB", "mariadb");

    private final String name;

    private final String identifier;

    StorageType(String name, String identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    public boolean isFile() {
        return this == FILE;
    }

    public boolean isSQL() {
        return this == MYSQL || this == MARIADB || this == POSTGRESQL;
    }

    public static StorageType parse(String name, StorageType def) {
        for (StorageType t : values()) {
            if (t.getIdentifier().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return def;
    }

    public static StorageType parseWithException(String name) {
        for (StorageType t : values()) {
            if (t.getIdentifier().equalsIgnoreCase(name)) {
                return t;
            }
        }
        throw new RuntimeException("Error while parse storage type.");
    }

    public static Collection<String> getTypesString() {
        val list = new ArrayList<String>(16);
        for (val type : values()) {
            list.add(type.toString());
        }
        return Collections.unmodifiableCollection(list);
    }
}
