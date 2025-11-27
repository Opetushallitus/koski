import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotEuropeanSchoolOfHelsinkiPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'

/**
 * AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus'
    tyyppi: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'europeanschoolofhelsinki'
    >
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset: Array<AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
  }

export const AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =
  (
    o: {
      tyyppi?: Koodistokoodiviite<
        'opiskeluoikeudentyyppi',
        'europeanschoolofhelsinki'
      >
      sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
      oid?: string
      koulutustoimija?: Koulutustoimija
      versionumero?: number
      suoritukset?: Array<AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus>
      päättymispäivä?: string
      oppilaitos?: Oppilaitos
      tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
      alkamispäivä?: string
    } = {}
  ): AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'europeanschoolofhelsinki',
      koodistoUri: 'opiskeluoikeudentyyppi'
    }),
    suoritukset: [],
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus',
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: []
    }),
    ...o
  })

AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus'
