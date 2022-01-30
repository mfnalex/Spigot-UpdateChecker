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

import org.bukkit.plugin.Plugin;

/**
 * Represents the source from where to fetch update information.
 */
public enum UpdateCheckSource {
    /**
     * SpigotMC API. Trustworthy, but slow. Requires the SpigotMC resource ID (the number at the end of your plugin's SpigotMC URL) as parameter in {@link UpdateChecker#init(Plugin, UpdateCheckSource, String)}.
     */
    SPIGOT,
    /**
     * Spiget API. Not official, but faster than SpigotMC API. Requires the SpigotMC resource ID (the number at the end of your plugin's SpigotMC URL) as parameter in {@link UpdateChecker#init(Plugin, UpdateCheckSource, String)}.
     */
    SPIGET,
    /**
     * Custom link on where to fetch update checking information. Requires an HTTP or HTTPS URL as parameter in {@link UpdateChecker#init(Plugin, UpdateCheckSource, String)}. The linked file must be a plaintext file containing nothing except for the latest version string.
     */
    CUSTOM_URL
}