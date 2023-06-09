package ua.klesaak.mineperms.manager.event.velocity;

import lombok.Getter;
import ua.klesaak.mineperms.manager.storage.entity.User;

@Getter
public class GroupChangeEventVelocity {
    private final User user;

    public GroupChangeEventVelocity(User user) {
        this.user = user;
    }
}
