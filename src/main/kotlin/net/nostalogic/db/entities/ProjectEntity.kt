package net.nostalogic.db.entities

import net.nostalogic.api.dto.Project
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object ProjectEntity  : Table("project") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 50)
    val archived = bool("archived").default(false)
    val ordinal = integer("ordinal")
    override val primaryKey = PrimaryKey(id)

    fun toDto(rr: ResultRow) = Project(id = rr[id].toString(), name = rr[name], archived = rr[archived])
}
