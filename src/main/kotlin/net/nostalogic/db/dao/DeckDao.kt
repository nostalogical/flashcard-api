package net.nostalogic.db.dao

import io.ktor.server.plugins.*
import net.nostalogic.api.dto.Deck
import net.nostalogic.db.entities.DeckCardEntity
import net.nostalogic.db.entities.DeckEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object DeckDao {

    fun createDeck(_projectId: String, deck: Deck): Deck {
        val projectUuid: UUID = UUID.fromString(_projectId)
        val parentUuid: UUID? = deck.parent?.id?.let { parentId -> UUID.fromString(parentId) }
        val nextOrdinal = getNextDeckOrdinal(projectUuid, parentUuid)
        val created = transaction {
            DeckEntity.insert {
                it[name] = deck.name!!
                it[projectId] = projectUuid
                it[parentId] = parentUuid
                it[ordinal] = nextOrdinal
            }.resultedValues?.first().let { DeckEntity.toDto(it!!) }
        }
        return created
    }

    fun addDeckCard(_deckId: String, _cardId: String): Deck {
        val nextOrdinal = getNextDeckCardOrdinal(_deckId)
        transaction {
            DeckCardEntity.insert {
                it[cardId] = UUID.fromString(_cardId)
                it[deckId] = UUID.fromString(_deckId)
                it[ordinal] = nextOrdinal
            }
        }
        return getDeck(_deckId) ?: throw NotFoundException()
    }

    fun bulkAddDeckCards(_deckId: String, _cardIds: List<String>): Deck {
        var nextOrdinal = getNextDeckCardOrdinal(_deckId)
        transaction {
            DeckCardEntity.batchInsert(_cardIds) { _cardId ->
                this[DeckCardEntity.cardId] = UUID.fromString(_cardId)
                this[DeckCardEntity.deckId] = UUID.fromString(_deckId)
                this[DeckCardEntity.ordinal] = nextOrdinal++
            }
        }
        return getDeck(_deckId) ?: throw NotFoundException()
    }

    fun removeDeckCards(_deckId: String, _cardIds: Collection<String>): Deck {
        val cardUuids = _cardIds.map { UUID.fromString(it) }.toList()
        transaction {
            DeckCardEntity.deleteWhere { DeckCardEntity.cardId inList cardUuids and (DeckCardEntity.deckId eq UUID.fromString(_deckId)) }
        }
        return getDeck(_deckId) ?: throw NotFoundException()
    }

    fun isCardInDeck(_deckId: String, _cardId: String): Boolean {
        return transaction {
            DeckCardEntity.select {
                DeckCardEntity.cardId eq UUID.fromString(_cardId) and (DeckCardEntity.deckId eq UUID.fromString(_deckId))
            }.count() > 0
        }
    }

    fun getDeck(deckId: String, attachDetails: Boolean = true): Deck? {
        val deck = transaction {
            DeckEntity.select{ DeckEntity.id eq UUID.fromString(deckId) }.singleOrNull()?.let { DeckEntity.toDto(it) }
        }
        if (deck != null && attachDetails)
            populateDecks(listOf(deck))
        return deck
    }

    fun getDecksForProject(projectId: String): List<Deck> {
        val decks = transaction {
            DeckEntity.select { DeckEntity.projectId eq UUID.fromString(projectId) and (DeckEntity.parentId.isNull()) }
                .map { DeckEntity.toDto(it) }
        }
        populateDecks(decks)
        return decks
    }

    fun deleteDeck(deckId: String) {
        val deckUuid = UUID.fromString(deckId)
        deleteChildDecks(deckUuid)
        transaction {
            DeckEntity.deleteWhere { DeckEntity.id eq deckUuid }
        }
    }

    private fun deleteChildDecks(deckId: UUID) {
        val childDeckIds: List<UUID> = transaction {
            DeckEntity.slice(DeckEntity.id)
                .select { DeckEntity.parentId eq  deckId }
                .map { it[DeckEntity.id] }
        }
        childDeckIds.forEach { deleteChildDecks(it) }
        transaction {
            DeckEntity.deleteWhere { DeckEntity.id inList childDeckIds }
        }
    }

    private fun getNextDeckOrdinal(projectUuid: UUID, parentUuid: UUID?): Int {
        val ordinal = transaction {
            DeckEntity.slice(DeckEntity.ordinal.max())
                .select { DeckEntity.projectId eq projectUuid and (DeckEntity.parentId eq parentUuid) }
                .maxByOrNull { DeckEntity.ordinal }?.get(DeckEntity.ordinal.max())
        }
        return ordinal?.let { it + 1 } ?: 0
    }

    private fun getNextDeckCardOrdinal(deckId: String): Int {
        val ordinal = transaction {
            DeckCardEntity.slice(DeckCardEntity.ordinal.max()).select { DeckCardEntity.deckId eq UUID.fromString(deckId) }
                .maxByOrNull { DeckCardEntity.ordinal }?.get(DeckCardEntity.ordinal.max())
        }
        return ordinal?.let { it + 1 } ?: 0
    }

    private fun populateDecks(decks: List<Deck>) {
        val parentIds = decks.filter { it.parent != null }.map { UUID.fromString(it.parent?.id) }.toHashSet()
        val deckIds = decks.map { UUID.fromString(it.id) }.toHashSet()
        val associatedDecks: List<Deck> = transaction {
            DeckEntity.select { DeckEntity.parentId inList deckIds or (DeckEntity.id inList parentIds) }
                .map { DeckEntity.toDto(it) }
        }

        val parentDecks = HashMap<String, Deck>()
        val childDecks = HashMap<String, ArrayList<Deck>>()
        associatedDecks.forEach {
            val id = UUID.fromString(it.id)
            if (parentIds.contains(id))
                parentDecks[it.id!!] = it
            val parentId = it.parent?.let { parent -> UUID.fromString(parent.id) }
            if (deckIds.contains(parentId)) {
                it.parent = null
                childDecks.computeIfAbsent(parentId.toString()) { ArrayList() }.add(it)
            }
        }

        val cardByDeck = CardDao.getAllCardsInDecks(deckIds)
        decks.forEach {
            if (parentDecks.containsKey(it.parent?.id))
                it.parent = parentDecks[it.parent?.id]
            if (childDecks.containsKey(it.id))
                it.children = childDecks[it.id]
            it.cards = cardByDeck[it.id]
        }

    }

}
