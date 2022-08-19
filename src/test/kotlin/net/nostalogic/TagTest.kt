package net.nostalogic

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.FlashCardTag
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TagTest : AppTest() {

    @Test
    fun `Create a card tag`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)

        val tag = FlashCardTag(name = "A tag name", value = "A tag value")
        client.post("/cards/${card1.id}/tags") {
            contentType(ContentType.Application.Json)
            setBody(tag)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val updatedTags: List<FlashCardTag> = body()
            assertNotNull(updatedTags)
            assertEquals(1, updatedTags.size)
            assertEquals(tag.name, updatedTags[0].name)
            assertEquals(tag.value, updatedTags[0].value)
            assertNotNull(updatedTags[0].id)
        }
    }

    @Test
    fun `Update a card tag`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val tag = createTag(client, card1.id!!)

        val update = FlashCardTag(name = tag.name, value =  "Update")
        client.put("/cards/${card1.id}/tags/${tag.id}") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedTags: List<FlashCardTag> = body()
            assertNotNull(updatedTags)
            assertEquals(1, updatedTags.size)
            assertEquals(tag.name, updatedTags[0].name)
            assertEquals(update.value, updatedTags[0].value)
            assertEquals(tag.id, updatedTags[0].id)
        }
    }

    @Test
    fun `Cannot duplicate existing tag name`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val tag = createTag(client, card1.id!!)

        val duplicate = FlashCardTag(name = tag.name, value =  "Update")
        client.post("/cards/${card1.id}/tags") {
            contentType(ContentType.Application.Json)
            setBody(duplicate)
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun `Updating a tag name will remove the old tag name`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val tag = createTag(client, card1.id!!)

        val update = FlashCardTag(name = "Changed", value =  "Update")
        client.put("/cards/${card1.id}/tags/${tag.id}") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedTags: List<FlashCardTag> = body()
            assertNotNull(updatedTags)
            assertEquals(1, updatedTags.size)
            assertEquals(update.name, updatedTags[0].name)
            assertEquals(update.value, updatedTags[0].value)
            assertEquals(tag.id, updatedTags[0].id,
                "This tag should have the same ID even if both name and value change")
        }

        client.post("/cards/${card1.id}/tags") {
            contentType(ContentType.Application.Json)
            setBody(tag)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val updatedTags: List<FlashCardTag> = body()
            assertNotNull(updatedTags)
            assertEquals(2, updatedTags.size,
                "Since the name on the original tag was changed, it should be possible to create a new tag with that name")
        }
    }

    @Test
    fun `A tag can have a null value`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val tag = createTag(client, card1.id!!, value = null)
        assertNull(tag.value)
    }

    @Test
    fun `An empty tag value is treated as null`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val tag = createTag(client, card1.id!!, value = " ")
        assertNull(tag.value)
    }

    @Test
    fun `A tag with a non-null value can be changed to null`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val tag = createTag(client, card1.id!!)

        val update = FlashCardTag(name = tag.name, value =  null)
        client.put("/cards/${card1.id}/tags/${tag.id}") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedTags: List<FlashCardTag> = body()
            assertNotNull(updatedTags)
            assertEquals(1, updatedTags.size)
            assertEquals(update.name, updatedTags[0].name)
            assertNull(update.value)
        }
    }

    @Test
    fun `Remove a card tag`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val tag = createTag(client, card1.id!!)

        client.delete("/cards/${card1.id}/tags/${tag.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val updatedTags: List<FlashCardTag> = body()
            assertNotNull(updatedTags)
            assertTrue(updatedTags.isEmpty())
        }
    }

    @Test
    fun `Retrieved cards contain tag details`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val tag = createTag(client, card1.id!!)

        client.get("/cards/${card1.id!!}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response: FlashCard = body()
            assertNotNull(response)
            assertNotNull(response.tags)
            assertEquals(1, response.tags!!.size)
            assertEquals(tag.id, response.tags!![0].id)
            assertEquals(tag.name, response.tags!![0].name)
            assertEquals(tag.value, response.tags!![0].value)
        }
    }

}
