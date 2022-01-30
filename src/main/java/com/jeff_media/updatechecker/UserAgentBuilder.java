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

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a User-Agent string. Always starts with "JEFF-Media-GbR-SpigotUpdateChecker/[version]" followed by all added parameters.
 */
@SuppressWarnings("unused")
public class UserAgentBuilder {

    private final StringBuilder builder = new StringBuilder("JEFF-Media-GbR-SpigotUpdateChecker/").append(UpdateChecker.VERSION);
    private final UpdateChecker instance = UpdateChecker.getInstance();
    private final List<String> list = new ArrayList<>();
    private final Plugin plugin = instance.getPlugin();

    /**
     * Returns the default User-Agent, consisting of Plugin name and version, Server version and Bukkit version
     *
     * @return UserAgentBuilder instance
     */
    public static UserAgentBuilder getDefaultUserAgent() {
        return new UserAgentBuilder().addPluginNameAndVersion().addServerVersion().addBukkitVersion();
    }

    /**
     * Adds the Bukkit version. For example "BukkitVersion/1.16.5-R0.1-SNAPSHOT"
     *
     * @return UserAgentBuilder instance
     */
    public UserAgentBuilder addBukkitVersion() {
        list.add("BukkitVersion/" + Bukkit.getBukkitVersion());
        return this;
    }

    /**
     * Adds a custom Key/Value string. For example "foo/bar"
     *
     * @param key   Key
     * @param value Value
     * @return UserAgentBuilder instance
     */
    public UserAgentBuilder addKeyValue(String key, String value) {
        list.add(key + "/" + value);
        return this;
    }

    /**
     * Adds a custom string. For example "foo"
     *
     * @param text Custom string
     * @return UserAgentBuilder instance
     */
    public UserAgentBuilder addPlaintext(String text) {
        list.add(text);
        return this;
    }

    /**
     * Adds the plugin and version. For example "AngelChest/3.11.0"
     *
     * @return UserAgentBuilder instance
     */
    public UserAgentBuilder addPluginNameAndVersion() {
        list.add(plugin.getName() + "/" + plugin.getDescription().getVersion());
        return this;
    }

    /**
     * Adds the Server version. For example "ServerVersion/git-Paper-584 (MC: 1.16.5)"
     *
     * @return UserAgentBuilder instance
     */
    public UserAgentBuilder addServerVersion() {
        list.add("ServerVersion/" + Bukkit.getVersion());
        return this;
    }

    /**
     * Returns the Spigot User ID of the user who downloaded the plugin. Only works for paid plugins from SpigotMC.org. For example "SpigotUID/175238"
     *
     * @return UserAgentBuilder instance
     */
    public UserAgentBuilder addSpigotUserId() {
        String uid = instance.isUsingPaidVersion() ? instance.getSpigotUserId() : "none";
        list.add("SpigotUID/" + uid);
        return this;
    }

    /**
     * Returns whether this copy of the .jar is a paid plugin downloaded from SpigotMC.org. For example "Paid/true"
     *
     * @return UserAgentBuilder instance
     */
    public UserAgentBuilder addUsingPaidVersion() {
        list.add("Paid/" + instance.isUsingPaidVersion());
        return this;
    }

    /**
     * Converts this UserAgentBuilder instance to a UserAgent string
     *
     * @return UserAgent string
     */
    protected String build() {
        if (list.size() > 0) {
            builder.append(" (");
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                builder.append(it.next());
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(")");
        }
        return builder.toString();
    }

}
