package thermostat.util.entities;

import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public class InsufficientPermissionsException extends RuntimeException {
    Set<Permission> permissionSet;

    public InsufficientPermissionsException(@NotNull EnumSet<Permission> permissionSet) {
        super("Command could not be executed due to the lack of one or multiple Permissions.");
    }

    public Set<Permission> getPermissionSet() {
        return permissionSet;
    }
}
