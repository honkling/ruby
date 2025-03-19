package me.honkling.ruby.command

import kotlinx.coroutines.launch
import me.honkling.ruby.config.punishmentsToml
import me.honkling.ruby.lib.hasPermission
import me.honkling.ruby.lib.mm
import me.honkling.ruby.lib.removeFirstOrNull
import me.honkling.ruby.lib.safeSlice
import me.honkling.ruby.punishment.calculateDuration
import me.honkling.ruby.punishment.issuePunishment
import me.honkling.ruby.punishment.reasonBook
import me.honkling.ruby.scope
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun punish(sender: CommandSender, args: MutableList<String>) {
    val name = args.removeFirstOrNull()
        ?: return sender.sendMessage("<failure>Please provide a username.".mm)

    scope.launch {
        val target = Bukkit.getPlayer(name)
            ?: Bukkit.getOfflinePlayerIfCached(name)
            ?: Bukkit.getOfflinePlayer(name)

        if (target.hasPermission("ruby.punish"))
            return@launch sender.sendMessage("<failure>You can't punish moderators.".mm)

        val reason = punishmentsToml.reasons.values.find { args.joinToString(" ").startsWith(it.name, true) }

        if (args.isNotEmpty()) {
            if (reason == null)
                return@launch sender.sendMessage("<failure>That isn't a valid reason.".mm)

            val notes = args.safeSlice(reason.name.split(" ").size + 1..<args.size).joinToString(" ")
            val duration = reason.duration?.let { calculateDuration(target, reason) }

            val result = issuePunishment(sender, target, reason, duration, notes)
            if (result.isFailure)
                sender.sendMessage("<failure>${result.exceptionOrNull()?.message ?: "An error occurred."}".mm)

            return@launch
        }

        if (sender !is Player)
            return@launch sender.sendMessage("<failure>You must provide a punishment reason.".mm)

        reasonBook(sender, target)
    }
}

fun punishComplete(args: MutableList<String>): List<String> {
    val reasons = punishmentsToml.reasons.values

    if (args.size == 1)
        return Bukkit.getOnlinePlayers()
            .map { it.name }
            .filter { args[0] in it }
            .toList()

    args.removeFirst()
    val input = args.joinToString("")

    if (reasons.none { it.name.equals(input, true) })
        return reasons.map { it.name }
            .filter { it.contains(input, true) }

    return listOf("[...notes]")
}