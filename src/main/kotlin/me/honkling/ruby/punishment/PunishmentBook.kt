package me.honkling.ruby.punishment

import me.honkling.ruby.lib.center
import me.honkling.ruby.lib.clampToLength
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.max

private const val punishmentsPerPage = lineCount - 1
private val format = DateTimeFormatter.ofPattern("uuuu-mm-dd hh:mm:ss")
    .withZone(ZoneOffset.UTC)

fun punishmentBook(player: Player, target: OfflinePlayer, isModerator: Boolean) {
    val punishments = getPunishments(target)
        .sortedBy { it.issuedAt }

    val pages = punishments.chunked(13)
        .mapIndexed { page, punishments -> createPunishmentList(page, player, target, punishments, isModerator) }
        .toMutableList()

    if (pages.isEmpty())
        pages += Component.text("No punishment data")
            .color(NamedTextColor.DARK_RED)

    val book = Book.book(Component.empty(), Component.empty(), pages)
    player.openBook(book)
}

fun punishmentInfoBook(player: Player, target: OfflinePlayer, punishment: Punishment, isModerator: Boolean) {
    val issuedAt = format.format(punishment.issuedAt)
    val expiresAt = if (punishment.expiresAt == Instant.MAX) "Never" else format.format(punishment.expiresAt)
    var firstPage = Component.text(punishment.reason.name.center(lineWidth))
        .appendNewline()
        .appendNewline()
        .append(Component.text("Issued at:")
            .color(NamedTextColor.BLUE))
        .appendNewline()
        .append(Component.text(issuedAt))
        .appendNewline()
        .append(Component.text(if (!punishment.isActive) "Expired at:" else "Expires at:")
            .color(NamedTextColor.BLUE))
        .appendNewline()
        .append(Component.text(expiresAt))
        .appendNewline()
        .appendNewline()
        .append(Component.text("Username:")
            .color(NamedTextColor.BLUE))
        .appendNewline()
        .append(Component.text(punishment.player.name ?: "<unknown name>"))

    firstPage = if (isModerator)
        firstPage.appendNewline()
            .append(Component.text("Moderator:")
                .color(NamedTextColor.BLUE))
            .appendNewline()
            .append(Component.text((punishment.moderator as? OfflinePlayer)?.name ?: "Console"))
            .appendNewline()
            .appendNewline()
            .append(Component.text(if (!punishment.repealed) "[Repeal]" else "")
                .color(NamedTextColor.DARK_RED)
                .clickEvent(ClickEvent.callback {
                    punishment.repeal(player)
                    punishmentBook(player, target, isModerator)
                }))
            .appendNewline()
    else firstPage.append(Component.text("\n".repeat(5)))

    firstPage = firstPage.append(Component.text("[Back]")
        .color(NamedTextColor.DARK_RED)
        .clickEvent(ClickEvent.callback {
            punishmentBook(player, target, isModerator)
        }))

    val pages = mutableListOf(firstPage)

    if (isModerator && punishment.notes.isNotBlank())
        pages += Component.empty()
            .append(Component.text("Notes:")
                .color(NamedTextColor.BLUE))
            .appendNewline()
            .append(Component.text(punishment.notes))

    player.openBook(Book.book(Component.empty(), Component.empty(), pages))
}

private fun createPunishmentList(page: Int, player: Player, target: OfflinePlayer, punishments: List<Punishment>, isModerator: Boolean): Component {
    val now = Instant.now()
    val start = punishmentsPerPage * page + 1
    val end = start + punishments.size - 1
    var component: Component = Component.text("  Punishments ($start-$end)\n")

    for ((index, punishment) in punishments.withIndex()) {
        val isActive = !punishment.repealed && punishment.expiresAt.isAfter(now)
        val repealed = if (punishment.repealed) "(R) " else ""
        component = component.append(Component.text((repealed + punishment.reason.name).clampToLength(lineWidth))
            .color(if (isActive) NamedTextColor.DARK_RED else NamedTextColor.BLACK)
            .clickEvent(ClickEvent.callback {
                punishmentInfoBook(player, target, punishment, isModerator)
            }))

        if (index + 1 < punishments.size)
            component = component.appendNewline()
    }

    return component
}