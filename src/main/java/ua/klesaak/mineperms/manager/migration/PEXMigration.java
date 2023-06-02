package ua.klesaak.mineperms.manager.migration;

import lombok.val;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PEXMigration {

    public void migrate() {//todo
         val permissionManager = PermissionsEx.getPermissionManager();
        permissionManager.getUsers();
        permissionManager.getGroupList();
    }
}
