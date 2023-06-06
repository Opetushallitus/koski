import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus } from './AktiivisetJaPaattyneetOpinnotInternationalSchoolVuosiluokanSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschool',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus',
  ...o
})

AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus'
