/*
 * Copyright (c) 2022 Alexander Majka (mfnalex), JEFF Media GbR
 * Website: https://www.jeff-media.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jeff_media.updatechecker;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

class UpdateCheckerMessages {

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
            if (UpdateChecker.getInstance().isSuppressUpToDateMessage()) return;
            plugin.getLogger().info(String.format("You are using the latest version of %s.", plugin.getName()));
            return;
        }

        List<String> lines = new ArrayList<>();

        lines.add(String.format("There is a new version of %s available!", plugin.getName()));
        lines.add(" ");
        lines.add(String.format("Your version:   %s%s", instance.isColoredConsoleOutput() ? MessageColor.VERSION_OLDER
            : "", event.getUsedVersion()));
        lines.add(String.format("Latest version: %s%s", instance.isColoredConsoleOutput() ? MessageColor.VERSION_NEWER
            : "", event.getLatestVersion()));

        List<String> downloadLinks = instance.getAppropriateDownloadLinks();

        if (downloadLinks.size() > 0) {
            lines.add(" ");
            lines.add("Please update to the newest version.");
            lines.add(" ");
            if (downloadLinks.size() == 1) {
                lines.add("Download:");
                lines.add("  " + downloadLinks.get(0));
            } else if (downloadLinks.size() == 2) {
                lines.add(String.format("Download (%s)", instance.getNamePaidVersion()));
                lines.add("  " + downloadLinks.get(0));
                lines.add(" ");
                lines.add(String.format("Download (%s)", instance.getNameFreeVersion()));
                lines.add("  " + downloadLinks.get(1));
            }
        }

        printNiceBoxToConsole(plugin.getLogger(), lines);
    }

    protected static void printCheckResultToPlayer(Player player, boolean showMessageWhenLatestVersion) {
        UpdateChecker instance = UpdateChecker.getInstance();
        if (instance.getLastCheckResult() == UpdateCheckResult.NEW_VERSION_AVAILABLE) {
            player.sendMessage(MessageColor.STANDARD + "There is a new version of " + MessageColor.HIGHLIGHT + instance.getPlugin().getName() + MessageColor.STANDARD + " available.");
            sendLinks(player);
            player.sendMessage(MessageColor.NOTE + "Latest version: " + MessageColor.VERSION_NEWER + instance.getLatestVersion() + MessageColor.NOTE + " | Your version: " + MessageColor.VERSION_OLDER
                + instance.getUsedVersion());
            player.sendMessage("");
        } else if (instance.getLastCheckResult() == UpdateCheckResult.UNKNOWN) {
            player.sendMessage(MessageColor.HIGHLIGHT + instance.getPlugin().getName() + MessageColor.SEVERE
                + " could not check for updates.");
        } else {
            if (showMessageWhenLatestVersion) {
                player.sendMessage(MessageColor.SUCCESS + "You are running the latest version of " + MessageColor.HIGHLIGHT + instance.getPlugin().getName());
            }
        }
    }

    private static void printNiceBoxToConsole(Logger logger, List<String> lines) {
        int longestLine = 0;
        for (String line : lines) {
            longestLine = Math.max(line.length(), longestLine);
        }
        longestLine += 2;
        if (longestLine > 120) longestLine = 120;
        longestLine += 2;
        StringBuilder dash = new StringBuilder(longestLine);
        Stream.generate(() -> "*").limit(longestLine).forEach(dash::append);

        logger.log(Level.WARNING, dash.toString());
        for (String line : lines) {
            logger.log(Level.WARNING, ("*" + " ") + line);
        }
        logger.log(Level.WARNING, dash.toString());
    }

    private static void sendLinks(@NotNull final Player... players) {

        UpdateChecker instance = UpdateChecker.getInstance();

        List<TextComponent> links = new ArrayList<>();

        List<String> downloadLinks = instance.getAppropriateDownloadLinks();

        if (downloadLinks.size() == 2) {
            links.add(createLink(String.format("Download (%s)", instance.getNamePaidVersion()), downloadLinks.get(0)));
            links.add(createLink(String.format("Download (%s)", instance.getNameFreeVersion()), downloadLinks.get(1)));
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

        for (Player player : players) {
            player.spigot().sendMessage(text);
        }
    }
}
