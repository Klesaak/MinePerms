package ua.klesaak.mineperms.manager.storage.entity.data;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.Set;

@Setter @Getter
public class GroupData {
    private String groupID;
    private String prefix;
    private String suffix;
    private String serializedInheritanceGroups;
    private String serializedPerms;

    private GroupData(String groupID, String prefix, String suffix, Set<String> inheritanceGroups, Set<String> permissions) {
        this.groupID = groupID;
        this.prefix = prefix;
        this.suffix = suffix;
        this.serializedInheritanceGroups = JsonData.GSON.toJson(inheritanceGroups);
        this.serializedPerms = JsonData.GSON.toJson(permissions);
    }

    private GroupData(Group group) {
        this(group.getGroupID(), group.getPrefix(), group.getSuffix(), group.getInheritanceGroups(), group.getPermissions());
    }

    public static GroupData from(Group group) {
        return new GroupData(group);
    }

    protected GroupData() {
    }

    public Group getGroup() {
        val group = new Group(this.groupID);
        group.setPrefix(this.prefix);
        group.setSuffix(this.suffix);
        group.setInheritanceGroups(this.getInheritedGroups());
        group.setPermissions(this.getPermissions());
        return group;
    }

    public Set<String> getInheritedGroups() {
        return JsonData.GSON.fromJson(this.serializedInheritanceGroups, new TypeToken<Set<String>>(){}.getType());
    }

    public Set<String> getPermissions() {
        return JsonData.GSON.fromJson(this.serializedPerms, new TypeToken<Set<String>>(){}.getType());
    }
}
