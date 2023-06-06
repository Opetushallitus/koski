import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot } from './AktiivisetJaPaattyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisatiedot'
import { AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotAmmatillinenPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ammatillinenkoulutus'>
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'ammatillinenkoulutus'
    >
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinenkoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus',
  ...o
})

AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus'
