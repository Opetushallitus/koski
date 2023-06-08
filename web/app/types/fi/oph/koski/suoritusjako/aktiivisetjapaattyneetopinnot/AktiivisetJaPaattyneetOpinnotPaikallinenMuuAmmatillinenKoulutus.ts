import { AktiivisetJaPäättyneetOpinnotPaikallinenKoodi } from './AktiivisetJaPaattyneetOpinnotPaikallinenKoodi'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus'
  tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
  kuvaus: LocalizedString
}

export const AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
    kuvaus: LocalizedString
  }): AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus'
