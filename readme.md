# Flashcard API

This is an API for handling flashcard collections.

# Purpose

This API allows user defined and managed flashcards to be created and "used". 

## Projects
Flashcards are split into **projects** at the highest level, for example _German_ and _French_ could be separate 
projects for someone learning both languages.

## Flashcards
Within a project **flashcards** can then be created. A flashcard has a front and a back, these are both free text 
fields. The intended usage is that the front and back of each flashcard are consistently the same type. For example in 
a _German_ project the front would always be a German word and the back would be the English word. For a project called 
_Physics_ the front could be "Newton's first law" and the back would be "_F = ma_". Consistent front and back allows
flashcards to be properly revised in bulk.

## Flashcard Properties
Flashcards can also have a number of properties attached to them. These are intended to allow attaching additional data
to each flashcard or connect them to other flashcards. The reason for this is that the more connections a person can 
make to a concept, the more likely they are to remember it. Besides numerous "flash" sessions with the cards, user
added properties are the best way to learn.

### Tags

A **tag** is name-value combination that can be attached to a card. All tags must have a name, but a value is optional,
allowing some flexibility in how they're used. For example in a language project, tags with names like "verb" and 
"adverb" could be used without values attached to categorise flashcards as these types. This would provide a quick way
to see if a word is a verb by checking its tag, and a way to search for all verbs or nouns by filtering on that tag.

Using values with tag names allows further data or filtering. For example a tag with the name "tense" could be created,
created, and used with values such as "past" or "present".

### Hints

A **hint** is a free text field that can be attached to a flashcard. This is intended to allow facts or reminders to be 
attached to a card to help the user remember what the back of the card is.

### Links

A **link** is a connection between two cards. A link can have a description, and can also be marked as "related" or 
"mistakable". The purpose of these link types is to distinguish the context of card relationships. For example, a 
"related" card is particularly relevant to show alongside tags and hints when browsing through flashcards to show 
anything related to it. A "mistakable" may not be related at all, but should be prioritised if trying to match a card 
front to the matching back. For example, the German words for "week" and "wake up" are "woche" and "wachen". These are 
unrelated but could easily be confused for each other.

### Decks

A **deck** is a sub-collection of cards within a project. Decks provide a way to break down large collections of 
flashcards into smaller, more manageable collections.

Decks can be nested, so one deck can contain multiple other decks to subdivide collections of flashcards.

Any flashcard in a project can be added to any deck without being permanently tied to that deck. For example one card 
can be added to multiple decks, and if any deck is deleted, any cards within it will remain in the project.

# Technical Details
This project is written in Kotlin, and using the [Ktor](https://ktor.io/docs/welcome.html) framework for the REST API 
and [Exposed](https://github.com/JetBrains/Exposed) for database interactions.

Test driven development is used, so any features or changes are done by adding new tests, primarily of endpoints. The 
test configuration uses an in memory h2 database and the base test class provides helper methods for creating entities 
through the API. 

## Frameworks

### Application
**Ktor** is the primary framework used in this application, handling server initialisation and configuration, and 
routing of requests and responses.
* https://ktor.io/docs/welcome.html
* https://ktor.io/docs/testing.html

Ktor can be used for both server and client development, this core application only uses server side code. For test 
purposes (retrieving data from the server endpoints) some Ktor client dependencies are used in the test scope.

### Database
**Exposed** is used for database interactions. This is a Kotlin specific framework but not specifically tied to Ktor. 
Examples of how this is used can be found in the Dao classes in this project, and further information can be found 
in these guides:
* https://www.baeldung.com/kotlin/exposed-persistence
* https://github.com/JetBrains/Exposed/wiki

### Dependencies
Dependency injection, specifically of services, is handled through the **Koin** framework, which is Ktor compatible.
* https://insert-koin.io/
