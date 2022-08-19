package net.nostalogic.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.nostalogic.api.dto.Deck
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.Project
import net.nostalogic.services.CardService
import net.nostalogic.services.DeckService
import net.nostalogic.services.ProjectService
import org.koin.ktor.ext.inject

fun Route.projects() {

    val projectService: ProjectService by inject()
    val cardService: CardService by inject()
    val deckService: DeckService by inject()

    post {
        var project = call.receive<Project>()
        project = projectService.createProject(project)
        call.respond(HttpStatusCode.Created, project)
    }

    get {
        val projects = projectService.getAllProjects()
        call.respond(HttpStatusCode.OK, projects)
    }

    delete("{projectId}") {
        val projectId: String = call.parameters["projectId"] ?: throw NotFoundException()
        call.respond(projectService.deleteProject(projectId))
    }

    post("{projectId}/cards") {
        val projectId: String = call.parameters["projectId"] ?: throw NotFoundException()
        var card = call.receive<FlashCard>()
        card = cardService.createCard(projectId, card)
        call.respond(HttpStatusCode.Created, card)
    }

    get("{projectId}/cards") {
        val projectId: String = call.parameters["projectId"] ?: throw NotFoundException()
        var ids: List<String> = call.parameters.getAll("id") ?: emptyList()
        if (ids.size == 1 && ids.first().contains(","))
            ids = ids.first().split(",")
        val cards: List<FlashCard> = if (ids.isEmpty()) cardService.getAllCards(projectId) else cardService.getCards(projectId, ids)
        call.respond(cards)
    }

    get("{projectId}/decks") {
        val projectId: String = call.parameters["projectId"] ?: throw NotFoundException()
        val decks: List<Deck> = deckService.getProjectDecks(projectId)
        call.respond(decks)
    }

    post("{projectId}/decks") {
        val projectId: String = call.parameters["projectId"] ?: throw NotFoundException()
        var deck = call.receive<Deck>()
        deck = deckService.createDeck(projectId, deck)
        call.respond(HttpStatusCode.Created, deck)
    }

}
