import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenKoodi`
 */
export type AktiivisetJaPäättyneetOpinnotPaikallinenKoodi = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenKoodi'
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}

export const AktiivisetJaPäättyneetOpinnotPaikallinenKoodi = (o: {
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}): AktiivisetJaPäättyneetOpinnotPaikallinenKoodi => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenKoodi',
  ...o
})

AktiivisetJaPäättyneetOpinnotPaikallinenKoodi.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenKoodi' as const

export const isAktiivisetJaPäättyneetOpinnotPaikallinenKoodi = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotPaikallinenKoodi =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenKoodi'
