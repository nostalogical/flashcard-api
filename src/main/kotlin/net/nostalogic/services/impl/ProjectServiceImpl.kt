package net.nostalogic.services.impl

import io.ktor.server.plugins.*
import net.nostalogic.api.dto.Project
import net.nostalogic.db.dao.ProjectDao
import net.nostalogic.services.ProjectService

class ProjectServiceImpl : ProjectService {

    companion object {
        const val MIN_NAME_LENGTH = 1;
        const val MAX_NAME_LENGTH = 50;
    }

    override fun createProject(project: Project): Project {
        validateProject(project)
        return ProjectDao.createProject(project)
    }

    override fun getAllProjects(): List<Project> {
        return ProjectDao.getAllProjects()
    }

    override fun deleteProject(projectId: String): Project {
        return ProjectDao.archiveProject(projectId) ?: throw NotFoundException()
    }

    override fun validateProject(project: Project?) {
        if (project == null)
            throw BadRequestException("Project data is required")
        if (project.name.isNullOrBlank())
            throw BadRequestException("Project name is required")
    }

}
