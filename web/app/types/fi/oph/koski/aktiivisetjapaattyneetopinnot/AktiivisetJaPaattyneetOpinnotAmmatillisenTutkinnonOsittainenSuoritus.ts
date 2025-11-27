import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus } from './AktiivisetJaPaattyneetOpinnotAmmatillinenTutkintoKoulutus'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotOsaamisalajakso } from './AktiivisetJaPaattyneetOpinnotOsaamisalajakso'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus'
    toinenTutkintonimike: boolean
    suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    tyyppi: Koodistokoodiviite<string, 'ammatillinentutkintoosittainen'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    toinenOsaamisala: boolean
    osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus =
  (o: {
    toinenTutkintonimike: boolean
    suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    tyyppi: Koodistokoodiviite<string, 'ammatillinentutkintoosittainen'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    toinenOsaamisala: boolean
    osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus => ({
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus'
