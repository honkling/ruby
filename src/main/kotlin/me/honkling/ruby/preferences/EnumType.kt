package me.honkling.ruby.preferences

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

class EnumType<T : Enum<T>>(val klass: KClass<T>) : PersistentDataType<Integer, T> {
    override fun getPrimitiveType(): Class<Integer> {
        return Integer::class.java
    }

    override fun getComplexType(): Class<T> {
        return klass.java
    }

    override fun toPrimitive(complex: T, context: PersistentDataAdapterContext): Integer {
        return Integer.valueOf(complex.ordinal) as Integer
    }

    override fun fromPrimitive(primitive: Integer, context: PersistentDataAdapterContext): T {
        return klass.java.enumConstants[primitive.toInt()]
    }
}