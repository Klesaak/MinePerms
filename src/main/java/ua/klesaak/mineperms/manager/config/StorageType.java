package ua.klesaak.mineperms.manager.config;

import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

    public static Collection<String> getTypesString() {
        val list = new ArrayList<String>(16);
        for (val type : values()) {
            list.add(type.toString());
        }
        return Collections.unmodifiableCollection(list);
    }
}
