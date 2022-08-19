package net.nostalogic.api.exceptions

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*

class ErrorHandler(private val call: ApplicationCall, private val cause: Throwable) {

    suspend fun handle() {
        if (cause is NotFoundException)
            return
        if (cause is BadRequestException)
            return
        if (cause.message?.contains("SQL", true) == true)
            call.respond(ErrorDto(500, "dbError", "An unexpected database error has occurred."))
        call.respond(ErrorDto(500, "unknownError", "An unexpected internal error has occurred."))
        throw cause
    }

}
