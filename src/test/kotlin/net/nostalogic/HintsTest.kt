package net.nostalogic

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashcardHint
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HintsTest : AppTest() {

    @Test
    fun `Create a card hint`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)

        val hint = FlashcardHint(hint = "A test hint")
        client.post("/cards/${card1.id}/hints") {
            contentType(ContentType.Application.Json)
            setBody(hint)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val updatedHints: List<FlashcardHint> = body()
            assertNotNull(updatedHints)
            assertEquals(1, updatedHints.size)
            assertNotNull(updatedHints[0].id)
            assertEquals(hint.hint, updatedHints[0].hint)
        }
    }

    @Test
    fun `Update a card hint`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)
        val hint1 = createHint(client, card1.id!!)
        val hint2 = createHint(client, card1.id!!)

        val hintUpdate = FlashcardHint(hint = "Update")
        client.put("/cards/${card1.id!!}/hints/${hint1.id!!}") {
            contentType(ContentType.Application.Json)
            setBody(hintUpdate)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedHints: List<FlashcardHint> = body()
            assertNotNull(updatedHints)
            assertEquals(2, updatedHints.size)
            assertEquals(hintUpdate.hint, updatedHints[0].hint)
            assertEquals(hint2.hint, updatedHints[1].hint)
        }
    }

    @Test
    fun `Remove a card hint`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)
        val hint1 = createHint(client, card1.id!!)
        val hint2 = createHint(client, card1.id!!)
        val hint3 = createHint(client, card1.id!!)

        client.delete("/cards/${card1.id!!}/hints/${hint2.id!!}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedHints: List<FlashcardHint> = body()
            assertNotNull(updatedHints)
            assertEquals(2, updatedHints.size)
            assertEquals(hint1.id, updatedHints[0].id)
            assertEquals(hint1.hint, updatedHints[0].hint)
            assertEquals(hint3.id, updatedHints[1].id)
            assertEquals(hint3.hint, updatedHints[1].hint)
        }
    }

    @Test
    fun `Retrieved cards contain link details`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)
        val hint1 = createHint(client, card1.id!!)
        createHint(client, card1.id!!)

        client.get("/cards/${card1.id!!}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response: FlashCard = body()
            assertNotNull(response)
            assertNotNull(response.hints)
            assertEquals(2, response.hints!!.size)
            assertEquals(hint1.id, response.hints!![0].id)
            assertEquals(hint1.hint, response.hints!![0].hint)
        }
    }

}
