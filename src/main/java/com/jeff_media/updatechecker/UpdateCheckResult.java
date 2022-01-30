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

/**
 * Represents whether a new version is available or not
 */
public enum UpdateCheckResult {
    /**
     * Result when a new version is available
     */
    NEW_VERSION_AVAILABLE,
    /**
     * Result when the plugin is already the latest version
     */
    RUNNING_LATEST_VERSION,
    /**
     * Result when the update check failed, or the version could not be compared
     */
    UNKNOWN
}
