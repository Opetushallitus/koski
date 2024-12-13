import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotTelmaKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotTelmaKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus'
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotTelmaKoulutus = (o: {
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotTelmaKoulutus => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus',
  ...o
})

AktiivisetJaPäättyneetOpinnotTelmaKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotTelmaKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotTelmaKoulutus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus'
