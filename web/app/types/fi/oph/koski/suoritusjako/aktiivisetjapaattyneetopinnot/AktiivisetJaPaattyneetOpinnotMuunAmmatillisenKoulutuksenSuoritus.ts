import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus } from './AktiivisetJaPaattyneetOpinnotAmmatillinenTutkintoKoulutus'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus } from './AktiivisetJaPaattyneetOpinnotMuuAmmatillinenKoulutus'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<string, 'muuammatillinenkoulutus'>
  täydentääTutkintoa?: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: Vahvistus
}

export const AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite<string, 'muuammatillinenkoulutus'>
    täydentääTutkintoa?: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus'
