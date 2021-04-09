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

    public static UpdateChecker getInstance() {
        if (instance == null) {
            instance = new UpdateChecker();
        }
        return instance;
    }

    public static UpdateChecker init(@NotNull Plugin plugin, @NotNull String apiLink) {
        Objects.requireNonNull(plugin, "Plugin cannot be null.");
        Objects.requireNonNull(apiLink, "API Link cannot be null.");

        UpdateChecker instance = getInstance();

        instance.main = plugin;
        instance.usedVersion = plugin.getDescription().getVersion().trim();
        instance.apiLink = apiLink;

        if (instance.detectPaidVersion()) instance.usingPaidVersion = true;

        if (!instance.listenerAlreadyRegistered) {
            Bukkit.getPluginManager().registerEvents(new UpdateCheckListener(), plugin);
        }

        return instance;
    }

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

    public void checkNow() {
        checkNow(Bukkit.getConsoleSender());
    }

    public void checkNow(@Nullable CommandSender... requesters) {
        if (main == null) {
            throw new IllegalStateException("Plugin has not been set.");
        }
        if (apiLink == null) {
            throw new IllegalStateException("API Link has not been set.");
        }

        if (userAgentString == null) {
            userAgentString = "JEFF-Media-GbR-SpigotUpdateChecker/" + VERSION;
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

    public String getChangelogLink() {
        return changelogLink;
    }

    public UpdateChecker setChangelogLink(String link) {
        changelogLink = link;
        return this;
    }

    public String getDonationLink() {
        return donationLink;
    }

    public UpdateChecker setDonationLink(@Nullable String donationLink) {
        this.donationLink = donationLink;
        return this;
    }

    protected Plugin getPlugin() {
        return main;
    }

    public UpdateChecker setColoredConsoleOutput(boolean coloredConsoleOutput) {
        this.coloredConsoleOutput = coloredConsoleOutput;
        return this;
    }

    public UpdateChecker setDownloadLink(@Nullable String downloadLink) {
        this.paidDownloadLink = null;
        this.freeDownloadLink = downloadLink;
        return this;
    }

    public UpdateChecker setFreeDownloadLink(@Nullable String freeDownloadLink) {
        this.freeDownloadLink = freeDownloadLink;
        return this;
    }

    public UpdateChecker setNameFreeVersion(String nameFreeVersion) {
        this.nameFreeVersion = nameFreeVersion;
        return this;
    }

    public UpdateChecker setNamePaidVersion(String namePaidVersion) {
        this.namePaidVersion = namePaidVersion;
        return this;
    }

    public UpdateChecker setNotifyByPermissionOnJoin(@Nullable String permission) {
        notifyPermission = permission;
        return this;
    }

    public UpdateChecker setNotifyOpsOnJoin(boolean notifyOpsOnJoin) {
        this.notifyOpsOnJoin = notifyOpsOnJoin;
        return this;
    }

    public UpdateChecker setNotifyRequesters(boolean notify) {
        notifyRequesters = notify;
        return this;
    }

    public UpdateChecker setPaidDownloadLink(String link) {
        paidDownloadLink = link;
        return this;
    }

    public UpdateChecker setUserAgent(@NotNull UserAgentBuilder userAgentBuilder) {
        userAgentString = userAgentBuilder.build();
        return this;
    }

    public UpdateChecker setUserAgent(@Nullable String userAgent) {
        userAgentString = userAgent;
        return this;
    }

    public UpdateChecker setUsingPaidVersion(boolean paidVersion) {
        usingPaidVersion = paidVersion;
        return this;
    }

}
