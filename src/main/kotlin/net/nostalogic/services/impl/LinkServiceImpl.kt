package net.nostalogic.services.impl

import io.ktor.server.plugins.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashcardConnection
import net.nostalogic.db.dao.CardDao
import net.nostalogic.db.dao.LinkDao
import net.nostalogic.constants.RelationshipType
import net.nostalogic.services.LinkService

class LinkServiceImpl : LinkService {

    override fun addOrUpdateLink(cardId: String, link: FlashcardConnection): List<FlashcardConnection> {
        validateLink(cardId, link)
        return if (LinkDao.doesLinkExist(cardId, link.id!!))
            LinkDao.updateLink(cardId, link)
        else LinkDao.createLink(cardId, link)
    }

    private fun validateLink(primaryCardId: String, link: FlashcardConnection) {
        CardDao.getCard(primaryCardId) ?: throw NotFoundException("Card not found")

        if (link.id.isNullOrBlank())
            throw NotFoundException("Related card ID is required")
        CardDao.getCard(link.id) ?: throw NotFoundException("Related card not found")

        if (link.related.isNullOrBlank() && link.mistakable.isNullOrBlank())
            throw BadRequestException("A link type is required (related and/or mistakable)")
        if (link.related?.isNotBlank() == true)
            validateRelationship(link.related)
        if (link.mistakable?.isNotBlank() == true)
            validateRelationship(link.mistakable)

    }

    private fun validateRelationship(relationshipName: String) {
        try {
            RelationshipType.valueOf(relationshipName)
        } catch (e: Exception) {
            throw BadRequestException("Relationship type \"${relationshipName}\" is not valid")
        }
    }

    override fun removeLink(card1: String, card2: String): List<FlashcardConnection> {
        return LinkDao.removeLink(card1, card2)
    }

    override fun populateLinks(cards: List<FlashCard>) {
        cards.forEach {
            it.connected = LinkDao.getCardLinks(it.id!!)
        }
    }
}
