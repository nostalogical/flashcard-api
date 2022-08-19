package net.nostalogic.db.entities

import net.nostalogic.api.dto.FlashcardConnection
import net.nostalogic.constants.RelationshipType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object CardLinkEntity  : Table("card_link") {
    val cardOne = reference("card_one", CardEntity.id)
    val cardTwo = reference("card_two", CardEntity.id)
    val description = text("description").nullable()
    val related = enumerationByName<RelationshipType>("related", 7).nullable()
    val mistakable = enumerationByName<RelationshipType>("mistakable", 7).nullable()
    val createdAt = long("created_at")

    fun toContextualDto(rr: ResultRow, primaryCardIsTwo: Boolean = false) = FlashcardConnection(
        id = rr[if (primaryCardIsTwo) cardOne else cardTwo].toString(),
        description = rr[description],
        related =  rr[related].let { if (primaryCardIsTwo) reverseRelationship(it) else it }?.toString(),
        mistakable = rr[mistakable].let { if (primaryCardIsTwo) reverseRelationship(it) else it }?.toString(),
        front = rr[CardEntity.front],
        back = rr[CardEntity.back],
        createdAt = rr[createdAt],
    )

    private fun reverseRelationship(type: RelationshipType?): RelationshipType? {
        return when (type) {
            RelationshipType.ONE_WAY -> RelationshipType.REVERSE
            RelationshipType.REVERSE -> RelationshipType.ONE_WAY
            else -> type
        }
    }
}
