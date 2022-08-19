package net.nostalogic

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.nostalogic.api.dto.Deck
import net.nostalogic.api.dto.FlashCard
import net.nostalogic.api.dto.Project
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ProjectsTest: AppTest() {

    @Test
    fun `No projects exist on startup`() = testApplication {
        val client = testClient(this)
        client.get("/projects").apply {
            assertEquals(HttpStatusCode.OK, status)
            val projects: List<Project> = body()
            assertNotNull(projects)
            assertTrue(projects.isEmpty())
        }
    }

    @Test
    fun `Create a flashcard project`() = testApplication {
        val client = testClient(this)

        val project = Project(name = "New project")
        val created: Project
        client.post("/projects") {
            contentType(ContentType.Application.Json)
            setBody(project)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            created = body()
            assertNotNull(created)
            assertEquals(project.name, created.name)
            assertFalse(created.archived!!)
        }

        client.get("/projects").apply {
            assertEquals(HttpStatusCode.OK, status)
            val projects: List<Project> = body()
            assertNotNull(projects)
            assertEquals(1, projects.size)
            assertEquals("A project should appear in the list all projects after being created",
                created, projects.first())
        }
    }

    @Test
    fun `Create multiple projects`() = testApplication {
        val client = testClient(this)

        createProject(client, "One")
        createProject(client, "Two")
        createProject(client, "Three")

        val projects: List<Project>
        client.get("/projects").apply {
            assertEquals(HttpStatusCode.OK, status)
            projects = body()
            assertNotNull(projects)
        }
        assertEquals(3, projects.size)
        val expectedOrder = listOf("One", "Two", "Three")
        for (name in expectedOrder) run {
            val project = projects[expectedOrder.indexOf(name)]
            assertEquals("Projects are expected to be listed in order of their creation time",
                name, project.name)
        }
    }

    @Test
    fun `Archive a project`() = testApplication {
        val client = testClient(this)

        val one = createProject(client, "One")
        createProject(client, "Two")

        client.delete("/projects/${one.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val deleted: Project = body()
            assertNotNull(deleted)
            assertEquals(one.id, deleted.id)
            assertTrue(deleted.archived!!)
        }

        val projects: List<Project>
        client.get("/projects").apply {
            assertEquals(HttpStatusCode.OK, status)
            projects = body()
            assertNotNull(projects)
        }
        assertEquals("After archiving a project it should no longer appear in the projects list",
            1, projects.size)
    }

    @Test
    fun `Create a card in a project`() = testApplication {
        val client = testClient(this)
        val one = createProject(client, "One")
        val payload = FlashCard(front = "Front", back = "Back")

        client.post("/projects/${one.id}/cards") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val created: FlashCard = body()
            assertNotNull(created)
            assertEquals(payload.front, created.front)
            assertEquals(payload.back, created.back)
            assertEquals(36, created.id?.length)
            assertEquals(one.id, created.projectId)
        }
    }

    @Test
    fun `Retrieve cards in project`() = testApplication {
        val client = testClient(this)
        val card1 = createCard(client)
        val card2 = createCard(client, card1.projectId)
        val card3 = createCard(client, card1.projectId)
        val expectedIds = hashSetOf(card1.id, card2.id, card3.id)

        client.get("/projects/${card1.projectId}/cards").apply {
            assertEquals(HttpStatusCode.OK, status)
            val projectCards: List<FlashCard> = body()
            assertNotNull(projectCards)
            assertEquals(3, projectCards.size)
            projectCards.forEach {
                assertContains(expectedIds, it.id)
                expectedIds.remove(it.id)
                assertEquals(card1.projectId, it.projectId)
            }
        }
    }

    @Test
    fun `Create a deck in a project`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)

        val payload = Deck(name = "Deck 1")
        client.post("/projects/${project.id}/decks") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val created: Deck = body()
            assertNotNull(created)
            assertEquals(project.id, created.projectId)
            assertEquals(payload.name, created.name)
        }
    }

    @Test
    fun `Get root decks in project`() = testApplication {
        val client = testClient(this)
        val project = createProject(client)

        val root1 = createDeck(client, project.id, name = "Deck 1")
        val root2 = createDeck(client, project.id, name = "Deck 2")
        createDeck(client, project.id, name = "Deck 3 (Nested)", root2.id)
        val expectedIds = hashSetOf(root1.id, root2.id)

        client.get("/projects/${project.id}/decks").apply {
            assertEquals(HttpStatusCode.OK, status)
            val decks: List<Deck> = body()
            assertNotNull(decks)
            assertEquals(2, decks.size)
            decks.forEach {
                assertContains(expectedIds, it.id)
                expectedIds.remove(it.id)
                assertEquals(project.id, it.projectId)
            }
        }
    }

}
