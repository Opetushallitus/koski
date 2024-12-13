import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus'
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    kuvaus?: LocalizedString
  }

export const AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    kuvaus?: LocalizedString
  }): AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus => ({
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus'
