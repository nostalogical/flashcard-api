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
import kotlin.test.assertNull

class LinkTest : AppTest() {

    @Test
    fun `Link one card to another with minimum details`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)

        val link = FlashcardConnection(id = card2.id, related = RelationshipType.TWO_WAY.name)
        client.put("/cards/${card1.id}/links") {
            contentType(ContentType.Application.Json)
            setBody(link)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedLinks: List<FlashcardConnection> = body()
            assertNotNull(updatedLinks)
            assertEquals(1, updatedLinks.size)
            assertEquals(card2.id!!, updatedLinks[0].id)
        }
    }

    @Test
    fun `Link multiple cards with full details`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)
        val card3 = createCard(client, project.id)

        val link1 = FlashcardConnection(id = card1.id,
            related = RelationshipType.TWO_WAY.name,
            mistakable = RelationshipType.REVERSE.name,
            description = "Link created via card 2. Directional relationships should be reversed on card 1."
        )
        client.put("/cards/${card2.id}/links") {
            contentType(ContentType.Application.Json)
            setBody(link1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        val link2 = FlashcardConnection(id = card3.id,
            related = RelationshipType.ONE_WAY.name,
        )
        client.put("/cards/${card1.id}/links") {
            contentType(ContentType.Application.Json)
            setBody(link2)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedLinks: List<FlashcardConnection> = body()
            assertNotNull(updatedLinks)
            assertEquals(2, updatedLinks.size)
            assertEquals(card2.id!!, updatedLinks[0].id)
            assertEquals(card2.front!!, updatedLinks[0].front)
            assertEquals(card2.back!!, updatedLinks[0].back)
            assertEquals(link1.description, updatedLinks[0].description)
            assertEquals(RelationshipType.TWO_WAY.name, updatedLinks[0].related)
            assertEquals(
                RelationshipType.ONE_WAY.name, updatedLinks[0].mistakable,
                message = "This relationship was created on card 2, so when viewed on card 1 it should be reversed.")
            assertEquals(card3.id!!, updatedLinks[1].id)
            assertEquals(card3.front!!, updatedLinks[1].front)
            assertEquals(card3.back!!, updatedLinks[1].back)
            assertEquals(link2.description, updatedLinks[1].description)
            assertEquals(RelationshipType.ONE_WAY.name, updatedLinks[1].related)
            assertNull(updatedLinks[1].mistakable)
        }
    }

    @Test
    fun `Update a linked card`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)
        createCardLink(client, card1.id!!, FlashcardConnection(id = card2.id, related = RelationshipType.TWO_WAY.name))
        val card3 = createCard(client, project.id)
        createCardLink(client, card1.id!!, FlashcardConnection(id = card3.id, related = RelationshipType.TWO_WAY.name))
        val card4 = createCard(client, project.id)
        createCardLink(client, card1.id!!, FlashcardConnection(id = card4.id, related = RelationshipType.TWO_WAY.name))

        val updateLink = FlashcardConnection(id = card3.id,
            related = RelationshipType.ONE_WAY.name,
            mistakable = RelationshipType.REVERSE.name,
            description = "Updated",
        )
        client.put("/cards/${card1.id}/links") {
            contentType(ContentType.Application.Json)
            setBody(updateLink)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedLinks: List<FlashcardConnection> = body()
            assertNotNull(updatedLinks)
            assertEquals(3, updatedLinks.size)
            assertEquals(card3.id!!, updatedLinks[1].id)
            assertEquals(card3.front!!, updatedLinks[1].front)
            assertEquals(card3.back!!, updatedLinks[1].back)
            assertEquals(updateLink.description, updatedLinks[1].description)
            assertEquals(updateLink.related, updatedLinks[1].related)
            assertEquals(updateLink.mistakable, updatedLinks[1].mistakable)
        }
    }

    @Test
    fun `Remove a linked card`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)
        createCardLink(client, card1.id!!, FlashcardConnection(id = card2.id, related = RelationshipType.TWO_WAY.name))
        val card3 = createCard(client, project.id)
        createCardLink(client, card1.id!!, FlashcardConnection(id = card3.id, related = RelationshipType.TWO_WAY.name))
        val card4 = createCard(client, project.id)
        createCardLink(client, card1.id!!, FlashcardConnection(id = card4.id, related = RelationshipType.TWO_WAY.name))

        client.delete("/cards/${card1.id!!}/links/${card3.id!!}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedLinks: List<FlashcardConnection> = body()
            assertNotNull(updatedLinks)
            assertEquals(2, updatedLinks.size)
            assertEquals(card2.id!!, updatedLinks[0].id)
            assertEquals(card4.id!!, updatedLinks[1].id)
        }
    }

    @Test
    fun `Retrieved cards contain link details`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)
        createCardLink(client, card1.id!!, FlashcardConnection(id = card2.id, related = RelationshipType.TWO_WAY.name))
        client.get("/cards/${card1.id!!}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response: FlashCard = body()
            assertNotNull(response)
            assertNotNull(response.connected)
            assertEquals(1, response.connected!!.size)
            assertEquals(card2.id, response.connected!![0].id)
            assertEquals(card2.front, response.connected!![0].front)
        }
    }

}
