package de.jeff_media.updatechecker;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class AutoUpdater {

    protected static boolean downloadJar(String url) {
        File tempFile = getTempFile(getPluginJar().getName()).getAbsoluteFile();
        System.out.println(tempFile.getAbsolutePath());
        if(!Bukkit.getUpdateFolderFile().isDirectory()) {
            Bukkit.getUpdateFolderFile().mkdirs();
        }
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected @Nullable
    static File getPluginJar() {
        try {
            return new File(AutoUpdater.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static File getTempFile(String fileName) {
        return new File(Bukkit.getUpdateFolderFile(), fileName);
    }

    public static void update(CommandSender... commandSenders) {
        UpdateChecker updateChecker = UpdateChecker.getInstance();
        boolean success = downloadJar(updateChecker.getAutoUpdateLink().replaceAll("\\{version}",updateChecker.getLatestVersion()).replaceAll("\\{name}", updateChecker.getPlugin().getName()));
        for (CommandSender commandSender : commandSenders) {
            Messages.printAutoUpdateResult(commandSender, success);
        }
        if(success) {
            updateChecker.setLastAutoUpdateVersion(updateChecker.getLatestVersion());
        }
    }
}
