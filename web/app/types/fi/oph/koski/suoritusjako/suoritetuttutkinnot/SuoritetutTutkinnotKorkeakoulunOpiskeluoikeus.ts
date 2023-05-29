import { Oppilaitos } from './Oppilaitos'
import { Koulutustoimija } from './Koulutustoimija'
import { SuoritetutTutkinnotKorkeakoulututkinnonSuoritus } from './SuoritetutTutkinnotKorkeakoulututkinnonSuoritus'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus`
 */
export type SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus'
  oppilaitos?: Oppilaitos
  koulutustoimija?: Koulutustoimija
  suoritukset: Array<SuoritetutTutkinnotKorkeakoulututkinnonSuoritus>
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
}

export const SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus = (
  o: {
    oppilaitos?: Oppilaitos
    koulutustoimija?: Koulutustoimija
    suoritukset?: Array<SuoritetutTutkinnotKorkeakoulututkinnonSuoritus>
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  } = {}
): SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus',
  ...o
})

SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus' as const

export const isSuoritetutTutkinnotKorkeakoulunOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus'
