package net.nostalogic.db.dao

import net.nostalogic.api.dto.FlashcardConnection
import net.nostalogic.db.entities.CardEntity
import net.nostalogic.db.entities.CardLinkEntity
import net.nostalogic.constants.RelationshipType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object LinkDao {

    fun createLink(_cardId: String, connection: FlashcardConnection): List<FlashcardConnection> {
        val relatedInput: RelationshipType? = connection.related?.let { RelationshipType.valueOf(it) }
        val mistakableInput: RelationshipType? = connection.mistakable?.let { RelationshipType.valueOf(it) }
        transaction {
            CardLinkEntity.insert {
                it[cardOne] = UUID.fromString(_cardId)
                it[cardTwo] = UUID.fromString(connection.id!!)
                it[description] = connection.description
                it[related] = relatedInput
                it[mistakable] = mistakableInput
                it[createdAt] = System.currentTimeMillis()
            }
        }
        return getCardLinks(_cardId)
    }

    fun updateLink(_cardId: String, connection: FlashcardConnection): List<FlashcardConnection> {
        val primaryId: UUID = UUID.fromString(_cardId)
        val secondaryId: UUID = UUID.fromString(connection.id)
        val relatedInput: RelationshipType? = connection.related?.let { RelationshipType.valueOf(it) }
        val mistakableInput: RelationshipType? = connection.mistakable?.let { RelationshipType.valueOf(it) }
        transaction {
            CardLinkEntity.update( {(CardLinkEntity.cardOne eq primaryId and (CardLinkEntity.cardTwo eq secondaryId)) or (CardLinkEntity.cardTwo eq primaryId and (CardLinkEntity.cardOne eq secondaryId))} ) {
                it[description] = connection.description
                it[related] = relatedInput
                it[mistakable] = mistakableInput
            }
        }

        return getCardLinks(_cardId)
    }

    fun removeLink(primaryCardId: String, secondaryCardId: String): List<FlashcardConnection> {
        val primaryId: UUID = UUID.fromString(primaryCardId)
        val secondaryId: UUID = UUID.fromString(secondaryCardId)
        transaction {
            CardLinkEntity.deleteWhere {(CardLinkEntity.cardOne eq primaryId and (CardLinkEntity.cardTwo eq secondaryId)) or (CardLinkEntity.cardTwo eq primaryId and (CardLinkEntity.cardOne eq secondaryId))}
        }
        return getCardLinks(primaryCardId)
    }

    fun getCardLinks(cardId: String): List<FlashcardConnection> {
        val cardUuid = UUID.fromString(cardId)
        val links = transaction {
            CardLinkEntity.join(CardEntity, JoinType.INNER, CardLinkEntity.cardTwo, CardEntity.id)
                .select { CardLinkEntity.cardOne eq cardUuid }
                .map { CardLinkEntity.toContextualDto(it) }
        }
        val reverseLinks = transaction {
            CardLinkEntity.join(CardEntity, JoinType.INNER, CardLinkEntity.cardOne, CardEntity.id)
                .select { CardLinkEntity.cardTwo eq cardUuid }
                .map { CardLinkEntity.toContextualDto(it, true) }
        }
        return (links + reverseLinks).sortedBy { it.createdAt }
    }

    fun doesLinkExist(cardOneId: String, cardTwoId: String): Boolean {
        val oneUuid = UUID.fromString(cardOneId)
        val twoUuid = UUID.fromString(cardTwoId)
        return transaction {
            CardLinkEntity.select {
                (CardLinkEntity.cardOne eq oneUuid and (CardLinkEntity.cardTwo eq twoUuid)) or (CardLinkEntity.cardOne eq twoUuid and (CardLinkEntity.cardTwo eq oneUuid))
            }.count() > 0
        }
    }

    fun deleteAllCardLinks(cardUuid: UUID) {
        transaction {
            CardLinkEntity.deleteWhere { CardLinkEntity.cardOne eq cardUuid or (CardLinkEntity.cardTwo eq cardUuid) }
        }
    }

}
