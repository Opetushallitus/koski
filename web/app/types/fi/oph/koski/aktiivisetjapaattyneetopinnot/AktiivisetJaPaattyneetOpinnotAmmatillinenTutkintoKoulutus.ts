import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus'
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  perusteenNimi?: LocalizedString
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus = (o: {
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  perusteenNimi?: LocalizedString
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus',
  ...o
})

AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus'
