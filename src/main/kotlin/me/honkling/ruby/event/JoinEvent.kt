package me.honkling.ruby.event

import me.honkling.ruby.lib.hasPermission
import me.honkling.ruby.punishment.PunishmentType
import me.honkling.ruby.punishment.getPunishments
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

object JoinEvent : Listener {
    @EventHandler
    fun onJoin(event: AsyncPlayerPreLoginEvent) {
        val punishments = getPunishments(event.uniqueId)
        val punishment = punishments.find { it.isActive && it.reason.type == PunishmentType.Ban }

        if (punishment != null && !hasPermission(event.uniqueId, "ruby.punish")) {
            event.kickMessage(punishment.kickMessage)
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_BANNED
        }
    }
}