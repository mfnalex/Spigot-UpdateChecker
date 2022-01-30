package de.jeff_media.updatechecker;

/**
 * Represents the source from where to fetch update information.
 */
public enum UpdateCheckSource {
    /**
     * SpigotMC API. Trustworthy, but slow. Requires the SpigotMC resource ID as parameter (the number at the end of your plugin's SpigotMC URL)
     */
    SPIGOT,
    /**
     * Spiget API. Not official, but faster than SpigotMC API. Requires the SpigotMC resource ID as parameter (the number at the end of your plugin's SpigotMC URL)
     */
    SPIGET,
    /**
     * Custom link on where to fetch update checking information. The linked file must be a plaintext file containing nothing except for the latest version string.
     */
    CUSTOM_URL
}