package net.nostalogic.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.nostalogic.api.dto.Deck
import net.nostalogic.api.dto.Project
import net.nostalogic.services.DeckService
import org.koin.ktor.ext.inject

fun Route.decks() {

    val deckService: DeckService by inject()

    route("decks/") {
        get("{deckId}") {
            val deckId: String = call.parameters["deckId"] ?: throw NotFoundException()
            val decks: Deck = deckService.getDeckById(deckId)
            call.respond(HttpStatusCode.OK, decks)
        }

        delete("{deckId}") {
            val deckId: String = call.parameters["deckId"] ?: throw NotFoundException()
            deckService.deleteDeck(deckId)
            call.respond(HttpStatusCode.OK)
        }

        post ("{deckId}/cards/{cardId}") {
            val deckId: String = call.parameters["deckId"] ?: throw NotFoundException()
            val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
            val deck: Deck = deckService.addCardToDeck(deckId, cardId)
            call.respond(HttpStatusCode.OK, deck)
        }

        put ("{deckId}/cards") {
            val deckId: String = call.parameters["deckId"] ?: throw NotFoundException()
            val cardIds: List<String> = call.receive()
            val deck: Deck = deckService.addCardsToDeck(deckId, cardIds)
            call.respond(HttpStatusCode.OK, deck)
        }

        delete ("{deckId}/cards/{cardId}") {
            val deckId: String = call.parameters["deckId"] ?: throw NotFoundException()
            val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
            val deck: Deck = deckService.removeCardFromDeck(deckId, cardId)
            call.respond(HttpStatusCode.OK, deck)
        }

        post ("{deckId}/cards/remove") {
            val deckId: String = call.parameters["deckId"] ?: throw NotFoundException()
            val cardIds: List<String> = call.receive()
            val deck: Deck = deckService.removeCardsFromDeck(deckId, cardIds)
            call.respond(HttpStatusCode.OK, deck)
        }
    }

}
