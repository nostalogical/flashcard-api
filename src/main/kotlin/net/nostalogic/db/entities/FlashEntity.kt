package net.nostalogic.db.entities

import org.jetbrains.exposed.sql.Table

object FlashEntity : Table("flash") {
    val id = uuid("id").autoGenerate()
    val cardId = reference("card_id", CardEntity.id)
    val duration = long("duration")
    val recordedAt = long("recorded_at")
    val success = bool("success")
    override val primaryKey = PrimaryKey(id)
}
