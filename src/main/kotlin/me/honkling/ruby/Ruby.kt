package me.honkling.ruby

import kotlinx.coroutines.launch
import me.honkling.commonlib.CommonLib
import me.honkling.ruby.command.punish
import me.honkling.ruby.command.punishComplete
import me.honkling.ruby.command.punishments
import me.honkling.ruby.config.PunishmentsToml
import me.honkling.ruby.config.punishmentsToml
import me.honkling.ruby.database.connectToDatabase
import me.honkling.ruby.database.connection
import me.honkling.ruby.database.prepare
import me.honkling.ruby.event.ChatEvent
import me.honkling.ruby.event.JoinEvent
import me.honkling.ruby.punishment.clearPunishmentCache
import net.luckperms.api.LuckPermsProvider
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val instance = JavaPlugin.getPlugin(Ruby::class.java)
val luckPerms = LuckPermsProvider.get()

class Ruby : JavaPlugin() {
    val punishmentCounts = mutableMapOf<PunishmentsToml.Reason, Int>()

    override fun onEnable() {
        CommonLib(this)
        punishmentsToml // Load `punishments.toml`

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ::clearPunishmentCache, 0L, 20L * 60L * 30L)

        scope.launch {
            connectToDatabase()

            val resultSet = prepare("select * from statistics").executeQuery()
            while (resultSet.next()) {
                val short = resultSet.getString("short")
                val reason = punishmentsToml.reasons[short]
                    ?: continue

                punishmentCounts[reason] = resultSet.getInt("count")
            }
        }

        val pluginManager = Bukkit.getPluginManager()
        pluginManager.registerEvents(JoinEvent, this)
        pluginManager.registerEvents(ChatEvent, this)

        val punishCommand = getCommand("punish")!!
        punishCommand.setExecutor { sender, _, _, args ->
            punish(sender, args.toMutableList())
            true
        }

        punishCommand.setTabCompleter { _, _, _, args ->
            return@setTabCompleter punishComplete(args.toMutableList())
        }

        val punishmentsCommand = getCommand("punishments")!!
        punishmentsCommand.setExecutor { sender, _, _, args ->
            punishments(sender, args.toList())
            true
        }
    }

    override fun onDisable() {
        scope.launch {
            val entries = punishmentCounts.entries
                .map { punishmentsToml.getShort(it.key) to it.value }

            for ((short, count) in entries)
                prepare("insert or replace into statistics(short, count) values(?, ?)", short, count).execute()
        }

        connection.close()
    }
}
