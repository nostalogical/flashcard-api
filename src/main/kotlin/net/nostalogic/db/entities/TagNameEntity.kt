package net.nostalogic.db.entities

import org.jetbrains.exposed.sql.Table

object TagNameEntity : Table("tag") {
    val id = uuid("id").autoGenerate()
    val projectId = reference("project_id", ProjectEntity.id)
    val tagName = varchar("tag_name", 200).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}
