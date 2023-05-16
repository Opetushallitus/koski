import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { SuoritetutTutkinnotAmmatillinenPäätasonSuoritus } from './SuoritetutTutkinnotAmmatillinenPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * SuoritetutTutkinnotAmmatillinenOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillinenOpiskeluoikeus`
 */
export type SuoritetutTutkinnotAmmatillinenOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillinenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ammatillinenkoulutus'>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<SuoritetutTutkinnotAmmatillinenPäätasonSuoritus>
  oppilaitos?: Oppilaitos
}

export const SuoritetutTutkinnotAmmatillinenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'ammatillinenkoulutus'
    >
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<SuoritetutTutkinnotAmmatillinenPäätasonSuoritus>
    oppilaitos?: Oppilaitos
  } = {}
): SuoritetutTutkinnotAmmatillinenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinenkoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillinenOpiskeluoikeus',
  ...o
})

SuoritetutTutkinnotAmmatillinenOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillinenOpiskeluoikeus' as const

export const isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotAmmatillinenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillinenOpiskeluoikeus'
