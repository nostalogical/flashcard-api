package net.nostalogic.services

import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashcardHint

interface HintService {

    fun createHint(cardId: String, hint: FlashcardHint): List<FlashcardHint>
    fun updateHint(cardId: String, hint: FlashcardHint): List<FlashcardHint>
    fun deleteHint(cardId: String, hintId: String): List<FlashcardHint>
    fun populateHints(cards: Collection<FlashCard>)

}
