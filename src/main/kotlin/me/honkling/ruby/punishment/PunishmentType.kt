package me.honkling.ruby.punishment

enum class PunishmentType(val hasDuration: Boolean) {
    Warn(false),
    Kick(false),
    Mute(true),
    Ban(true)
}