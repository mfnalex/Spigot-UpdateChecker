package de.jeff_media.updatechecker;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

class InternalUpdateCheckListener implements Listener {

    private final UpdateChecker instance;

    InternalUpdateCheckListener() {
        instance = UpdateChecker.getInstance();
    }

    @EventHandler
    public void notifyOnJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        if ((player.isOp() && instance.isNotifyOpsOnJoin()) || (instance.getNotifyPermission() != null && player.hasPermission(instance.getNotifyPermission()))) {
            Messages.printCheckResultToPlayer(player, false);
        }
    }

    @EventHandler
    public void onUpdateCheck(UpdateCheckEvent event) {

        if (instance.isNotifyRequesters()) {
            if (event.getRequesters() != null) {
                for (CommandSender commandSender : event.getRequesters()) {
                    if (commandSender instanceof Player) {
                        Messages.printCheckResultToPlayer((Player) commandSender, true);
                    } else {
                        Messages.printCheckResultToConsole(event);
                    }
                }
            }
        }
        if(event.getAutoUpdate() && event.getSuccess() == UpdateCheckSuccess.SUCCESS && !UpdateChecker.getInstance().isUsingLastestVersion() && !event.getLatestVersion().equals(UpdateChecker.getInstance().getLastAutoUpdateVersion())) {
            AutoUpdater.update(event.getRequesters());
        }
    }

}
