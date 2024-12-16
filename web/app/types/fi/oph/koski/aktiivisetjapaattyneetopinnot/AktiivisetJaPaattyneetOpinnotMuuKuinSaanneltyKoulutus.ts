import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus'
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus = (o: {
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus',
  ...o
})

AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus'
