import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot } from './AktiivisetJaPaattyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisatiedot'
import { AktiivisetJaPäättyneetOpinnotPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotPaatasonSuoritus'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus'
    tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset: Array<AktiivisetJaPäättyneetOpinnotPäätasonSuoritus>
    päättymispäivä?: string
    järjestämislupa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    oppilaitos?: Oppilaitos
  }

export const AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus =
  (o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotPäätasonSuoritus>
    päättymispäivä?: string
    järjestämislupa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    oppilaitos?: Oppilaitos
  }): AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'tuva',
      koodistoUri: 'opiskeluoikeudentyyppi'
    }),
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: []
    }),
    suoritukset: [],
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus'
