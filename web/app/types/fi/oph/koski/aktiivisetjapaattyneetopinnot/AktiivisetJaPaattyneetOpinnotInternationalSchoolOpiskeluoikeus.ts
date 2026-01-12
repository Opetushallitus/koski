import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus } from './AktiivisetJaPaattyneetOpinnotInternationalSchoolVuosiluokanSuoritus'
import { Oppilaitos } from './Oppilaitos'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'

/**
 * AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
}

export const AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
  } = {}
): AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschool',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus',
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus'
