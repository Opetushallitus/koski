import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotYlioppilastutkinnonPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'

/**
 * Ylioppilastutkinnon opiskeluoikeus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
  koulutustoimija?: Koulutustoimija
  suoritukset: Array<AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
  alkamispäivä?: string
}

export const AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
    koulutustoimija?: Koulutustoimija
    suoritukset?: Array<AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
    tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
  } = {}
): AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ylioppilastutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus',
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus'
