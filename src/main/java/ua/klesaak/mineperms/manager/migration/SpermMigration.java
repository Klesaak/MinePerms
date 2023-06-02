package ua.klesaak.mineperms.manager.migration;

import lombok.val;
import ru.Den_Abr.SimplePerms.SimplePermsCommon;

public class SpermMigration {

    public void migrate() {//todo
        val backend = SimplePermsCommon.getInstance().getPermissionManager().getBackend();
        backend.getAllGroups();
        backend.getAllUsers();
    }
}
