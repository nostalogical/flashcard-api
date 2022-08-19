package net.nostalogic

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import net.nostalogic.api.exceptions.ErrorHandler
import net.nostalogic.db.DatabaseFactory
import org.koin.fileProperties
import org.koin.ktor.ext.getProperty
import org.koin.ktor.plugin.Koin

/**
 * Koin handles service injection, so this should be configured before any modules that require the properties service.
 */
fun Application.configureKoin() {
    install(Koin) {
        modules(standardServiceBinds)
        fileProperties("/app.properties")
    }
}

fun Application.configureDatabase() {
    DatabaseFactory.init(getProperty("db.driverClass"), getProperty("db.url"))
}

fun Application.configureSerialisation() {
    install(ContentNegotiation) {
        gson()
    }
}

fun Application.configureCors() {
    install(CORS) {
        anyHost()
    }
}

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            ErrorHandler(call, cause).handle()
        }
    }
}
