package net.nostalogic

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashcardConnection
import net.nostalogic.constants.RelationshipType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CardsTest : AppTest() {

    @Test
    fun `Get a flashcard by its ID`() = testApplication {
        val client = testClient(this)
        val card = createCard(client)
        client.get("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response: FlashCard = body()
            assertNotNull(response)
            assertEquals(card.front, response.front)
            assertEquals(card.back, response.back)
            assertNotNull(response.connected)
            assertNotNull(response.tags)
            assertNotNull(response.hints)
        }
    }

    @Test
    fun `Get a flashcard with details attached`() = testApplication {
        val client = testClient(this)
        val card = createCard(client)
        val card2 = createCard(client, card.projectId)
        val card3 = createCard(client, card.projectId)
        createCardLink(client, card.id!!, FlashcardConnection(id = card2.id, related = RelationshipType.ONE_WAY.name))
        createCardLink(client, card.id!!, FlashcardConnection(id = card3.id, related = RelationshipType.TWO_WAY.name))
        createHint(client, card.id!!)
        createHint(client, card.id!!)
        createTag(client, card.id!!)
        createTag(client, card.id!!)

        client.get("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response: FlashCard = body()
            assertNotNull(response)
            assertEquals(card.front, response.front)
            assertEquals(card.back, response.back)
            assertNotNull(response.connected)
            assertNotNull(response.hints)
            assertNotNull(response.tags)
            assertEquals(2, response.connected!!.size)
            assertEquals(2, response.hints!!.size)
            assertEquals(2, response.tags!!.size)
        }
    }

    @Test
    fun `Update a flashcard`() = testApplication {
        val client = testClient(this)
        val card = createCard(client)
        val update = FlashCard(front = "Updated", back = card.back)
        client.put("/cards/${card.id}"){
            contentType(ContentType.Application.Json)
            setBody(update)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response: FlashCard = body()
            assertNotNull(response)
            assertEquals("Updated", response.front)
            assertEquals(card.back, response.back)
        }
    }

    @Test
    fun `Soft delete a flashcard`() = testApplication {
        val client = testClient(this)
        val card = createCard(client)
        client.delete("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response: FlashCard = body()
            assertNotNull(response)
            assertTrue(response.deleted!!)
        }
    }

    @Test
    fun `Hard delete a flashcard`() = testApplication {
        val client = testClient(this)
        val card = createCard(client)
        client.delete("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.delete("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.NotFound, status,
                "A hard deletion occurs when a card marked for deletion is again deleted, " +
                        "removing it from the database. Any further requests for this therefore return a 404.")
        }
    }

    @Test
    fun `Hard delete a flashcard with entities attached`() = testApplication {
        val client = testClient(this)
        val card = createCard(client)
        val card2 = createCard(client, card.projectId)
        val card3 = createCard(client, card.projectId)
        createCardLink(client, card.id!!, FlashcardConnection(id = card2.id, related = RelationshipType.ONE_WAY.name))
        createCardLink(client, card.id!!, FlashcardConnection(id = card3.id, related = RelationshipType.TWO_WAY.name))
        createHint(client, card.id!!)
        createHint(client, card.id!!)
        createTag(client, card.id!!)
        createTag(client, card.id!!)

        client.delete("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.delete("/cards/${card.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
