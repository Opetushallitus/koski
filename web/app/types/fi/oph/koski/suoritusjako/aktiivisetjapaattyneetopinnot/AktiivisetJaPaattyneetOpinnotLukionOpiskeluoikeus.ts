import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'lukiokoulutus'>
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotPäätasonSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'lukiokoulutus'>
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukiokoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus',
  ...o
})

AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus'
