import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Koulutustoimija } from './Koulutustoimija'
import { SuoritetutTutkinnotKorkeakoulunLisätiedot } from './SuoritetutTutkinnotKorkeakoulunLisatiedot'
import { SuoritetutTutkinnotKorkeakoulututkinnonSuoritus } from './SuoritetutTutkinnotKorkeakoulututkinnonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus`
 */
export type SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  koulutustoimija?: Koulutustoimija
  lisätiedot?: SuoritetutTutkinnotKorkeakoulunLisätiedot
  suoritukset: Array<SuoritetutTutkinnotKorkeakoulututkinnonSuoritus>
  oppilaitos?: Oppilaitos
}

export const SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
    koulutustoimija?: Koulutustoimija
    lisätiedot?: SuoritetutTutkinnotKorkeakoulunLisätiedot
    suoritukset?: Array<SuoritetutTutkinnotKorkeakoulututkinnonSuoritus>
    oppilaitos?: Oppilaitos
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
