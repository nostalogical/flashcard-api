package net.nostalogic.db.dao

import net.nostalogic.api.dto.Project
import net.nostalogic.db.entities.ProjectEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object ProjectDao {

    fun createProject(project: Project): Project {
        val nextOrdinal = getNextOrdinal()
        val created = transaction {
            ProjectEntity.insert {
                it[name] = project.name!!
                it[ordinal] = nextOrdinal
            }.resultedValues?.first().let { ProjectEntity.toDto(it!!) }
        }
        return created
    }

    fun getAllProjects(): List<Project> {
        val projects = transaction {
            ProjectEntity.select { ProjectEntity.archived eq false }
                .orderBy(ProjectEntity.ordinal).map { ProjectEntity.toDto(it) }
        }
        return projects
    }

    private fun getProject(projectId: String): Project? {
        val project = transaction {
            ProjectEntity.select { ProjectEntity.id eq UUID.fromString(projectId) }
                .singleOrNull()?.let { ProjectEntity.toDto(it) }
        }
        return project
    }

    private fun getNextOrdinal(): Int {
        val ordinal = transaction {
            ProjectEntity.slice(ProjectEntity.ordinal.max()).selectAll()
                .maxByOrNull { ProjectEntity.ordinal }?.get(ProjectEntity.ordinal.max())
        }
        return ordinal?.let { it + 1 } ?: 0
    }

    fun archiveProject(projectId: String): Project? {
        transaction {
            ProjectEntity.update({ ProjectEntity.id eq UUID.fromString(projectId) }) {
                it[archived] = true
            }
        }
        return getProject(projectId)
    }

    fun clearAll() {
        transaction {
            ProjectEntity.deleteAll()
        }
    }

}
