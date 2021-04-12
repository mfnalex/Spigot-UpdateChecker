package de.jeff_media.updatechecker;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class VersionUtils {

    /*public static void main(String[] args) {
        String me = "1.1.1-b";
        String ne = "1.1.1-c";

        System.out.println(isNewVersionReallyNewer(me,ne) ? "Update available" : "No update available");
    }*/

    public static boolean isNewVersionReallyNewer(String usedVersion, String latestVersion) {
        DefaultArtifactVersion used = new DefaultArtifactVersion(usedVersion);
        DefaultArtifactVersion latest = new DefaultArtifactVersion(latestVersion);
        return used.compareTo(latest) < 0;
    }
}
