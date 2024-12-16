import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'

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
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  }

export const AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    kuvaus?: LocalizedString
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
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
