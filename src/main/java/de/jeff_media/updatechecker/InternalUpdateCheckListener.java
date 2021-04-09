package de.jeff_media.updatechecker;

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
        if ((player.isOp() && instance.notifyOpsOnJoin) || (instance.notifyPermission != null && player.hasPermission(instance.notifyPermission))) {
            Messages.printCheckResultToPlayer(player,false);
        }
    }

    @EventHandler
    public void onUpdateCheck(UpdateCheckEvent event) {
        if (!instance.notifyRequesters) return;
        if (event.getRequesters() == null) return;
        for (CommandSender commandSender : event.getRequesters()) {
            if (commandSender instanceof Player) {
                Messages.printCheckResultToPlayer((Player) commandSender,true);
            } else {
                Messages.printCheckResultToConsole(event);
            }

        }
    }

}
