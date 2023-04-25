package ua.klesaak.mineperms.manager.storage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter @EqualsAndHashCode @ToString
public class User {
    private final UUID userUUID;
    protected volatile String prefix, suffix, group;
    protected final Set<String> permissions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public User(UUID userUUID) {
        this.userUUID = userUUID;
    }
}
