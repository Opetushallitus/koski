import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus'
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
}

export const AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus = (o: {
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
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
