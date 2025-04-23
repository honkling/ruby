package me.honkling.ruby.punishment

import me.honkling.ruby.config.legacy
import me.honkling.ruby.config.punishmentsToml
import me.honkling.ruby.database.prepare
import me.honkling.ruby.event.PunishmentRepealEvent
import me.honkling.ruby.lib.mm
import me.honkling.ruby.lib.toByteArray
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.nio.ByteBuffer
import java.sql.ResultSet
import java.time.Instant
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class Punishment(
    val id: UUID,
    val player: OfflinePlayer,
    val moderator: Any,
    val short: String,
    val duration: Duration,
    val issuedAt: Instant,
    var repealed: Boolean,
    val notes: String
) {
    val reason = punishmentsToml.reasons[short] ?: legacy
    val expiresAt: Instant = if (duration == Duration.INFINITE) Instant.MAX
                    else Instant.ofEpochSecond(issuedAt.epochSecond + duration.inWholeSeconds)
    val isActive: Boolean
        get() = !repealed && expiresAt.isAfter(Instant.now())

    val kickMessage
        get() = "<failure>You ${if (reason.type == PunishmentType.Ban) "are banned" else "have been kicked"} from this server for <bad2>${reason.name}</bad2>:\n<bad2>${reason.description}".mm

    val chatMessage
        get() = "\n<failure>You ${if (reason.type == PunishmentType.Mute) "are muted" else "have been warned"} on this server for <bad2>${reason.name}</bad2>:\n<bad2>${reason.description}\n".mm

    constructor(resultSet: ResultSet) : this(
        resultSet.getUUID("id")!!,
        Bukkit.getOfflinePlayer(resultSet.getUUID("player")!!),
        resultSet.getUUID("moderator")?.let(Bukkit::getOfflinePlayer) ?: Bukkit.getConsoleSender(),
        resultSet.getString("short"),
        resultSet.getLong("duration").toDuration(DurationUnit.SECONDS),
        Instant.ofEpochSecond(resultSet.getLong("issuedAt")),
        resultSet.getBoolean("repealed"),
        resultSet.getString("notes")
    )

    fun repeal(moderator: CommandSender) {
        val event = PunishmentRepealEvent(moderator, this)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled)
            return

        val audience = Audience.audience(Bukkit.getOnlinePlayers()
            .filter { it.hasPermission("ruby.punish") })

        audience.sendMessage("<good><good2>${moderator.name}</good2> repealed <good2>${player.name}</good2>'s <good2>${reason.name}</good2> ${reason.type.name.lowercase()}.".mm)
        repealed = true
        commit()
    }

    private fun commit() {
        prepare(
            """
                insert or replace into punishments(id, player, moderator, short, duration, issuedAt, repealed, notes) values(?, ?, ?, ?, ?, ?, ?, ?);
            """.trimIndent(),
            id.toByteArray(),
            player.uniqueId.toByteArray(),
            (moderator as? OfflinePlayer)?.uniqueId?.toByteArray(),
            short,
            issuedAt.epochSecond,
            repealed,
            notes
        ).execute()
    }
}

private fun ResultSet.getUUID(id: String): UUID? {
    val bytes = getBytes(id)
        ?: return null

    val buffer = ByteBuffer.wrap(bytes)
    val mostSignificant = buffer.getLong()
    val leastSignificant = buffer.getLong()
    return UUID(mostSignificant, leastSignificant)
}