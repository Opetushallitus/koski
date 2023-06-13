import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso } from './AktiivisetJaPaattyneetOpinnotKorkeakoulunOpintojakso'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus'
  tyyppi: Koodistokoodiviite<string, 'korkeakoulunopintojakso'>
  suorituskieli?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite<string, 'korkeakoulunopintojakso'>
    suorituskieli?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
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
