import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'

/**
 * AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus'
    tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'aikuistenperusopetus'>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset: Array<AktiivisetJaPäättyneetOpinnotPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
  }

export const AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus =
  (
    o: {
      tyyppi?: Koodistokoodiviite<
        'opiskeluoikeudentyyppi',
        'aikuistenperusopetus'
      >
      sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
      oid?: string
      koulutustoimija?: Koulutustoimija
      versionumero?: number
      suoritukset?: Array<AktiivisetJaPäättyneetOpinnotPäätasonSuoritus>
      päättymispäivä?: string
      oppilaitos?: Oppilaitos
      tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
      alkamispäivä?: string
    } = {}
  ): AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'aikuistenperusopetus',
      koodistoUri: 'opiskeluoikeudentyyppi'
    }),
    suoritukset: [],
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus',
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: []
    }),
    ...o
  })

AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus'
