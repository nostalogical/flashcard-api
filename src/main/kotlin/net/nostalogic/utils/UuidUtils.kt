package net.nostalogic.utils

import io.ktor.server.plugins.*

object UuidUtils {

    private const val UUID_REGEXP = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"

    fun filterValidUuids(ids: Collection<String>): Set<String> {
        return ids.filter { Regex(UUID_REGEXP).matches(it) }.toSet()
    }

    private fun isUuid(id: String): Boolean {
        return id.matches(Regex(UUID_REGEXP))
    }

    fun requireValidUuid(id: String?) {
        if (id.isNullOrBlank() || !isUuid(id))
            throw BadRequestException("Specified ID is invalid")
    }

}
