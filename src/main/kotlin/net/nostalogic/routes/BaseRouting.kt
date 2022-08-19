package net.nostalogic.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {

    routing {
        route("/projects") {
            projects()
        }
        decks()
        tags()
        route("/cards") {
            cards()
            hints()
            links()
        }
    }
}
