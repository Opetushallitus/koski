import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus'
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    perusteenDiaarinumero?: string
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }

export const AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    perusteenDiaarinumero?: string
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus => ({
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus'
