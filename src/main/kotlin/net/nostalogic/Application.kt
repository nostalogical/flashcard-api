package net.nostalogic

import io.ktor.server.application.*
import io.ktor.server.netty.*
import net.nostalogic.routes.configureRouting

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureKoin()
    configureDatabase()
    configureSerialisation()
    configureCors()
    configureErrorHandling()
    configureRouting()
}




