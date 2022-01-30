package de.jeff_media.updatechecker;

import org.apache.commons.lang.math.NumberUtils;

import java.util.*;

public class DefaultArtifactVersion implements ArtifactVersion {
    private Integer majorVersion;
    private Integer minorVersion;
    private Integer incrementalVersion;
    private Integer buildNumber;
    private String qualifier;
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
    public int compareTo(final ArtifactVersion otherVersion) {
        if (otherVersion instanceof DefaultArtifactVersion) {
            return this.comparable.compareTo(((DefaultArtifactVersion)otherVersion).comparable);
        }
        return this.compareTo((ArtifactVersion)new DefaultArtifactVersion(otherVersion.toString()));
    }

    public final void parseVersion(final String version) {
        this.comparable = new ComparableVersion(version);
        final int index = version.indexOf(45);
        String part2 = null;
        String part3;
        if (index < 0) {
            part3 = version;
        }
        else {
            part3 = version.substring(0, index);
            part2 = version.substring(index + 1);
        }
        if (part2 != null) {
            if (part2.length() == 1 || !part2.startsWith("0")) {
                this.buildNumber = tryParseInt(part2);
                if (this.buildNumber == null) {
                    this.qualifier = part2;
                }
            }
            else {
                this.qualifier = part2;
            }
        }
        if (!part3.contains(".") && !part3.startsWith("0")) {
            this.majorVersion = tryParseInt(part3);
            if (this.majorVersion == null) {
                this.qualifier = version;
                this.buildNumber = null;
            }
        }
        else {
            boolean fallback = false;
            final StringTokenizer tok = new StringTokenizer(part3, ".");
            if (tok.hasMoreTokens()) {
                this.majorVersion = getNextIntegerToken(tok);
                if (this.majorVersion == null) {
                    fallback = true;
                }
            }
            else {
                fallback = true;
            }
            if (tok.hasMoreTokens()) {
                this.minorVersion = getNextIntegerToken(tok);
                if (this.minorVersion == null) {
                    fallback = true;
                }
            }
            if (tok.hasMoreTokens()) {
                this.incrementalVersion = getNextIntegerToken(tok);
                if (this.incrementalVersion == null) {
                    fallback = true;
                }
            }
            if (tok.hasMoreTokens()) {
                this.qualifier = tok.nextToken();
                fallback = NumberUtils.isDigits(this.qualifier);
            }
            if (part3.contains("..") || part3.startsWith(".") || part3.endsWith(".")) {
                fallback = true;
            }
            if (fallback) {
                this.qualifier = version;
                this.majorVersion = null;
                this.minorVersion = null;
                this.incrementalVersion = null;
                this.buildNumber = null;
            }
        }
    }

    private static Integer getNextIntegerToken(final StringTokenizer tok) {
        final String s = tok.nextToken();
        if (s.length() > 1 && s.startsWith("0")) {
            return null;
        }
        return tryParseInt(s);
    }

    private static Integer tryParseInt(final String s) {
        if (!NumberUtils.isDigits(s)) {
            return null;
        }
        try {
            final long longValue = Long.parseLong(s);
            if (longValue > 2147483647L) {
                return null;
            }
            return (int)longValue;
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return this.comparable.toString();
    }
}