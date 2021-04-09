package de.jeff_media.updatechecker;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Automatically checks for updates
 */
public class UpdateChecker {

    protected static final String VERSION = "1.0.0";
    private static UpdateChecker instance = null;
    private final boolean listenerAlreadyRegistered = false;
    protected String cachedLatestVersion = null;
    protected boolean coloredConsoleOutput = false;
    protected String nameFreeVersion = "Free";
    protected String namePaidVersion = "Plus";
    protected boolean notifyOpsOnJoin = true;
    protected String notifyPermission = null;
    protected boolean notifyRequesters = true;
    protected String usedVersion = null;
    private String apiLink = null;
    private String changelogLink = null;
    private String donationLink = null;
    private String freeDownloadLink = null;
    private Plugin main = null;
    private String paidDownloadLink = null;
    private int task = -1;
    private String userAgentString = null;
    private boolean usingPaidVersion = false;
    private static final String SPIGOT_UPDATE_API = "https://api.spigotmc.org/legacy/update.php?resource=";
    private static final String SPIGOT_DOWNLOAD_LINK = "https://www.spigotmc.org/resources/";
    private static final String SPIGOT_CHANGELOG_SUFFIX = "/history";

    /**
     * Gets the UpdateChecker instance
     * @return
     */
    public static UpdateChecker getInstance() {
        if (instance == null) {
            instance = new UpdateChecker();
        }
        return instance;
    }

    /**
     * Use UpdateChecker.init() instead. You can later get the instance by using UpdateChecker.getInstance()
     */
    private UpdateChecker() {

    }

    public static UpdateChecker init(@NotNull Plugin plugin, int spigotResourceId) {
        return init(plugin,SPIGOT_UPDATE_API+spigotResourceId);
    }

    /**
     * Initializes the UpdateChecker instance. HAS to be called before the UpdateChecker can run.
     * @param plugin Main class of your plugin
     * @param apiLink HTTP(S) link to a file containing a string with the latest version of your plugin.
     * @return
     */
    public static UpdateChecker init(@NotNull Plugin plugin, @NotNull String apiLink) {
        Objects.requireNonNull(plugin, "Plugin cannot be null.");
        Objects.requireNonNull(apiLink, "API Link cannot be null.");

        UpdateChecker instance = getInstance();

        instance.main = plugin;
        instance.usedVersion = plugin.getDescription().getVersion().trim();
        instance.apiLink = apiLink;

        if (instance.detectPaidVersion()) instance.usingPaidVersion = true;

        if (!instance.listenerAlreadyRegistered) {
            Bukkit.getPluginManager().registerEvents(new InternalUpdateCheckListener(), plugin);
        }

        return instance;
    }

