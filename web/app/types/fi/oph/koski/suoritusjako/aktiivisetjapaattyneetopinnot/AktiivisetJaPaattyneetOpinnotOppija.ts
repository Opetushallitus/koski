import { Henkilo } from './Henkilo'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeus } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeus'

/**
 * AktiivisetJaPäättyneetOpinnotOppija
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppija`
 */
export type AktiivisetJaPäättyneetOpinnotOppija = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppija'
  henkilö: Henkilo
  opiskeluoikeudet: Array<AktiivisetJaPäättyneetOpinnotOpiskeluoikeus>
}

export const AktiivisetJaPäättyneetOpinnotOppija = (o: {
  henkilö: Henkilo
  opiskeluoikeudet?: Array<AktiivisetJaPäättyneetOpinnotOpiskeluoikeus>
}): AktiivisetJaPäättyneetOpinnotOppija => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppija',
  opiskeluoikeudet: [],
  ...o
})

AktiivisetJaPäättyneetOpinnotOppija.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppija' as const

export const isAktiivisetJaPäättyneetOpinnotOppija = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotOppija =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppija'
