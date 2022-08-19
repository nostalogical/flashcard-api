package net.nostalogic.services

import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashCardTag

interface TagService {

    fun populateTags(cards: Collection<FlashCard>)
    fun createTag(cardId: String, tag: FlashCardTag): List<FlashCardTag>
    fun updateTag(cardId: String, tagId: String, tag: FlashCardTag): List<FlashCardTag>
    fun removeTag(cardId: String, cardTagId: String): List<FlashCardTag>
    fun searchTagNames(projectId: String, search: String?): List<FlashCardTag>
    fun searchTagValues(projectId: String, tagId: String, search: String?): List<String>

}
