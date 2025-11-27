import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'vapaansivistystyonkoulutus'
  >
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
}

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'vapaansivistystyonkoulutus'
    >
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
  } = {}
): AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vapaansivistystyonkoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus',
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus'
