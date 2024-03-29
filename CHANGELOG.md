## 2.3.1
- Added option to show a support link (thanks @lokka30)

## 2.3.0
- Switched to new SpigotMC API

## 2.2.0
- Added support for Polymart 
- Fixed console not showing the custom plugin name when using setNamePaidVersion and setNameFreeVersion

## 2.1.0

Added support for GitHub Release tags.

Example:

```java
UpdateChecker updateChecker = new UpdateChecker(myPugin, UpdateCheckSource.GITHUB_RELEASE_TAG, "JEFF-Media-GbR/ChestSort");
```

## 2.0.0
- Added support for Spiget
- Switched from static init() methods and static getInstance() method to a regular constructor. Since more than one UpdateChecker instances can exist at the same time, you are expected to keep track of the instance(s) you created yourself.
- Changed Maven GroupID to com.jeff_media and Java Package name to com.jeff_media.updatechecker

This is how to create a new UpdateChecker as of 2.0.0 now:
```java
// Get version information from SpigotMC.org ("12345" is your SpigotMC resource ID):
UpdateChecker updateChecker = new UpdateChecker(myPlugin, UpdateCheckSource.SPIGOT, "12345");

// Get version information from Spiget.org ("12345" is your SpigotMC resource ID):
UpdateChecker updateChecker = new UpdateChecker(myPlugin, UpdateCheckSource.SPIGET, "12345");

// Get version information from an HTTP(S) URL:
UpdateChecker updateChecker = new UpdateChecker(myPlugin, UpdateCheckSource.SPIGOT, "https://api.jeff-media.com/chestsort/latest-version.txt");
```

## 1.3.2

Attached javadocs and sources

## 1.3.1

Added UpdateChecker#setUsedVersion(String)

## 1.3.0

Reduced file size from 248KB to 33KB by getting rid of the maven-artifact dependency.

## 1.2.4

Changed relocation of dependencies in pom.xml

## 1.2.3

Changed console output formatting a tiny bit

## 1.2.2

Fixed message "Could not check for updates" being shown to OPs on join when an update check hasn't been done yet

## 1.2.1

Fixed `checkNow()`, `checkNow(CommandSender...)` and `stop()` not returning the instance

## 1.2.0

Detects whether the currently used version is newer than the version found by the UpdateChecker

## 1.1.0

Added support for custom tasks to run after update checks

## 1.0.0

Initial release