package de.jeff_media.updatechecker;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Automatically checks for updates
 */
public class UpdateChecker {

    protected static final String VERSION = "1.0.0";
    private static final String SPIGOT_CHANGELOG_SUFFIX = "/history";
    private static final String SPIGOT_DOWNLOAD_LINK = "https://www.spigotmc.org/resources/";
    private static final String SPIGOT_UPDATE_API = "https://api.spigotmc.org/legacy/update.php?resource=";
    private static UpdateChecker instance = null;
    private final Messages messages;
    private static boolean listenerAlreadyRegistered = false;
    @SuppressWarnings("CanBeFinal")
    private final String spigotUserId = "%%__USER__%%";
    private String apiLink = null;
    private String changelogLink = null;
    private boolean checkedAtLeastOnce = false;
    private boolean coloredConsoleOutput = false;
    private String donationLink = null;
    private String freeDownloadLink = null;
    private String latestVersion = null;
    private Plugin main = null;
    private String nameFreeVersion = "Free";
    private String namePaidVersion = "Paid";
    private boolean notifyOpsOnJoin = true;
    private String notifyPermission = null;
    private boolean notifyRequesters = true;
    private boolean suppressUpToDateMessage = false;
    private BiConsumer<CommandSender[], Exception> onFail = (requesters, ex) -> ex.printStackTrace();
    private BiConsumer<CommandSender[], String> onSuccess = (requesters, latestVersion) -> {
    };
    private String paidDownloadLink = null;
    private int taskId = -1;
    private int timeout = 0;
    private String usedVersion = null;
    private String userAgentString = null;
    private boolean usingPaidVersion = false;
    /**
     * Use UpdateChecker.init() instead. You can later get the instance by using
     * UpdateChecker.getInstance()
     */
    private UpdateChecker() {
        messages = new Messages();
    }

    /**
     * Gets the current UpdateChecker singleton
     *
     * @return UpdateChecker instance being ran
     */
    public static UpdateChecker getInstance() {
        if (instance == null) {
            instance = new UpdateChecker();
        }
        return instance;
    }

    /**
     * Initializes the UpdateChecker instance. HAS to be called before the
     * UpdateChecker can run.
     *
     * @param plugin           Main class of your plugin
     * @param spigotResourceId SpigotMC Resource ID to get the latest version String
     *                         from the SpigotMC Web API
     * @return UpdateChecker instance being ran
     */

    public static UpdateChecker init(@NotNull Plugin plugin, int spigotResourceId) {
        return init(plugin, SPIGOT_UPDATE_API + spigotResourceId);
    }

    /**
     * Initializes the UpdateChecker instance. HAS to be called before the
     * UpdateChecker can run.
     *
     * @param plugin  Main class of your plugin
     * @param apiLink HTTP(S) link to a file containing a string with the latest
     *                version of your plugin.
     * @return UpdateChecker instance being ran
     */
    public static UpdateChecker init(@NotNull Plugin plugin, @NotNull String apiLink) {
        Objects.requireNonNull(plugin, "Plugin cannot be null.");
        Objects.requireNonNull(apiLink, "API Link cannot be null.");

        UpdateChecker instance = getInstance();

        instance.main = plugin;
        instance.usedVersion = plugin.getDescription().getVersion().trim();
        instance.apiLink = apiLink;

        if (instance.detectPaidVersion())
            instance.usingPaidVersion = true;

        if (!listenerAlreadyRegistered) {
            Bukkit.getPluginManager().registerEvents(new InternalUpdateCheckListener(), plugin);
            listenerAlreadyRegistered = true;
        }

        return instance;
    }

    /**
     * Checks whether one version is really newer than another according to the semantic versioning scheme, including letters.
     *
     * @param myVersion    One version string
     * @param otherVersion Another version string
     * @return true if the other version is indeed newer, otherwise false
     */
    public static boolean isOtherVersionNewer(String myVersion, String otherVersion) {
        DefaultArtifactVersion used = new DefaultArtifactVersion(myVersion);
        DefaultArtifactVersion latest = new DefaultArtifactVersion(otherVersion);
        return used.compareTo(latest) < 0;
    }

