package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.Permission;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public interface CommandEvent {

    void checkPermissions();

    @Nonnull
    EnumSet<Permission> findMissingPermissions(EnumSet<Permission> permissionsToSeek, EnumSet<Permission> givenPermissions);

    void execute();

}
