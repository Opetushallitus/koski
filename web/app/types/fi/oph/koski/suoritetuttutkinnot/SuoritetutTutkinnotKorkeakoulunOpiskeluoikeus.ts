import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SuoritetutTutkinnotKorkeakoulututkinnonSuoritus } from './SuoritetutTutkinnotKorkeakoulututkinnonSuoritus'
import { Oppilaitos } from './Oppilaitos'
import { Koulutustoimija } from './Koulutustoimija'
import { SuoritetutTutkinnotKorkeakoulunLisätiedot } from './SuoritetutTutkinnotKorkeakoulunLisatiedot'

/**
 * SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus`
 */
export type SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  suoritukset: Array<SuoritetutTutkinnotKorkeakoulututkinnonSuoritus>
  oppilaitos?: Oppilaitos
  koulutustoimija?: Koulutustoimija
  lisätiedot?: SuoritetutTutkinnotKorkeakoulunLisätiedot
}

export const SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
    suoritukset?: Array<SuoritetutTutkinnotKorkeakoulututkinnonSuoritus>
    oppilaitos?: Oppilaitos
    koulutustoimija?: Koulutustoimija
    lisätiedot?: SuoritetutTutkinnotKorkeakoulunLisätiedot
  } = {}
): SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus',
  ...o
})

SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus' as const

export const isSuoritetutTutkinnotKorkeakoulunOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus'
