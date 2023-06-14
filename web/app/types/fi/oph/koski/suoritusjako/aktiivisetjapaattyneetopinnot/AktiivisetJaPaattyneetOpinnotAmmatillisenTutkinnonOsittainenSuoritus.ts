import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus } from './AktiivisetJaPaattyneetOpinnotAmmatillinenTutkintoKoulutus'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { AktiivisetJaPäättyneetOpinnotOsaamisalajakso } from './AktiivisetJaPaattyneetOpinnotOsaamisalajakso'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus'
    toinenTutkintonimike: boolean
    tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    tyyppi: Koodistokoodiviite<string, 'ammatillinentutkintoosittainen'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    toinenOsaamisala: boolean
    suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus =
  (o: {
    toinenTutkintonimike: boolean
    tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    tyyppi: Koodistokoodiviite<string, 'ammatillinentutkintoosittainen'>
    suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    toinenOsaamisala: boolean
    suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus'
