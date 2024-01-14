package ua.klesaak.mineperms.manager.storage.entity.adapter;

import com.google.gson.*;
import ua.klesaak.mineperms.manager.storage.entity.Group;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class GroupDataAdapter implements JsonDeserializer<Group>, JsonSerializer<Group> {

    @Override
    public Group deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = (JsonObject)jsonElement;
        String groupId = jsonObject.get("groupId").getAsString();
        String prefix = jsonObject.get("prefix").getAsString();
        String suffix = jsonObject.get("suffix").getAsString();
        Set<String> inheritanceGroups = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        jsonObject.get("permissions").getAsJsonArray().forEach(element -> permissions.add(element.getAsString()));
        jsonObject.get("inheritanceGroups").getAsJsonArray().forEach(element -> inheritanceGroups.add(element.getAsString()));
        Group group = new Group(groupId);
        group.setPrefix(prefix);
        group.setSuffix(suffix);
        group.setPermissions(permissions);
        group.setInheritanceGroups(inheritanceGroups);

        return group;
    }

    @Override
    public JsonElement serialize(Group group, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("groupId", group.getGroupId());
        jsonObject.addProperty("prefix", group.getPrefix());
        jsonObject.addProperty("suffix", group.getSuffix());
        JsonArray inheritanceGroups = new JsonArray();
        group.getInheritanceGroups().forEach(inheritanceGroups::add);
        jsonObject.add("inheritanceGroups", inheritanceGroups);
        JsonArray permissions = new JsonArray();
        group.getPermissions().forEach(permissions::add);
        jsonObject.add("permissions", permissions);
        return jsonObject;
    }
}
