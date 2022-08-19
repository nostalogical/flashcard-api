package net.nostalogic.api.dto

data class Deck(
    val id: String? = null,
    val name: String? = null,
    var projectId: String? = null,

    var cards: List<FlashCard>? = null,
    var parent: Deck? = null,
    var children: List<Deck>? = null,
)
