# SpigotUpdateChecker
<!--- Buttons start -->
<p align="center">
  <a href="https://www.spigotmc.org/threads/spigotupdatechecker-powerful-update-checker-with-only-one-line-of-code.500010/">
    <img src="https://static.jeff-media.com/img/button_spigotmc_thread.png" alt="SpigotMC Thread">
  </a>
  <a href="https://hub.jeff-media.com/javadocs/spigotupdatechecker/">
    <img src="https://static.jeff-media.com/img/button_javadocs.png" alt="Javadocs">
  </a>
  <a href="https://discord.jeff-media.com/">
    <img src="https://static.jeff-media.com/img/button_discord.png" alt="Discord">
  </a>
  <a href="https://paypal.me/mfnalex">
    <img src="https://static.jeff-media.com/img/button_donate.png">
  </a>
</p>
<!--- Buttons end -->

SpigotUpdateChecker is a simple but powerful library to add a perfectly working update checker to your plugins. **Scroll
all the way to the bottom for maven information, JavaDocs and a complete Example Plugin!**

<p align="center">
    <img src="https://api.jeff-media.de/img/updatechecker2.png">
</p>

Author: mfnalex

Contributors: MrNemo64

[Related SpigotMC thread](https://www.spigotmc.org/threads/powerful-update-checker-with-only-one-line-of-code.500010/)

## Features

You can issue manual and repeated update checks and send the result as ingame message to specific players and/or have
them printed to the console.

All checks are done asynchronously. When the check is done, a custom event is called. The update checker itself listens
to it and can automatically notify Operators on Join or players with a specific permission.

Of course, you can also just listen to the UpdateCheckEvent yourself to do whatever you like once a new version is
detected.

It is also possible to define two download links if your plugin is available as both, a free and paid version, and you
can add links to your donation page and changelog.

You can either provide all those links, including to the API endpoint where the latest version is checked yourself, or
just provide the SpigotMC Resource ID of your plugin for the Update Checker to get those links automatically.

**Supported API endpoints** to retrieve version information:

- SpigotMC API
- Spiget API
- Polymart API
- GitHub Release Tags
- Your own custom URL pointing to a text file

## Maven

The UpdateChecker is available in my public repository:

```xml
<repositories>
    <repository>
        <id>jeff-media-public</id>
        <url>https://hub.jeff-media.com/nexus/repository/jeff-media-public/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.jeff_media</groupId>
        <artifactId>SpigotUpdateChecker</artifactId>
        <version>2.2.0</version> <!-- Check on GitHub for the latest version -->
        <scope>compile</scope>
    </dependency>
</dependencies>
```

Please note that you will also have to shade and relocate the UpdateChecker into your .jar file:

```xml
<build>
   ...
    <plugins>
        ...
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.1</version>
            <configuration>
                <relocations>
                    <!-- Using the maven-shade-plugin to shade and relocate the UpdateChecker -->
                    <!-- Replace "your.package" with your plugin's package name -->
                    <relocation>
                        <pattern>com.jeff_media.updatechecker</pattern>
                        <shadedPattern>your.package.updatechecker</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Failing to relocate the package will make the UpdateChecker throw an exception, so RELOCATE IT!**

## Example

To get a working UpdateChecker, this is already enough:

```java
public class MyPlugin extends JavaPlugin {
    // To get the Resource ID, look at the number at the end of the URL of your plugin's SpigotMC page
    private static final int SPIGOT_RESOURCE_ID = 59773;

    @Override
    public void onEnable() {
        new UpdateChecker(this, UpdateCheckSource.SPIGOT, SPIGOT_RESOURCE_ID) // You can also use Spiget instead of Spigot - Spiget's API is usually much faster up to date.
                .checkEveryXHours(24) // Check every 24 hours
                .checkNow(); // And check right now
    }
}
```

The code above will print a message to the console once a new version is available and also send a message to every OP
joining the server. If no new version is found, no message will be sent.

Of course, there are many more options you can use. For example:

```java
import com.jeff_media.updatechecker.UpdateCheckSource;

public class MyPlugin extends JavaPlugin {
    private static final int SPIGOT_RESOURCE_ID = 59773;

    @Override
    public void onEnable() {
        new UpdateChecker(this, UpdateCheckSource.CUSTOM_URL, "https://api.jeff-media.de/chestsort/latest-version.txt") // A link to a URL that contains the latest version as String
                .setDownloadLink("https://www.chestsort.de") // You can either use a custom URL or the Spigot Resource ID
                .setDonationLink("https://paypal.me/mfnalex")
                .setChangelogLink(SPIGOT_RESOURCE_ID) // Same as for the Download link: URL or Spigot Resource ID
                .setNotifyOpsOnJoin(true) // Notify OPs on Join when a new version is found (default)
                .setNotifyByPermissionOnJoin("myplugin.updatechecker") // Also notify people on join with this permission
                .setUserAgent(new UserAgentBuilder().addPluginNameAndVersion())
                .checkEveryXHours(0.5) // Check every 30 minutes
                .checkNow(); // And check right now
    }
}
```

## Differentiating between free and paid versions

Now imagine you have two versions of your plugin. One free version and a paid version with extra features, like my
AngelChest Free and AngelChest Plus plugin. If both plugins share the same codebase and only decide on runtime whether
to unlock the premium features, you can easily get something like this working:

Users of the free version will get links to both versions, so they can see the advantages of your paid version, while we
don't want to send the free version link to users who already bought the paid version. The Update Checker uses
SpigotMC's [Premium Resource Placeholders](https://www.spigotmc.org/wiki/premium-resource-placeholders-identifiers/)
to detect whether a server is using the paid version, but you can also override this detection using
*UpdateChecker#setUsingPaidVersion(boolean)*.

To achieve this, you can just do this:

```java
import com.jeff_media.updatechecker.UpdateCheckSource;

public class MyPlugin extends JavaPlugin {
    private static final int ANGELCHEST_FREE = 60383;
    private static final int ANGELCHEST_PLUS = 88214;
    private final boolean usingPaidVersion = howEverYouDetectIt();

    @Override
    public void onEnable() {
        new UpdateChecker(this, UpdateCheckSource.CUSTOM_URL, "https://api.jeff-media.de/angelchest/latest-version.txt")
                .setFreeDownloadLink(ANGELCHEST_FREE)
                .setPaidDownloadLink(ANGELCHEST_PLUS)
                .setNameFreeVersion("Free") // Optional. It's the suffix for the download links
                .setNamePaidVersion("Plus") // when both links are shown.
                .checkNow();
    }
}
```

Users of the free version will now see both links:

<p align="center">
    <img src="https://api.jeff-media.de/img/updatechecker1.png">
</p>

Users of the paid version will however only get the paid version's download link, just like in the screenshots at the
top.

## Using Consumers

You can use Consumers to change the behaviour of the Update Checker.

```java
import com.jeff_media.updatechecker.UpdateCheckSource;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new UpdateChecker(this, UpdateCheckSource.CUSTOM_URL, "https://api.jeff-media.de/angelchest/latest-version.txt")
                .setDownloadLink("https://www.chestsort.de")
                .onSuccess((commandSenders, latestVersion) -> {
                    for (CommandSender sender : commandSenders) {
                        sender.sendMessage("This code will run after the update check was successfull.");
                    }
                })
                .onFail((commandSenders, exception) -> {
                    for (CommandSender sender : commandSenders) {
                        sender.sendMessage("This code will run after the update check failed.");
                    }
                })
                .setNotifyRequesters(false) // Do not show the default messages, instead only run our custom consumers
                .checkNow();
    }
}
```

## Building & Obfuscation

The .jar published in my public repository has been run through allatori to decrease the file size by about 30%.
It does not affect performance in any negative way.
The used obfuscation settings are allowed on SpigotMC for both free and paid plugins.

**If you like to build it yourself**, just comment out the maven-exec-plugin part in your pom.xml
(currently lines 167 to 192).

## JavaDocs and Example plugin

JavaDocs are available here: https://hub.jeff-media.com/javadocs/spigotupdatechecker/

Example plugin: https://github.com/JEFF-Media-GbR/Spigot-UpdateChecker-Example

## Other libraries by me

### [CustomBlockData](https://github.com/JEFF-Media-GbR/CustomBlockData)
My **CustomBlockData** library provides a PersistentDataContainer for every block in your world - easily save EVERY information you like inside blocks, without any external storage needed!

### [MorePersistentDataTypes](https://github.com/JEFF-Media-GbR/MorePersistentDataTypes)
Adds a ton of new **PersistentDataTypes** to use with Bukkit's PersistentDataContainer.

## Discord

Feel free to join my Discord for help.

<a href="https://discord.jeff-media.de"><img src="https://api.jeff-media.de/img/discord1.png"></a>
