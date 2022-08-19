package net.nostalogic.api.dto

data class Project(
    val id: String? = null,
    val name: String?,
    var cardCount: Int? = null,
    var deckCount: Int? = null,
    val archived: Boolean? = null,
)
