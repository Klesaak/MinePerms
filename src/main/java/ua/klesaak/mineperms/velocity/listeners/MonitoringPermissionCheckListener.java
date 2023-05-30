package ua.klesaak.mineperms.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import lombok.NonNull;
import ua.klesaak.mineperms.velocity.MinePermsVelocity;

public class MonitoringPermissionCheckListener {
 /*   private final MinePermsVelocity plugin;

    public MonitoringPermissionCheckListener(MinePermsVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onOtherPermissionSetup(PermissionsSetupEvent event) {
        // players are handled separately
        if (event.getSubject() instanceof Player) {
            return;
        }

        event.setProvider(new MonitoredPermissionProvider(event.getProvider()));
    }

    private final class MonitoredPermissionProvider implements PermissionProvider {
        private final PermissionProvider delegate;

        MonitoredPermissionProvider(PermissionProvider delegate) {
            this.delegate = delegate;
        }

        @Override
        public @NonNull PermissionFunction createFunction(@NonNull PermissionSubject subject) {
            PermissionFunction function = this.delegate.createFunction(subject);
            return new MonitoredPermissionFunction(subject, function);
        }
    }

    private final class MonitoredPermissionFunction implements PermissionFunction {
        private final VerboseCheckTarget verboseCheckTarget;
        private final PermissionFunction delegate;

        MonitoredPermissionFunction(PermissionSubject subject, PermissionFunction delegate) {
            this.delegate = delegate;
            this.verboseCheckTarget = VerboseCheckTarget.internal(determineName(subject));
        }

        @Override
        public com.velocitypowered.api.permission.@NonNull Tristate getPermissionValue(@NonNull String permission) {
            com.velocitypowered.api.permission.Tristate setting = this.delegate.getPermissionValue(permission);

            // report result
            Tristate result = CompatibilityUtil.convertTristate(setting);

            MonitoringPermissionCheckListener.this.plugin.getVerboseHandler().offerPermissionCheckEvent(CheckOrigin.PLATFORM_API_HAS_PERMISSION_SET, this.verboseCheckTarget, QueryOptionsImpl.DEFAULT_CONTEXTUAL, permission, TristateResult.forMonitoredResult(result));
            MonitoringPermissionCheckListener.this.plugin.getPermissionRegistry().offer(permission);

            return setting;
        }
    }

    private String determineName(PermissionSubject subject) {
        if (subject == this.plugin.getServer().getConsoleCommandSource()) {
            return "console";
        }
        return subject.getClass().getSimpleName();
    }*/
}