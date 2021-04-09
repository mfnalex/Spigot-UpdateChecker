package de.jeff_media.updatechecker;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdateCheckEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UpdateChecker instance;
    private final UpdateCheckResult result;
    private final UpdateCheckSuccess success;
    private @Nullable CommandSender[] requesters = null;

    protected UpdateCheckEvent(UpdateCheckSuccess success) {
        instance = UpdateChecker.getInstance();
        this.success = success;
        if (success == UpdateCheckSuccess.FAIL && instance.cachedLatestVersion == null) {
            result = UpdateCheckResult.UNKNOWN;
        } else {
            if (instance.usedVersion.equals(instance.cachedLatestVersion)) {
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

    public @Nullable String getLatestVersion() {
        return instance.cachedLatestVersion;
    }

    public @Nullable CommandSender[] getRequesters() {
        if (requesters == null || requesters.length == 0) return null;
        return requesters;
    }

    protected UpdateCheckEvent setRequesters(@Nullable CommandSender... requesters) {
        this.requesters = requesters;
        return this;
    }

    public UpdateCheckResult getResult() {
        return result;
    }

    public UpdateCheckSuccess getSuccess() {
        return success;
    }

    public @NotNull String getUsedVersion() {
        return instance.usedVersion;
    }

    protected UpdateCheckEvent setStatus(UpdateCheckSuccess success) {
        return this;
    }

}
