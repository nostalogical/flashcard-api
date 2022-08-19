package net.nostalogic.api.dto

data class FlashcardConnection(
    val id: String? = null,
    var front: String? = null,
    var back: String? = null,
    var description: String? = null,
    val related: String? = null,
    val mistakable: String? = null,
    @Transient val createdAt: Long? = null,
)
