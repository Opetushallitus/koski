import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotLaajuus } from './AktiivisetJaPaattyneetOpinnotLaajuus'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus',
  ...o
})

AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus'
