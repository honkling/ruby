package me.honkling.ruby.punishment

import me.honkling.ruby.config.PunishmentsToml
import me.honkling.ruby.config.punishmentsToml
import me.honkling.ruby.instance
import me.honkling.ruby.lib.clampToLength
import me.honkling.ruby.lib.safeSlice
import me.honkling.ruby.preferences.SortType
import me.honkling.ruby.preferences.sortingPreference
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

const val lineCount = 14
const val lineWidth = 113

fun reasonBook(player: Player, target: OfflinePlayer) {
    val sorting = player.sortingPreference
    var reasons = punishmentsToml.reasons.entries.toList()
    reasons = when (sorting) {
        SortType.Popular -> reasons.sortedBy { (_, reason) -> instance.punishmentCounts[reason] ?: 0 }
        else -> reasons.sortedBy { (_, reason) -> reason.name }
    }

    if (sorting == SortType.ReverseAlphabetical)
        reasons = reasons.reversed()

    val boundary = lineCount - 3
    val pagedReasons = listOf(
        reasons.safeSlice(0..<boundary),
        *reasons.safeSlice(boundary..<reasons.size).chunked(lineCount).toTypedArray()
    )

    val pages = mutableListOf(
        Component.text("Select sorting:")
            .decorate(TextDecoration.BOLD)
            .appendNewline()
            .append(Component.empty()
                .color(NamedTextColor.BLACK)
                .append(sortType(player, target, SortType.Popular))
                .appendSpace()
                .append(sortType(player, target, SortType.Alphabetical))
                .appendSpace()
                .append(sortType(player, target, SortType.ReverseAlphabetical)))
            .appendNewline()
            .appendNewline()
            .append(createReasonList(target, pagedReasons[0])
                .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)),
        *pagedReasons.safeSlice(1..<pagedReasons.size).map { createReasonList(target, it) }.toTypedArray()
    )

    val book = Book.book(Component.empty(), Component.empty(), pages)
    player.openBook(book)
}

private fun createReasonList(
    target: OfflinePlayer,
    reasons: List<Map.Entry<String, PunishmentsToml.Reason>>
): Component {
    var listing: Component = Component.empty()

    for ((index, entry) in reasons.withIndex()) {
        val (short, reason) = entry

        listing = listing.append(Component.text(reason.name.clampToLength(lineWidth))
            .clickEvent(ClickEvent.runCommand("/p ${target.name} $short"))
            .hoverEvent(HoverEvent.showText(Component.text(reason.name)
                .color(NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.BOLD)
                .appendNewline()
                .append(Component.text(reason.description)
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)))))

        if (index + 1 < reasons.size)
            listing = listing.appendNewline()
    }

    return listing
}

private fun sortType(player: Player, target: OfflinePlayer, type: SortType): Component {
    val state = if (player.sortingPreference == type) TextDecoration.State.TRUE
        else TextDecoration.State.FALSE

    return Component.text("[${type.display}]")
        .decoration(TextDecoration.BOLD, state)
        .clickEvent(ClickEvent.callback {
            player.sortingPreference = type
            reasonBook(player, target)
        })
}