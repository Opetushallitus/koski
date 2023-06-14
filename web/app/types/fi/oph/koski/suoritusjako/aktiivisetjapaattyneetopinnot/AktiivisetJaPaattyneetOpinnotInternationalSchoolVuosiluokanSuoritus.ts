import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli } from './AktiivisetJaPaattyneetOpinnotInternationalSchoolKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus'
    tyyppi: Koodistokoodiviite<
      string,
      | 'internationalschoolmypvuosiluokka'
      | 'internationalschooldiplomavuosiluokka'
    >
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite<
      string,
      | 'internationalschoolmypvuosiluokka'
      | 'internationalschooldiplomavuosiluokka'
    >
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli
    toimipiste?: Toimipiste
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus'
