package thermostat.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Computes permissions based on Discord's documentation.
 */
public final class PermissionComputer {
    /**
     * @param member Member to check permissions for
     * @param eventChannel Channel to retrieve overrides from
     * @param requiredPermissions List of permissions that will be checked if member has
     * @return EnumSet of missing permissions.
     */
    @Nonnull
    public static EnumSet<Permission> getMissingPermissions(@Nonnull Member member, @Nonnull TextChannel eventChannel, @Nonnull EnumSet<Permission> requiredPermissions) {
        EnumSet<Permission> missingPermissions = EnumSet.noneOf(Permission.class);
        long memberPermissions = computePermissions(member, eventChannel);

        // check if each permission is contained in the permissions long
        for (Permission permission : requiredPermissions) {
            if ((memberPermissions & permission.getRawValue()) != permission.getRawValue()) {
                missingPermissions.add(permission);
            }
        }

        return missingPermissions;
    }

    /**
     * @param member Member to compute permissions for
     * @param channel Channels to get overrides from
     * @return Raw permissions long for given member
     */
    private static long computePermissions(@Nonnull Member member, @Nonnull TextChannel channel) {
        return computeOverrides(computeBasePermissions(member), member, channel);
    }

    /**
     * @param member Calculates base permissions based on the
     *               permissions given to @everyone and member's roles
     * @return long with general permissions
     */
    private static long computeBasePermissions(@Nonnull Member member) {
        if (member.isOwner()) return Permission.ALL_PERMISSIONS;

        long everyonePermissions = member.getGuild().getPublicRole().getPermissionsRaw();

        for (Role role : member.getRoles()) {
            everyonePermissions |= role.getPermissionsRaw();
        }

        if ((everyonePermissions & Permission.ADMINISTRATOR.getRawValue()) == Permission.ADMINISTRATOR.getRawValue()) {
            return Permission.ALL_PERMISSIONS;
        }

        return everyonePermissions;
    }

    /**
     * @param basePermissions base permissions, see computeBasePermissions
     * @param member Member to compute permissions for
     * @param channel Channel to retrieve overrides from
     * @return permissions long with computed base & channel specific overrides
     */
    private static long computeOverrides(long basePermissions, @Nonnull Member member, @Nonnull TextChannel channel) {

        // Administrator overrides everything, so just return that.
        if ((basePermissions & Permission.ADMINISTRATOR.getRawValue()) == Permission.ADMINISTRATOR.getRawValue()) {
            return Permission.ALL_PERMISSIONS;
        }

        long permissions = basePermissions;

        // Everyone overrides
        {
            // Get the @everyone role override for the channel
            PermissionOverride everyoneOverride = channel.getPermissionOverride(channel.getGuild().getPublicRole());

            if (everyoneOverride != null) {
                // pass permissions the given denied and allowed permissions
                permissions &= ~everyoneOverride.getDeniedRaw();
                permissions |= everyoneOverride.getAllowedRaw();
            }
        }

        // Role Overrides
        {
            long allowed = 0, denied = 0;

            // for every role add allowed and denied permissions
            for (Role role : member.getRoles()) {
                PermissionOverride roleOverride = channel.getPermissionOverride(role);

                if (roleOverride != null) {
                    allowed |= roleOverride.getAllowedRaw();
                    denied |= roleOverride.getDeniedRaw();
                }
            }

            permissions &= ~denied;
            permissions |= allowed;
        }

        // Add member specific override
        {
            PermissionOverride memberOverride = channel.getPermissionOverride(member);

            if (memberOverride != null) {
                permissions &= ~memberOverride.getDeniedRaw();
                permissions |= memberOverride.getAllowedRaw();
            }
        }

        return permissions;
    }
}
