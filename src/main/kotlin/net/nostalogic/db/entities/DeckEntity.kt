package net.nostalogic.db.entities

import net.nostalogic.api.dto.Deck
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object DeckEntity  : Table("deck") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 100)
    val projectId = reference("project_id", ProjectEntity.id)
    val parentId = reference("parent_id", id).nullable()
    val ordinal = integer("ordinal")
    override val primaryKey = PrimaryKey(id)

    fun toDto(rr: ResultRow) = Deck(
        id = rr[id].toString(),
        name = rr[name],
        projectId = rr[projectId].toString(),
        parent = rr[parentId]?.let { Deck(id = it.toString()) }
    )
}
