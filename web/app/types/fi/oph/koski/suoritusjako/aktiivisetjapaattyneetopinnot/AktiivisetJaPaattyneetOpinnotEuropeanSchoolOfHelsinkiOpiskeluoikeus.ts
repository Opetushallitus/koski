import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotEuropeanSchoolOfHelsinkiPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus'
    tyyppi: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'europeanschoolofhelsinki'
    >
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset: Array<AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
  }

export const AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =
  (
    o: {
      tyyppi?: Koodistokoodiviite<
        'opiskeluoikeudentyyppi',
        'europeanschoolofhelsinki'
      >
      tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
      alkamispäivä?: string
      sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
      oid?: string
      koulutustoimija?: Koulutustoimija
      versionumero?: number
      suoritukset?: Array<AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus>
      päättymispäivä?: string
      oppilaitos?: Oppilaitos
    } = {}
  ): AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'europeanschoolofhelsinki',
      koodistoUri: 'opiskeluoikeudentyyppi'
    }),
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: []
    }),
    suoritukset: [],
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus'
