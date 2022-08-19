package net.nostalogic

import com.github.javafaker.Faker
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.testing.*
import net.nostalogic.api.dto.*
import net.nostalogic.db.DatabaseFactory
import org.junit.Assert
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class AppTest {

    protected fun testClient(testApp: ApplicationTestBuilder): HttpClient {
        return testApp.createClient {
            install(ContentNegotiation) {
                gson()
            }
        }
    }

    protected suspend fun createProject(client: HttpClient, name: String = "Test Project"): Project {
        var project = Project(name = name)
        client.post("/projects") {
            contentType(ContentType.Application.Json)
            setBody(project)
        }.apply {
            Assert.assertEquals(HttpStatusCode.Created, status)
            project = body()
        }
        return project
    }

    /**
     * Creates a flashcard with random details within the specified project. If no project ID is specified, one will be
     * automatically created.
     */
    protected suspend fun createCard(client: HttpClient, projectId: String? = null): FlashCard {
        val definiteProjectId: String = projectId ?: createProject(client).id!!
        val country = Faker.instance().country()
        val payload = FlashCard(
            front = country.name(),
            back = country.capital()
        )
        client.post("/projects/${definiteProjectId}/cards") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.apply {
            val createdCard: FlashCard = body()
            assertNotNull(createdCard)
            return createdCard
        }
    }

    protected suspend fun createDeck(client: HttpClient, projectId: String? = null,
                 name: String = "Test Deck", parentId: String? = null): Deck {
        val definiteProjectId: String = projectId ?: createProject(client).id!!
        val parent: Deck? = parentId?.let { Deck(id = it) }
        val payload = Deck(
            name = name,
            parent = parent
        )
        client.post("/projects/${definiteProjectId}/decks"){
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.apply {
            Assert.assertEquals(HttpStatusCode.Created, status)
            val createdDeck: Deck = body()
            assertNotNull(createdDeck)
            return createdDeck
        }
    }

    protected suspend fun createCardLink(client: HttpClient, card1: String, link: FlashcardConnection) {
        client.put("/cards/${card1}/links") {
            contentType(ContentType.Application.Json)
            setBody(link)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    protected suspend fun createHint(client: HttpClient, cardId: String,
                           hint: String = Faker.instance().hitchhikersGuideToTheGalaxy().quote()): FlashcardHint {
        client.post("/cards/${cardId}/hints") {
            contentType(ContentType.Application.Json)
            setBody(FlashcardHint(hint = hint))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val updatedHints: List<FlashcardHint> = body()
            assertNotNull(updatedHints)
            assertTrue(updatedHints.isNotEmpty())
            return updatedHints.last()
        }
    }

    protected suspend fun createTag(client: HttpClient, cardId: String,
                                    name: String = Faker.instance().hacker().ingverb(),
                                    value: String? = Faker.instance().hacker().adjective()): FlashCardTag {
        val tag = FlashCardTag(name = name, value = value)
        client.post("/cards/${cardId}/tags") {
            contentType(ContentType.Application.Json)
            setBody(tag)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val updatedTags: List<FlashCardTag> = body()
            assertNotNull(updatedTags)
            assertTrue(updatedTags.isNotEmpty())
            return updatedTags.last()
        }
    }

    @AfterTest
    fun teardown() {
        DatabaseFactory.wipe()
    }

}
