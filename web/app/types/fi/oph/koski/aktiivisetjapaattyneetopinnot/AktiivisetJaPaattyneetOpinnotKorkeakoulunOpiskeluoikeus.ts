import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus } from './AktiivisetJaPaattyneetOpinnotKorkeakouluSuoritus'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Oppilaitos } from './Oppilaitos'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot } from './AktiivisetJaPaattyneetOpinnotKorkeakoulunOpiskeluoikeudenLisatiedot'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus>
  päättymispäivä?: string
  luokittelu?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
  oppilaitos?: Oppilaitos
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus>
    päättymispäivä?: string
    luokittelu?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    oppilaitos?: Oppilaitos
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot
  } = {}
): AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus',
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus'
