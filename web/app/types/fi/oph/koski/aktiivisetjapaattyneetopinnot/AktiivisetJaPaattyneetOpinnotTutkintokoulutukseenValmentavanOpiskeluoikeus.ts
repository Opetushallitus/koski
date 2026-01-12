import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { AktiivisetJaPäättyneetOpinnotPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot } from './AktiivisetJaPaattyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisatiedot'

/**
 * AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus'
    tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset: Array<AktiivisetJaPäättyneetOpinnotPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot
  }

export const AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus =
  (
    o: {
      tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
      sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
      oid?: string
      versionumero?: number
      suoritukset?: Array<AktiivisetJaPäättyneetOpinnotPäätasonSuoritus>
      päättymispäivä?: string
      oppilaitos?: Oppilaitos
      tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
      alkamispäivä?: string
      koulutustoimija?: Koulutustoimija
      lisätiedot?: AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot
    } = {}
  ): AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'tuva',
      koodistoUri: 'opiskeluoikeudentyyppi'
    }),
    suoritukset: [],
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus',
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: []
    }),
    ...o
  })

AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus'
