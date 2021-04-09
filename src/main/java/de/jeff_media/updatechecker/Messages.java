package de.jeff_media.updatechecker;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

class Messages {

    @NotNull
    private static TextComponent createLink(@NotNull final String text, @NotNull final String link) {
        final ComponentBuilder lore = new ComponentBuilder("Link: ")
                .bold(true)
                .append(link)
                .bold(false);
        final TextComponent component = new TextComponent(text);
        component.setBold(true);
        // TODO: Make color configurable
        component.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        //noinspection deprecation
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, lore.create()));
        return component;
    }

    protected static void printCheckResultToConsole(UpdateCheckEvent event) {

        final UpdateChecker instance = UpdateChecker.getInstance();
        final Plugin plugin = instance.getPlugin();

        if (event.getSuccess() == UpdateCheckSuccess.FAIL || event.getResult() == UpdateCheckResult.UNKNOWN) {
            plugin.getLogger().warning("Could not check for updates.");
            return;
        }

        if (event.getResult() == UpdateCheckResult.RUNNING_LATEST_VERSION) {
            plugin.getLogger().info(String.format("You are using the latest version of %s.", plugin.getName()));
            return;
        }

        plugin.getLogger().warning("=================================================");
        plugin.getLogger().warning(String.format("There is a new version of %s available!", plugin.getName()));
        plugin.getLogger().warning(String.format("  Your version: %s%s", instance.coloredConsoleOutput ? ChatColor.RED : "", event.getUsedVersion()));
        plugin.getLogger().warning(String.format("Latest version: %s%s", instance.coloredConsoleOutput ? ChatColor.GREEN : "", event.getLatestVersion()));

        ArrayList<String> downloadLinks = instance.getAppropiateDownloadLinks();

        if (downloadLinks.size() > 0) {
            plugin.getLogger().warning("Please update to the newest version.");
            plugin.getLogger().warning(" ");
            if (downloadLinks.size() == 1) {
                plugin.getLogger().warning("Download:");
                plugin.getLogger().warning(downloadLinks.get(0));
            } else if (downloadLinks.size() == 2) {
                plugin.getLogger().warning("Download (Plus):");
                plugin.getLogger().warning(downloadLinks.get(0));
                plugin.getLogger().warning(" ");
                plugin.getLogger().warning("Download (Free):");
                plugin.getLogger().warning(downloadLinks.get(1));
            }

        }
        plugin.getLogger().warning("=================================================");
    }

    protected static void printCheckResultToPlayer(Player player) {
        UpdateChecker instance = UpdateChecker.getInstance();
        player.sendMessage(ChatColor.GRAY + "There is a new version of " + ChatColor.GOLD + instance.getPlugin().getName() + ChatColor.GRAY + " available.");
        sendLinks(player);
        player.sendMessage(ChatColor.DARK_GRAY + "Latest version: " + ChatColor.GREEN + instance.cachedLatestVersion + ChatColor.DARK_GRAY + " | Your version: " + ChatColor.RED + instance.usedVersion);
        player.sendMessage("");
    }

    private static void sendLinks(@NotNull final Player player) {

        UpdateChecker instance = UpdateChecker.getInstance();

        ArrayList<TextComponent> links = new ArrayList<>();

        ArrayList<String> downloadLinks = instance.getAppropiateDownloadLinks();

        if (downloadLinks.size() == 2) {
            links.add(createLink(String.format("Download (%s)", instance.namePaidVersion), downloadLinks.get(0)));
            links.add(createLink(String.format("Download (%s)", instance.nameFreeVersion), downloadLinks.get(1)));
        } else if (downloadLinks.size() == 1) {
            links.add(createLink("Download", downloadLinks.get(0)));
        }
        if (instance.getDonationLink() != null) {
            links.add(createLink("Donate", instance.getDonationLink()));
        }
        if (instance.getChangelogLink() != null) {
            links.add(createLink("Changelog", instance.getChangelogLink()));
        }

        final TextComponent placeholder = new TextComponent(" | ");
        placeholder.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        TextComponent text = new TextComponent("");

        Iterator<TextComponent> iterator = links.iterator();
        while (iterator.hasNext()) {
            TextComponent next = iterator.next();
            text.addExtra(next);
            if (iterator.hasNext()) {
                text.addExtra(placeholder);
            }
        }

        player.spigot().sendMessage(text);
    }
}
