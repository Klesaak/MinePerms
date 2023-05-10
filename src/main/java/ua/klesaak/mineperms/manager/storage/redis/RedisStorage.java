package ua.klesaak.mineperms.manager.storage.redis;


import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;

public class RedisStorage extends Storage {


    public RedisStorage(MinePermsManager manager) {
        super(manager);
    }

    @Override
    public void cacheUser(String nickName) {

    }

    @Override
    public void unCacheUser(String nickName) {

    }

    @Override
    public void saveUser(String nickName) {

    }

    @Override
    public void saveUser(String nickName, User user) {

    }

    @Override
    public void saveGroup(String groupID) {

    }

    @Override
    public User getUser(String nickName) {
        return null;
    }

    @Override
    public String getUserPrefix(String nickName) {
        return null;
    }

    @Override
    public String getUserSuffix(String nickName) {
        return null;
    }

    @Override
    public void addUserPermission(String nickName, String permission) {

    }

    @Override
    public void removeUserPermission(String nickName, String permission) {

    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {

    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {

    }

    @Override
    public void setUserGroup(String nickName, String groupID) {

    }

    @Override
    public void deleteUser(String nickName) {

    }

    @Override
    public void updateUser(String nickName) {

    }

    @Override
    public void addGroupPermission(String groupID, String permission) {

    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {

    }

    @Override
    public void addGroupParent(String groupID, String parentID) {

    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {

    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {

    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {

    }

    @Override
    public void deleteGroup(String groupID) {

    }

    @Override
    public void createGroup(String groupID) {

    }

    @Override
    public void updateGroup(String groupID) {

    }

    @Override
    public void close() {

    }
}
