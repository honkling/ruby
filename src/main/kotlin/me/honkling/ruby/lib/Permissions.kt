package me.honkling.ruby.lib

import me.honkling.ruby.luckPerms
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

fun hasPermission(uuid: UUID, node: String): Boolean {
    val user = luckPerms.userManager.getUser(uuid)
        ?: return false

    val data = user.cachedData.permissionData
    return data.checkPermission(node).asBoolean()
}

fun OfflinePlayer.hasPermission(node: String): Boolean
    = hasPermission(uniqueId, node)