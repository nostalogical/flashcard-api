package net.nostalogic.services

import net.nostalogic.api.dto.FlashCard

interface CardService {

    fun getAllCards(projectId: String): List<FlashCard>
    fun getCards(projectId: String, cardIds: List<String>): List<FlashCard>
    fun getCard(cardId: String): FlashCard
    fun createCard(projectId: String, card: FlashCard): FlashCard
    fun updateCard(cardId: String, card: FlashCard): FlashCard
    fun deleteCard(cardId: String)
    fun validateCard(card: FlashCard)

}
