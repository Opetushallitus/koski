import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus'
    tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'aikuistenperusopetus'>
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

export const AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus =
  (
    o: {
      tyyppi?: Koodistokoodiviite<
        'opiskeluoikeudentyyppi',
        'aikuistenperusopetus'
      >
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
  ): AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'aikuistenperusopetus',
      koodistoUri: 'opiskeluoikeudentyyppi'
    }),
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: []
    }),
    suoritukset: [],
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus'
