package net.nostalogic.db.dao

import io.ktor.server.plugins.*
import net.nostalogic.api.dto.FlashCardTag
import net.nostalogic.db.entities.CardTagEntity
import net.nostalogic.db.entities.TagNameEntity
import org.apache.commons.lang3.StringUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.ArrayList

object TagDao {

    fun createTag(_projectId: String, _cardId: String, cardTag: FlashCardTag): List<FlashCardTag> {
        val dbTagId = getOrCreateTagNameId(_projectId, cardTag.name!!)
        val nextOrdinal = getNextCardTagOrdinal(_cardId)
        transaction {
            CardTagEntity.insert {
                it[cardId] = UUID.fromString(_cardId)
                it[tagId] = dbTagId
                it[tagValue] = StringUtils.trimToNull(cardTag.value)
                it[ordinal] = nextOrdinal
            }
        }
        return getTagsForCard(_cardId)
    }

    private fun getNextCardTagOrdinal(cardId: String): Int {
        val ordinal = transaction {
            CardTagEntity.slice(CardTagEntity.ordinal.max())
                .select { CardTagEntity.cardId eq UUID.fromString(cardId) }
                .maxByOrNull { CardTagEntity.ordinal }?.get(CardTagEntity.ordinal.max())
        }
        return ordinal?.let { it + 1 } ?: 0
    }

    private fun getOrCreateTagNameId(_projectId: String, _tagName: String): UUID {
        return getTagNameId(_projectId, _tagName) ?: createTagName(_projectId, _tagName)
    }

    private fun getTagNameId(_projectId: String, _tagName: String): UUID? {
        return transaction {
            TagNameEntity.slice(TagNameEntity.id)
                .select { TagNameEntity.projectId eq UUID.fromString(_projectId) and (TagNameEntity.tagName eq _tagName) }
                .singleOrNull()?.let { it[TagNameEntity.id] }
        }
    }

    private fun createTagName(_projectId: String, _tagName: String): UUID {
        return transaction {
            TagNameEntity.insert {
                it[projectId] = UUID.fromString(_projectId)
                it[tagName] = _tagName
            }
        }.resultedValues?.first().let { it!![TagNameEntity.id] }
    }

    fun doesTagExistOnCard(_projectId: String, _cardId: String, _tagName: String): Boolean {
        val tagNameId: UUID? = getTagNameId(_projectId, _tagName)
        if (tagNameId == null)
            return false
        return transaction {
            CardTagEntity.select { CardTagEntity.cardId eq UUID.fromString(_cardId) and
                    (CardTagEntity.tagId eq tagNameId) }
                .count() > 0
        }
    }

    fun updateTag(projectId: String, cardId: String, cardTagId: String, tag: FlashCardTag): List<FlashCardTag> {
        val tagUuid = UUID.fromString(cardTagId)
        val currentTagNameId: UUID = transaction {
            CardTagEntity.join(TagNameEntity, JoinType.INNER)
                .slice(TagNameEntity.id)
                .select { CardTagEntity.id eq tagUuid }
                .singleOrNull()?.let { it[TagNameEntity.id] }
        } ?: throw BadRequestException("Tag with ID $cardTagId does not exist")

        val dbTagId = getOrCreateTagNameId(projectId, tag.name!!)

        transaction {
            CardTagEntity.update({ CardTagEntity.id eq tagUuid }) {
                it[tagId] = dbTagId
                it[tagValue] = tag.value
            }
        }

        deleteTagNameIfUnused(currentTagNameId)

        return getTagsForCard(cardId)
    }

    fun doesCardTagExist(cardId: String, tagId: String): Boolean {
        val cardUuid = UUID.fromString(cardId)
        val tagUuid = UUID.fromString(tagId)
        return transaction {
            CardTagEntity.select {
                (CardTagEntity.id eq tagUuid and (CardTagEntity.cardId eq cardUuid))
            }.count() > 0
        }
    }

    fun removeTag(cardId: String, cardTagId: String): List<FlashCardTag> {
        transaction {
            val tagUuid: UUID? = CardTagEntity
                .slice(CardTagEntity.tagId)
                .select { CardTagEntity.id eq UUID.fromString(cardTagId) }
                .singleOrNull()?.let { it[CardTagEntity.tagId] }
            CardTagEntity.deleteWhere { CardTagEntity.id eq UUID.fromString(cardTagId) }
            if (tagUuid != null)
                deleteTagNameIfUnused(tagUuid)
        }
        return getTagsForCard(cardId)
    }

    private fun deleteTagNameIfUnused(tagUuid: UUID) {
        transaction {
            val tagUnused: Boolean = CardTagEntity
                .join(TagNameEntity, JoinType.INNER)
                .slice(CardTagEntity.tagId)
                .select { CardTagEntity.tagId eq tagUuid }
                .count() == 0L
            if (tagUnused) {
                TagNameEntity.deleteWhere { TagNameEntity.id eq tagUuid }
            }
        }
    }

    fun searchTagNames(projectId: String, search: String?): List<FlashCardTag> {
        val searchCondition: Op<Boolean> = if (search == null) Op.TRUE
        else TagNameEntity.tagName like search.trim()

        return transaction {
            TagNameEntity.slice(TagNameEntity.tagName)
                .select { TagNameEntity.projectId eq (UUID.fromString(projectId)) and (searchCondition) }
                .map { FlashCardTag(
                    id = it[TagNameEntity.id].toString(),
                    name = it[TagNameEntity.tagName],
                ) }
        }
    }

    fun searchTagValues(projectId: String, tagId: String, search: String?): List<String> {
        val searchCondition: Op<Boolean> = if (search == null) Op.TRUE
        else CardTagEntity.tagValue like search.trim()

        return transaction {
            CardTagEntity
                .join(TagNameEntity, JoinType.INNER)
                .slice(CardTagEntity.tagValue)
                .select { CardTagEntity.tagId eq (UUID.fromString(tagId)) and
                        (CardTagEntity.tagValue.isNotNull()) and
                        (searchCondition) and
                        (TagNameEntity.projectId eq UUID.fromString(projectId)) }
                .map { it[CardTagEntity.tagValue]!! }
        }
    }

    private fun getTagsForCard(cardId: String): List<FlashCardTag> {
        return getTagsForCards(setOf(cardId))[cardId] ?: emptyList()
    }

    fun getTagsForCards(cardIds: Collection<String>): Map<String, List<FlashCardTag>> {
        if (cardIds.isEmpty()) return emptyMap()
        val cardUuids = cardIds.map { UUID.fromString(it) }

        val tagsByCard = cardIds.associateWith { ArrayList<FlashCardTag>() }
        transaction {
            CardTagEntity.join(TagNameEntity, JoinType.INNER)
                .select { CardTagEntity.cardId inList cardUuids }
                .orderBy(CardTagEntity.ordinal)
                .forEach { tagsByCard[it[CardTagEntity.cardId].toString()]!!
                    .add(CardTagEntity.toContextualDto(it)) }
        }
        return tagsByCard
    }

    fun deleteAllCardTags(cardUuid: UUID) {
        val affectedTags: List<UUID> = transaction {
            val tagUuids = CardTagEntity.slice(CardTagEntity.tagId)
                    .select { CardTagEntity.cardId eq cardUuid }
                    .map { it[CardTagEntity.tagId] }
            CardTagEntity.deleteWhere { CardTagEntity.cardId eq cardUuid }
            return@transaction tagUuids
        }
        affectedTags.forEach { deleteTagNameIfUnused(it) }
    }

}
