package ua.klesaak.mineperms.manager.migration;

import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;

import java.util.Collection;

public interface IMigrationPlugin {
    Collection<User> getAllUsers();
    Collection<Group> getAllGroups();
}
