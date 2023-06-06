import { AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus } from './AktiivisetJaPaattyneetOpinnotVapaanSivistyonKoulutus'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus'
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
  tyyppi: Koodistokoodiviite<
    string,
    | 'vstmaahanmuuttajienkotoutumiskoulutus'
    | 'vstoppivelvollisillesuunnattukoulutus'
    | 'vstjotpakoulutus'
    | 'vstlukutaitokoulutus'
  >
}

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<
      string,
      | 'vstmaahanmuuttajienkotoutumiskoulutus'
      | 'vstoppivelvollisillesuunnattukoulutus'
      | 'vstjotpakoulutus'
      | 'vstlukutaitokoulutus'
    >
  }): AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus'
