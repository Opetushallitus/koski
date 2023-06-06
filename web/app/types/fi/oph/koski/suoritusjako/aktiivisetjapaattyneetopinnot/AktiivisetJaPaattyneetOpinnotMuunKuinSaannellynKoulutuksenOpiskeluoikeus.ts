import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeudenTila'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus } from './AktiivisetJaPaattyneetOpinnotMuunKuinSaannellynKoulutuksenPaatasonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus'
    tyyppi: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'muukuinsaanneltykoulutus'
    >
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
    alkamispäivä?: string
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset: Array<AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus>
    päättymispäivä?: string
    oppilaitos?: Oppilaitos
  }

export const AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus =
  (
    o: {
      tyyppi?: Koodistokoodiviite<
        'opiskeluoikeudentyyppi',
        'muukuinsaanneltykoulutus'
      >
      tila?: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
      alkamispäivä?: string
      sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
      oid?: string
      koulutustoimija?: Koulutustoimija
      versionumero?: number
      suoritukset?: Array<AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus>
      päättymispäivä?: string
      oppilaitos?: Oppilaitos
    } = {}
  ): AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'muukuinsaanneltykoulutus',
      koodistoUri: 'opiskeluoikeudentyyppi'
    }),
    tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: []
    }),
    suoritukset: [],
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus' as const

export const isAktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus'
