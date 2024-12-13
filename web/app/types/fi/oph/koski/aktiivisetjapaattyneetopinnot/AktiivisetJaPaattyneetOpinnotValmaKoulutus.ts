import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotValmaKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotValmaKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus'
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotValmaKoulutus = (o: {
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotValmaKoulutus => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus',
  ...o
})

AktiivisetJaPäättyneetOpinnotValmaKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotValmaKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotValmaKoulutus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus'
