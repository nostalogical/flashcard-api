package net.nostalogic.db.dao

import net.nostalogic.api.dto.FlashcardHint
import net.nostalogic.db.entities.CardHintEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object HintDao {

    fun createHint(_cardId: String, cardHint: FlashcardHint): List<FlashcardHint> {
        val nextOrdinal = getNextCardHintOrdinal(_cardId)
        transaction {
            CardHintEntity.insert {
                it[hint] = cardHint.hint!!
                it[cardId] = UUID.fromString(_cardId)
                it[ordinal] = nextOrdinal
            }
        }
        return getAllHintsForCard(_cardId)
    }

    private fun getNextCardHintOrdinal(cardId: String): Int {
        val ordinal = transaction {
            CardHintEntity.slice(CardHintEntity.ordinal.max())
                .select { CardHintEntity.cardId eq UUID.fromString(cardId) }
                .maxByOrNull { CardHintEntity.ordinal }?.get(CardHintEntity.ordinal.max())
        }
        return ordinal?.let { it + 1 } ?: 0
    }

    fun updateHint(cardId: String, cardHint: FlashcardHint): List<FlashcardHint> {
        transaction {
            CardHintEntity.update({ CardHintEntity.id eq UUID.fromString(cardHint.id) }) {
                it[hint] = cardHint.hint!!
            }
        }
        return getAllHintsForCard(cardId)
    }

    fun deleteHint(cardId: String, hintId: String): List<FlashcardHint> {
        transaction {
            CardHintEntity.deleteWhere { CardHintEntity.id eq UUID.fromString(hintId) }
        }
        return getAllHintsForCard(cardId)
    }

    fun doesCardHintExist(cardId: String, hintId: String): Boolean {
        val cardUuid = UUID.fromString(cardId)
        val hintUuid = UUID.fromString(hintId)
        return transaction {
            CardHintEntity.select {
                (CardHintEntity.id eq hintUuid and (CardHintEntity.cardId eq cardUuid))
            }.count() > 0
        }
    }

    fun getAllHintsForCard(cardId: String): List<FlashcardHint> {
        val hints = transaction {
            CardHintEntity.select{ CardHintEntity.cardId eq UUID.fromString(cardId) }
                .orderBy(CardHintEntity.ordinal)
                .map { CardHintEntity.toDto(it) }
        }
        return hints
    }

    fun deleteAllCardHints(cardUuid: UUID) {
        transaction {
            CardHintEntity.deleteWhere { CardHintEntity.cardId eq cardUuid }
        }
    }

}
