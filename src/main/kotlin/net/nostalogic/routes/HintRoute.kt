package net.nostalogic.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.nostalogic.api.dto.FlashcardHint
import net.nostalogic.services.HintService
import org.koin.ktor.ext.inject

fun Route.hints() {

    val hintService: HintService by inject()

    post("{cardId}/hints") {
        val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
        val hint: FlashcardHint = call.receive()
        val cardHints = hintService.createHint(cardId, hint)
        call.respond(HttpStatusCode.Created, cardHints)
    }

    put("{cardId}/hints/{hintId}") {
        val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
        val hintId: String = call.parameters["hintId"] ?: throw NotFoundException()
        val hint: FlashcardHint = call.receive()
        hint.id = hintId
        val cardHints = hintService.updateHint(cardId, hint)
        call.respond(HttpStatusCode.OK, cardHints)
    }

    delete("{cardId}/hints/{hintId}") {
        val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
        val hintId: String = call.parameters["hintId"] ?: throw NotFoundException()
        val cardHints = hintService.deleteHint(cardId, hintId)
        call.respond(HttpStatusCode.OK, cardHints)
    }

}
