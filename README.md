# SpigotUpdateChecker
The SpigotUpdateChecker is a simple library for you to add a perfectly working update checker to your plugins. **Scroll all the way to the bottom for maven information, JavaDocs and a complete Example Plugin!**

![Screenshot](https://api.jeff-media.de/img/updatecheckeringame.png)
![Screenshot](https://api.jeff-media.de/img/updatecheckerconsole.png)

## Features
The SpigotUpdateChecker can automatically notify Operators on Join or players with a specific permission, you can issue manual checks and send the result ingame to specific players or have them printed to the console. You can define two download links if your plugin is available as free and paid version and add links to your donation page and changelog.

## Example
To get a working UpdateChecker, this is already enough:

```java
public class MyPlugin extends JavaPlugin {
    // To get the Resource ID, look at the number at the end of the URL of your plugin's SpigotMC page
    private static final int SPIGOT_RESOURCE_ID = 59773;

    @Override
    public void onEnable() {
        UpdateChecker.init(this, SPIGOT_RESOURCE_ID)
                .checkEveryXHours(24) // Check every 24 hours
                .checkNow(); // And check right now
    }
}
```

Of course, there are many more options you can use. For example:

```java
public class MyPlugin extends JavaPlugin {
    private static final int SPIGOT_RESOURCE_ID = 59773;

    @Override
    public void onEnable() {
        UpdateChecker.init(this, "https://api.jeff-media.de/chestsort/latest-version.txt") // A link to a URL that contains the latest version as String
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
Now imagine you have two versions of your plugin. One free version and a paid version with extra features, like my AngelChest Free and AngelChest Plus plugin. If both plugins share the same codebase and only decide on runtime whether to unlock the premium features, you can easily get something like this working. Users of the free version will get links to both versions, so they can see the advantages of your paid version, while we don't want to send the free version link to users who already bought the paid version. To achieve this, you can just do this:

```java
public class MyPlugin extends JavaPlugin {
    private static final int ANGELCHEST_FREE = 60383;
    private static final int ANGELCHEST_PLUS = 88214;
    private boolean usingPaidVersion = howEverYouDetectIt();
    
    @Override
    public void onEnable() {
        UpdateChecker.init(this, "https://api.jeff-media.de/angelchest/latest-version.txt")
                .setFreeDownloadLink(ANGELCHEST_FREE)
                .setPaidDownloadLink(ANGELCHEST_PLUS)
                .setNameFreeVersion("Free") // Optional. It's the suffix for the download links
                .setNamePaidVersion("Plus") // when both links are shown.
                .setUsingPaidVersion(usingPaidVersion)
                .checkNow();
    }
}
```
Users of the free version will now see both links:

![Screenshot](https://api.jeff-media.de/img/updatecheckeringamefreeversion.png)

Users of the paid version will however only get the paid version's download link.

## Maven
```xml
<repositories>
    <repository>
        <id>jeff-media-gbr</id>
        <url>https://repo.jeff-media.de/maven2/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>de.jeff_media</groupId>
        <artifactId>SpigotUpdateChecker</artifactId>
        <version>1.0.0</version>
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
                    <!-- Replace "my.example.plugin" with your plugin's package name -->
                    <relocation>
                        <pattern>de.jeff_media.updatechecker</pattern>
                        <shadedPattern>my.example.plugin.updatechecker</shadedPattern>
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

## JavaDocs and Example plugin
JavaDocs are available here: https://repo.jeff-media.de/javadocs/SpigotUpdateChecker/
Example plugin: https://github.com/JEFF-Media-GbR/Spigot-UpdateChecker-Example