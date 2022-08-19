package net.nostalogic

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.nostalogic.api.dto.Deck
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DecksTest : AppTest() {

    @Test
    fun `Get a deck by ID`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        createDeck(client, project.id, name = "Deck 1")
        val root2 = createDeck(client, project.id, name = "Deck 2")
        val nested1 = createDeck(client, project.id, name = "Deck 3 (Nested)", root2.id)
        val nested2 = createDeck(client, project.id, name = "Deck 4 (Nested)", nested1.id)
        val nested3 = createDeck(client, project.id, name = "Deck 5 (Nested)", nested1.id)
        val expectedIds = hashSetOf(nested2.id, nested3.id)
        val expectedNames = hashSetOf(nested2.name, nested3.name)

        client.get("/decks/${nested1.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val deck: Deck = body()
            assertNotNull(deck)
            assertEquals(nested1.id, deck.id)
            assertEquals(nested1.name, deck.name)
            assertEquals(nested1.projectId, deck.projectId)
            assertNotNull(deck.parent,
                "If a deck has a parent deck it should be present in the payload")
            assertEquals(root2.id, deck.parent!!.id)
            assertEquals(root2.name, deck.parent!!.name)
            assertNotNull(deck.children,
                "If a deck has child decks they should be present in the payload")
            assertEquals(2, deck.children!!.size)
            deck.children!!.forEach {
                assertContains(expectedIds, it.id)
                expectedIds.remove(it.id)
                assertContains(expectedNames, it.name)
                expectedNames.remove(it.name)
                assertEquals(project.id, it.projectId)
            }
        }
    }

    @Test
    fun `Delete a deck by ID`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val deck = createDeck(client, project.id, name = "Deck 1")
        client.delete("/decks/${deck.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/decks/${deck.id}").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun `Delete a deck with child decks`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val deck = createDeck(client, project.id, name = "Deck 1")
        val childDeck = createDeck(client, project.id, name = "Deck 2", parentId = deck.id)
        createDeck(client, project.id, name = "Deck 3", parentId = childDeck.id)
        client.delete("/decks/${deck.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `Add a card to a deck`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val deck = createDeck(client, project.id, name = "Deck 1")
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)
        client.post("/decks/${deck.id}/cards/${card1.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedDeck: Deck = body()
            assertNotNull(updatedDeck.cards)
            assertEquals(1, updatedDeck.cards?.size)
        }
        client.post("/decks/${deck.id}/cards/${card2.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedDeck: Deck = body()
            assertNotNull(updatedDeck.cards)
            assertEquals(2, updatedDeck.cards?.size)
            assertEquals(card1.id, updatedDeck.cards!![0].id)
            assertEquals(card1.front, updatedDeck.cards!![0].front)
            assertEquals(card2.id, updatedDeck.cards!![1].id)
            assertEquals(card2.front, updatedDeck.cards!![1].front)
        }
    }

    @Test
    fun `Bulk add cards to a deck`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val deck = createDeck(client, project.id, name = "Deck 1")
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)
        val card3 = createCard(client, project.id)
        val card4 = createCard(client, project.id)
        val card5 = createCard(client, project.id)
        val cardIds: List<String> = listOf(card1.id!!, card2.id!!, card3.id!!, card4.id!!, card5.id!!)
        client.put("/decks/${deck.id}/cards") {
            contentType(ContentType.Application.Json)
            setBody(cardIds)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedDeck: Deck = body()
            assertNotNull(updatedDeck.cards)
            assertEquals(5, updatedDeck.cards?.size)
            val deckCardIs: List<String> = updatedDeck.cards!!.map { it.id!! }
            assertEquals(deckCardIs, cardIds)
        }
    }

    @Test
    fun `Remove a card from a deck`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val deck = createDeck(client, project.id, name = "Deck 1")
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)
        client.post("/decks/${deck.id}/cards/${card1.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.post("/decks/${deck.id}/cards/${card2.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.delete("/decks/${deck.id}/cards/${card1.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedDeck: Deck = body()
            assertNotNull(updatedDeck.cards)
            assertEquals(1, updatedDeck.cards?.size)
            assertEquals(card2.id, updatedDeck.cards!![0].id)
            assertEquals(card2.front, updatedDeck.cards!![0].front)
        }
    }

    @Test
    fun `Bulk remove cards from deck`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)
        val deck = createDeck(client, project.id, name = "Deck 1")
        val card1 = createCard(client, project.id)
        val card2 = createCard(client, project.id)
        val card3 = createCard(client, project.id)
        val card4 = createCard(client, project.id)
        val card5 = createCard(client, project.id)
        val cardIds: List<String> = listOf(card1.id!!, card2.id!!, card3.id!!, card4.id!!, card5.id!!)
        client.put("/decks/${deck.id}/cards") {
            contentType(ContentType.Application.Json)
            setBody(cardIds)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        val cardsToRemove: List<String> = listOf(card1.id!!, card3.id!!, card5.id!!)
        val expectedCards: List<String> = listOf(card2.id!!, card4.id!!)
        client.post("/decks/${deck.id}/cards/remove") {
            contentType(ContentType.Application.Json)
            setBody(cardsToRemove)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedDeck: Deck = body()
            assertNotNull(updatedDeck.cards)
            assertEquals(2, updatedDeck.cards?.size)
            val deckCardIs: List<String> = updatedDeck.cards!!.map { it.id!! }
            assertEquals(deckCardIs, expectedCards)
        }
    }

}
