package ua.klesaak.mineperms.manager.migration;

import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.Collection;

public interface IMigrationPlugin {
    Collection<User> getAllUsers();
    Collection<Group> getAllGroups();
}
