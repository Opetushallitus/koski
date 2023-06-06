import { AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso } from './AktiivisetJaPaattyneetOpinnotKorkeakoulunOpintojakso'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus'
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso
  vahvistus?: Vahvistus
  toimipiste?: Toimipiste
  tyyppi: Koodistokoodiviite<string, 'korkeakoulunopintojakso'>
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<string, 'korkeakoulunopintojakso'>
  }): AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus'
