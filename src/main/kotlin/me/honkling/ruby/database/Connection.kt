package me.honkling.ruby.database

import me.honkling.ruby.config.punishmentsToml
import me.honkling.ruby.instance
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

lateinit var connection: Connection; private set

fun connectToDatabase() {
    Class.forName("org.sqlite.JDBC")
    connection = DriverManager.getConnection("jdbc:sqlite:${instance.dataFolder.resolve("punishments.db").absolutePath}")
    initializeTables()
}

fun prepare(@Language("SQLite") sql: String, vararg values: Any?): PreparedStatement {
    val statement = connection.prepareStatement(sql)

    for ((index, value) in values.withIndex())
        statement.setObject(index + 1, value)

    return statement
}

private fun initializeTables() {
    connection.prepareStatement("""
        create table if not exists punishments(
            id blob not null unique primary key,
            moderator blob,
            player blob not null,
            short text not null,
            duration integer,
            issuedAt integer not null,
            repealed integer not null default 0,
            notes text not null default ""
        );
    """).execute()

    connection.prepareStatement("""
        create table if not exists statistics(
            short text not null unique primary key,
            count integer not null default 0
        );
    """.trimIndent()).executeUpdate()

    for (key in punishmentsToml.reasons.keys)
        prepare("""
            insert or ignore into statistics(short) values(?);
        """.trimIndent(), key).execute()
}