package net.nostalogic.db

import net.nostalogic.db.entities.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    val loaded = System.currentTimeMillis()

    fun init(driverClassName: String?, jdbcUrl: String?) {
        if (driverClassName.isNullOrBlank() || jdbcUrl.isNullOrBlank())
            throw RuntimeException("Database configuration not found")
        val database = Database.connect(jdbcUrl, driverClassName)


        transaction(database) {
            SchemaUtils.create(
                ProjectEntity,
                CardEntity,
                CardTagEntity,
                CardHintEntity,
                CardLinkEntity,
                FlashEntity,
                TagNameEntity,
                DeckCardEntity,
                DeckEntity,
            )
        }
    }

    /**
     * Delete all data from all tables. Used for cleanup after tests.
     */
    fun wipe() {
        transaction {
            CardTagEntity.deleteAll()
            CardHintEntity.deleteAll()
            CardLinkEntity.deleteAll()
            FlashEntity.deleteAll()
            TagNameEntity.deleteAll()
            DeckCardEntity.deleteAll()
            CardEntity.deleteAll()
            DeckEntity.deleteAll()
            ProjectEntity.deleteAll()
        }
    }
}
