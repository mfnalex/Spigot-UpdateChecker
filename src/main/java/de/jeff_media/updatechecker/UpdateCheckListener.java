package de.jeff_media.updatechecker;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

class UpdateCheckListener implements Listener {

    private final UpdateChecker instance;

    UpdateCheckListener() {
        instance = UpdateChecker.getInstance();
    }

    @EventHandler
    public void notifyOnJoin(PlayerJoinEvent playerJoinEvent) {
        if (!instance.isCheckedAtLeastOnce()) return;
        Player player = playerJoinEvent.getPlayer();
        if ((player.isOp() && instance.isNotifyOpsOnJoin()) || (instance.getNotifyPermission() != null && player.hasPermission(instance.getNotifyPermission()))) {
            UpdateCheckerMessages.printCheckResultToPlayer(player, false);
        }
    }

    @EventHandler
    public void onUpdateCheck(UpdateCheckEvent event) {
        if (!instance.isCheckedAtLeastOnce()) return;
        if (!instance.isNotifyRequesters()) return;
        if (event.getRequesters() == null) return;
        for (CommandSender commandSender : event.getRequesters()) {
            if (commandSender instanceof Player) {
                UpdateCheckerMessages.printCheckResultToPlayer((Player) commandSender, true);
            } else {
                UpdateCheckerMessages.printCheckResultToConsole(event);
            }

        }
    }

}
