import { AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli } from './AktiivisetJaPaattyneetOpinnotESHKoulutusmoduuli'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus'
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<
      string,
      | 'europeanschoolofhelsinkivuosiluokkasecondarylower'
      | 'europeanschoolofhelsinkivuosiluokkasecondaryupper'
      | 'ebtutkinto'
    >
  }

export const AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite<
      string,
      | 'europeanschoolofhelsinkivuosiluokkasecondarylower'
      | 'europeanschoolofhelsinkivuosiluokkasecondaryupper'
      | 'ebtutkinto'
    >
  }): AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus'
