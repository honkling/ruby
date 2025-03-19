package me.honkling.ruby.event

import io.papermc.paper.event.player.AsyncChatEvent
import me.honkling.ruby.punishment.PunishmentType
import me.honkling.ruby.punishment.getPunishments
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ChatEvent : Listener {
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val punishments = getPunishments(player)
        val punishment = punishments.find { it.isActive && it.reason.type == PunishmentType.Mute }

        if (punishment != null && !player.hasPermission("ruby.punish")) {
            event.player.sendMessage(punishment.chatMessage)
            event.isCancelled = true
        }
    }
}