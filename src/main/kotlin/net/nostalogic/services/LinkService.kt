package net.nostalogic.services

import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashcardConnection

interface LinkService {

    fun addOrUpdateLink(cardId: String, link: FlashcardConnection): List<FlashcardConnection>
    fun removeLink(card1: String, card2: String): List<FlashcardConnection>
    fun populateLinks(cards: List<FlashCard>)

}
