package me.honkling.ruby.punishment

import me.honkling.ruby.config.PunishmentsToml
import me.honkling.ruby.config.punishmentsToml
import me.honkling.ruby.database.prepare
import me.honkling.ruby.instance
import me.honkling.ruby.lib.mm
import me.honkling.ruby.lib.toByteArray
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Instant
import java.util.*
import kotlin.time.Duration

private val punishmentCache = mutableMapOf<UUID, MutableList<Punishment>>()

fun issuePunishment(
    moderator: CommandSender,
    target: OfflinePlayer,
    reason: PunishmentsToml.Reason,
    duration: Duration? = null,
    notes: String = ""
): Result<Punishment> {
    val now = Instant.now()
    val id = UUID.randomUUID()
    val short = punishmentsToml.reasons.entries
        .find { it.value == reason }?.key
        ?: throw IllegalArgumentException("Unknown reason '${reason.name}'")

    val punishments = getPunishments(target)

    if (punishments.any { reason.type == it.reason.type && it.isActive })
        return Result.failure(IllegalStateException("That player already has an active punishment."))

    val displayDuration = if (reason.type.hasDuration && duration != null) " (${duration.toString().replace("Infinity", "permanent")})" else ""
    val audience = Audience.audience(Bukkit.getOnlinePlayers()
        .filter { it.hasPermission("ruby.punish") })

    audience.sendMessage("<good><good2>${moderator.name}</good2> punished <good2>${target.name}</good2> for <good2>${reason.name}$displayDuration".mm)

    prepare(
        """
            insert into punishments(id, player, moderator, short, duration, issuedAt, notes)
            values(?, ?, ?, ?, ?, ?, ?);
        """.trimIndent(),
        id.toByteArray(),
        target.uniqueId.toByteArray(),
        (moderator as? OfflinePlayer)?.uniqueId?.toByteArray(),
        short,
        duration?.inWholeSeconds,
        now.epochSecond,
        notes
    ).execute()

    instance.punishmentCounts[reason] = (instance.punishmentCounts[reason] ?: 0) + 1
    val punishment = Punishment(id, target, moderator, short, duration ?: Duration.ZERO, now, false, notes)
    punishments += punishment

    if (target is Player)
        Bukkit.getScheduler().runTask(instance, Runnable {
            when (reason.type) {
                PunishmentType.Ban, PunishmentType.Kick ->
                    target.kick(punishment.kickMessage)
                PunishmentType.Mute, PunishmentType.Warn ->
                    target.sendMessage(punishment.chatMessage)
            }
        })

    return Result.success(punishment)
}

fun getPunishments(uuid: UUID): MutableList<Punishment> {
    if (uuid in punishmentCache)
        return punishmentCache[uuid]!!

    val punishments = mutableListOf<Punishment>()
    val resultSet = prepare("select * from punishments where player = ?", uuid.toByteArray())
        .executeQuery()

    while (resultSet.next())
         punishments += Punishment(resultSet)

    punishmentCache[uuid] = punishments
    return punishments
}

fun getPunishments(player: OfflinePlayer): MutableList<Punishment>
    = getPunishments(player.uniqueId)

fun calculateDuration(player: OfflinePlayer, reason: PunishmentsToml.Reason): Duration {
    val punishments = getPunishments(player).filter { it.reason == reason && !it.repealed }
    val scale = punishmentsToml.scale

    return scale.getOrNull(reason.duration!! + punishments.size)
        ?: Duration.INFINITE
}

fun clearPunishmentCache() {
    punishmentCache.clear()
}