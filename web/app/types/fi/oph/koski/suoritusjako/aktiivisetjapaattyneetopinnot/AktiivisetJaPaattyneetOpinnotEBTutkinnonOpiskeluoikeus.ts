import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotEBTutkinnonPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ebtutkinto'>
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ebtutkinto'>
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus',
  ...o
})

AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus'
