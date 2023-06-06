import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli } from './AktiivisetJaPaattyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { AktiivisetJaPäättyneetOpinnotOsaamisalajakso } from './AktiivisetJaPaattyneetOpinnotOsaamisalajakso'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus'
  toinenTutkintonimike?: boolean
  tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite
  toinenOsaamisala?: boolean
  suoritustapa?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus = (o: {
  toinenTutkintonimike?: boolean
  tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite
  toinenOsaamisala?: boolean
  suoritustapa?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}): AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus',
  ...o
})

AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus'
