package de.jeff_media.updatechecker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Automatically checks for updates
 */
public class UpdateChecker {

	protected static final String VERSION = "1.0.0";
	private static final String SPIGOT_CHANGELOG_SUFFIX = "/history";
	private static final String SPIGOT_DOWNLOAD_LINK = "https://www.spigotmc.org/resources/";
	private static final String SPIGOT_UPDATE_API = "https://api.spigotmc.org/legacy/update.php?resource=";
	private static UpdateChecker instance = null;
	private static boolean listenerAlreadyRegistered = false;
	// MrNemo64 start
	private BiConsumer<CommandSender[], String> onSuccessfulCachedLatestVersion = (requesters, cachedVersion) -> {};
	private Consumer<Exception> onUnsuccessfulCachedLatestVersion = (ex) -> ex.printStackTrace();
	// MrNemo64 end
	private String cachedLatestVersion = null;
	private String nameFreeVersion = "Free";
	private String namePaidVersion = "Paid";
	private String notifyPermission = null;
	@SuppressWarnings("CanBeFinal")
	private String spigotUserId = "%%__USER__%%";
	private String usedVersion = null;
	private String apiLink = null;
	private String changelogLink = null;
	private String donationLink = null;
	private String freeDownloadLink = null;
	private String paidDownloadLink = null;
	private String userAgentString = null;
	private boolean coloredConsoleOutput = false;
	private boolean notifyOpsOnJoin = true;
	private boolean notifyRequesters = true;
	private boolean usingPaidVersion = false;
	private Plugin main = null;
	private int taskId = -1;
	private int timeout = 0;

	/**
	 * Use UpdateChecker.init() instead. You can later get the instance by using
	 * UpdateChecker.getInstance()
	 */
	private UpdateChecker() {

	}

	/**
	 * Gets the UpdateChecker instance
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
	 * @return The UpdateChecker instance
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
	 * @return The UpdateChecker instance
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
	 * Starts to check every X hours for updates - If a task is already running, it
	 * gets cancelled and replaced with the new one, so don't be afraid to use this
	 * in your reload function. The first check will also happen after X hours so
	 * you might want to call checkNow() too. When you set notifyRequesters to true
	 * (default), the Console will get a notification about the check result.
	 *
	 * @param hours Amount of hours in between checks
	 * @return The UpdateChecker instance
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
	public void checkNow() {
		checkNow(Bukkit.getConsoleSender());
	}

	/**
	 * Returns the last successful Check Result
	 * 
	 * @return
	 */
	public UpdateCheckResult getLastCheckResult() {
		if (cachedLatestVersion == null)
			return UpdateCheckResult.UNKNOWN;
		if (cachedLatestVersion.equals(usedVersion))
			return UpdateCheckResult.RUNNING_LATEST_VERSION;
		return UpdateCheckResult.NEW_VERSION_AVAILABLE;
	}

	/**
	 * Checks for updates now and sends the result to the given list of
	 * CommandSenders. Can be null to silently check for updates.
	 *
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
				cachedLatestVersion = reader.readLine().trim();
				reader.close();
				updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.SUCCESS);
			} catch(final Exception e) {
				updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.FAIL);
				Bukkit.getScheduler().runTask(main, () -> getOnUnsuccessfulCachedLatestVersion().accept(e));
			}

			UpdateCheckEvent finalUpdateCheckEvent = updateCheckEvent.setRequesters(requesters);
			Bukkit.getScheduler().runTask(main, () -> {
				// MrNemo64 start
				if (finalUpdateCheckEvent.getSuccess() == UpdateCheckSuccess.SUCCESS)
					getOnSuccessfulCachedLatestVersion().accept(requesters, cachedLatestVersion);
				// MrNemo64 end
				Bukkit.getPluginManager().callEvent(finalUpdateCheckEvent);
			});
		});
	}

	/**
	 * Checks that the class was properly relocated. Proudly stolen from bStats.org
	 */
	private void checkRelocation() {
		final String defaultPackage = new String(new byte[] { 'd', 'e', '.', 'j', 'e', 'f', 'f', '_', 'm', 'e', 'd', 'i',
				'a', '.', 'u', 'p', 'd', 'a', 't', 'e', 'c', 'h', 'e', 'c', 'k', 'e', 'r' });
		final String examplePackage = new String(new byte[] { 'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e' });
		if (this.getClass().getPackage().getName().startsWith(defaultPackage)
				|| this.getClass().getPackage().getName().startsWith(examplePackage)) {
			throw new IllegalStateException("UpdateChecker class has not been relocated correctly!");
		}
	}

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
	 *         the first element is the paid download link.
	 */
	public List<String> getAppropiateDownloadLinks() {
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
	 * Returns the latest version string found by the UpdateChecker, or null if all
	 * checks until yet have failed.
	 *
	 * @return
	 */
	public String getCachedLatestVersion() {
		return cachedLatestVersion;
	}

	/**
	 * Returns the changelog link
	 *
	 * @return
	 */
	public String getChangelogLink() {
		return changelogLink;
	}

	/**
	 * Sets a link to your plugin's changelog generated from your plugin's SpigotMC
	 * Resource ID
	 *
	 * @param spigotResourceId
	 * @return
	 */
	public UpdateChecker setChangelogLink(int spigotResourceId) {
		return setChangelogLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId + SPIGOT_CHANGELOG_SUFFIX);
	}

