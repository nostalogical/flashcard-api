package net.nostalogic.db.dao

import io.ktor.server.plugins.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.db.entities.CardEntity
import net.nostalogic.db.entities.DeckCardEntity
import net.nostalogic.utils.UuidUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object CardDao {

    fun getCard(id: String): FlashCard? {
        val card = transaction {
            CardEntity.select { CardEntity.id eq UUID.fromString(id) }.singleOrNull()?.let { CardEntity.toDto(it) }

        }
        return card
    }

    fun getCards(projectId: String, ids: Set<String>): List<FlashCard> {
        val card = transaction {
            CardEntity.select { CardEntity.id inList ids.map { UUID.fromString(it) }.toList() and (CardEntity.projectId eq UUID.fromString(projectId)) }
                .orderBy(CardEntity.front).map { CardEntity.toDto(it) }

        }
        return card
    }

    fun getAllCardsInProject(_projectId: String): List<FlashCard> {
        val cards = transaction {
            CardEntity.select { CardEntity.projectId eq UUID.fromString(_projectId) }.orderBy(CardEntity.front)
                .map { CardEntity.toDto(it) }
        }
        return cards
    }

    fun getAllCardsInDecks(_deckIds: Collection<UUID>): Map<String, List<FlashCard>> {
        val decks: Map<String, ArrayList<FlashCard>> = _deckIds.associate { it.toString() to ArrayList() }
        transaction {
            CardEntity.join(DeckCardEntity, JoinType.INNER)
                .select { CardEntity.deleted eq false and (DeckCardEntity.deckId inList _deckIds) }
                .orderBy(DeckCardEntity.ordinal)
                .forEach{ decks[it[DeckCardEntity.deckId].toString()]!!.add(CardEntity.toDto(it)) }
        }
        return decks
    }

    fun createCard(_projectId: String, card: FlashCard): FlashCard {
        val created = transaction {
            CardEntity.insert {
                it[front] = card.front!!
                it[back] = card.back!!
                it[projectId] = UUID.fromString(_projectId)
            }.resultedValues?.first().let { CardEntity.toDto(it!!) }
        }
        return created
    }

    fun updateCard(cardId: String, card: FlashCard): FlashCard? {
        transaction {
            CardEntity.update ({CardEntity.id eq UUID.fromString(cardId)}) {
                it[front] = card.front!!
                it[back] = card.back!!
                it[deleted] = false
            }
        }
        return getCard(cardId)
    }

    fun deleteCard(cardId: String, hardDelete: Boolean) {
        val cardUuid = UUID.fromString(cardId)
        HintDao.deleteAllCardHints(cardUuid)
        LinkDao.deleteAllCardLinks(cardUuid)
        TagDao.deleteAllCardTags(cardUuid)
        transaction {
            if (hardDelete) {
                CardEntity.deleteWhere { CardEntity.id eq cardUuid }
            } else {
                CardEntity.update ({CardEntity.id eq cardUuid}) {
                    it[deleted] = true
                }
            }
        }
    }

    fun confirmCardExists(cardId: String) {
        UuidUtils.requireValidUuid(cardId)
        if (getCard(cardId) == null)
            throw BadRequestException("Card with ID $cardId does not exist")
    }

}
