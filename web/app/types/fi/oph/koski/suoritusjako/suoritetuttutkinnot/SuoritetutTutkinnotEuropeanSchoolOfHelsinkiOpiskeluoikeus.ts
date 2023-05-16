import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Koulutustoimija } from './Koulutustoimija'
import { SuoritetutTutkinnotEBTutkinnonSuoritus } from './SuoritetutTutkinnotEBTutkinnonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus`
 */
export type SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'europeanschoolofhelsinki'
  >
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<SuoritetutTutkinnotEBTutkinnonSuoritus>
  oppilaitos?: Oppilaitos
}

export const SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'europeanschoolofhelsinki'
    >
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<SuoritetutTutkinnotEBTutkinnonSuoritus>
    oppilaitos?: Oppilaitos
  } = {}
): SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinki',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus',
  ...o
})

SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus' as const

export const isSuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus'
