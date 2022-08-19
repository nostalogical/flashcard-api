package net.nostalogic

import net.nostalogic.services.*
import net.nostalogic.services.impl.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val standardServiceBinds = module {
    singleOf(::ProjectServiceImpl) { bind<ProjectService>() }
    singleOf(::HintServiceImpl) { bind<HintService>() }
    singleOf(::LinkServiceImpl) { bind<LinkService>() }
    singleOf(::TagServiceImpl) { bind<TagService>() }
    singleOf(::CardServiceImpl) { bind<CardService>() }
    singleOf(::DeckServiceImpl) { bind<DeckService>() }
}
