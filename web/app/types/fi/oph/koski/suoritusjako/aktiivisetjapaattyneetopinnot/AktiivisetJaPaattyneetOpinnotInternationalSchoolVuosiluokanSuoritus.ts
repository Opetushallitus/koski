import { AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli } from './AktiivisetJaPaattyneetOpinnotInternationlSchoolKoulutusmoduuli'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus'
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<
      string,
      | 'internationalschoolmypvuosiluokka'
      | 'internationalschooldiplomavuosiluokka'
    >
  }

export const AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<
      string,
      | 'internationalschoolmypvuosiluokka'
      | 'internationalschooldiplomavuosiluokka'
    >
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
