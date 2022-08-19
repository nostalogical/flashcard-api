package net.nostalogic.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.nostalogic.api.dto.FlashCardTag
import net.nostalogic.services.TagService
import org.koin.ktor.ext.inject

fun Route.tags() {

    val tagService: TagService by inject()

    route("cards/{cardId}/tags") {
        post {
            val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
            val tag = call.receive<FlashCardTag>()
            val cardTags: List<FlashCardTag> = tagService.createTag(cardId, tag)
            call.respond(HttpStatusCode.Created, cardTags)
        }

        put("{tagId}") {
            val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
            val tagId: String = call.parameters["tagId"] ?: throw NotFoundException()
            val tag = call.receive<FlashCardTag>()
            val cardTags: List<FlashCardTag> = tagService.updateTag(cardId, tagId, tag)
            call.respond(HttpStatusCode.OK, cardTags)
        }

        delete("{tagId}") {
            val cardId: String = call.parameters["cardId"] ?: throw NotFoundException()
            val tagId: String = call.parameters["tagId"] ?: throw NotFoundException()
            val cardTags: List<FlashCardTag> = tagService.removeTag(cardId, tagId)
            call.respond(HttpStatusCode.OK, cardTags)
        }
    }

    get("projects/{projectId}/tags") {
        val projectId: String = call.parameters["projectId"] ?: throw NotFoundException()
        val query: String? = call.request.queryParameters["query"]
        val results: List<FlashCardTag> = tagService.searchTagNames(projectId, query)
        call.respond(HttpStatusCode.OK, results)
    }

    get("projects/{projectId}/tags/{tagId}") {
        val projectId: String = call.parameters["projectId"] ?: throw NotFoundException()
        val tagId: String = call.parameters["tagId"] ?: throw NotFoundException()
        val query: String? = call.request.queryParameters["query"]
        val results: List<String> = tagService.searchTagValues(projectId, tagId, query)
        call.respond(HttpStatusCode.OK, results)
    }

}
