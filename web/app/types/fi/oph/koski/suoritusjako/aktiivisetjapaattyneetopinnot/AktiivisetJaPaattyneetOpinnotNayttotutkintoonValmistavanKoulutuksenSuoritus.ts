import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus } from './AktiivisetJaPaattyneetOpinnotNayttotutkintoonValmistavaKoulutus'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { AktiivisetJaPäättyneetOpinnotOsaamisalajakso } from './AktiivisetJaPaattyneetOpinnotOsaamisalajakso'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus'
    tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    tyyppi: Koodistokoodiviite<string, 'nayttotutkintoonvalmistavakoulutus'>
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus =
  (o: {
    tutkintonimike?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    tyyppi: Koodistokoodiviite<string, 'nayttotutkintoonvalmistavakoulutus'>
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    osaamisala?: Array<AktiivisetJaPäättyneetOpinnotOsaamisalajakso>
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavanKoulutuksenSuoritus'
