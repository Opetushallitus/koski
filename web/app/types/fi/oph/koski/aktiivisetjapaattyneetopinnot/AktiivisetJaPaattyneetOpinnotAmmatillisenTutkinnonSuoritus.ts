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
 * AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus'
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkinto'>
  osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus = (o: {
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkinto'>
  osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}): AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus',
  ...o
})

AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus'