    /**
     * Returns whether the message "You are using the latest version of <PluginName>" will be suppressed.
     *
     * @return True when the message will be suppressed, otherwise false
     */
    public boolean isSuppressUpToDateMessage() {
        return suppressUpToDateMessage;
    }

    /**
     * Starts to check every X hours for updates. If a task is already running, it
     * gets cancelled and replaced with the new one, so don't be afraid to use this
     * in your reload function. The first check will also happen after X hours so
     * you might want to call checkNow() too. When you set notifyRequesters to true
     * (default), the Console will get a notification about the check result.
     *
     * @param hours Amount of hours in between checks
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker checkEveryXHours(double hours) {
        double minutes = hours * 60;
        double seconds = minutes * 60;
        long ticks = ((int) seconds) * 20;
        stop();
        if (ticks > 0) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> checkNow(Bukkit.getConsoleSender()), ticks,
                    ticks);
        } else {
            taskId = -1;
        }
        return this;
    }

    /**
     * Checks for updates now and sends the result to the console when
     * notifyRequesters is set to true (default)
     */
    public UpdateChecker checkNow() {
        checkNow(Bukkit.getConsoleSender());
        return this;
    }

    /**
     * Checks for updates now and sends the result to the given list of
     * CommandSenders. Can be null to silently check for updates.
     *
     * @param requesters CommandSenders to send the result to, or null
     */
    public UpdateChecker checkNow(@Nullable CommandSender... requesters) {
        if (main == null) {
            throw new IllegalStateException("Plugin has not been set.");
        }
        if (apiLink == null) {
            throw new IllegalStateException("API Link has not been set.");
        }

        checkedAtLeastOnce = true;

        if (userAgentString == null) {
            userAgentString = UserAgentBuilder.getDefaultUserAgent().build();
        }

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

            UpdateCheckEvent updateCheckEvent;

            try {
                final HttpURLConnection httpConnection = (HttpURLConnection) new URL(apiLink).openConnection();
                httpConnection.addRequestProperty("User-Agent", userAgentString);
                if (timeout > 0) {
                    httpConnection.setConnectTimeout(timeout);
                }
                final InputStreamReader input = new InputStreamReader(httpConnection.getInputStream());
                final BufferedReader reader = new BufferedReader(input);
                latestVersion = reader.readLine().trim();
                reader.close();

                if (!isUsingLatestVersion() && !isOtherVersionNewer(usedVersion, latestVersion)) {
                    latestVersion = usedVersion;
                }

                updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.SUCCESS);
            } catch (final Exception e) {
                updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.FAIL);
                Bukkit.getScheduler().runTask(main, () -> getOnFail().accept(requesters, e));
            }

            UpdateCheckEvent finalUpdateCheckEvent = updateCheckEvent.setRequesters(requesters);

            Bukkit.getScheduler().runTask(main, () -> {

                if (finalUpdateCheckEvent.getSuccess() == UpdateCheckSuccess.SUCCESS) {
                    getOnSuccess().accept(requesters, latestVersion);
                }

                Bukkit.getPluginManager().callEvent(finalUpdateCheckEvent);
            });

        });
        return this;
    }

    /**
     * Checks that the class was properly relocated. Proudly stolen from bStats.org
     */
    private void checkRelocation() {
        final String defaultPackage = new String(new byte[]{'d', 'e', '.', 'j', 'e', 'f', 'f', '_', 'm', 'e', 'd', 'i', 'a', '.', 'u', 'p', 'd', 'a', 't', 'e', 'c', 'h', 'e', 'c', 'k', 'e', 'r'});
        final String examplePackage = new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
        if (this.getClass().getPackage().getName().startsWith(defaultPackage) || this.getClass().getPackage().getName().startsWith(examplePackage)) {
            throw new IllegalStateException("UpdateChecker class has not been relocated correctly!");
        }
    }

    /**
     * Detects whether the Spigot User ID placeholder has been properly replaced by a numeric string
     *
     * @return true if the Spigot User ID placeholder has been properly replaced by a numeric string
     */
    private boolean detectPaidVersion() {
        return spigotUserId.matches("^[0-9]+$");
    }

    /**
     * Returns a list of applicable Download links.
     * <p>
     * If using the free version and there are links for the free and paid version,
     * element 0 will be the link to the paid version and element will be the link
     * to the free version
     * <p>
     * If using the paid version, there will be only one element containing the link
     * to the paid version, or, if that is not set, the link to the free version.
     * <p>
     * If there is no paid version, there will be only one element containing the
     * link to the free version, or, if that is not set, the link to the plus
     * version.
     * <p>
     * If no download links are set, returns an empty list.
     *
     * @return List of zero to two download links. If the list contains two links,
     * the first element is the paid download link.
     */
    public List<String> getAppropriateDownloadLinks() {
        List<String> list = new ArrayList<>();

        if (usingPaidVersion) {
            if (paidDownloadLink != null) {
                list.add(paidDownloadLink);
            } else if (freeDownloadLink != null) {
                list.add(freeDownloadLink);
            }
        } else {
            if (paidDownloadLink != null) {
                list.add(paidDownloadLink);
            }
            if (freeDownloadLink != null) {
                list.add(freeDownloadLink);
            }
        }
        return list;
    }

    /**
     * Returns the changelog link
     *
     * @return Changelog Link
     */
    public String getChangelogLink() {
        return changelogLink;
    }

    /**
     * Returns the messages instance
     *
     * @return Messages instance
     */
    public Messages getMessages() {
        return messages;
    }

    /**
     * Sets a link to your plugin's changelog generated from your plugin's SpigotMC
     * Resource ID
     *
     * @param spigotResourceId Spigot Resource ID
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setChangelogLink(int spigotResourceId) {
        return setChangelogLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId + SPIGOT_CHANGELOG_SUFFIX);
    }

    /**
     * Sets a link to your plugin's changelog.
     *
     * @param link Changelog link
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setChangelogLink(@Nullable String link) {
        changelogLink = link;
        return this;
    }

    /**
     * Returns the donation link
     *
     * @return Donation link
     */
    public String getDonationLink() {
        return donationLink;
    }

    /**
     * Sets a link to your plugin's donation website
     *
     * @param donationLink Donation link
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setDonationLink(@Nullable String donationLink) {
        this.donationLink = donationLink;
        return this;
    }

    /**
     * Returns the last successful UpdateCheckResult
     *
     * @return Last successful UpdateCheckResult
     */
    public UpdateCheckResult getLastCheckResult() {
        if (latestVersion == null) {
            return UpdateCheckResult.UNKNOWN;
        }
        if (latestVersion.equals(usedVersion)) {
            return UpdateCheckResult.RUNNING_LATEST_VERSION;
        }
        return UpdateCheckResult.NEW_VERSION_AVAILABLE;
    }

    /**
     * Returns the latest version string found by the UpdateChecker, or null if all
     * checks until yet have failed.
     *
     * @return Latest version string found by the UpdateChecker
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * Returns the name/suffix of the free plugin version
     *
     * @return Name/suffix of the free plugin version
     */
    public String getNameFreeVersion() {
        return nameFreeVersion;
    }

    /**
     * Sets the name/suffix for the free version's name. E.g. when you set this to
     * "Free", the Download link for the free version will be shown as "Download
     * (Free): [Link]"
     *
     * @param nameFreeVersion Name/suffix of the free plugin version
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setNameFreeVersion(String nameFreeVersion) {
        this.nameFreeVersion = nameFreeVersion;
        return this;
    }

    /**
     * Returns the name/suffix of the paid plugin version
     *
     * @return Name/suffix of the paid plugin version
     */
    public String getNamePaidVersion() {
        return namePaidVersion;
    }

    /**
     * Sets the name/suffix for the paid version's name. E.g. when you set this to
     * "Platinum version", the Download link for the paid version will be shown as
     * "Download (Platinum version): [Link]"
     *
     * @param namePaidVersion Name/suffix of the paid plugin version
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setNamePaidVersion(String namePaidVersion) {
        this.namePaidVersion = namePaidVersion;
        return this;
    }

    /**
     * Returns the permission required to receive UpdateChecker messages on join
     *
     * @return Permission required to receive UpdateChecker messages on join, or null if not set
     */
    public @Nullable String getNotifyPermission() {
        return notifyPermission;
    }

    /**
     * Gets the task that will run when/after the update check fails.
     *
     * @return Task that will run when/after the update check fails.
     */
    public BiConsumer<CommandSender[], Exception> getOnFail() {
        return onFail;
    }

    /**
     * Gets the task that will run when/after the update check succeeds.
     *
     * @return Task that will run when/after the update check succeeds.
     */
    public BiConsumer<CommandSender[], String> getOnSuccess() {
        return onSuccess;
    }

    /**
     * Gets the plugin that instantiated this UpdateChecker instance
     *
     * @return Plugin that instantiated this UpdateChecker instance
     */
    protected Plugin getPlugin() {
        return main;
    }

    /**
     * Gets the Spigot User ID of the user who downloaded the plugin if it's a premium plugin, otherwise "%%__USER__%%"
     *
     * @return Spigot User ID of the user who downloaded the plugin if it's a premium plugin, otherwise "%%__USER__%%"
     */
    public String getSpigotUserId() {
        return spigotUserId;
    }

    /**
     * Gets the version string of the currently used plugin version
     *
     * @return Version string of the currently used plugin version
     */
    public String getUsedVersion() {
        return usedVersion;
    }

    public boolean isCheckedAtLeastOnce() {
        return checkedAtLeastOnce;
    }

    /**
     * Returns whether colored console output is enabled
     *
     * @return true when colored console output is enabled, otherwise false
     */
    public boolean isColoredConsoleOutput() {
        return coloredConsoleOutput;
    }

    /**
     * Sets whether or not the used and latest version will be displayed in color in the console
     *
     * @param coloredConsoleOutput Whether to use color in the console output
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setColoredConsoleOutput(boolean coloredConsoleOutput) {
        this.coloredConsoleOutput = coloredConsoleOutput;
        return this;
    }

    /**
     * Returns whether OPs will be notified on join when a new version is available
     *
     * @return true when OPs will be notified on join when a new version is available, otherwise false
     */
    public boolean isNotifyOpsOnJoin() {
        return notifyOpsOnJoin;
    }

    /**
     * Whether or not to inform OPs on join when there is a new version available.
     *
     * @param notifyOpsOnJoin Whether to inform on OPs on join when there is a new version available
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setNotifyOpsOnJoin(boolean notifyOpsOnJoin) {
        this.notifyOpsOnJoin = notifyOpsOnJoin;
        return this;
    }

    /**
     * Gets whether the given CommandSenders will be informed about UpdateChecker results
     *
     * @return Whether or not to inform given CommandSenders about UpdateChecker results
     */
    public boolean isNotifyRequesters() {
        return notifyRequesters;
    }

    /**
     * Whether or not CommandSenders who request an update check will be notified of the result.
     * When you use your own tasks using onSuccess and onFail, consider setting this to false.
     *
     * @param notify Whether or not to notify given CommandSenders about UpdateChecker results
     * @return true when CommandSenders will be notified, otherwise false
     */
    public UpdateChecker setNotifyRequesters(boolean notify) {
        notifyRequesters = notify;
        return this;
    }

    /**
     * Checks whether the latest found version of the plugin is being used.
     *
     * @return true if the latest found version is the one currently in use, otherwise false
     */
    public boolean isUsingLatestVersion() {
        return usedVersion.equals(instance.latestVersion);
    }

    /**
     * Returns whether the paid version of the plugin is installed.
     *
     * @return True if the paid version is used, otherwise false
     */
    public boolean isUsingPaidVersion() {
        return usingPaidVersion;
    }

    /**
     * Tells the UpdateChecker whether the server already uses the paid version of
     * your plugin. If yes, the downloads to the free version are not shown. You can
     * ignore this if you only offer one version of your plugin. When this value is
     * not set, the Update Checker automatically sets this to true by checking the
     * %%__USER__%% placeholder, see
     * https://www.spigotmc.org/wiki/premium-resource-placeholders-identifiers/
     *
     * @param paidVersion Whether or not the user is using the paid version of your plugin
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setUsingPaidVersion(boolean paidVersion) {
        usingPaidVersion = paidVersion;
        return this;
    }

    /**
     * Sets a task that will run when/after the update check has failed.
     *
     * @param onFail Task that will run when/after the update check has failed.
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker onFail(BiConsumer<CommandSender[], Exception> onFail) {
        this.onFail = onFail == null ? (requesters, ex) -> ex.printStackTrace() : onFail;
        return this;
    }

    /**
     * Sets a task that will run when/after the update check has succeeded.
     *
     * @param onSuccess Task that will run when/after the update check has succeeded.
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker onSuccess(BiConsumer<CommandSender[], String> onSuccess) {
        this.onSuccess = onSuccess == null ? (requesters, latestVersion) -> {
        } : onSuccess;
        return this;
    }

    /**
     * Sets the download link for your plugin generated from your plugin's SpigotMC
     * Resource ID. Use this if there is only one version of your plugin, either
     * only a free or only a paid version.
     *
     * @param spigotResourceId Spigot Resource ID
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setDownloadLink(int spigotResourceId) {
        return setDownloadLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId);
    }

    /**
     * Sets the download link for your plugin. Use this if there is only one version
     * of your plugin, either only a free or only a paid version.
     *
     * @param downloadLink Download link
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setDownloadLink(@Nullable String downloadLink) {
        this.paidDownloadLink = null;
        this.freeDownloadLink = downloadLink;
        return this;
    }

    /**
     * Sets whether the message "You are using the latest version of <PluginName>" should be suppressed.
     * Defaults to false
     *
     * @param suppress Whether to suppress the message "You are using the latest version of <PluginName>"
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker suppressUpToDateMessage(boolean suppress) {
        this.suppressUpToDateMessage = suppress;
        return this;
    }

    /**
     * Sets the download link for the free version of your plugin generated from
     * your plugin's SpigotMC Resource ID. Use this if there is both, a free and a
     * paid version of your plugin available.
     *
     * @param spigotResourceId Spigot Resource ID of the free version
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setFreeDownloadLink(int spigotResourceId) {
        return setFreeDownloadLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId);
    }

    /**
     * Sets the download link for the free version of your plugin. Use this if there
     * is both, a free and a paid version of your plugin available.
     *
     * @param freeDownloadLink Download link of the free version
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setFreeDownloadLink(@Nullable String freeDownloadLink) {
        this.freeDownloadLink = freeDownloadLink;
        return this;
    }

    /**
     * Sets the permission needed to be informed about UpdateChecker results on join.
     *
     * @param permission Permission needed to be informed about UpdateChecker results on join
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setNotifyByPermissionOnJoin(@Nullable String permission) {
        notifyPermission = permission;
        return this;
    }

    /**
     * Sets the download link for the paid version of your plugin generated from
     * your plugin's SpigotMC Resource ID. Use this if there is both, a free and a
     * paid version of your plugin available.
     *
     * @param spigotResourceId Spigot Resource ID of the paid version
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setPaidDownloadLink(int spigotResourceId) {
        return setPaidDownloadLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId);
    }

    /**
     * Sets the download link for the paid version of your plugin. Use this if there
     * is both, a free and a paid version of your plugin available.
     *
     * @param link Download link of the paid version
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setPaidDownloadLink(@NotNull String link) {
        paidDownloadLink = link;
        return this;
    }

    /**
     * Sets the timeout for the HTTP(S) connection in milliseconds. 0 = use Java's
     * default value
     *
     * @param timeout Timeout in milliseconds, or 0 to use Java's default value
     */
    public UpdateChecker setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the UserAgent string using a UserAgentBuilder
     *
     * @param userAgentBuilder UserAgentBuilder instance
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setUserAgent(@NotNull UserAgentBuilder userAgentBuilder) {
        userAgentString = userAgentBuilder.build();
        return this;
    }

    /**
     * Sets the UserAgent string using plain text
     *
     * @param userAgent UserAgent string
     * @return UpdateChecker instance being ran
     */
    public UpdateChecker setUserAgent(@Nullable String userAgent) {
        userAgentString = userAgent;
        return this;
    }



    /**
     * Stops the scheduled update checks. THIS IS NOT NEEDED when calling
     * checkEveryXHours(double) again, as the UpdateChecker will automatically stop
     * its previous task.
     */
    public UpdateChecker stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        taskId = -1;
        return this;
    }

}
