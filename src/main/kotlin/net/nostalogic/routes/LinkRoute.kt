package net.nostalogic.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.nostalogic.api.dto.FlashcardConnection
import net.nostalogic.services.LinkService
import org.koin.ktor.ext.inject

fun Route.links() {

    val linkService: LinkService by inject()

    put("{cardId}/links") {
        val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
        val connection: FlashcardConnection = call.receive()
        val cardLinks: List<FlashcardConnection> = linkService.addOrUpdateLink(cardId, connection)
        call.respond(HttpStatusCode.OK, cardLinks)
    }

    delete("{cardId}/links/{linkedCardId}") {
        val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
        val linkedId: String = call.parameters["linkedCardId"] ?: throw NotFoundException()
        val remainingLinks: List<FlashcardConnection> = linkService.removeLink(cardId, linkedId)
        call.respond(HttpStatusCode.OK, remainingLinks)
    }

}
