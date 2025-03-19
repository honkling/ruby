package me.honkling.ruby.lib

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

val discord = TextColor.color(0xB3, 0xB9, 0xFC)
val discord2 = TextColor.color(0xD6, 0xDA, 0xFF)
val good = TextColor.color(0x8F, 0xF7, 0x9B)
val good2 = TextColor.color(0xBF, 0xFF, 0xC6)
val bad = TextColor.color(0xFF, 0x6E, 0x6E)
val bad2 = TextColor.color(0xFC, 0x88, 0x79)
val useful = TextColor.color(0x00, 0x71, 0xBC)
val useful2 = TextColor.color(0x15, 0x8C, 0xDB)

val miniMessage = MiniMessage.builder()
    .tags(TagResolver.resolver(
        TagResolver.standard(),
        TagResolver.resolver("discord", Tag.styling(discord)),
        TagResolver.resolver("discord2", Tag.styling(discord2)),
        TagResolver.resolver("good", Tag.styling(good)),
        TagResolver.resolver("good2", Tag.styling(good2)),
        TagResolver.resolver("bad", Tag.styling(bad)),
        TagResolver.resolver("bad2", Tag.styling(bad2)),
        TagResolver.resolver("useful", Tag.styling(useful)),
        TagResolver.resolver("useful2", Tag.styling(useful2)),
        Placeholder.parsed("success", "<good>✔ "),
        Placeholder.parsed("failure", "<bad>⚠ "),
        Placeholder.parsed("info", "<useful>ⓘ ")
    ))
    .build()

fun String.mm(vararg resolvers: TagResolver): Component {
    return miniMessage.deserialize(this, *resolvers)
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
}

val String.mm
    get() = mm()