import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot } from './AktiivisetJaPaattyneetOpinnotKorkeakoulunOpiskeluoikeudenLisatiedot'
import { AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus } from './AktiivisetJaPaattyneetOpinnotKorkeakouluSuoritus'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus>
  päättymispäivä?: string
  luokittelu?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
  oppilaitos?: Oppilaitos
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus>
    päättymispäivä?: string
    luokittelu?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    oppilaitos?: Oppilaitos
  } = {}
): AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus',
  ...o
})

AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus'
