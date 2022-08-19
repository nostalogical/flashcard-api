package net.nostalogic.services.impl

import io.ktor.server.plugins.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashcardHint
import net.nostalogic.db.dao.CardDao
import net.nostalogic.db.dao.HintDao
import net.nostalogic.services.HintService
import net.nostalogic.utils.UuidUtils

class HintServiceImpl : HintService {

    override fun createHint(cardId: String, hint: FlashcardHint): List<FlashcardHint> {
        CardDao.confirmCardExists(cardId)
        validateHint(hint)
        return HintDao.createHint(cardId, hint)
    }

    override fun updateHint(cardId: String, hint: FlashcardHint): List<FlashcardHint> {
        CardDao.confirmCardExists(cardId)
        validateHint(hint)
        UuidUtils.requireValidUuid(hint.id)
        if (!HintDao.doesCardHintExist(cardId, hint.id!!))
            throw BadRequestException("This hint does not exist on this card")
        return HintDao.updateHint(cardId, hint)
    }

    override fun deleteHint(cardId: String, hintId: String): List<FlashcardHint> {
        if (!HintDao.doesCardHintExist(cardId, hintId))
            throw BadRequestException("This hint does not exist on this card")
        return HintDao.deleteHint(cardId, hintId)
    }

    override fun populateHints(cards: Collection<FlashCard>) {
        cards.forEach {
            it.hints = HintDao.getAllHintsForCard(it.id!!)
        }
    }

    private fun validateHint(hint: FlashcardHint) {
        if (hint.hint?.isEmpty() == true)
            throw BadRequestException("A hint cannot be empty.")
    }

}
