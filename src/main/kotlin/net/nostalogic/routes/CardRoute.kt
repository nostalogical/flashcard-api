package net.nostalogic.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.services.CardService
import org.koin.ktor.ext.inject

fun Route.cards() {

    val cardService: CardService by inject()

    get("{cardId}") {
        val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
        val card = cardService.getCard(cardId)
        call.respond(card)
    }

    put("{cardId}") {
        val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
        val card = call.receive<FlashCard>()
        call.respond(cardService.updateCard(cardId, card))
    }

    delete("{cardId}") {
        val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
        cardService.deleteCard(cardId)
        call.respond(HttpStatusCode.OK)
    }

}
