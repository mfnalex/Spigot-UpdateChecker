/*
 * Copyright (c) 2022 Alexander Majka (mfnalex), JEFF Media GbR
 * Website: https://www.jeff-media.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jeff_media.updatechecker;

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

    @SuppressWarnings("unused")
    @EventHandler
    public void notifyOnJoin(PlayerJoinEvent playerJoinEvent) {
        if (!instance.isCheckedAtLeastOnce()) return;
        Player player = playerJoinEvent.getPlayer();
        if ((player.isOp() && instance.isNotifyOpsOnJoin()) || (instance.getNotifyPermission() != null && player.hasPermission(instance.getNotifyPermission()))) {
            UpdateCheckerMessages.printCheckResultToPlayer(player, false);
        }
    }

    @SuppressWarnings("unused")
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
