package net.nostalogic.db.entities

import net.nostalogic.api.dto.FlashCard
import net.nostalogic.db.entities.ProjectEntity.default
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object CardEntity : Table("card") {
    val id = uuid("id").autoGenerate()
    val projectId = reference("project_id", ProjectEntity.id)
    val deleted = bool("deleted").default(false)
    val front = text("front")
    val back = text("back")
    override val primaryKey = PrimaryKey(id)

    fun toDto(rr: ResultRow) = FlashCard(
        id = rr[id].toString(),
        front = rr[front],
        back = rr[back],
        projectId = rr[projectId].toString(),
        deleted = rr[deleted],
    )
}
