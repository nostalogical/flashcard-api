package net.nostalogic.services.impl

import io.ktor.server.plugins.*
import io.netty.util.internal.StringUtil
import net.nostalogic.api.dto.Deck
import net.nostalogic.db.dao.CardDao
import net.nostalogic.db.dao.DeckDao
import net.nostalogic.services.DeckService
import net.nostalogic.utils.UuidUtils
import org.h2.util.StringUtils

class DeckServiceImpl : DeckService {

    override fun createDeck(projectId: String, deck: Deck): Deck {
        validateDeck(deck)
        return DeckDao.createDeck(projectId, deck)
    }

    override fun getProjectDecks(projectId: String): List<Deck> {
        UuidUtils.requireValidUuid(projectId)
        return DeckDao.getDecksForProject(projectId)
    }

    override fun getDeckById(deckId: String): Deck {
        return DeckDao.getDeck(deckId) ?: throw NotFoundException()
    }

    override fun deleteDeck(deckId: String) {
        DeckDao.getDeck(deckId, false) ?: throw NotFoundException()
        DeckDao.deleteDeck(deckId)
    }

    override fun addCardToDeck(deckId: String, cardId: String): Deck {
        val deck = DeckDao.getDeck(deckId, false) ?: throw NotFoundException("Deck not found")
        val card = CardDao.getCard(cardId) ?: throw NotFoundException("Card not found")
        if (!deck.projectId.equals(card.projectId))
            throw BadRequestException("Deck and card belong to different projects")
        if (DeckDao.isCardInDeck(deckId, cardId))
            throw BadRequestException("Card is already in this deck")
        return DeckDao.addDeckCard(deckId, cardId)
    }

    override fun addCardsToDeck(deckId: String, cardIds: List<String>): Deck {
        val deck = DeckDao.getDeck(deckId, false) ?: throw NotFoundException("Deck not found")
        val cardsInProject = CardDao.getCards(deck.projectId!!, cardIds.toSet()).map { it.id!! }.toHashSet()
        val validCardIds = cardIds.filter { !DeckDao.isCardInDeck(deckId, it) && cardsInProject.contains(it) }
        return DeckDao.bulkAddDeckCards(deckId, validCardIds)
    }

    override fun removeCardFromDeck(deckId: String, cardId: String): Deck {
        DeckDao.getDeck(deckId, false) ?: throw NotFoundException("Deck not found")
        CardDao.getCard(cardId) ?: throw NotFoundException("Card not found")
        if (!DeckDao.isCardInDeck(deckId, cardId))
            throw BadRequestException("Card is not in this deck")
        return DeckDao.removeDeckCards(deckId, setOf(cardId))
    }

    override fun removeCardsFromDeck(deckId: String, cardIds: List<String>): Deck {
        DeckDao.getDeck(deckId, false) ?: throw NotFoundException("Deck not found")
        return DeckDao.removeDeckCards(deckId, cardIds)
    }

    private fun validateDeck(deck: Deck) {
        if (deck.name.isNullOrBlank())
            throw BadRequestException("Deck name is required")
        if (!deck.parent?.id.isNullOrBlank() && DeckDao.getDeck(deck.parent!!.id!!) == null)
            throw NotFoundException("Parent deck not found")
    }

}
