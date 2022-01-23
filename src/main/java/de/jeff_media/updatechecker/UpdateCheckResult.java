package de.jeff_media.updatechecker;

/**
 * Represents whether a new version is available or not
 */
public enum UpdateCheckResult {
    /**
     * Result when a new version is available
     */
    NEW_VERSION_AVAILABLE,
    /**
     * Result when the plugin is already the latest version
     */
    RUNNING_LATEST_VERSION,
    /**
     * Result when the update check failed, or the version could not be compared
     */
    UNKNOWN
}
