package net.nostalogic.db.entities

import net.nostalogic.api.dto.FlashCardTag
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object CardTagEntity : Table("card_tag") {
    val id = uuid("id").autoGenerate()
    val cardId = reference("card_id", CardEntity.id)
    val tagId = reference("tag_id", TagNameEntity.id)
    val tagValue = varchar("tag_value", 500).nullable()
    val ordinal = integer("ordinal")
    override val primaryKey = PrimaryKey(id)
    init {
        uniqueIndex(cardId, tagId)
        index(false, tagId, tagValue)
    }

    fun toContextualDto(rr: ResultRow) = FlashCardTag(
        id = rr[id].toString(),
        name = rr[TagNameEntity.tagName],
        value = rr[tagValue],
    )
}
