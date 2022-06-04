package com.jeff_media.updatechecker;

import java.util.Objects;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * This enum stores a list of colors used for the update checker's messages.
 */
public enum MessageColor {

    SEVERE(ChatColor.RED),
    HIGHLIGHT(ChatColor.GOLD),
    NOTE(ChatColor.DARK_GRAY),
    STANDARD(ChatColor.GRAY),
    SUCCESS(ChatColor.GREEN),
    VERSION_NEWER(ChatColor.GREEN),
    VERSION_OLDER(ChatColor.RED);

    private final ChatColor defaultColor;
    private ChatColor color;

    /**
     * Declare a new message color type
     *
     * @param defaultColor color to be used by default
     */
    MessageColor(final @NotNull ChatColor defaultColor) {
        Objects.requireNonNull(defaultColor, "defaultColor");

        this.defaultColor = defaultColor;
        this.color = defaultColor;
    }

    /**
     * Used to more concisely concatenate message colors
     *
     * @return color, in string form
     */
    @Override
    @NotNull
    public String toString() {
        return getColor().toString();
    }

    /**
     * Get the default color used for this message type
     *
     * @return default color
     */
    @SuppressWarnings("unused")
    @NotNull
    public ChatColor getDefaultColor() {
        return defaultColor;
    }

    /**
     * Get the color used for this message type
     *
     * @return color
     */
    @NotNull
    public ChatColor getColor() {
        return color;
    }

    /**
     * Change the color for this message type
     *
     * @param color color to use
     */
    @SuppressWarnings("unused")
    public void setColor(final @NotNull ChatColor color) {
        this.color = Objects.requireNonNull(color, "color");
    }

}