	/**
	 * Sets a link to your plugin's changelog.
	 *
	 * @param link
	 * @return
	 */
	public UpdateChecker setChangelogLink(@Nullable String link) {
		changelogLink = link;
		return this;
	}

	/**
	 * Returns the donation link
	 *
	 * @return
	 */
	public String getDonationLink() {
		return donationLink;
	}

	/**
	 * Sets a link to your plugin's donation website
	 *
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
	 * Gets the version string of the currently used plugin version.
	 *
	 * @return
	 */
	public String getUsedVersion() {
		return usedVersion;
	}

	/**
	 * Sets whether or not the used and latest version will be displayed in color in
	 * the console
	 *
	 * @param coloredConsoleOutput
	 * @return
	 */
	public UpdateChecker setColoredConsoleOutput(boolean coloredConsoleOutput) {
		this.coloredConsoleOutput = coloredConsoleOutput;
		return this;
	}

	/**
	 * Sets the download link for your plugin generated from your plugin's SpigotMC
	 * Resource ID. Use this if there is only one version of your plugin, either
	 * only a free or only a paid version.
	 *
	 * @param spigotResourceId
	 * @return
	 */
	public UpdateChecker setDownloadLink(int spigotResourceId) {
		return setDownloadLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId);
	}

	/**
	 * Sets the download link for your plugin. Use this if there is only one version
	 * of your plugin, either only a free or only a paid version.
	 *
	 * @param downloadLink
	 * @return
	 */
	public UpdateChecker setDownloadLink(@Nullable String downloadLink) {
		this.paidDownloadLink = null;
		this.freeDownloadLink = downloadLink;
		return this;
	}

	/**
	 * Sets the download link for the free version of your plugin generated from
	 * your plugin's SpigotMC Resource ID. Use this if there is both, a free and a
	 * paid version of your plugin available.
	 *
	 * @param spigotResourceId
	 * @return
	 */
	public UpdateChecker setFreeDownloadLink(int spigotResourceId) {
		return setFreeDownloadLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId);
	}

	/**
	 * Sets the download link for the free version of your plugin. Use this if there
	 * is both, a free and a paid version of your plugin available.
	 *
	 * @param freeDownloadLink
	 * @return
	 */
	public UpdateChecker setFreeDownloadLink(@Nullable String freeDownloadLink) {
		this.freeDownloadLink = freeDownloadLink;
		return this;
	}

	/**
	 * Sets the suffix for the free version's name. E.g. when you set this to
	 * "Free", the Download link for the free version will be shown as "Download
	 * (Free): [Link]"
	 *
	 * @param nameFreeVersion
	 * @return
	 */
	public UpdateChecker setNameFreeVersion(String nameFreeVersion) {
		this.nameFreeVersion = nameFreeVersion;
		return this;
	}

	/**
	 * Sets the suffix for the paid version's name. E.g. when you set this to
	 * "Platinum version", the Download link for the paid version will be shown as
	 * "Download (Platinum version): [Link]"
	 *
	 * @param namePaidVersion
	 * @return
	 */
	public UpdateChecker setNamePaidVersion(String namePaidVersion) {
		this.namePaidVersion = namePaidVersion;
		return this;
	}

	/**
	 * You can set a permission name. Players joining with this permission will be
	 * informed when there is a new version available.
	 *
	 * @param permission
	 * @return
	 */
	public UpdateChecker setNotifyByPermissionOnJoin(@Nullable String permission) {
		notifyPermission = permission;
		return this;
	}

	/**
	 * Whether or not to inform OPs on join when there is a new version available.
	 *
	 * @param notifyOpsOnJoin
	 * @return
	 */
	public UpdateChecker setNotifyOpsOnJoin(boolean notifyOpsOnJoin) {
		this.notifyOpsOnJoin = notifyOpsOnJoin;
		return this;
	}

	/**
	 * Whether or not CommandSenders who request an update check will be notified of
	 * the result.
	 *
	 * @param notify
	 * @return
	 */
	public UpdateChecker setNotifyRequesters(boolean notify) {
		notifyRequesters = notify;
		return this;
	}

	/**
	 * Sets the download link for the paid version of your plugin generated from
	 * your plugin's SpigotMC Resource ID. Use this if there is both, a free and a
	 * paid version of your plugin available.
	 *
	 * @param spigotResourceId
	 * @return
	 */
	public UpdateChecker setPaidDownloadLink(int spigotResourceId) {
		return setPaidDownloadLink(SPIGOT_DOWNLOAD_LINK + spigotResourceId);
	}

	/**
	 * Sets the download link for the paid version of your plugin. Use this if there
	 * is both, a free and a paid version of your plugin available.
	 *
	 * @param link
	 * @return
	 */
	public UpdateChecker setPaidDownloadLink(@NotNull String link) {
		paidDownloadLink = link;
		return this;
	}

	/**
	 * Sets the timeout for the HTTP(S) connection in milliseconds. 0 = use Java's
	 * default value
	 *
	 * @param timeout
	 */
	public UpdateChecker setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Sets the UserAgent string using a UserAgentBuilder
	 *
	 * @param userAgentBuilder
	 * @return
	 */
	public UpdateChecker setUserAgent(@NotNull UserAgentBuilder userAgentBuilder) {
		userAgentString = userAgentBuilder.build();
		return this;
	}

	/**
	 * Sets the UserAgent string using plain text
	 *
	 * @param userAgent
	 * @return
	 */
	public UpdateChecker setUserAgent(@Nullable String userAgent) {
		userAgentString = userAgent;
		return this;
	}

	/**
	 * Tells the UpdateChecker whether the server already uses the paid version of
	 * your plugin. If yes, the downloads to the free version are not shown. You can
	 * ignore this if you only offer one version of your plugin. When this value is
	 * not set, the Update Checker automatically sets this to true by checking the
	 * %%__USER__%% placeholder, see
	 * https://www.spigotmc.org/wiki/premium-resource-placeholders-identifiers/
	 *
	 * @param paidVersion
	 * @return
	 */
	public UpdateChecker setUsingPaidVersion(boolean paidVersion) {
		usingPaidVersion = paidVersion;
		return this;
	}

	/**
	 * Stops the scheduled update checks - THIS IS NOT NEEDED when calling
	 * checkEveryXHours(double) again, as the UpdateChecker will automatically stop
	 * its previous task.
	 */
	public void stop() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
		}
		taskId = -1;
	}

	// MrNemo64 start
	/**
	 * @return True if the lastest version si being used, false otherwise
	 */
	public boolean isUsingLastestVersion() {
		return usedVersion.equals(instance.cachedLatestVersion);
	}

	/**
	 * @return the nameFreeVersion
	 */
	public String getNameFreeVersion() {
		return nameFreeVersion;
	}

	/**
	 * @return the namePaidVersion
	 */
	public String getNamePaidVersion() {
		return namePaidVersion;
	}

	/**
	 * @return the notifyPermission
	 */
	public String getNotifyPermission() {
		return notifyPermission;
	}

	/**
	 * @return the spigotUserId
	 */
	public String getSpigotUserId() {
		return spigotUserId;
	}

	/**
	 * @return the coloredConsoleOutput
	 */
	public boolean isColoredConsoleOutput() {
		return coloredConsoleOutput;
	}

	/**
	 * @return the notifyOpsOnJoin
	 */
	public boolean isNotifyOpsOnJoin() {
		return notifyOpsOnJoin;
	}

	/**
	 * @return the notifyRequesters
	 */
	public boolean isNotifyRequesters() {
		return notifyRequesters;
	}

	/**
	 * @return the usingPaidVersion
	 */
	public boolean isUsingPaidVersion() {
		return usingPaidVersion;
	}

	/**
	 * @return the onSuccessfulCachedLatestVersion
	 */
	public BiConsumer<CommandSender[], String> getOnSuccessfulCachedLatestVersion() {
		return onSuccessfulCachedLatestVersion;
	}

	/**
	 * @param onSuccessfulCachedLatestVersion the onSuccessfulCachedLatestVersion to
	 *                                        set. If null setted to default. This
	 *                                        consumer runs before the event and
	 *                                        only runs if the check vas successful
	 */
	public UpdateChecker setOnSuccessfulCachedLatestVersion(
			BiConsumer<CommandSender[], String> onSuccessfulCachedLatestVersion) {
		this.onSuccessfulCachedLatestVersion = onSuccessfulCachedLatestVersion == null ? (requesters, cachedVersion) -> {}
				: onSuccessfulCachedLatestVersion;
		return this;
	}

	/**
	 * @return the onUnsuccessfulCachedLatestVersion
	 */
	public Consumer<Exception> getOnUnsuccessfulCachedLatestVersion() {
		return onUnsuccessfulCachedLatestVersion;
	}

	/**
	 * @param onUnsuccessfulCachedLatestVersion If null setted to default. The
	 *                                          recived parameter is the exception
	 *                                          thrown in the version getting
	 *                                          process
	 */
	public UpdateChecker setOnUnsuccessfulCachedLatestVersion(Consumer<Exception> onUnsuccessfulCachedLatestVersion) {
		this.onUnsuccessfulCachedLatestVersion = onUnsuccessfulCachedLatestVersion == null ? (ex) -> ex.printStackTrace()
				: onUnsuccessfulCachedLatestVersion;
		return this;
	}

	// MrNemo64 end

}
