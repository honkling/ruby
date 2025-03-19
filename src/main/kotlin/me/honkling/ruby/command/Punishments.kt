package me.honkling.ruby.command

import kotlinx.coroutines.launch
import me.honkling.ruby.lib.mm
import me.honkling.ruby.punishment.punishmentBook
import me.honkling.ruby.scope
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun punishments(sender: CommandSender, args: List<String>) {
    if (sender !is Player)
        return sender.sendMessage("<failure>Only players can use this command.".mm)

    val isModerator = sender.hasPermission("ruby.punishments.other")

    scope.launch {
        if (args.isEmpty())
            return@launch punishmentBook(sender, sender, isModerator)

        val name = args[0]
        val target = Bukkit.getPlayer(name)
            ?: Bukkit.getOfflinePlayerIfCached(name)
            ?: Bukkit.getOfflinePlayer(name)

        if (!isModerator && target != sender)
            return@launch sender.sendMessage("<failure>You cannot view others' punishments.".mm)

        punishmentBook(sender, target, isModerator)
    }
}
