package net.nostalogic.services.impl

import io.ktor.server.plugins.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.db.dao.CardDao
import net.nostalogic.services.CardService
import net.nostalogic.services.HintService
import net.nostalogic.services.LinkService
import net.nostalogic.services.TagService
import net.nostalogic.utils.UuidUtils

class CardServiceImpl(
    private val tagService: TagService,
    private val hintService: HintService,
    private val linkService: LinkService,
) : CardService {

    override fun getAllCards(projectId: String): List<FlashCard> {
        return CardDao.getAllCardsInProject(projectId)
    }

    override fun getCards(projectId: String, cardIds: List<String>): List<FlashCard> {
        return populateCards(CardDao.getCards(projectId, UuidUtils.filterValidUuids(cardIds)))
    }

    override fun getCard(cardId: String): FlashCard {
        UuidUtils.requireValidUuid(cardId)
        val card = CardDao.getCard(cardId) ?: throw NotFoundException()
        return populateCard(card)
    }

    override fun createCard(projectId: String, card: FlashCard): FlashCard {
        validateCard(card)
        return CardDao.createCard(projectId, card)
    }

    override fun updateCard(cardId: String, card: FlashCard): FlashCard {
        validateCard(card)
        UuidUtils.requireValidUuid(cardId)
        CardDao.updateCard(cardId, card)
        return getCard(cardId)
    }

    override fun deleteCard(cardId: String) {
        val card = getCard(cardId)
        CardDao.deleteCard(cardId, hardDelete = card.deleted == true)
    }

    private fun populateCard(card: FlashCard): FlashCard {
        return populateCards(listOf(card)).first()
    }

    private fun populateCards(cards: List<FlashCard>): List<FlashCard> {
        tagService.populateTags(cards)
        hintService.populateHints(cards)
        linkService.populateLinks(cards)
        return cards
    }

    override fun validateCard(card: FlashCard) {
        if (card.front.isNullOrBlank())
            throw BadRequestException("Card front is required")
        if (card.back.isNullOrBlank())
            throw BadRequestException("Card back is required")
    }

}
