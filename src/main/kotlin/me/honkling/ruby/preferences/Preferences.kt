package me.honkling.ruby.preferences

import me.honkling.ruby.instance
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

private val sortingKey = NamespacedKey(instance, "sorting")
private val sortingType = EnumType(SortType::class)

var Player.sortingPreference: SortType
    get() = persistentDataContainer.getOrDefault(sortingKey, sortingType, SortType.Popular)
    set(value) {
        persistentDataContainer.set(sortingKey, sortingType, value)
    }