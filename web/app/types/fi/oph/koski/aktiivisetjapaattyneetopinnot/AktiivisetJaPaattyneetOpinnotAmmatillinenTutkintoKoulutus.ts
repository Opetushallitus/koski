import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  perusteenNimi?: LocalizedString
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  perusteenNimi?: LocalizedString
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
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