    /**
     * Starts to check every X hours for updates. The first check will also happen after X hours so you might want to call checkNow() too. When you set notifyRequesters to true (default), the Console will get a notification about the check result.
     * @param hours Amount of hours in between checks
     * @return
     */
    public UpdateChecker checkEveryXHours(double hours) {
        double minutes = hours * 60;
        double seconds = minutes * 60;
        long ticks = ((int) seconds) * 20;
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
        if (ticks > 0) {
            task = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, ()->checkNow(Bukkit.getConsoleSender()), ticks, ticks);
        } else {
            task = -1;
        }
        return this;
    }

    /**
     * Checks for updates now and sends the result to the console when notifyRequesters is set to true (default)
     */
    public void checkNow() {
        checkNow(Bukkit.getConsoleSender());
    }

    /**
     * Checks for updates now and sends the result to the given list of CommandSenders. Can be null to silently check for updates.
     * @param requesters CommandSenders to send the result to, or null
     */
    public void checkNow(@Nullable CommandSender... requesters) {
        if (main == null) {
            throw new IllegalStateException("Plugin has not been set.");
        }
        if (apiLink == null) {
            throw new IllegalStateException("API Link has not been set.");
        }

        if (userAgentString == null) {
            userAgentString = UserAgentBuilder.getDefaultUserAgent().build();
        }

        Bukkit.getScheduler().runTaskAsynchronously(main, ()->{

            UpdateCheckEvent updateCheckEvent;

            try {
                final HttpURLConnection httpConnection = (HttpURLConnection) new URL(apiLink).openConnection();
                httpConnection.addRequestProperty("User-Agent", userAgentString);

                final InputStreamReader input = new InputStreamReader(httpConnection.getInputStream());
                final BufferedReader reader = new BufferedReader(input);
                cachedLatestVersion = reader.readLine().trim();
                reader.close();
                updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.SUCCESS);
            } catch (final MalformedURLException urlException) {
                urlException.printStackTrace();
                updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.FAIL);
            } catch (final IOException ioException) {
                updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.FAIL);
            }

            UpdateCheckEvent finalUpdateCheckEvent = updateCheckEvent.setRequesters(requesters);
            Bukkit.getScheduler().runTask(main, ()->Bukkit.getPluginManager().callEvent(finalUpdateCheckEvent));
        });
    }

    private boolean detectPaidVersion() {
        try {
            Method getUIDMethod = main.getClass().getMethod("getUID", null);
            return ((String) getUIDMethod.invoke(null, null)).matches("^[0-9]+$");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return false;
        }
    }

    /**
     * Returns a list of applicable Download links.
     * <p>
     * If using the free version and there are links for the free and paid version, element 0 will be the link to the paid version and element will be the link to the free version
     * <p>
     * If using the paid version, there will be only one element containing the link to the paid version, or, if that is not set, the link to the free version.
     * <p>
     * If there is no paid version, there will be only one element containing the link to the free version, or, if that is not set, the link to the plus version.
     * <p>
     * If no download links are set, returns an empty list.
     *
     * @return List of zero to two download links. If the list contains two links, the first element is the paid download link.
     */
    public ArrayList<String> getAppropiateDownloadLinks() {
        ArrayList<String> list = new ArrayList<>();

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
     * @return
     */
    public String getChangelogLink() {
        return changelogLink;
    }

    /**
     * Sets a link to your plugin's changelog generated from your plugin's SpigotMC Resource ID
     * @param spigotResourceId
     * @return
     */
    public UpdateChecker setChangelogLink(int spigotResourceId) {
        return setChangelogLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId + SPIGOT_CHANGELOG_SUFFIX);
    }
    /**
     * Sets a link to your plugin's changelog.
     * @param link
     * @return
     */
    public UpdateChecker setChangelogLink(String link) {
        changelogLink = link;
        return this;
    }

    /**
     * Returns the donation link
     * @return
     */
    public String getDonationLink() {
        return donationLink;
    }

    /**
     * Sets a link to your plugin's donation website
     * @param donationLink
     * @return
     */
    public UpdateChecker setDonationLink(@Nullable String donationLink) {
        this.donationLink = donationLink;
        return this;
    }

    protected Plugin getPlugin() {
        return main;
    }

    /**
     * Sets whether or not the used and latest version will be displayed in color in the console
     * @param coloredConsoleOutput
     * @return
     */
    public UpdateChecker setColoredConsoleOutput(boolean coloredConsoleOutput) {
        this.coloredConsoleOutput = coloredConsoleOutput;
        return this;
    }

    /**
     * Sets the download link for your plugin generated from your plugin's SpigotMC Resource ID. Use this if there is only one version of your plugin, either only a free or only a paid version.
     * @param spigotResourceId
     * @return
     */
    public UpdateChecker setDownloadLink(int spigotResourceId) {
        return setDownloadLink(SPIGOT_DOWNLOAD_LINK+spigotResourceId);
    }

    /**
     * Sets the download link for your plugin. Use this if there is only one version of your plugin, either only a free or only a paid version.
     * @param downloadLink
     * @return
     */
    public UpdateChecker setDownloadLink(@Nullable String downloadLink) {
        this.paidDownloadLink = null;
        this.freeDownloadLink = downloadLink;
        return this;
    }

    /**
     * Sets the download link for the free version of your plugin generated from your plugin's SpigotMC Resource ID. Use this if there is both, a free and a paid version of your plugin available.
     * @param spigotResourceId
     * @return
     */
    public UpdateChecker setFreeDownloadLink(int spigotResourceId) {
        return setFreeDownloadLink(SPIGOT_DOWNLOAD_LINK+spigotResourceId);
    }

    /**
     * Sets the download link for the free version of your plugin. Use this if there is both, a free and a paid version of your plugin available.
     * @param freeDownloadLink
     * @return
     */
    public UpdateChecker setFreeDownloadLink(@Nullable String freeDownloadLink) {
        this.freeDownloadLink = freeDownloadLink;
        return this;
    }

    /**
     * Sets the suffix for the free version's name. E.g. when you set this to "Free", the Download link for the free version will be shown as "Download (Free): [Link]"
     * @param nameFreeVersion
     * @return
     */
    public UpdateChecker setNameFreeVersion(String nameFreeVersion) {
        this.nameFreeVersion = nameFreeVersion;
        return this;
    }

    /**
     * Sets the suffix for the paid version's name. E.g. when you set this to "Platinum version", the Download link for the paid version will be shown as "Download (Platinum version): [Link]"
     * @param namePaidVersion
     * @return
     */
    public UpdateChecker setNamePaidVersion(String namePaidVersion) {
        this.namePaidVersion = namePaidVersion;
        return this;
    }

    /**
     * You can set a permission name. Players joining with this permission will be informed when there is a new version available.
     * @param permission
     * @return
     */
    public UpdateChecker setNotifyByPermissionOnJoin(@Nullable String permission) {
        notifyPermission = permission;
        return this;
    }

    /**
     * Whether or not to inform OPs on join when there is a new version available.
     * @param notifyOpsOnJoin
     * @return
     */
    public UpdateChecker setNotifyOpsOnJoin(boolean notifyOpsOnJoin) {
        this.notifyOpsOnJoin = notifyOpsOnJoin;
        return this;
    }

    /**
     * Whether or not CommandSenders who request an update check will be notified of the result.
     * @param notify
     * @return
     */
    public UpdateChecker setNotifyRequesters(boolean notify) {
        notifyRequesters = notify;
        return this;
    }

    /**
     * Sets the download link for the paid version of your plugin generated from your plugin's SpigotMC Resource ID. Use this if there is both, a free and a paid version of your plugin available.
     * @param spigotResourceId
     * @return
     */
    public UpdateChecker setPaidDownloadLink(int spigotResourceId) {
        return setPaidDownloadLink(SPIGOT_DOWNLOAD_LINK+spigotResourceId);
    }

    /**
     * Sets the download link for the paid version of your plugin. Use this if there is both, a free and a paid version of your plugin available.
     * @param link
     * @return
     */
    public UpdateChecker setPaidDownloadLink(String link) {
        paidDownloadLink = link;
        return this;
    }

    /**
     * Sets the UserAgent string using a UserAgentBuilder
     * @param userAgentBuilder
     * @return
     */
    public UpdateChecker setUserAgent(@NotNull UserAgentBuilder userAgentBuilder) {
        userAgentString = userAgentBuilder.build();
        return this;
    }

    /**
     * Sets the UserAgent string using plain text
     * @param userAgent
     * @return
     */
    public UpdateChecker setUserAgent(@Nullable String userAgent) {
        userAgentString = userAgent;
        return this;
    }

    /**
     * Tells the UpdateChecker whether the server already uses the paid version of your plugin. If yes, the downloads to the free version are not shown. You can ignore this if you only offer one version of your plugin.
     * @param paidVersion
     * @return
     */
    public UpdateChecker setUsingPaidVersion(boolean paidVersion) {
        usingPaidVersion = paidVersion;
        return this;
    }
}
