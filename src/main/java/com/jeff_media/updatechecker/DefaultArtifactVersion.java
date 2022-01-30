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

import org.jetbrains.annotations.NotNull;

class DefaultArtifactVersion implements ArtifactVersion {
    private ComparableVersion comparable;

    public DefaultArtifactVersion(final String version) {
        this.parseVersion(version);
    }

    @Override
    public int hashCode() {
        return 11 + this.comparable.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || (other instanceof ArtifactVersion && this.compareTo((ArtifactVersion)other) == 0);
    }

    @Override
    public int compareTo(final @NotNull ArtifactVersion otherVersion) {
        if (otherVersion instanceof DefaultArtifactVersion) {
            return this.comparable.compareTo(((DefaultArtifactVersion)otherVersion).comparable);
        }
        return this.compareTo(new DefaultArtifactVersion(otherVersion.toString()));
    }

    public final void parseVersion(final String version) {
        this.comparable = new ComparableVersion(version);
    }

    @Override
    public String toString() {
        return this.comparable.toString();
    }
}