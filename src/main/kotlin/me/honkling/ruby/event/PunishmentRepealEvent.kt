package me.honkling.ruby.event

import me.honkling.ruby.punishment.Punishment
import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

private val handlerList = HandlerList()

class PunishmentRepealEvent(val moderator: CommandSender, val punishment: Punishment) : Event(), Cancellable {
    private var cancelled = false

    companion object {
        @JvmStatic
        fun getHandlerList() = handlerList
    }

    override fun getHandlers() = handlerList

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}