import { Oppilaitos } from './Oppilaitos'

/**
 * SisältäväOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.SisältäväOpiskeluoikeus`
 */
export type SisältäväOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.SisältäväOpiskeluoikeus'
  oid: string
  oppilaitos: Oppilaitos
}

export const SisältäväOpiskeluoikeus = (o: {
  oid: string
  oppilaitos: Oppilaitos
}): SisältäväOpiskeluoikeus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.SisältäväOpiskeluoikeus',
  ...o
})

SisältäväOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.SisältäväOpiskeluoikeus' as const

export const isSisältäväOpiskeluoikeus = (
  a: any
): a is SisältäväOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.SisältäväOpiskeluoikeus'
