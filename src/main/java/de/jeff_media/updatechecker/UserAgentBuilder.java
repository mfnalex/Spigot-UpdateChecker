package de.jeff_media.updatechecker;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Creates a User-Agent string
 */
public class UserAgentBuilder {

    private final UpdateChecker instance = UpdateChecker.getInstance();
    private final Plugin plugin = instance.getPlugin();
    private final StringBuilder builder = new StringBuilder("JEFF-Media-GbR-SpigotUpdateChecker/").append(UpdateChecker.VERSION);
    private final ArrayList<String> list = new ArrayList<>();

    public static UserAgentBuilder getDefaultUserAgent() {
        return new UserAgentBuilder().addPluginNameAndVersion().addServerVersion().addBukkitVersion();
    }

    /**
     * Adds the Bukkit version
     * @return
     */
    public UserAgentBuilder addBukkitVersion() {
        list.add("BukkitVersion/" + Bukkit.getBukkitVersion());
        return this;
    }

    /**
     * Adds a custom Key/Value string
     * @param key Key
     * @param value Value
     * @return
     */
    public UserAgentBuilder addKeyValue(String key, String value) {
        list.add(key + "/" + value);
        return this;
    }

    /**
     * Adds a custom string
     * @param text
     * @return
     */
    public UserAgentBuilder addPlaintext(String text) {
        list.add(text);
        return this;
    }

    /**
     * Adds the plugin and version, e.g. AngelChest/3.11.0
     * @return
     */
    public UserAgentBuilder addPluginNameAndVersion() {
        list.add(plugin.getName() + "/" + plugin.getDescription().getVersion());
        return this;
    }

    /**
     * Adds the Server version
     * @return
     */
    public UserAgentBuilder addServerVersion() {
        list.add("ServerVersion/" + Bukkit.getVersion());
        return this;
    }

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
