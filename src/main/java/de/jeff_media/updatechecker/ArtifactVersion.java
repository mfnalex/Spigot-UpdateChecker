package de.jeff_media.updatechecker;

public interface ArtifactVersion extends Comparable<ArtifactVersion>
{
    int getMajorVersion();

    int getMinorVersion();

    int getIncrementalVersion();

    int getBuildNumber();

    String getQualifier();

    void parseVersion(final String p0);
}
