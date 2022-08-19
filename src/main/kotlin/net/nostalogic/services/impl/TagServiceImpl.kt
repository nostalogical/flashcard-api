package net.nostalogic.services.impl

import io.ktor.server.plugins.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashCardTag
import net.nostalogic.db.dao.CardDao
import net.nostalogic.db.dao.TagDao
import net.nostalogic.services.TagService
import net.nostalogic.utils.UuidUtils

class TagServiceImpl : TagService {

    override fun populateTags(cards: Collection<FlashCard>) {
        val tagsForCards: Map<String, List<FlashCardTag>> = TagDao.getTagsForCards(cards.map { it.id!! })
        cards.forEach {
            it.tags = tagsForCards[it.id!!] ?: emptyList()
        }
    }

    override fun createTag(cardId: String, tag: FlashCardTag): List<FlashCardTag> {
        validateTag(tag)
        val card = CardDao.getCard(cardId) ?: throw BadRequestException("Card with ID $cardId does not exist")
        if (TagDao.doesTagExistOnCard(card.projectId!!, cardId, tag.name!!))
            throw BadRequestException("This tag already exists on this card")
        return TagDao.createTag(card.projectId, cardId, tag)
    }

    override fun updateTag(cardId: String, tagId: String, tag: FlashCardTag): List<FlashCardTag> {
        val card = CardDao.getCard(cardId) ?: throw BadRequestException("Card with ID $cardId does not exist")
        UuidUtils.requireValidUuid(tagId)
        validateTag(tag)
        return TagDao.updateTag(card.projectId!!, cardId, tagId, tag)
    }

    override fun removeTag(cardId: String, cardTagId: String): List<FlashCardTag> {
        CardDao.confirmCardExists(cardId)
        UuidUtils.requireValidUuid(cardTagId)
        if (!TagDao.doesCardTagExist(cardId, cardTagId))
            throw BadRequestException("tag hint does not exist on this card")
        return TagDao.removeTag(cardId, cardTagId)
    }

    override fun searchTagNames(projectId: String, search: String?): List<FlashCardTag> {
        UuidUtils.requireValidUuid(projectId)
        return TagDao.searchTagNames(projectId, search)
    }

    override fun searchTagValues(projectId: String, tagId: String, search: String?): List<String> {
        UuidUtils.requireValidUuid(projectId)
        UuidUtils.requireValidUuid(tagId)
        return TagDao.searchTagValues(projectId, tagId, search)
    }

    private fun validateTag(tag: FlashCardTag) {
        if (tag.name.isNullOrBlank())
            throw BadRequestException("Tag name is required")
        if (tag.name.length > 200)
            throw BadRequestException("Tag name cannot be over 200 characters long")
        if (tag.value?.isNotBlank() == true) {
            if (tag.value.length > 500)
                throw BadRequestException("Tag value cannot be over 500 characters long")
        }
    }

}
