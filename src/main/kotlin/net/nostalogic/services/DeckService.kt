package net.nostalogic.services

import net.nostalogic.api.dto.Deck

interface DeckService {

    fun createDeck(projectId: String, deck: Deck): Deck
    fun getProjectDecks(projectId: String): List<Deck>
    fun getDeckById(deckId: String): Deck
    fun deleteDeck(deckId: String)
    fun addCardToDeck(deckId: String, cardId: String): Deck
    fun addCardsToDeck(deckId: String, cardIds: List<String>): Deck
    fun removeCardFromDeck(deckId: String, cardId: String): Deck
    fun removeCardsFromDeck(deckId: String, cardIds: List<String>): Deck

}
