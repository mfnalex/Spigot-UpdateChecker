package de.jeff_media.updatechecker;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called whenever an update check is finished.
 */
public class UpdateCheckEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UpdateChecker instance;
    private final UpdateCheckResult result;
    private final UpdateCheckSuccess success;
    private @Nullable CommandSender[] requesters = null;

    protected UpdateCheckEvent(UpdateCheckSuccess success) {
        instance = UpdateChecker.getInstance();
        this.success = success;
        if (success == UpdateCheckSuccess.FAIL && instance.getLatestVersion() == null) {
            result = UpdateCheckResult.UNKNOWN;
        } else {
            if (instance.isUsingLatestVersion()) {
                result = UpdateCheckResult.RUNNING_LATEST_VERSION;
            } else {
                result = UpdateCheckResult.NEW_VERSION_AVAILABLE;
            }
        }
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the latest version string found by the UpdateChecker, or null if all previous checks have failed.
     *
     * @return Latest version string found by the UpdateChecker, or null if all previous checks have failed
     */
    public @Nullable String getLatestVersion() {
        return instance.getLatestVersion();
    }

    /**
     * Gets an array of all CommandSenders who have requested this update check. Normally this will either be the ConsoleCommandSender or a player.
     *
     * @return Array of all CommandSenders who have requested this update check
     */
    public @Nullable CommandSender[] getRequesters() {
        if (requesters == null || requesters.length == 0) return null;
        return requesters;
    }

    /**
     * Sets the CommandSenders who requested this update check.
     *
     * @param requesters CommandSenders who requested this update check
     * @return UpdateCheckEvent instance
     */
    protected UpdateCheckEvent setRequesters(@Nullable CommandSender... requesters) {
        this.requesters = requesters;
        return this;
    }

    /**
     * Gets the result, i.e. whether a new version is available or not.
     *
     * @return UpdateCheckResult of this update check
     */
    public UpdateCheckResult getResult() {
        return result;
    }

    /**
     * Checks whether the update checking attempt was successful or failed.
     *
     * @return UpdateCheckSuccess of this update check
     */
    public UpdateCheckSuccess getSuccess() {
        return success;
    }

    /**
     * Gets the version string of the currently used plugin version.
     *
     * @return Version string of the currently used plugin version
     */
    public @NotNull String getUsedVersion() {
        return instance.getUsedVersion();
    }

}
