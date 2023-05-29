import { Oppilaitos } from './Oppilaitos'
import { Koulutustoimija } from './Koulutustoimija'
import { SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus } from './SuoritetutTutkinnotYlioppilastutkinnonPaatasonSuoritus'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * Ylioppilastutkinnon opiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus`
 */
export type SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus'
  oppilaitos?: Oppilaitos
  koulutustoimija?: Koulutustoimija
  suoritukset: Array<SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus>
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
}

export const SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus = (
  o: {
    oppilaitos?: Oppilaitos
    koulutustoimija?: Koulutustoimija
    suoritukset?: Array<SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus>
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
  } = {}
): SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ylioppilastutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus',
  ...o
})

SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus' as const

export const isSuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus'
