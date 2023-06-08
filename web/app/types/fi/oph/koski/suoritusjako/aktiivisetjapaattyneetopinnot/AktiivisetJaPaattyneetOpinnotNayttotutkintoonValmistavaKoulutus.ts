import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotNäyttötutkintoonValmistavaKoulutus'
