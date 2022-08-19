package net.nostalogic.services

import net.nostalogic.api.dto.Project

interface ProjectService {

    fun createProject(project: Project): Project
    fun getAllProjects(): List<Project>
    fun validateProject(project: Project?)
    fun deleteProject(projectId: String): Project

}
