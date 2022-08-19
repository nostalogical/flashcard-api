package net.nostalogic.db.entities

import net.nostalogic.api.dto.FlashcardHint
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object CardHintEntity : Table("card_hint") {
    val id = uuid("id").autoGenerate()
    val cardId = reference("card_id", CardEntity.id)
    val hint = text("hint")
    val ordinal = integer("ordinal")
    override val primaryKey = PrimaryKey(id)

    fun toDto(rr: ResultRow) = FlashcardHint(
        id = rr[id].toString(),
        hint = rr[hint],
    )
}
