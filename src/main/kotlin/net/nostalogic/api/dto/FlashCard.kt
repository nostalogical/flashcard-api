package net.nostalogic.api.dto

data class FlashCard(
    val id: String? = null,
    val projectId: String? = null,
    val front: String? = null,
    val back: String? = null,
    val deleted: Boolean? = null,
    var tags: List<FlashCardTag>? = null,
    var hints: List<FlashcardHint>? = null,
    var connected: List<FlashcardConnection>? = null,
)
