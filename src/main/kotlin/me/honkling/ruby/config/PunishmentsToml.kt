package me.honkling.ruby.config

import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.tomlMapper
import me.honkling.commonlib.config.decoder.use
import me.honkling.commonlib.config.getAndMapConfig
import me.honkling.ruby.lib.parseDuration
import me.honkling.ruby.punishment.PunishmentType
import kotlin.time.Duration

var punishmentsToml: PunishmentsToml = reloadPunishmentsToml(); private set
val legacy = PunishmentsToml.Reason(
    "Legacy",
    "An old punishment that we don't recognize anymore.",
    PunishmentType.Warn,
    0
)

data class PunishmentsToml(
    val scale: List<Duration>,
    val reasons: Map<String, Reason>
) {
    data class Reason(
        val name: String,
        val description: String,
        val type: PunishmentType,
        val duration: Int? = 0
    )

    fun getShort(reason: Reason): String {
        return reasons.entries.find { it.value == reason }?.key
            ?: "legacy"
    }
}

fun reloadPunishmentsToml(): PunishmentsToml {
    val mapper = tomlMapper {
        use(Duration::class to { _, value ->
            if (value !is TomlValue.String)
                value
            else parseDuration(value.value).getOrThrow()
        })
    }

    punishmentsToml = getAndMapConfig<PunishmentsToml>("punishments.toml", mapper)
    return punishmentsToml
}