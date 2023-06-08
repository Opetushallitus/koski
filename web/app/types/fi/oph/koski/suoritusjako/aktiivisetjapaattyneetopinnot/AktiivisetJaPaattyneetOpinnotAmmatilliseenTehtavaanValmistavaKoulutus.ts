import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus'
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    kuvaus?: LocalizedString
  }

export const AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    kuvaus?: LocalizedString
  }): AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus'
