import { AktiivisetJaPäättyneetOpinnotPaikallinenKoodi } from './AktiivisetJaPaattyneetOpinnotPaikallinenKoodi'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus'
    tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
    kuvaus: LocalizedString
  }

export const AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
    kuvaus: LocalizedString
  }): AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus'
