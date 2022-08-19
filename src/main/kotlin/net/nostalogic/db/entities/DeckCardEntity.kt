package net.nostalogic.db.entities

import net.nostalogic.api.dto.DeckCard
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object DeckCardEntity  : Table("deck_card") {
    val id = uuid("id").autoGenerate()
    val cardId = reference("card_id", CardEntity.id)
    val deckId = reference("deck_id", DeckEntity.id)
    val ordinal = integer("ordinal")
    override val primaryKey = PrimaryKey(id)

    fun toDto(rr: ResultRow) = DeckCard(
        id = rr[id].toString(),
        cardId = rr[cardId].toString(),
        deckId = rr[deckId].toString(),
    )
}
